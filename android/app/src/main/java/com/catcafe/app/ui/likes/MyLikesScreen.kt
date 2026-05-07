package com.catcafe.app.ui.likes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.catcafe.app.data.model.LikeResponse
import com.catcafe.app.data.repository.LikeRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyLikesScreen(onNavigateBack: () -> Unit) {
    val repository = remember { LikeRepository() }
    val scope = rememberCoroutineScope()
    var likes by remember { mutableStateOf<List<LikeResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf<Int?>(null) }

    fun refresh() {
        scope.launch {
            isLoading = true
            repository.getLikes(likeType = selectedType).onSuccess { likes = it.items }
            isLoading = false
        }
    }

    LaunchedEffect(selectedType) { refresh() }

    val typeTabs = listOf(null to "全部", 0 to "商品", 1 to "猫咪")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("我的点赞") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                )
                ScrollableTabRow(selectedTabIndex = typeTabs.indexOfFirst { it.first == selectedType }) {
                    typeTabs.forEach { (type, label) ->
                        Tab(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            text = { Text(label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (likes.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("暂无点赞记录")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(likes) { like ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    like.objectName ?: "ID: ${like.objectId}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    if (like.likeType == 0) "商品" else "猫咪",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    like.createTime.take(19).replace("T", " "),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(onClick = {
                                scope.launch {
                                    repository.unlike(like.likeId).onSuccess { refresh() }
                                }
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "取消点赞",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
