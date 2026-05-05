from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
from passlib.context import CryptContext

from database import get_db
from models import User
from schemas import UserLogin, UserRegister, UserResponse

app = FastAPI(title="猫咖点单系统")

# bcrypt 密码哈希上下文，后续登录验证也用这个
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


@app.get("/health")
def health():
    return {"status": "ok", "message": "猫咖服务运行中"}


@app.post("/api/register", response_model=UserResponse)
def register(data: UserRegister, db: Session = Depends(get_db)):
    """
    用户注册
    - 检查用户名是否已存在
    - 密码使用 bcrypt 哈希存储，不存明文
    """
    exists = db.query(User).filter(User.userName == data.userName).first()
    if exists:
        raise HTTPException(status_code=422, detail="用户名已存在")

    hashed_password = pwd_context.hash(data.userPassword)
    user = User(
        userPassword=hashed_password,
        userName=data.userName,
        userType=0,
    )
    db.add(user)
    db.commit()
    db.refresh(user)
    return user


@app.post("/api/login", response_model=UserResponse)
def login(data: UserLogin, db: Session = Depends(get_db)):
    """用户登录"""
    user = db.query(User).filter(User.userName == data.userName).first()
    if not user:
        raise HTTPException(status_code=401, detail="用户名或密码错误")
    if not pwd_context.verify(data.userPassword, user.userPassword):
        raise HTTPException(status_code=401, detail="用户名或密码错误")
    return user
