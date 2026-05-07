package com.catcafe.app.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.catcafe.app.data.model.UserDetailResponse
import com.catcafe.app.data.repository.UserRepository
import com.catcafe.app.util.TokenManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToMyLikes: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val tokenManager = TokenManager(context)
    val repository = remember { UserRepository() }
    val scope = rememberCoroutineScope()

    var user by remember { mutableStateOf<UserDetailResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        repository.getMyProfile().onSuccess { user = it }
        isLoading = false
    }

    Scaffold(topBar = { TopAppBar(title = { Text("我的") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else user?.let { u ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(u.userName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(u.phone, style = MaterialTheme.typography.bodyMedium)
                        if (u.userType == 1) {
                            Text("管理员", color = MaterialTheme.colorScheme.primary)
                        }
                        u.gender?.let {
                            Text(if (it == 1) "男" else "女", style = MaterialTheme.typography.bodySmall)
                        }
                        u.birthday?.let {
                            Text("生日: $it", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                ListItem(
                    headlineContent = { Text("编辑资料") },
                    leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigateToEditProfile() }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("修改密码") },
                    leadingContent = { Icon(Icons.Default.Lock, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigateToChangePassword() }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("我的点赞") },
                    leadingContent = { Icon(Icons.Default.Favorite, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigateToMyLikes() }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("退出登录", color = MaterialTheme.colorScheme.error) },
                    leadingContent = {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    modifier = Modifier.clickable { showLogoutDialog = true }
                )
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("退出登录") },
                text = { Text("确定要退出登录吗？") },
                confirmButton = {
                    TextButton(onClick = {
                        showLogoutDialog = false
                        scope.launch {
                            tokenManager.clear()
                            onLogout()
                        }
                    }) { Text("确定") }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) { Text("取消") }
                }
            )
        }
    }
}

