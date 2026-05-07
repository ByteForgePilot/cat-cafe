from fastapi import FastAPI

from routers import user, cat, product, order, comment, likes

app = FastAPI(title="猫咖点单系统")


@app.get("/health")
def health():
    return {"status": "ok", "message": "猫咖服务运行中"}


app.include_router(user.router)
app.include_router(cat.router)
app.include_router(product.router)
app.include_router(order.router)
app.include_router(comment.router)
app.include_router(likes.router)
