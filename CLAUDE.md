# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# 猫咖点单系统

四人课设，Android + Python FastAPI + MySQL 8.0。作者负责订单模块后端 + Android 全部。

## 常用命令

```bash
# 启动 MySQL（Windows 管理员终端）
net start MySQL80

# 启动后端
cd backend
.venv\Scripts\activate
uvicorn main:app --reload
```

启动后 API 文档页：http://127.0.0.1:8000/docs

## 技术栈

- 后端：Python 3.13 + FastAPI 0.115 + SQLAlchemy 2.0 + PyMySQL + passlib(bcrypt)
- 数据库：MySQL 8.0.45，root 密码 yang，库名 cat_cafe
- 前端：Android Studio（作者）、Web（组员 C）

## .env 格式

`backend/.env`（Git 不追踪）：

```
DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASSWORD=yang
DB_NAME=cat_cafe
JWT_SECRET=<随机密钥>
JWT_ALGORITHM=HS256
JWT_EXPIRE_HOURS=24
```

## 项目结构

```
backend/      Python FastAPI — 详见 backend/CLAUDE.md
android/      Android Studio 项目（待创建）— 详见 android/CLAUDE.md
database/     init.sql（建表脚本，手动执行不停机）
docs/         开发日志、需求分析等
```

## 关键设计决策

- 6 张表：user / catinformation / product / orders / comment / likes
- orders 通过 batchNo 支持多商品下单（同一批次多条记录共用 batchNo）
- comment 通过 targetType(0=商品,1=猫咪) + targetId 复用一张表评两类对象
- 密码 bcrypt 哈希存储（passlib）
- 建表用 init.sql 手动执行，不用 SQLAlchemy create_all
- 数据库字段 camelCase，接口路径 kebab-case

## 接口编写规范

每个接口三步：schemas.py 定义请求/响应 → routers/xxx.py 写路由（`db: Session = Depends(get_db)`）→ /docs 测试

鉴权依赖：`get_current_user`（需登录）、`get_current_admin`（需管理员），从 `auth` 模块导入

## 当前进度 & 分工

- 数据库设计完成，init.sql 可执行
- 后端 API 全部完成 31/31 + /health
- Android 前端（待创建）
- API 清单详见 docs/开发日志.md

| 成员 | 负责 |
|------|------|
| 作者 | 订单模块后端（5接口）+ Android 全部 |
| B | 用户模块剩余（6）+ 猫咪模块（5） |
| D | 商品模块（5）+ 评论模块（4）+ 点赞模块（3） |
| C | Web 前端 |
