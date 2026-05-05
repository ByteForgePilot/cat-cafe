# 猫咖点单系统

四人课设，Android + Python FastAPI + MySQL 8.0。

## 技术栈
- 后端：Python 3.13 + FastAPI + SQLAlchemy + PyMySQL（作者负责部分后端 + Android 全部）
- 前端：Android Studio（作者）、Web（组员 C）
- 数据库：MySQL 8.0.45，root 密码 yang，库名 cat_cafe（作者设计）

## 项目结构
```
android/     Android Studio 项目（待创建）
backend/     Python FastAPI，详见 backend/CLAUDE.md
database/    init.sql（建表脚本，手动执行不停机）
docs/        开发日志、需求分析等
```

## 关键设计决策
- 6 张表：user / catinformation / product / orders / comment / likes
- orders 通过 batchNo 支持多商品下单
- comment 通过 targetType + targetId 同时评商品和猫咪
- 密码 bcrypt 哈希存储，passlib 库
- 建表用 init.sql 手动执行，不用 SQLAlchemy create_all

## 当前进度
- 数据库设计完成，init.sql 可执行
- 后端骨架完成（config / database / models / schemas）
- API 完成 2/30：注册、登录
- API 清单详见 docs/开发日志.md

## 组员分工
作者：订单模块后端（5接口）+ Android 全部
B：用户模块剩余（6）+ 猫咪模块（5）
D：商品模块（5）+ 评论模块（4）+ 点赞模块（3）
C：Web 前端
