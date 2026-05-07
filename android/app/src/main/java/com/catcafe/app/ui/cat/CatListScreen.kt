package com.catcafe.app.ui.cat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.catcafe.app.data.model.CatResponse
import com.catcafe.app.data.repository.CatRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatListScreen(onNavigateToDetail: (Int) -> Unit) {
    val repository = remember { CatRepository() }
    val scope = rememberCoroutineScope()
    var cats by remember { mutableStateOf<List<CatResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            repository.getCats().onSuccess { cats = it.items }
            isLoading = false
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("在岗猫咪") }) }) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(cats) { cat ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { onNavigateToDetail(cat.catId) }
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            AsyncImage(
                                model = cat.catAvatar,
                                contentDescription = cat.catName,
                                modifier = Modifier.size(80.dp),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(cat.catName, fontWeight = FontWeight.Bold)
                                cat.catBreed?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                cat.catAge?.let { Text("${it}岁", style = MaterialTheme.typography.bodySmall) }
                                cat.description?.let {
                                    Text(it, maxLines = 2, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
