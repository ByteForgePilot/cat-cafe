"""猫咪模块 — 猫咪列表/详情/管理员CRUD"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from database import get_db
from models import Catinformation, User
from schemas import CatCreate, CatUpdate, CatResponse, PaginatedCats
from auth import get_current_admin

router = APIRouter(prefix="/api", tags=["猫咪模块"])


@router.get("/cats", response_model=PaginatedCats)
def list_cats(skip: int = 0, limit: int = 20, db: Session = Depends(get_db)):
    q = db.query(Catinformation).filter(Catinformation.status == 1)
    total = q.count()
    cats = q.order_by(Catinformation.catId.desc()).offset(skip).limit(limit).all()
    return PaginatedCats(
        total=total,
        items=[CatResponse.model_validate(c) for c in cats],
    )


@router.get("/cats/{cat_id}", response_model=CatResponse)
def get_cat(cat_id: int, db: Session = Depends(get_db)):
    cat = db.query(Catinformation).filter(Catinformation.catId == cat_id).first()
    if not cat:
        raise HTTPException(status_code=404, detail="猫咪不存在")
    return cat


@router.post("/admin/cats", response_model=CatResponse)
def create_cat(
    data: CatCreate,
    db: Session = Depends(get_db),
    admin: User = Depends(get_current_admin),
):
    cat = Catinformation(**data.model_dump())
    db.add(cat)
    db.commit()
    db.refresh(cat)
    return cat


@router.put("/admin/cats/{cat_id}", response_model=CatResponse)
def update_cat(
    cat_id: int,
    data: CatUpdate,
    db: Session = Depends(get_db),
    admin: User = Depends(get_current_admin),
):
    cat = db.query(Catinformation).filter(Catinformation.catId == cat_id).first()
    if not cat:
        raise HTTPException(status_code=404, detail="猫咪不存在")
    for field, value in data.model_dump(exclude_none=True).items():
        setattr(cat, field, value)
    db.commit()
    db.refresh(cat)
    return cat


@router.delete("/admin/cats/{cat_id}")
def delete_cat(
    cat_id: int,
    db: Session = Depends(get_db),
    admin: User = Depends(get_current_admin),
):
    cat = db.query(Catinformation).filter(Catinformation.catId == cat_id).first()
    if not cat:
        raise HTTPException(status_code=404, detail="猫咪不存在")
    db.delete(cat)
    db.commit()
    return {"message": "猫咪信息已删除"}
