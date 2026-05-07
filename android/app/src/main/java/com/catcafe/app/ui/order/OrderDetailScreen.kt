package com.catcafe.app.ui.order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.catcafe.app.data.model.BatchOrderResponse
import com.catcafe.app.data.model.OrderUpdateRequest
import com.catcafe.app.data.repository.OrderRepository
import com.catcafe.app.util.TokenManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: Int,
    onNavigateBack: () -> Unit
) {
    val repository = remember { OrderRepository() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val tokenManager = TokenManager(context)

    var order by remember { mutableStateOf<BatchOrderResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isDeleting by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var isAdmin = remember { tokenManager.isAdminBlocking() }

    fun refreshOrder() {
        scope.launch {
            repository.getOrderDetail(orderId).fold(
                onSuccess = { order = it },
                onFailure = { errorMessage = it.message }
            )
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { refreshOrder() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("订单详情") },
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
        } else order?.let { o ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("批次号: ${o.batchNo}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "状态: ${when (o.orderStatus) {
                                0 -> "待支付"; 1 -> "已支付"; 2 -> "制作中"; 3 -> "已完成"; 4 -> "已取消"; else -> "未知"
                            }}",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("下单时间: ${o.createTime.take(19).replace("T", " ")}")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("商品明细", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                o.items.forEach { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.productName, fontWeight = FontWeight.Bold)
                                Text("单价: ¥%.2f x ${item.quantity}".format(item.price))
                            }
                            Text(
                                "¥%.2f".format(item.totalAmount),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("合计: ¥%.2f".format(o.totalAmount), fontWeight = FontWeight.Bold)
                    }
                }

                o.items.firstOrNull()?.let { first ->
                    first.userPhone?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("联系电话: $it")
                    }
                    first.orderNote?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("备注: $it")
                    }
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                resultMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Delete button
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除订单")
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("确认删除") },
                text = { Text("确定要删除这个订单吗？") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            isDeleting = true
                            repository.deleteOrder(orderId).fold(
                                onSuccess = { onNavigateBack() },
                                onFailure = { errorMessage = it.message }
                            )
                            isDeleting = false
                        }
                    }) { Text("确定") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
                }
            )
        }
    }
}
