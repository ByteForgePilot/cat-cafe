"""订单模块 — 下单/查单/改单/删单"""
import uuid
from datetime import datetime
from decimal import Decimal
from collections import defaultdict

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from database import get_db
from models import Order, Product, User
from schemas import (
    OrderCreate, OrderUpdate, AdminOrderUpdate,
    BatchOrderResponse, OrderDetailItem, PaginatedOrders,
)
from auth import get_current_user

router = APIRouter(prefix="/api", tags=["订单模块"])


def _build_batch(orders: list[Order]) -> BatchOrderResponse:
    """将同一批次的多条 Order 聚合为一个 BatchOrderResponse"""
    first = orders[0]
    items = []
    total = Decimal("0")
    for o in orders:
        product_name = o.product.productName if o.product else "已删除的商品"
        items.append(OrderDetailItem(
            orderId=o.orderId,
            productId=o.productId,
            productName=product_name,
            productQuantity=o.productQuantity,
            totalAmount=o.totalAmount,
        ))
        total += o.totalAmount
    return BatchOrderResponse(
        batchNo=first.batchNo,
        userId=first.userId,
        orderStatus=first.orderStatus,
        paymentMethod=first.paymentMethod,
        orderTime=first.orderTime,
        userPhone=first.userPhone,
        userName=first.userName,
        orderNote=first.orderNote,
        totalAmount=total,
        items=items,
    )


def _group_by_batch(orders: list[Order]) -> list[BatchOrderResponse]:
    """将订单列表按 batchNo 分组合并"""
    groups: dict[str, list[Order]] = defaultdict(list)
    singles: list[Order] = []
    for o in orders:
        if o.batchNo:
            groups[o.batchNo].append(o)
        else:
            singles.append(o)
    result = []
    for batch_orders in groups.values():
        result.append(_build_batch(batch_orders))
    for o in singles:
        # 单商品订单也包装为 batch 格式，items 只有一个元素
        result.append(_build_batch([o]))
    # 按下单时间倒序
    result.sort(key=lambda b: b.orderTime, reverse=True)
    return result


@router.post("/orders", response_model=BatchOrderResponse)
def create_order(
    data: OrderCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    batch_no = uuid.uuid4().hex[:16]
    created: list[Order] = []

    for item in data.items:
        product = db.query(Product).filter(Product.productId == item.productId).first()
        if not product:
            raise HTTPException(status_code=404, detail=f"商品不存在: ID={item.productId}")
        if product.status == 0:
            raise HTTPException(status_code=400, detail=f"商品已下架: {product.productName}")
        if product.stockQuantity < item.productQuantity:
            raise HTTPException(status_code=400,
                detail=f"库存不足: {product.productName} (剩余 {product.stockQuantity})")

        product.stockQuantity -= item.productQuantity
        total_amount = product.price * item.productQuantity

        order = Order(
            batchNo=batch_no,
            userId=current_user.userId,
            productId=item.productId,
            productQuantity=item.productQuantity,
            totalAmount=total_amount,
            orderStatus=0,
            paymentMethod=data.paymentMethod,
            userPhone=data.userPhone,
            userName=data.userName,
            orderNote=data.orderNote,
        )
        db.add(order)
        created.append(order)

    db.commit()
    for o in created:
        db.refresh(o)
    return _build_batch(created)


@router.get("/orders", response_model=PaginatedOrders)
def list_orders(
    orderStatus: int | None = None,
    keyword: str | None = None,
    skip: int = 0,
    limit: int = 20,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    q = db.query(Order).filter(Order.userId == current_user.userId)

    if orderStatus is not None:
        q = q.filter(Order.orderStatus == orderStatus)
    if keyword:
        q = q.join(Product, Order.productId == Product.productId, isouter=True)\
             .filter(Product.productName.like(f"%{keyword}%"))

    q = q.order_by(Order.orderTime.desc())
    total = q.count()
    orders = q.offset(skip).limit(limit).all()

    # 按 batchNo 聚合：需要查出同批次的所有记录以正确展示
    all_ids = set()
    for o in orders:
        all_ids.add(o.orderId)
    # 对每个有 batchNo 的订单，查出同批次的所有记录
    batch_orders_map: dict[int, list[Order]] = {}
    for o in orders:
        if o.batchNo:
            siblings = db.query(Order).filter(
                Order.batchNo == o.batchNo,
                Order.userId == current_user.userId,
            ).all()
            for sib in siblings:
                key = sib.orderId
                batch_orders_map[key] = siblings
    # 重组：保证每个批次只出现一次
    seen_batches: set[str] = set()
    unique_orders: list[Order] = []
    for o in orders:
        if o.batchNo:
            if o.batchNo not in seen_batches:
                seen_batches.add(o.batchNo)
                unique_orders.append(o)
        else:
            unique_orders.append(o)

    batched = _group_by_batch(unique_orders)
    return PaginatedOrders(total=total, items=batched)


@router.get("/orders/{order_id}", response_model=BatchOrderResponse)
def get_order(
    order_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    order = db.query(Order).filter(Order.orderId == order_id).first()
    if not order:
        raise HTTPException(status_code=404, detail="订单不存在")

    # 权限：顾客只能看自己的，管理员可看任意
    if current_user.userType != 1 and order.userId != current_user.userId:
        raise HTTPException(status_code=403, detail="无权查看此订单")

    # 查出同批次所有记录
    if order.batchNo:
        siblings = db.query(Order).filter(Order.batchNo == order.batchNo).all()
    else:
        siblings = [order]

    return _build_batch(siblings)


@router.put("/orders/{order_id}", response_model=BatchOrderResponse)
def update_order(
    order_id: int,
    data: AdminOrderUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    order = db.query(Order).filter(Order.orderId == order_id).first()
    if not order:
        raise HTTPException(status_code=404, detail="订单不存在")

    is_admin = current_user.userType == 1

    if not is_admin:
        # 顾客：只能改自己订单
        if order.userId != current_user.userId:
            raise HTTPException(status_code=403, detail="无权修改此订单")
        # 顾客：只能改未支付订单
        if order.orderStatus != 0:
            raise HTTPException(status_code=400, detail="订单状态不允许修改")
        # 顾客：不能改 orderStatus（用 OrderUpdate 校验）
        if data.orderStatus is not None:
            raise HTTPException(status_code=403, detail="无权修改订单状态")

    # 管理员处理状态变更
    if is_admin and data.orderStatus is not None:
        if data.orderStatus == 1 and order.orderStatus != 1:
            order.paymentTime = datetime.now()
        elif data.orderStatus == 3 and order.orderStatus != 3:
            order.completionTime = datetime.now()
        elif data.orderStatus == 4 and order.orderStatus != 4:
            # 取消 → 恢复库存（整批恢复）
            _restore_stock(order, db)

    # 更新字段
    if data.orderStatus is not None:
        order.orderStatus = data.orderStatus
    if data.userPhone is not None:
        order.userPhone = data.userPhone
    if data.userName is not None:
        order.userName = data.userName
    if data.orderNote is not None:
        order.orderNote = data.orderNote

    db.commit()
    db.refresh(order)

    # 返回整批
    if order.batchNo:
        siblings = db.query(Order).filter(Order.batchNo == order.batchNo).all()
    else:
        siblings = [order]
    return _build_batch(siblings)


@router.delete("/orders/{order_id}")
def delete_order(
    order_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    order = db.query(Order).filter(Order.orderId == order_id).first()
    if not order:
        raise HTTPException(status_code=404, detail="订单不存在")

    is_admin = current_user.userType == 1

    if not is_admin:
        if order.userId != current_user.userId:
            raise HTTPException(status_code=403, detail="无权删除此订单")
        if order.orderStatus not in (3, 4):
            raise HTTPException(status_code=400, detail="只能删除已完成或已取消的订单")
    else:
        # 管理员删非完成/非取消订单 → 恢复库存
        if order.orderStatus not in (3, 4):
            _restore_stock(order, db)

    db.delete(order)
    db.commit()
    return {"message": "订单已删除"}


def _restore_stock(order: Order, db: Session):
    """取消或删除订单时恢复整批库存"""
    if order.batchNo:
        siblings = db.query(Order).filter(Order.batchNo == order.batchNo).all()
    else:
        siblings = [order]

    for o in siblings:
        product = db.query(Product).filter(Product.productId == o.productId).first()
        if product:
            product.stockQuantity += o.productQuantity
