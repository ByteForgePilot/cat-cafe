"""评论模块 — 发表评论/查看评论/审核评论/删除评论"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from database import get_db
from models import Comment, User, Product, Catinformation
from schemas import CommentCreate, CommentAudit, CommentResponse, PaginatedComments
from auth import get_current_user, get_current_admin

router = APIRouter(prefix="/api", tags=["评论模块"])


def _resolve_target(targetType: int, targetId: int, db: Session) -> str | None:
    """验证评论目标是否存在，返回目标名称或 None"""
    if targetType == 0:
        p = db.query(Product).filter(Product.productId == targetId).first()
        return p.productName if p else None
    elif targetType == 1:
        c = db.query(Catinformation).filter(Catinformation.catId == targetId).first()
        return c.catName if c else None
    return None


def _make_comment_response(c: Comment) -> CommentResponse:
    return CommentResponse(
        commentId=c.commentId,
        targetType=c.targetType,
        targetId=c.targetId,
        userId=c.userId,
        userName=c.user.userName if c.user else None,
        content=c.content,
        publishTime=c.publishTime,
        auditStatus=c.auditStatus,
    )


@router.post("/comments", response_model=CommentResponse)
def create_comment(
    data: CommentCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    target_name = _resolve_target(data.targetType, data.targetId, db)
    if target_name is None:
        raise HTTPException(status_code=404, detail="评论对象不存在")
    comment = Comment(
        targetType=data.targetType,
        targetId=data.targetId,
        userId=current_user.userId,
        content=data.content,
        auditStatus=0,
    )
    db.add(comment)
    db.commit()
    db.refresh(comment)
    return _make_comment_response(comment)


@router.get("/comments", response_model=PaginatedComments)
def list_comments(
    targetType: int | None = None,
    targetId: int | None = None,
    skip: int = 0,
    limit: int = 20,
    db: Session = Depends(get_db),
):
    q = db.query(Comment).filter(Comment.auditStatus == 1)
    if targetType is not None and targetId is not None:
        q = q.filter(Comment.targetType == targetType, Comment.targetId == targetId)
    total = q.count()
    comments = q.order_by(Comment.publishTime.desc()).offset(skip).limit(limit).all()
    return PaginatedComments(
        total=total,
        items=[_make_comment_response(c) for c in comments],
    )


@router.put("/admin/comments/{comment_id}/audit", response_model=CommentResponse)
def audit_comment(
    comment_id: int,
    data: CommentAudit,
    db: Session = Depends(get_db),
    admin: User = Depends(get_current_admin),
):
    comment = db.query(Comment).filter(Comment.commentId == comment_id).first()
    if not comment:
        raise HTTPException(status_code=404, detail="评论不存在")
    comment.auditStatus = data.auditStatus
    db.commit()
    db.refresh(comment)
    return _make_comment_response(comment)


@router.delete("/comments/{comment_id}")
def delete_comment(
    comment_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    comment = db.query(Comment).filter(Comment.commentId == comment_id).first()
    if not comment:
        raise HTTPException(status_code=404, detail="评论不存在")
    if comment.userId != current_user.userId and current_user.userType != 1:
        raise HTTPException(status_code=403, detail="无权删除此评论")
    db.delete(comment)
    db.commit()
    return {"message": "评论已删除"}
