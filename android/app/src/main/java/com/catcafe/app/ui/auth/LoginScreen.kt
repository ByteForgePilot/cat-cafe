package com.catcafe.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.catcafe.app.data.model.LoginRequest
import com.catcafe.app.data.repository.AuthRepository
import com.catcafe.app.util.TokenManager
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val tokenManager = TokenManager(context)
    val repository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()

    var userName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("猫咖点单", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("欢迎回来", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = { userName = it; errorMessage = null },
            label = { Text("用户名") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = { Text("密码") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(8.dp))

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (userName.isBlank() || password.isBlank()) {
                    errorMessage = "请填写用户名和密码"
                    return@Button
                }
                scope.launch {
                    isLoading = true
                    errorMessage = null
                    val result = repository.login(LoginRequest(userName, password))
                    isLoading = false
                    result.fold(
                        onSuccess = { loginResp ->
                            scope.launch {
                                tokenManager.saveAuth(loginResp.token, loginResp.user.userId, loginResp.user.userType)
                                onLoginSuccess()
                            }
                        },
                        onFailure = { errorMessage = it.message ?: "登录失败" }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else Text("登录")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("没有账号？立即注册")
        }
    }
}
