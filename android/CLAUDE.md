# Android 开发说明

## 技术栈
- Kotlin 1.9.22 + Jetpack Compose (BOM 2024.02.00) + Material3
- Retrofit2 2.9.0 + OkHttp 4.12.0 + Gson
- DataStore Preferences 1.0.0（token 持久化）
- Coil Compose 2.5.0（图片加载）
- Navigation Compose 2.7.7
- compileSdk 34, minSdk 26, AGP 8.2.2, Gradle 8.5

## 打开/构建
1. 用 Android Studio Hedgehog (2023.1.1+) 打开 `android/` 目录
2. Sync Gradle → Run 'app' on emulator or device
3. 模拟器自动使用 `10.0.2.2:8000` 连接宿主机后端
4. 真机需修改 `util/Constants.kt` 中的 `BASE_URL` 为局域网 IP

## 项目结构

```
app/src/main/java/com/catcafe/app/
├── CatCafeApp.kt          # Application
├── MainActivity.kt        # 单 Activity，加载 NavGraph
├── data/
│   ├── api/
│   │   ├── ApiService.kt       # Retrofit 接口（24 个 API）
│   │   └── RetrofitClient.kt   # Retrofit 单例 + Auth 拦截器
│   ├── model/             # 数据类（@SerializedName 映射后端 JSON）
│   │   ├── User.kt, Cat.kt, Product.kt
│   │   ├── Order.kt, Comment.kt, Like.kt
│   └── repository/        # 每个模块一个 Repository，封装 api call
├── ui/
│   ├── theme/Theme.kt     # Material3 暖棕色主题
│   ├── navigation/NavGraph.kt  # 所有路由 + 底部导航（4 Tab）
│   ├── auth/              # LoginScreen, RegisterScreen
│   ├── home/HomeScreen.kt # 首页（推荐猫咪 + 商品）
│   ├── cat/               # CatListScreen, CatDetailScreen（点赞+评论入口）
│   ├── product/           # ProductListScreen（分类筛选）, ProductDetailScreen
│   ├── order/             # OrderCreateScreen（购物车）, OrderListScreen, OrderDetailScreen
│   ├── profile/           # ProfileScreen, EditProfileScreen, ChangePasswordScreen
│   ├── comment/CommentListScreen.kt
│   └── likes/MyLikesScreen.kt
└── util/
    ├── Constants.kt       # BASE_URL, DataStore Keys
    └── TokenManager.kt    # DataStore 读写 token/userId/userType
```

## 架构要点
- MVVM 简化版：Composable 直接调用 Repository（无 ViewModel 中间层，项目规模小）
- Token 管理：登录/注册后存入 DataStore，RetrofitClient 拦截器自动携带 Bearer token
- 底部导航 4 Tab：首页 / 猫咪 / 订单 / 我的
- 订单下单页使用本地购物车状态管理（选择商品+数量），批量提交
- 401 响应统一由错误消息提示用户重新登录
