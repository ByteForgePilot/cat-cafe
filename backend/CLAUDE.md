# 后端开发说明

## 技术
Python 3.13 + FastAPI 0.115 + SQLAlchemy 2.0 + PyMySQL + passlib(bcrypt)

## 启动
```
cd backend
.venv\Scripts\activate
uvicorn main:app --reload
```
文档页 http://127.0.0.1:8000/docs

## 文件
- main.py — 入口，路由
- models.py — 6 张表 ORM
- schemas.py — 请求/响应 Pydantic 模型
- database.py — 引擎 + Session + Base
- config.py — 读 .env 拼连接串
- .env — 数据库密码（Git 不上传）
- .venv/ — 虚拟环境（Git 不上传）

## 已完成接口
- POST /api/register — 用户注册（bcrypt 哈希密码）
- POST /api/login — 用户登录

## 接口编写规范
每个接口：在 schemas.py 定义请求/响应 → 在 main.py 写路由（db: Session = Depends(get_db)）→ /docs 测试
详细 API 清单见 docs/开发日志.md

## 命名
数据库字段 camelCase，接口路径 kebab-case
