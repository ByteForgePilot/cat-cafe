"""商品模块 — 商品列表/详情/管理员上架下架"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from database import get_db
from models import Product, User
from schemas import ProductCreate, ProductUpdate, ProductResponse, PaginatedProducts
from auth import get_current_admin

router = APIRouter(prefix="/api", tags=["商品模块"])


@router.get("/products", response_model=PaginatedProducts)
def list_products(
    category: int | None = None,
    skip: int = 0,
    limit: int = 20,
    db: Session = Depends(get_db),
):
    q = db.query(Product).filter(Product.status == 1)
    if category is not None:
        q = q.filter(Product.category == category)
    total = q.count()
    products = q.order_by(Product.productId.desc()).offset(skip).limit(limit).all()
    return PaginatedProducts(
        total=total,
        items=[ProductResponse.model_validate(p) for p in products],
    )


@router.get("/products/{product_id}", response_model=ProductResponse)
def get_product(product_id: int, db: Session = Depends(get_db)):
    product = db.query(Product).filter(Product.productId == product_id).first()
    if not product:
        raise HTTPException(status_code=404, detail="商品不存在")
    return product


@router.post("/admin/products", response_model=ProductResponse)
def create_product(
    data: ProductCreate,
    db: Session = Depends(get_db),
    admin: User = Depends(get_current_admin),
):
    product = Product(**data.model_dump())
    db.add(product)
    db.commit()
    db.refresh(product)
    return product


@router.put("/admin/products/{product_id}", response_model=ProductResponse)
def update_product(
    product_id: int,
    data: ProductUpdate,
    db: Session = Depends(get_db),
    admin: User = Depends(get_current_admin),
):
    product = db.query(Product).filter(Product.productId == product_id).first()
    if not product:
        raise HTTPException(status_code=404, detail="商品不存在")
    for field, value in data.model_dump(exclude_none=True).items():
        setattr(product, field, value)
    db.commit()
    db.refresh(product)
    return product


@router.put("/admin/products/{product_id}/off")
def delist_product(
    product_id: int,
    db: Session = Depends(get_db),
    admin: User = Depends(get_current_admin),
):
    product = db.query(Product).filter(Product.productId == product_id).first()
    if not product:
        raise HTTPException(status_code=404, detail="商品不存在")
    if product.status == 0:
        return {"message": "商品已是下架状态"}
    product.status = 0
    db.commit()
    return {"message": "商品已下架"}
