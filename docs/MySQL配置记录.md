# MySQL 8.0.45 安装配置记录

## 安装信息

| 项目 | 值 |
|------|-----|
| 版本 | MySQL 8.0.45 (ZIP Archive) |
| 安装路径 | `E:\mysql-8.0.45-winx64\` |
| 配置文件 | `E:\mysql-8.0.45-winx64\my.ini` |
| 端口 | 3306 |
| 字符集 | utf8mb4 / utf8mb4_unicode_ci |
| root 密码 | `yang` |

## 管理命令

```bash
# 启动 MySQL 服务
net start MySQL80

# 停止 MySQL 服务
net stop MySQL80

# 命令行连接
mysql -u root -p

# 导入 SQL 文件
mysql -u root -pyang cat_cafe < database/init.sql
```

## 数据库

| 项目 | 值 |
|------|-----|
| 库名 | `cat_cafe` |
| 字符集 | utf8mb4 / utf8mb4_unicode_ci |
| 引擎 | InnoDB |

### 6 张表

| 表名 | 说明 | 记录数 |
|------|------|--------|
| `user` | 用户 | userType: 0=顾客, 1=管理员 |
| `catinformation` | 猫咪信息 | status: 0=休息, 1=在岗 |
| `product` | 商品 | category: 0=服务 1=餐饮 2=猫咪用品; status: 0=下架 1=在售 |
| `orders` | 订单 | batchNo 批次号, orderStatus: 0未付 1已付 2待取 3完成 4取消 |
| `comment` | 评论 | targetType: 0=商品 1=猫咪; auditStatus: 0待审 1通过 2拒绝 |
| `likes` | 点赞 | likeType: 0=商品 1=评论 2=猫咪; 联合唯一索引防重复 |

## 配置文件 (my.ini)

```ini
[mysqld]
basedir=E:/mysql-8.0.45-winx64
datadir=E:/mysql-8.0.45-winx64/data
port=3306
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
default_authentication_plugin=mysql_native_password

[client]
default-character-set=utf8mb4
```

## 安装步骤回顾

1. 下载 `mysql-8.0.45-winx64.zip`
2. 解压到 `E:\`
3. 创建 `my.ini` 配置文件
4. 初始化数据目录：`mysqld --initialize-insecure`
5. 启动：`mysqld --defaults-file=.../my.ini`
6. 设置 root 密码：`ALTER USER 'root'@'localhost' IDENTIFIED BY 'yang'`
7. 建库：`CREATE DATABASE cat_cafe`
8. 建表：`mysql -u root -pyang cat_cafe < init.sql`
9. 注册服务：`mysqld --install MySQL80`（需管理员）
10. 加入 PATH：`setx PATH "%PATH%;E:\mysql-8.0.45-winx64\bin"`
