# 猫咖点单系统 — API 测试指南

> 写于 2026-05-07，后端 31 个 API 全部实现完成。

## 前置条件

1. MySQL 服务已启动（管理员终端运行 `net start MySQL80`）
2. 后端服务已启动：

```bash
cd backend
.venv\Scripts\activate
uvicorn main:app --reload
```

3. 浏览器打开 **http://127.0.0.1:8000/docs** — 这是 Swagger 交互式 API 文档页，下方所有测试都在这个页面上完成。

---

## Swagger 页面布局说明

```
┌──────────────────────────────────────────────┐
│  页面顶部：绿色 "Authorize" 按钮 ← 这里输入 token │
│                                              │
│  ┌─ 用户模块 (展开/折叠) ──────────────────┐   │
│  │  POST   /api/register                  │   │
│  │  POST   /api/login                     │   │
│  │  GET    /api/user/me        🔒         │   │
│  │  ...                                   │   │
│  └────────────────────────────────────────┘   │
│  ┌─ 猫咪模块 ─────────────────────────────┐   │
│  │  GET    /api/cats                       │   │
│  │  ...                                   │   │
│  └────────────────────────────────────────┘   │
│  ... 其他模块 ...                             │
└──────────────────────────────────────────────┘
```

🔒 标记表示该接口需要登录（需要先 Authorize）。

**测试一个接口的操作步骤：**
1. 点接口名展开
2. 点「Try it out」按钮
3. 在输入框填入 JSON（如需）
4. 点「Execute」按钮
5. 下方出现 Response，查看 `Server response` 里的 `Code` 和 `Response body`

---

## 第 1 步：注册账号

**接口**：`POST /api/register`

**说明**：创建一个新用户。密码用 bcrypt 哈希存储，不存明文。注册成功直接返回 JWT token（相当于自动登录）。

**操作**：展开接口 → Try it out → 在 Request body 输入：

```json
{
  "userName": "zhangsan",
  "userPassword": "123456"
}
```

点 Execute。

**预期结果**（Code 200）：

```json
{
  "userId": 1,
  "userName": "zhangsan",
  "userType": 0,
  "registerTime": "2026-05-07T08:15:49",
  "token": "eyJhbGciOiJIUzI1NiIs..."
}
```

| 字段 | 含义 |
|------|------|
| userId | 系统分配的唯一 ID |
| userType | 0=普通顾客, 1=管理员（管理员可以上架商品、审核评论等） |
| token | JWT 登录凭证，**后续几乎所有接口都需要它** |

**常见错误**：

| Code | 内容 | 原因 |
|------|------|------|
| 422 | "用户名已存在" | 换一个 userName |
| 422 | 字段校验失败 | 密码至少 6 位、用户名不超过 50 字符 |

---

## 第 2 步：授权（最关键的步骤，容易漏掉）

**为什么需要这步**：注册/登录后拿到 token，但 Swagger 不会自动帮你带上。必须手动告诉它。

**操作**：

1. 复制上一步返回的 `token` 字段值（长字符串）
2. 滚动到页面**最顶部**
3. 点右侧绿色 **「Authorize」** 按钮
4. 在弹窗的输入框里输入（注意 Bearer 后面有一个空格）：

```
Bearer eyJhbGciOiJIUzI1NiIs...
```

5. 点「Authorize」→ 弹窗关闭出现「Logout」按钮表示成功
6. 点「Close」

**验证授权是否成功**：找 `GET /api/user/me` 点 Execute，返回 200 能看到你的用户信息就表示成功。返回 401 说明授权没设置对。

**退出授权**：点顶部「Authorize」→「Logout」→「Close」。

---

## 第 3 步：个人信息管理

### 3.1 查看自己的信息

**接口**：`GET /api/user/me` 🔒

直接点 Execute（不需要填参数，token 里已包含你的身份）。

**预期**：

```json
{
  "userId": 1,
  "userName": "zhangsan",
  "userType": 0,
  "gender": null,
  "birthday": null,
  "registerTime": "2026-05-07T08:15:49"
}
```

### 3.2 修改个人信息

**接口**：`PUT /api/user/me` 🔒

```json
{
  "userName": "zhangsan_new",
  "gender": 1,
  "birthday": "2000-06-15"
}
```

| 字段 | 说明 | 可选 |
|------|------|------|
| userName | 新用户名，不能和别人重复 | 是 |
| gender | 1=男, 2=女 | 是 |
| birthday | 生日 | 是 |

**注意**：三个字段都是可选的，只填你要改的就行。比如只想改性别就只传 `{"gender": 1}`。

**常见错误**：422 "用户名已存在" → userName 被别人占用了，换一个。

### 3.3 修改密码

**接口**：`PUT /api/user/me/password` 🔒

```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

成功后返回 `{"message": "密码修改成功"}`。**之后的请求需要用新密码登录**。

**常见错误**：400 "原密码错误" → oldPassword 填错了。

### 3.4 找回密码（不需要登录）

**接口**：`POST /api/reset-password`

```json
{
  "userName": "zhangsan",
  "newPassword": "888888"
}
```

这个接口是公开的——用户忘记密码时，通过用户名重置。生产环境会加手机验证码，课设阶段简化实现。

### 3.5 注销账户

**接口**：`DELETE /api/user/me` 🔒

直接 Execute。成功后账户被删除。如果有未完成的订单，会被拒绝。

---

## 第 4 步：理解鉴权拦截

### 4.1 测试"没登录被拦截"

先退出授权：点顶部「Authorize」→「Logout」→「Close」。

然后 Execute `GET /api/user/me`。

**预期**：**401 Unauthorized**，响应体 `{"detail": "Not authenticated"}`。

这说明未登录的用户无法访问个人信息——鉴权机制生效了。

### 4.2 测试"非管理员被拦截"

重新登录（此时你的 userType 还是 0=顾客），重新 Authorize。

Execute `POST /api/admin/cats`（或任何 `/api/admin/...` 接口）。

**预期**：**403 Forbidden**，响应体 `{"detail": "无管理员权限"}`。

这说明只有管理员才能访问管理类接口——权限分级生效了。

### 4.3 总结：接口的三种权限

| 权限级别 | 示例 | 说明 |
|----------|------|------|
| 公开（无需登录） | `/api/cats`, `/api/products`, `/api/login`, `/api/register` | 任何人都能调 |
| 需要登录 🔒 | `/api/user/me`, `/api/orders`, `/api/comments`, `/api/likes` | 需要带有效 token |
| 需要管理员 🔒👑 | `/api/admin/cats`, `/api/admin/products`, `/api/admin/users` | token 对应的用户 userType 必须为 1 |

---

## 第 5 步：完整业务流程（注册→上架→下单→评论→点赞）

从这里开始，我们模拟一个真实的业务场景。你既是顾客又是管理员。

### 5.1 把自己升级为管理员

新注册的用户默认是顾客（userType=0）。在数据库里改成管理员：

打开终端（非管理员也可以），执行：

```bash
"E:/mysql-8.0.45-winx64/bin/mysql" -u root -pyang -e "UPDATE cat_cafe.user SET userType=1 WHERE userName='zhangsan';"
```

**重新登录**拿新 token（userType 会变成 1），重新 Authorize。

在 Swagger 里执行 `POST /api/login`：

```json
{
  "userName": "zhangsan",
  "userPassword": "你的当前密码"
}
```

复制新 token，点「Authorize」→「Logout」→ 输入 `Bearer 新token` →「Authorize」。

验证：`GET /api/user/me` 返回的 `userType` 应该是 `1`。

### 5.2 上架商品

**接口**：`POST /api/admin/products` 🔒👑

```json
{
  "productName": "拿铁咖啡",
  "category": 1,
  "price": 28.00,
  "stockQuantity": 50,
  "imageUrl": "/images/latte.jpg",
  "description": "意式浓缩加新鲜牛奶，口感醇厚"
}
```

| 字段 | 含义 | 可选值 |
|------|------|--------|
| category | 商品分类 | 0=服务, 1=餐饮, 2=猫咪用品 |
| price | 单价（元） | 必须 > 0 |
| stockQuantity | 库存数量 | 整数，默认 0 |
| status | 状态 | 1=上架（默认）, 0=下架 |

多创建几个商品方便后续测试：

```json
// 猫咪零食
{"productName":"猫咪零食","category":2,"price":15.00,"stockQuantity":100,"imageUrl":"/images/snack.jpg","description":"三文鱼味猫零食"}

// 撸猫体验
{"productName":"撸猫30分钟","category":0,"price":38.00,"stockQuantity":20,"imageUrl":"/images/cuddle.jpg","description":"和猫咪亲密互动"}

// 美式咖啡
{"productName":"美式咖啡","category":1,"price":22.00,"stockQuantity":80,"imageUrl":"/images/americano.jpg","description":"精选阿拉比卡豆"}
```

### 5.3 浏览商品

**接口**：`GET /api/products`（公开，不需登录）

直接 Execute。

按分类筛选：在 `category` 输入框填 `1`（餐饮），再 Execute。

**预期**：返回 `"total": 2`，`items` 里只有拿铁和美式。

### 5.4 添加猫咪

**接口**：`POST /api/admin/cats` 🔒👑

```json
{
  "catName": "大橘",
  "breed": "橘猫",
  "birthday": "2023-06-15",
  "status": 1,
  "personality": "亲人，喜欢晒太阳，会主动蹭手",
  "photoUrl": "/images/orange.jpg",
  "notes": "体重偏重，控制零食投喂量"
}
```

再添加第二只：

```json
{
  "catName": "奶盖",
  "breed": "英短",
  "birthday": "2024-02-20",
  "status": 1,
  "personality": "高冷优雅，但会偷偷观察人类",
  "photoUrl": "/images/cream.jpg",
  "notes": ""
}
```

### 5.5 浏览猫咪

**接口**：`GET /api/cats`（公开，不需登录）

直接 Execute。只会显示 `status=1`（在岗）的猫咪。如果某只猫咪设为休息（status=0），列表里看不到，但管理员可以通过详情接口查看。

### 5.6 下单（核心功能）

**接口**：`POST /api/orders` 🔒

**一次买多个商品**（系统自动生成 batchNo 把多个商品归为一单）：

```json
{
  "items": [
    {"productId": 1, "productQuantity": 2},
    {"productId": 2, "productQuantity": 1}
  ],
  "paymentMethod": 0,
  "userPhone": "13800138000",
  "userName": "张三",
  "orderNote": "少冰，谢谢"
}
```

| 字段 | 说明 |
|------|------|
| items | 商品列表，至少一个 |
| paymentMethod | 0=微信, 1=支付宝, 2=现金（可选） |
| userPhone | 收货人电话（下单时快照） |
| userName | 收货人姓名（下单时快照） |
| orderNote | 备注（可选） |

**预期**：返回聚合后的订单，包含 `batchNo`、总金额、每个商品的明细。同时**库存自动扣减**——下单后再查商品详情，会发现 stockQuantity 减少了。

**可能错误**：

| Code | 示例 | 原因 |
|------|------|------|
| 400 | "库存不足: 拿铁咖啡 (剩余 3)" | 买的比库存多 |
| 400 | "商品已下架: xxx" | 该商品 status=0 |
| 404 | "商品不存在: ID=99" | productId 无效 |

### 5.7 查看订单列表

**接口**：`GET /api/orders` 🔒

支持筛选：

| 参数 | 用法 |
|------|------|
| orderStatus | 筛选状态：0=未付, 1=已付, 2=待取, 3=完成, 4=取消 |
| keyword | 搜商品名，如 "拿铁" |
| skip/limit | 分页，默认 0/20 |

同批次的多个商品会自动聚合为一个订单展示。

### 5.8 修改订单

**接口**：`PUT /api/orders/{orderId}` 🔒

**顾客**只能改未支付订单的联系方式和备注（不能改状态、不能改别人的）：

```json
{
  "userPhone": "13900139000",
  "orderNote": "换大杯"
}
```

**管理员**可以改任何订单的状态：

```json
{
  "orderStatus": 1
}
```

状态转换会触发自动操作：

| 状态码 | 含义 | 自动行为 |
|--------|------|----------|
| 0 | 未支付 | — |
| 1 | 已支付 | 自动填 paymentTime |
| 2 | 待拿取 | — |
| 3 | 已完成 | 自动填 completionTime |
| 4 | 已取消 | **恢复库存** |

### 5.9 删除订单

**接口**：`DELETE /api/orders/{orderId}` 🔒

- **顾客**：只能删状态为 3（已完成）或 4（已取消）的订单
- **管理员**：可以删任意订单，删未完成订单时自动恢复库存

### 5.10 发表评论

**接口**：`POST /api/comments` 🔒

```json
{
  "targetType": 0,
  "targetId": 1,
  "content": "咖啡很不错，猫咪也很可爱！"
}
```

| 字段 | 说明 |
|------|------|
| targetType | 0=评商品, 1=评猫咪 |
| targetId | 商品或猫咪的 ID |
| content | 评论内容 |

**注意**：评论发表后 `auditStatus=0`（待审核），**公开列表里看不到**，需要管理员审核后才展示。这是为了防止不良内容。

### 5.11 查看评论

**接口**：`GET /api/comments`（公开）

只显示 `auditStatus=1`（已通过审核）的评论。可选筛选 `targetType` + `targetId` 看特定对象的评论。

### 5.12 审核评论（管理员）

**接口**：`PUT /api/admin/comments/{commentId}/audit` 🔒👑

```json
{
  "auditStatus": 1
}
```

| 值 | 含义 |
|----|------|
| 1 | 通过审核 → 公开可见 |
| 2 | 拒绝 → 不展示 |

审核后再执行 `GET /api/comments`，评论就可见了。

### 5.13 点赞

**接口**：`POST /api/likes` 🔒

```json
{
  "likeType": 2,
  "objectId": 1,
  "linkUrl": "/cats/1"
}
```

| 字段 | 说明 |
|------|------|
| likeType | 0=赞商品, 1=赞评论, 2=赞猫咪 |
| objectId | 被赞对象的 ID |
| linkUrl | 跳转链接，用于"我的点赞"页面点击跳回去（可选） |

**重复点赞同一个对象**会返回 409 "已经点过赞了"。

### 5.14 查看我的点赞

**接口**：`GET /api/likes` 🔒

返回你点过的所有赞，`objectName` 字段显示被赞对象的名称（商品名/猫咪名/评论内容摘要）。

支持 `likeType` 过滤。

### 5.15 取消点赞

**接口**：`DELETE /api/likes/{likeId}` 🔒

只能取消自己的点赞，取消别人的会返回 403。

---

## 错误码速查

| Code | 含义 | 常见场景 |
|------|------|----------|
| 200 | 成功 | 一切正常 |
| 400 | 业务错误 | 库存不足、原密码错误、订单状态不允许修改 |
| 401 | 未认证 | 没带 token 或 token 过期/无效 |
| 403 | 无权限 | 顾客访问管理接口、顾客修改他人数据 |
| 404 | 不存在 | 查的用户/商品/猫咪/订单/评论/点赞不存在 |
| 409 | 冲突 | 重复点赞 |
| 422 | 数据校验失败 | 用户名已存在、必填字段缺失、格式错误 |
| 500 | 服务器错误 | 数据库没启动、代码 bug |

---

## 接口清单

### 公开接口（不需要登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/register` | 注册 |
| POST | `/api/login` | 登录 |
| POST | `/api/reset-password` | 找回密码 |
| GET | `/api/cats` | 猫咪列表（仅 status=1） |
| GET | `/api/cats/{catId}` | 猫咪详情 |
| GET | `/api/products` | 商品列表（仅 status=1，支持 category 筛选） |
| GET | `/api/products/{productId}` | 商品详情 |
| GET | `/api/comments` | 评论列表（仅 auditStatus=1） |

### 需登录 🔒

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/user/me` | 查个人信息 |
| PUT | `/api/user/me` | 改个人信息 |
| PUT | `/api/user/me/password` | 改密码 |
| DELETE | `/api/user/me` | 注销账户 |
| POST | `/api/orders` | 下单 |
| GET | `/api/orders` | 订单列表 |
| GET | `/api/orders/{id}` | 订单详情 |
| PUT | `/api/orders/{id}` | 修改订单 |
| DELETE | `/api/orders/{id}` | 删除订单 |
| POST | `/api/comments` | 发表评论 |
| DELETE | `/api/comments/{id}` | 删自己的评论 |
| POST | `/api/likes` | 点赞 |
| DELETE | `/api/likes/{id}` | 取消自己的点赞 |
| GET | `/api/likes` | 我的点赞 |

### 需管理员 🔒👑

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/users` | 查所有用户 |
| DELETE | `/api/admin/users/{id}` | 删用户 |
| POST | `/api/admin/cats` | 新增猫咪 |
| PUT | `/api/admin/cats/{id}` | 修改猫咪 |
| DELETE | `/api/admin/cats/{id}` | 删除猫咪 |
| POST | `/api/admin/products` | 上架商品 |
| PUT | `/api/admin/products/{id}` | 修改商品 |
| PUT | `/api/admin/products/{id}/off` | 下架商品 |
| PUT | `/api/admin/comments/{id}/audit` | 审核评论 |
