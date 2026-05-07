package com.catcafe.app.ui.cat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.catcafe.app.data.model.CatResponse
import com.catcafe.app.data.model.LikeCreateRequest
import com.catcafe.app.data.repository.CatRepository
import com.catcafe.app.data.repository.CommentRepository
import com.catcafe.app.data.repository.LikeRepository
import com.catcafe.app.util.TokenManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatDetailScreen(
    catId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToComments: (targetType: Int, targetId: Int) -> Unit
) {
    val repository = remember { CatRepository() }
    val likeRepo = remember { LikeRepository() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val tokenManager = TokenManager(context)

    var cat by remember { mutableStateOf<CatResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isLiked by remember { mutableStateOf(false) }
    var likeId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(catId) {
        repository.getCatDetail(catId).onSuccess {
            cat = it
            isLoading = false
        }
        likeRepo.getLikes(likeType = 1).onSuccess { likes ->
            val found = likes.items.find { it.objectId == catId }
            if (found != null) {
                isLiked = true
                likeId = found.likeId
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(cat?.catName ?: "猫咪详情") },
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
        } else cat?.let { c ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                AsyncImage(
                    model = c.catAvatar,
                    contentDescription = c.catName,
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(c.catName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            scope.launch {
                                if (isLiked && likeId != null) {
                                    likeRepo.unlike(likeId!!).onSuccess {
                                        isLiked = false
                                        likeId = null
                                    }
                                } else {
                                    likeRepo.like(LikeCreateRequest(likeType = 1, objectId = catId)).onSuccess {
                                        isLiked = true
                                        likeId = it.likeId
                                    }
                                }
                            }
                        }) {
                            Icon(
                                if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (isLiked) "取消点赞" else "点赞",
                                tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    c.catBreed?.let { Text("品种: $it", modifier = Modifier.padding(top = 8.dp)) }
                    c.catAge?.let { Text("年龄: ${it}岁", modifier = Modifier.padding(top = 4.dp)) }
                    c.catGender?.let {
                        Text("性别: ${if (it == 1) "公" else "母"}", modifier = Modifier.padding(top = 4.dp))
                    }
                    c.description?.let {
                        Text(it, modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyLarge)
                    }
                }

                Divider()

                Button(
                    onClick = { onNavigateToComments(1, catId) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text("查看评论")
                }
            }
        }
    }
}
