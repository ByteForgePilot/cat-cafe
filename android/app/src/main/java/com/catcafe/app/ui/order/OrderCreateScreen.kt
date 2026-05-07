package com.catcafe.app.ui.order

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.catcafe.app.data.model.*
import com.catcafe.app.data.repository.OrderRepository
import com.catcafe.app.data.repository.ProductRepository
import kotlinx.coroutines.launch

data class CartItem(
    val product: ProductResponse,
    var quantity: Int = 1
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderCreateScreen(
    onOrderSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val productRepo = remember { ProductRepository() }
    val orderRepo = remember { OrderRepository() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var products by remember { mutableStateOf<List<ProductResponse>>(emptyList()) }
    var cartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var userPhone by remember { mutableStateOf("") }
    var orderNote by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var orderResult by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        productRepo.getProducts(limit = 50).onSuccess { result ->
            products = result.items
            isLoading = false
        }
    }

    val totalAmount = cartItems.sumOf { it.product.price * it.quantity }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("下单") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(tonalElevation = 3.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("合计: ¥%.2f".format(totalAmount), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    isSubmitting = true
                                    errorMessage = null
                                    val items = cartItems.map { OrderItem(it.product.productId, it.quantity) }
                                    val request = OrderCreateRequest(
                                        items = items,
                                        userPhone = userPhone.ifBlank { null },
                                        orderNote = orderNote.ifBlank { null }
                                    )
                                    orderRepo.createOrder(request).fold(
                                        onSuccess = {
                                            orderResult = "下单成功！批次号: ${it.batchNo}"
                                            // Navigate after a brief delay to show success
                                            kotlinx.coroutines.delay(1500)
                                            onOrderSuccess()
                                        },
                                        onFailure = { errorMessage = it.message }
                                    )
                                    isSubmitting = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSubmitting
                        ) {
                            if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            else Text("提交订单")
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                item {
                    Text("选择商品", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                }

                itemsIndexed(products) { index, product ->
                    val inCart = cartItems.find { it.product.productId == product.productId }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.productName, fontWeight = FontWeight.Bold)
                                Text("¥%.2f".format(product.price), color = MaterialTheme.colorScheme.primary)
                                Text("库存: ${product.stockQuantity}", style = MaterialTheme.typography.bodySmall)
                            }
                            if (inCart != null) {
                                IconButton(onClick = {
                                    if (inCart.quantity == 1) {
                                        cartItems = cartItems.filter { it != inCart }
                                    } else {
                                        inCart.quantity--
                                        cartItems = cartItems.toList()
                                    }
                                }) { Icon(Icons.Default.Remove, contentDescription = "减少") }
                                Text("${inCart.quantity}", modifier = Modifier.padding(horizontal = 8.dp))
                                IconButton(onClick = {
                                    if (inCart.quantity < product.stockQuantity) {
                                        inCart.quantity++
                                        cartItems = cartItems.toList()
                                    }
                                }) { Icon(Icons.Default.Add, contentDescription = "增加") }
                            } else {
                                IconButton(onClick = {
                                    cartItems = cartItems + CartItem(product, 1)
                                }) { Icon(Icons.Default.Add, contentDescription = "添加") }
                            }
                        }
                    }
                }

                if (cartItems.isNotEmpty()) {
                    item {
                        Text(
                            "订单信息",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                        OutlinedTextField(
                            value = userPhone,
                            onValueChange = { userPhone = it },
                            label = { Text("联系电话（可选）") },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = orderNote,
                            onValueChange = { orderNote = it },
                            label = { Text("备注（可选）") },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            maxLines = 3
                        )
                    }
                }

                errorMessage?.let {
                    item {
                        Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                    }
                }

                orderResult?.let {
                    item {
                        Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(16.dp))
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}
