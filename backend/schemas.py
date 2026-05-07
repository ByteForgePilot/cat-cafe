"""
Pydantic 数据模型 — 定义每个接口的请求格式和响应格式。
FastAPI 收到请求后自动用这些模型校验数据，不合规的直接 422 拒绝。

和 models.py 的区别：
  models.py  → 映射数据库表（SQLAlchemy ORM）
  schemas.py → 校验 HTTP 请求/响应（Pydantic），不直接操作数据库
"""

from datetime import datetime, date
from pydantic import BaseModel, ConfigDict, Field


# ============================================
# 用户模块
# ============================================

class UserRegister(BaseModel):
    """注册请求"""
    userName:     str = Field(min_length=1, max_length=50, description="用户名")
    userPassword: str = Field(min_length=6, max_length=128, description="密码")


class UserLogin(BaseModel):
    """登录请求"""
    userName:     str = Field(min_length=1, max_length=50)
    userPassword: str = Field(min_length=1, max_length=128)


class UserResponse(BaseModel):
    """注册成功后的响应 — 不返回密码"""
    model_config = ConfigDict(from_attributes=True)
    # from_attributes=True 告诉 Pydantic: "你可以从 SQLAlchemy ORM 对象直接读取属性"
    # 这样 User 查询出来后直接塞进 UserResponse，不用手动逐字段赋值

    userId: int
    userName: str
    userType: int
    registerTime: datetime


class LoginResponse(BaseModel):
    """登录/注册成功响应 — 包含 JWT token"""
    model_config = ConfigDict(from_attributes=True)

    userId: int
    userName: str
    userType: int
    registerTime: datetime
    token: str


# ============================================
# 订单模块
# ============================================

from decimal import Decimal


class OrderItem(BaseModel):
    """订单中的单个商品"""
    productId: int = Field(gt=0)
    productQuantity: int = Field(ge=1)


class OrderCreate(BaseModel):
    """下单请求 — 支持多商品"""
    items: list[OrderItem] = Field(min_length=1)
    paymentMethod: int | None = Field(None, ge=0, le=2)
    userPhone: str = Field(min_length=1, max_length=20)
    userName: str = Field(min_length=1, max_length=50)
    orderNote: str | None = Field(None, max_length=500)


class OrderUpdate(BaseModel):
    """顾客修改订单 — 仅限联系方式/备注"""
    userPhone: str | None = Field(None, min_length=1, max_length=20)
    userName: str | None = Field(None, min_length=1, max_length=50)
    orderNote: str | None = Field(None, max_length=500)


class AdminOrderUpdate(BaseModel):
    """管理员修改订单 — 可改状态"""
    orderStatus: int | None = Field(None, ge=0, le=4)
    userPhone: str | None = Field(None, min_length=1, max_length=20)
    userName: str | None = Field(None, min_length=1, max_length=50)
    orderNote: str | None = Field(None, max_length=500)


class OrderDetailItem(BaseModel):
    """批次中的单个订单记录"""
    model_config = ConfigDict(from_attributes=True)

    orderId: int
    productId: int
    productName: str
    productQuantity: int
    totalAmount: Decimal


class BatchOrderResponse(BaseModel):
    """按 batchNo 聚合后的订单展示"""
    batchNo: str | None
    userId: int
    orderStatus: int
    paymentMethod: int | None
    orderTime: datetime
    userPhone: str
    userName: str
    orderNote: str | None
    totalAmount: Decimal
    items: list[OrderDetailItem]


class PaginatedOrders(BaseModel):
    total: int
    items: list[BatchOrderResponse]


# ============================================
# 用户模块（扩展）
# ============================================

class UserDetailResponse(BaseModel):
    """完整个人信息"""
    model_config = ConfigDict(from_attributes=True)

    userId: int
    userName: str
    userType: int
    gender: int | None
    birthday: date | None
    registerTime: datetime


class UserUpdate(BaseModel):
    """修改个人信息 — 全部可选"""
    userName: str | None = Field(None, min_length=1, max_length=50)
    gender: int | None = Field(None, ge=1, le=2)
    birthday: date | None = None


class UserPasswordChange(BaseModel):
    """已登录状态改密码"""
    oldPassword: str
    newPassword: str = Field(min_length=6, max_length=128)


class UserResetPassword(BaseModel):
    """找回密码"""
    userName: str = Field(min_length=1, max_length=50)
    newPassword: str = Field(min_length=6, max_length=128)


class AdminUserListItem(BaseModel):
    """管理员用户列表项"""
    model_config = ConfigDict(from_attributes=True)

    userId: int
    userName: str
    userType: int
    gender: int | None
    registerTime: datetime


class PaginatedUsers(BaseModel):
    total: int
    items: list[AdminUserListItem]


# ============================================
# 猫咪模块
# ============================================

class CatCreate(BaseModel):
    catName: str = Field(min_length=1, max_length=50)
    breed: str = Field(max_length=50)
    birthday: date
    status: int = Field(default=1, ge=0, le=1)
    personality: str
    photoUrl: str = Field(max_length=255)
    notes: str


class CatUpdate(BaseModel):
    """修改猫咪信息 — 全部可选"""
    catName: str | None = Field(None, min_length=1, max_length=50)
    breed: str | None = Field(None, max_length=50)
    birthday: date | None = None
    status: int | None = Field(None, ge=0, le=1)
    personality: str | None = None
    photoUrl: str | None = Field(None, max_length=255)
    notes: str | None = None


class CatResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    catId: int
    catName: str
    breed: str
    birthday: date
    status: int
    personality: str
    photoUrl: str
    notes: str


class PaginatedCats(BaseModel):
    total: int
    items: list[CatResponse]


# ============================================
# 商品模块
# ============================================

class ProductCreate(BaseModel):
    productName: str = Field(min_length=1, max_length=100)
    category: int = Field(ge=0, le=2)
    price: Decimal = Field(gt=0, max_digits=10, decimal_places=2)
    stockQuantity: int = Field(default=0, ge=0)
    imageUrl: str = Field(max_length=255)
    description: str | None = None
    status: int = Field(default=1, ge=0, le=1)


class ProductUpdate(BaseModel):
    """修改商品信息 — 全部可选"""
    productName: str | None = Field(None, min_length=1, max_length=100)
    category: int | None = Field(None, ge=0, le=2)
    price: Decimal | None = Field(None, gt=0, max_digits=10, decimal_places=2)
    stockQuantity: int | None = Field(None, ge=0)
    imageUrl: str | None = Field(None, max_length=255)
    description: str | None = None
    status: int | None = Field(None, ge=0, le=1)


class ProductResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    productId: int
    productName: str
    category: int
    price: Decimal
    stockQuantity: int
    imageUrl: str
    description: str | None
    status: int
    createTime: datetime


class PaginatedProducts(BaseModel):
    total: int
    items: list[ProductResponse]


# ============================================
# 评论模块
# ============================================

class CommentCreate(BaseModel):
    targetType: int = Field(ge=0, le=1)
    targetId: int = Field(gt=0)
    content: str = Field(min_length=1)


class CommentAudit(BaseModel):
    auditStatus: int = Field(ge=1, le=2)


class CommentResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    commentId: int
    targetType: int
    targetId: int
    userId: int
    userName: str | None = None
    content: str
    publishTime: datetime
    auditStatus: int


class PaginatedComments(BaseModel):
    total: int
    items: list[CommentResponse]


# ============================================
# 点赞模块
# ============================================

class LikeCreate(BaseModel):
    likeType: int = Field(ge=0, le=2)
    objectId: int = Field(gt=0)
    linkUrl: str | None = Field(None, max_length=255)


class LikeResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    likeId: int
    likeType: int
    objectId: int
    userId: int
    linkUrl: str | None
    createTime: datetime
    objectName: str | None = None


class PaginatedLikes(BaseModel):
    total: int
    items: list[LikeResponse]
