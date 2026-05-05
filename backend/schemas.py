"""
Pydantic 数据模型 — 定义每个接口的请求格式和响应格式。
FastAPI 收到请求后自动用这些模型校验数据，不合规的直接 422 拒绝。

和 models.py 的区别：
  models.py  → 映射数据库表（SQLAlchemy ORM）
  schemas.py → 校验 HTTP 请求/响应（Pydantic），不直接操作数据库
"""

from datetime import datetime
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
