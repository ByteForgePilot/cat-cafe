package com.catcafe.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.catcafe.app.data.api.RetrofitClient
import com.catcafe.app.ui.auth.LoginScreen
import com.catcafe.app.ui.auth.RegisterScreen
import com.catcafe.app.ui.cat.CatDetailScreen
import com.catcafe.app.ui.cat.CatListScreen
import com.catcafe.app.ui.comment.CommentListScreen
import com.catcafe.app.ui.home.HomeScreen
import com.catcafe.app.ui.likes.MyLikesScreen
import com.catcafe.app.ui.order.OrderCreateScreen
import com.catcafe.app.ui.order.OrderDetailScreen
import com.catcafe.app.ui.order.OrderListScreen
import com.catcafe.app.ui.product.ProductDetailScreen
import com.catcafe.app.ui.product.ProductListScreen
import com.catcafe.app.ui.profile.ChangePasswordScreen
import com.catcafe.app.ui.profile.EditProfileScreen
import com.catcafe.app.ui.profile.ProfileScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val CAT_LIST = "cat_list"
    const val CAT_DETAIL = "cat_detail/{catId}"
    const val PRODUCT_LIST = "product_list"
    const val PRODUCT_DETAIL = "product_detail/{productId}"
    const val ORDER_CREATE = "order_create"
    const val ORDER_LIST = "order_list"
    const val ORDER_DETAIL = "order_detail/{orderId}"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val CHANGE_PASSWORD = "change_password"
    const val COMMENT_LIST = "comment_list/{targetType}/{targetId}"
    const val MY_LIKES = "my_likes"
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(token: String?) {
    val navController = rememberNavController()
    val startDestination = if (token.isNullOrEmpty()) Routes.LOGIN else Routes.HOME

    val bottomNavItems = listOf(
        BottomNavItem(Routes.HOME, "首页") { Icon(Icons.Default.Home, contentDescription = "首页") },
        BottomNavItem(Routes.CAT_LIST, "猫咪") { Icon(Icons.Default.Favorite, contentDescription = "猫咪") },
        BottomNavItem(Routes.ORDER_LIST, "订单") { Icon(Icons.Default.ShoppingCart, contentDescription = "订单") },
        BottomNavItem(Routes.PROFILE, "我的") { Icon(Icons.Default.Person, contentDescription = "我的") },
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(Routes.HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = item.icon,
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Routes.HOME) {
                HomeScreen(
                    onNavigateToCatDetail = { catId -> navController.navigate("cat_detail/$catId") },
                    onNavigateToProductDetail = { productId -> navController.navigate("product_detail/$productId") },
                    onNavigateToProductList = { navController.navigate(Routes.PRODUCT_LIST) }
                )
            }
            composable(Routes.CAT_LIST) {
                CatListScreen(
                    onNavigateToDetail = { catId -> navController.navigate("cat_detail/$catId") }
                )
            }
            composable(
                Routes.CAT_DETAIL,
                arguments = listOf(navArgument("catId") { type = NavType.IntType })
            ) { backStackEntry ->
                val catId = backStackEntry.arguments?.getInt("catId") ?: 0
                CatDetailScreen(
                    catId = catId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToComments = { targetType, targetId ->
                        navController.navigate("comment_list/$targetType/$targetId")
                    }
                )
            }
            composable(Routes.PRODUCT_LIST) {
                ProductListScreen(
                    onNavigateToDetail = { productId -> navController.navigate("product_detail/$productId") }
                )
            }
            composable(
                Routes.PRODUCT_DETAIL,
                arguments = listOf(navArgument("productId") { type = NavType.IntType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getInt("productId") ?: 0
                ProductDetailScreen(
                    productId = productId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToComments = { targetType, targetId ->
                        navController.navigate("comment_list/$targetType/$targetId")
                    },
                    onNavigateToOrderCreate = { navController.navigate(Routes.ORDER_CREATE) }
                )
            }
            composable(Routes.ORDER_CREATE) {
                OrderCreateScreen(
                    onOrderSuccess = {
                        navController.navigate(Routes.ORDER_LIST) {
                            popUpTo(Routes.HOME)
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Routes.ORDER_LIST) {
                OrderListScreen(
                    onNavigateToDetail = { orderId -> navController.navigate("order_detail/$orderId") }
                )
            }
            composable(
                Routes.ORDER_DETAIL,
                arguments = listOf(navArgument("orderId") { type = NavType.IntType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getInt("orderId") ?: 0
                OrderDetailScreen(
                    orderId = orderId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onNavigateToEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                    onNavigateToChangePassword = { navController.navigate(Routes.CHANGE_PASSWORD) },
                    onNavigateToMyLikes = { navController.navigate(Routes.MY_LIKES) },
                    onLogout = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.EDIT_PROFILE) {
                EditProfileScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Routes.CHANGE_PASSWORD) {
                ChangePasswordScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(
                Routes.COMMENT_LIST,
                arguments = listOf(
                    navArgument("targetType") { type = NavType.IntType },
                    navArgument("targetId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val targetType = backStackEntry.arguments?.getInt("targetType") ?: 0
                val targetId = backStackEntry.arguments?.getInt("targetId") ?: 0
                CommentListScreen(
                    targetType = targetType,
                    targetId = targetId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Routes.MY_LIKES) {
                MyLikesScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
