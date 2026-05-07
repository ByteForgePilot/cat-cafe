package com.catcafe.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.catcafe.app.data.model.CatResponse
import com.catcafe.app.data.model.ProductResponse
import com.catcafe.app.data.repository.CatRepository
import com.catcafe.app.data.repository.ProductRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCatDetail: (Int) -> Unit,
    onNavigateToProductDetail: (Int) -> Unit,
    onNavigateToProductList: () -> Unit
) {
    val catRepo = remember { CatRepository() }
    val productRepo = remember { ProductRepository() }
    val scope = rememberCoroutineScope()

    var cats by remember { mutableStateOf<List<CatResponse>>(emptyList()) }
    var products by remember { mutableStateOf<List<ProductResponse>>(emptyList()) }

    LaunchedEffect(Unit) {
        catRepo.getCats(limit = 5).onSuccess { cats = it.items }
        productRepo.getProducts(limit = 5).onSuccess { products = it.items }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("猫咖点单") }) }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            item {
                Text(
                    "在岗猫咪",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {
                    items(cats) { cat ->
                        Card(
                            modifier = Modifier
                                .width(160.dp)
                                .padding(end = 12.dp)
                                .clickable { onNavigateToCatDetail(cat.catId) }
                        ) {
                            AsyncImage(
                                model = cat.catAvatar,
                                contentDescription = cat.catName,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(cat.catName, fontWeight = FontWeight.Bold)
                                cat.catBreed?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("精选商品", style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = onNavigateToProductList) { Text("查看全部") }
                }
            }
            items(products) { product ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { onNavigateToProductDetail(product.productId) }
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        AsyncImage(
                            model = product.imageUrl,
                            contentDescription = product.productName,
                            modifier = Modifier.size(72.dp),
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
                        }
                        Icon(Icons.Default.Coffee, contentDescription = null)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
