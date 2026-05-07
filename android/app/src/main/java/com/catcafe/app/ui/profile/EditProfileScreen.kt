package com.catcafe.app.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.catcafe.app.data.model.UserDetailResponse
import com.catcafe.app.data.model.UserUpdateRequest
import com.catcafe.app.data.repository.UserRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onNavigateBack: () -> Unit) {
    val repository = remember { UserRepository() }
    val scope = rememberCoroutineScope()

    var user by remember { mutableStateOf<UserDetailResponse?>(null) }
    var userName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf<Int?>(null) }
    var birthday by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        repository.getMyProfile().onSuccess {
            user = it
            userName = it.userName
            gender = it.gender
            birthday = it.birthday ?: ""
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑资料") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it; errorMessage = null },
                    label = { Text("用户名") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Gender selector
                Text("性别", modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = gender == 1,
                        onClick = { gender = 1 },
                        label = { Text("男") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = gender == 0,
                        onClick = { gender = 0 },
                        label = { Text("女") }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = birthday,
                    onValueChange = { birthday = it; errorMessage = null },
                    label = { Text("生日 (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                successMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = {
                        scope.launch {
                            isSaving = true
                            errorMessage = null
                            successMessage = null
                            val request = UserUpdateRequest(
                                userName = if (userName != user?.userName) userName else null,
                                gender = if (gender != user?.gender) gender else null,
                                birthday = if (birthday != user?.birthday) birthday else null
                            )
                            repository.updateProfile(request).fold(
                                onSuccess = {
                                    successMessage = "保存成功"
                                    user = it
                                },
                                onFailure = { errorMessage = it.message }
                            )
                            isSaving = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                ) {
                    if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    else Text("保存")
                }
            }
        }
    }
}
