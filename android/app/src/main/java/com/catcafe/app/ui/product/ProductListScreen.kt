package com.catcafe.app.ui.product

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
import com.catcafe.app.data.model.ProductResponse
import com.catcafe.app.data.repository.ProductRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(onNavigateToDetail: (Int) -> Unit) {
    val repository = remember { ProductRepository() }
    val scope = rememberCoroutineScope()
    var products by remember { mutableStateOf<List<ProductResponse>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(selectedCategory) {
        scope.launch {
            isLoading = true
            repository.getProducts(category = selectedCategory).onSuccess { products = it.items }
            isLoading = false
        }
    }

    val categories = listOf(null to "全部", "饮品" to "饮品", "甜品" to "甜品", "简餐" to "简餐")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text("商品列表") })
                ScrollableTabRow(selectedTabIndex = categories.indexOfFirst { it.first == selectedCategory }) {
                    categories.forEach { (cat, label) ->
                        Tab(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            text = { Text(label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(products) { product ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { onNavigateToDetail(product.productId) }
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            AsyncImage(
                                model = product.imageUrl,
                                contentDescription = product.productName,
                                modifier = Modifier.size(80.dp),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.productName, fontWeight = FontWeight.Bold)
                                product.description?.let {
                                    Text(it, maxLines = 2, style = MaterialTheme.typography.bodySmall)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("¥%.2f".format(product.price), color = MaterialTheme.colorScheme.primary)
                                Text("库存: ${product.stockQuantity}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
