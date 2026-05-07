package com.catcafe.app.ui.comment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.catcafe.app.data.model.CommentCreateRequest
import com.catcafe.app.data.model.CommentResponse
import com.catcafe.app.data.repository.CommentRepository
import com.catcafe.app.util.TokenManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentListScreen(
    targetType: Int,
    targetId: Int,
    onNavigateBack: () -> Unit
) {
    val repository = remember { CommentRepository() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val tokenManager = TokenManager(context)

    var comments by remember { mutableStateOf<List<CommentResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var newComment by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun refreshComments() {
        scope.launch {
            isLoading = true
            repository.getComments(targetType = targetType, targetId = targetId).onSuccess {
                comments = it.items
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { refreshComments() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (targetType == 0) "商品评论" else "猫咪评论") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newComment,
                        onValueChange = { newComment = it },
                        label = { Text("发表评论") },
                        modifier = Modifier.weight(1f),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newComment.isBlank()) return@IconButton
                            scope.launch {
                                isSubmitting = true
                                errorMessage = null
                                repository.createComment(
                                    CommentCreateRequest(targetType, targetId, newComment)
                                ).fold(
                                    onSuccess = {
                                        newComment = ""
                                        refreshComments()
                                    },
                                    onFailure = { errorMessage = it.message }
                                )
                                isSubmitting = false
                            }
                        },
                        enabled = !isSubmitting
                    ) {
                        if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        else Text("发送")
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (comments.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("暂无评论，快来发表第一条评论吧")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                errorMessage?.let {
                    item {
                        Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                    }
                }
                items(comments) { comment ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row {
                                Text(
                                    comment.userName ?: "用户${comment.userId}",
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    comment.createTime.take(19).replace("T", " "),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(comment.content)
                        }
                    }
                }
            }
        }
    }
}
