"""点赞模块 — 点赞/取消点赞/我的点赞"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy.exc import IntegrityError

from database import get_db
from models import Likes, User, Product, Comment, Catinformation
from schemas import LikeCreate, LikeResponse, PaginatedLikes
from auth import get_current_user

router = APIRouter(prefix="/api", tags=["点赞模块"])


def _resolve_object_name(likeType: int, objectId: int, db: Session) -> str:
    """解析点赞对象名称"""
    try:
        if likeType == 0:
            p = db.query(Product).filter(Product.productId == objectId).first()
            return p.productName if p else "已删除"
        elif likeType == 1:
            c = db.query(Comment).filter(Comment.commentId == objectId).first()
            return c.content[:30] if c else "已删除"
        elif likeType == 2:
            cat = db.query(Catinformation).filter(Catinformation.catId == objectId).first()
            return cat.catName if cat else "已删除"
    except Exception:
        return "已删除"
    return "已删除"


@router.post("/likes", response_model=LikeResponse)
def create_like(
    data: LikeCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    # 检查重复点赞
    exists = db.query(Likes).filter(
        Likes.userId == current_user.userId,
        Likes.likeType == data.likeType,
        Likes.objectId == data.objectId,
    ).first()
    if exists:
        raise HTTPException(status_code=409, detail="已经点过赞了")

    like = Likes(
        likeType=data.likeType,
        objectId=data.objectId,
        userId=current_user.userId,
        linkUrl=data.linkUrl,
    )
    db.add(like)
    try:
        db.commit()
    except IntegrityError:
        db.rollback()
        raise HTTPException(status_code=409, detail="已经点过赞了")
    db.refresh(like)
    return LikeResponse(
        likeId=like.likeId,
        likeType=like.likeType,
        objectId=like.objectId,
        userId=like.userId,
        linkUrl=like.linkUrl,
        createTime=like.createTime,
        objectName=_resolve_object_name(like.likeType, like.objectId, db),
    )


@router.delete("/likes/{like_id}")
def delete_like(
    like_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    like = db.query(Likes).filter(Likes.likeId == like_id).first()
    if not like:
        raise HTTPException(status_code=404, detail="点赞不存在")
    if like.userId != current_user.userId:
        raise HTTPException(status_code=403, detail="无权取消此点赞")
    db.delete(like)
    db.commit()
    return {"message": "已取消点赞"}


@router.get("/likes", response_model=PaginatedLikes)
def list_my_likes(
    likeType: int | None = None,
    skip: int = 0,
    limit: int = 20,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    q = db.query(Likes).filter(Likes.userId == current_user.userId)
    if likeType is not None:
        q = q.filter(Likes.likeType == likeType)
    total = q.count()
    likes = q.order_by(Likes.createTime.desc()).offset(skip).limit(limit).all()
    items = []
    for lk in likes:
        items.append(LikeResponse(
            likeId=lk.likeId,
            likeType=lk.likeType,
            objectId=lk.objectId,
            userId=lk.userId,
            linkUrl=lk.linkUrl,
            createTime=lk.createTime,
            objectName=_resolve_object_name(lk.likeType, lk.objectId, db),
        ))
    return PaginatedLikes(total=total, items=items)
