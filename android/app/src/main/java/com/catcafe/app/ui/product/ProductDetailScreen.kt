package com.catcafe.app.ui.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.catcafe.app.data.model.ProductResponse
import com.catcafe.app.data.repository.ProductRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToComments: (targetType: Int, targetId: Int) -> Unit,
    onNavigateToOrderCreate: () -> Unit
) {
    val repository = remember { ProductRepository() }
    val scope = rememberCoroutineScope()
    var product by remember { mutableStateOf<ProductResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(productId) {
        repository.getProductDetail(productId).onSuccess {
            product = it
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product?.productName ?: "商品详情") },
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
        } else product?.let { p ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                AsyncImage(
                    model = p.imageUrl,
                    contentDescription = p.productName,
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(p.productName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("¥%.2f".format(p.price), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    p.category?.let { Text("分类: $it") }
                    Text("库存: ${p.stockQuantity}")
                    p.description?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                Divider()

                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onNavigateToComments(0, productId) },
                        modifier = Modifier.weight(1f)
                    ) { Text("评论") }
                    Button(
                        onClick = onNavigateToOrderCreate,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("下单")
                    }
                }
            }
        }
    }
}
