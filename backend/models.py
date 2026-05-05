from sqlalchemy import (
    BigInteger, Integer, String, Text, Date, DateTime, Numeric,
    Column, ForeignKey, func,
)
from sqlalchemy.orm import relationship

from database import Base


class User(Base):
    """用户表"""
    __tablename__ = "user"

    # Column: 声明一个数据库字段
    # 第一个参数是类型，BigInteger=大整数，String(50)=最长50字符
    # primary_key=True   → 主键
    # autoincrement=True → 自增（主键默认开启）
    # nullable=False     → NOT NULL，不能为空
    # unique=True        → 唯一约束
    # default=0          → 插入时若未传值，默认为 0
    # server_default     → 由 MySQL 服务端生成默认值（用于 CURRENT_TIMESTAMP）
    userId       = Column(BigInteger, primary_key=True, autoincrement=True)
    userPassword = Column(String(255), nullable=False)
    userName     = Column(String(50), nullable=False, unique=True)
    userType     = Column(Integer, nullable=False, default=0)
    gender       = Column(Integer, nullable=True)
    birthday     = Column(Date, nullable=True)
    registerTime = Column(DateTime, nullable=False, server_default=func.now())

    # relationship: ORM 层面的关联，不是数据库字段
    # back_populates 和对方表里的外键字段对应，实现 user.orders 直接拿到订单列表
    orders  = relationship("Order",  back_populates="user")
    comments = relationship("Comment", back_populates="user")
    likes   = relationship("Likes",   back_populates="user")


class Catinformation(Base):
    __tablename__ = "catinformation"

    catId       = Column(BigInteger, primary_key=True, autoincrement=True)
    catName     = Column(String(50), nullable=False)
    breed       = Column(String(50), nullable=False)
    birthday    = Column(Date, nullable=False)
    status      = Column(Integer, nullable=False, default=1)
    personality = Column(Text, nullable=False)
    photoUrl    = Column(String(255), nullable=False)
    notes       = Column(Text, nullable=False)


class Product(Base):
    __tablename__ = "product"

    productId     = Column(BigInteger, primary_key=True, autoincrement=True)
    productName   = Column(String(100), nullable=False)
    category      = Column(Integer, nullable=False)
    price         = Column(Numeric(10, 2), nullable=False)
    stockQuantity = Column(Integer, nullable=False, default=0)
    imageUrl      = Column(String(255), nullable=False)
    description   = Column(Text, nullable=True)
    status        = Column(Integer, nullable=False, default=1)
    createTime    = Column(DateTime, nullable=False, server_default=func.now())

    orders = relationship("Order", back_populates="product")


class Order(Base):
    __tablename__ = "orders"

    orderId         = Column(BigInteger, primary_key=True, autoincrement=True)
    batchNo         = Column(String(32), nullable=True)
    userId          = Column(BigInteger, ForeignKey("user.userId", ondelete="RESTRICT"), nullable=False)
    productId       = Column(BigInteger, ForeignKey("product.productId", ondelete="RESTRICT"), nullable=False)
    productQuantity = Column(Integer, nullable=False, default=1)
    totalAmount     = Column(Numeric(10, 2), nullable=False)
    orderStatus     = Column(Integer, nullable=False, default=0)
    paymentMethod   = Column(Integer, nullable=True)
    orderTime       = Column(DateTime, nullable=False, server_default=func.now())
    paymentTime     = Column(DateTime, nullable=True)
    completionTime  = Column(DateTime, nullable=True)
    userPhone       = Column(String(20), nullable=False)
    userName        = Column(String(50), nullable=False)
    orderNote       = Column(String(500), nullable=True)

    # ForeignKey("user.userId") 建立外键约束，ondelete="RESTRICT" 表示有关联订单时不能删除用户
    # relationship 使 Python 代码可以通过 order.user 直接拿到关联的 User 对象
    user    = relationship("User",    back_populates="orders")
    product = relationship("Product", back_populates="orders")


class Comment(Base):
    __tablename__ = "comment"

    commentId   = Column(BigInteger, primary_key=True, autoincrement=True)
    targetType  = Column(Integer, nullable=False)
    targetId    = Column(BigInteger, nullable=False)
    userId      = Column(BigInteger, ForeignKey("user.userId", ondelete="CASCADE"), nullable=False)
    content     = Column(Text, nullable=False)
    publishTime = Column(DateTime, nullable=False, server_default=func.now())
    auditStatus = Column(Integer, nullable=False, default=0)

    user = relationship("User", back_populates="comments")


class Likes(Base):
    __tablename__ = "likes"

    likeId     = Column(BigInteger, primary_key=True, autoincrement=True)
    likeType   = Column(Integer, nullable=False)
    objectId   = Column(BigInteger, nullable=False)
    userId     = Column(BigInteger, ForeignKey("user.userId", ondelete="CASCADE"), nullable=False)
    linkUrl    = Column(String(255), nullable=True)
    createTime = Column(DateTime, nullable=False, server_default=func.now())

    user = relationship("User", back_populates="likes")
