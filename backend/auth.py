"""JWT 认证模块 — token 签发 + get_current_user / get_current_admin 依赖注入"""
import os
from datetime import datetime, timedelta

from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from jose import JWTError, jwt
from sqlalchemy.orm import Session

from config import JWT_SECRET, JWT_ALGORITHM
from database import get_db
from models import User

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/api/login")

JWT_EXPIRE_HOURS = int(os.getenv("JWT_EXPIRE_HOURS", 24))


def create_access_token(data: dict) -> str:
    to_encode = data.copy()
    expire = datetime.utcnow() + timedelta(hours=JWT_EXPIRE_HOURS)
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, JWT_SECRET, algorithm=JWT_ALGORITHM)


def get_current_user(
    db: Session = Depends(get_db),
    token: str = Depends(oauth2_scheme),
) -> User:
    try:
        payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
        user_id: str = payload.get("sub")
        if user_id is None:
            raise HTTPException(status_code=401, detail="认证失败")
    except JWTError:
        raise HTTPException(status_code=401, detail="认证失败")

    user = db.query(User).filter(User.userId == int(user_id)).first()
    if user is None:
        raise HTTPException(status_code=401, detail="用户不存在")
    return user


def get_current_admin(current_user: User = Depends(get_current_user)) -> User:
    if current_user.userType != 1:
        raise HTTPException(status_code=403, detail="无管理员权限")
    return current_user
