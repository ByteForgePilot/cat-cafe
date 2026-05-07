"""用户模块 — 注册/登录/个人信息管理/管理员用户管理"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from passlib.context import CryptContext

from database import get_db
from models import User
from schemas import (
    UserLogin, UserRegister, LoginResponse,
    UserDetailResponse, UserUpdate, UserPasswordChange,
    UserResetPassword, AdminUserListItem, PaginatedUsers,
)
from auth import create_access_token, get_current_user, get_current_admin

router = APIRouter(prefix="/api", tags=["用户模块"])
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


@router.post("/register", response_model=LoginResponse)
def register(data: UserRegister, db: Session = Depends(get_db)):
    exists = db.query(User).filter(User.userName == data.userName).first()
    if exists:
        raise HTTPException(status_code=422, detail="用户名已存在")

    hashed = pwd_context.hash(data.userPassword)
    user = User(userPassword=hashed, userName=data.userName, userType=0)
    db.add(user)
    db.commit()
    db.refresh(user)

    token = create_access_token({"sub": str(user.userId), "userType": user.userType})
    return LoginResponse(
        userId=user.userId,
        userName=user.userName,
        userType=user.userType,
        registerTime=user.registerTime,
        token=token,
    )


@router.post("/login", response_model=LoginResponse)
def login(data: UserLogin, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.userName == data.userName).first()
    if not user:
        raise HTTPException(status_code=401, detail="用户名或密码错误")
    if not pwd_context.verify(data.userPassword, user.userPassword):
        raise HTTPException(status_code=401, detail="用户名或密码错误")

    token = create_access_token({"sub": str(user.userId), "userType": user.userType})
    return LoginResponse(
        userId=user.userId,
        userName=user.userName,
        userType=user.userType,
        registerTime=user.registerTime,
        token=token,
    )


# ── 个人信息 ──

@router.get("/user/me", response_model=UserDetailResponse)
def get_me(current_user: User = Depends(get_current_user)):
    return current_user


@router.put("/user/me", response_model=UserDetailResponse)
def update_me(
    data: UserUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    if data.userName is not None and data.userName != current_user.userName:
        dup = db.query(User).filter(
            User.userName == data.userName,
            User.userId != current_user.userId,
        ).first()
        if dup:
            raise HTTPException(status_code=422, detail="用户名已存在")
        current_user.userName = data.userName
    if data.gender is not None:
        current_user.gender = data.gender
    if data.birthday is not None:
        current_user.birthday = data.birthday
    db.commit()
    db.refresh(current_user)
    return current_user


@router.put("/user/me/password")
def change_password(
    data: UserPasswordChange,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    if not pwd_context.verify(data.oldPassword, current_user.userPassword):
        raise HTTPException(status_code=400, detail="原密码错误")
    current_user.userPassword = pwd_context.hash(data.newPassword)
    db.commit()
    return {"message": "密码修改成功"}


@router.post("/reset-password")
def reset_password(data: UserResetPassword, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.userName == data.userName).first()
    if not user:
        raise HTTPException(status_code=404, detail="用户不存在")
    user.userPassword = pwd_context.hash(data.newPassword)
    db.commit()
    return {"message": "密码重置成功"}


@router.delete("/user/me")
def delete_me(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    db.delete(current_user)
    db.commit()
    return {"message": "账户已注销"}


# ── 管理员 ──

@router.get("/admin/users", response_model=PaginatedUsers)
def admin_list_users(
    userName: str | None = None,
    userType: int | None = None,
    skip: int = 0,
    limit: int = 20,
    db: Session = Depends(get_db),
    admin: User = Depends(get_current_admin),
):
    q = db.query(User)
    if userName is not None:
        q = q.filter(User.userName.like(f"%{userName}%"))
    if userType is not None:
        q = q.filter(User.userType == userType)
    total = q.count()
    users = q.order_by(User.userId.desc()).offset(skip).limit(limit).all()
    return PaginatedUsers(
        total=total,
        items=[AdminUserListItem.model_validate(u) for u in users],
    )


@router.delete("/admin/users/{user_id}")
def admin_delete_user(
    user_id: int,
    db: Session = Depends(get_db),
    admin: User = Depends(get_current_admin),
):
    if user_id == admin.userId:
        raise HTTPException(status_code=400, detail="不能删除自己的账号，请使用注销功能")
    target = db.query(User).filter(User.userId == user_id).first()
    if not target:
        raise HTTPException(status_code=404, detail="用户不存在")
    db.delete(target)
    db.commit()
    return {"message": "用户已删除"}
