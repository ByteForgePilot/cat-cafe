# 后端开发说明

## 技术
Python 3.13 + FastAPI 0.115 + SQLAlchemy 2.0 + PyMySQL + passlib(bcrypt) + python-jose(JWT)

## 启动
```bash
cd backend
.venv\Scripts\activate
uvicorn main:app --reload
```
文档页 http://127.0.0.1:8000/docs

## 文件
```
backend/
├── main.py       # FastAPI 入口 — 创建 app + router includes
├── config.py     # 读 .env，拼 DATABASE_URL + JWT 配置
├── database.py   # 引擎 + SessionLocal + Base + get_db()
├── models.py     # 6 张表 ORM 模型（不改动）
├── schemas.py    # 请求/响应 Pydantic 模型（按模块分段）
├── auth.py       # JWT token 创建/验证 + get_current_user + get_current_admin
├── routers/
│   ├── user.py     # 注册/登录/个人信息/管理员用户管理 (9 routes)
│   ├── cat.py      # 猫咪列表/详情/管理员 CRUD (5 routes)
│   ├── product.py  # 商品列表/详情/管理员上下架 (5 routes)
│   ├── order.py    # 下单/查单/改单/删单 (5 routes)
│   ├── comment.py  # 评论/查看/审核/删除 (4 routes)
│   └── likes.py    # 点赞/取消/我的点赞 (3 routes)
├── .env             # 数据库密码 + JWT 密钥（不追踪）
└── .venv/           # 虚拟环境（不追踪）
```

## API 状态 (31/31 ✅)

| 模块 | 接口 | 说明 |
|------|------|------|
| 用户 | POST /api/register | 注册 + JWT token |
| 用户 | POST /api/login | 登录 + JWT token |
| 用户 | GET /api/user/me | 查个人信息 |
| 用户 | PUT /api/user/me | 改个人信息 |
| 用户 | PUT /api/user/me/password | 改密码 |
| 用户 | POST /api/reset-password | 找回密码（公开） |
| 用户 | DELETE /api/user/me | 注销账户 |
| 用户 | GET /api/admin/users | 管理员查所有用户 |
| 用户 | DELETE /api/admin/users/{id} | 管理员删用户 |
| 猫咪 | GET /api/cats | 猫咪列表（仅在岗） |
| 猫咪 | GET /api/cats/{id} | 猫咪详情 |
| 猫咪 | POST /api/admin/cats | 管理员新增猫咪 |
| 猫咪 | PUT /api/admin/cats/{id} | 管理员修改猫咪 |
| 猫咪 | DELETE /api/admin/cats/{id} | 管理员删除猫咪 |
| 商品 | GET /api/products | 商品列表（仅上架，支持分类筛选） |
| 商品 | GET /api/products/{id} | 商品详情 |
| 商品 | POST /api/admin/products | 管理员上架商品 |
| 商品 | PUT /api/admin/products/{id} | 管理员修改商品 |
| 商品 | PUT /api/admin/products/{id}/off | 管理员下架商品 |
| 订单 | POST /api/orders | 多商品下单+扣库存+事务 |
| 订单 | GET /api/orders | 列表+筛选+按 batchNo 聚合 |
| 订单 | GET /api/orders/{id} | 详情含同批次记录 |
| 订单 | PUT /api/orders/{id} | 顾客限改自己未支付/管理员改所有 |
| 订单 | DELETE /api/orders/{id} | 顾客限删已完成/管理员全删+恢复库存 |
| 评论 | POST /api/comments | 发表评论 |
| 评论 | GET /api/comments | 查看评论（仅已审核） |
| 评论 | PUT /api/admin/comments/{id}/audit | 管理员审核评论 |
| 评论 | DELETE /api/comments/{id} | 删除评论（作者/管理员） |
| 点赞 | POST /api/likes | 点赞（防重复） |
| 点赞 | DELETE /api/likes/{id} | 取消点赞 |
| 点赞 | GET /api/likes | 我的点赞 |

## 鉴权模式

需要登录的接口：`current_user: User = Depends(get_current_user)`
需要管理员的接口：`admin: User = Depends(get_current_admin)`

## 命名
数据库字段 camelCase，接口路径 kebab-case
