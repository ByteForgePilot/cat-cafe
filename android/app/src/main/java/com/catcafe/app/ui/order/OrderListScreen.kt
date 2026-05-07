package com.catcafe.app.ui.order

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.catcafe.app.data.model.BatchOrderResponse
import com.catcafe.app.data.repository.OrderRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(onNavigateToDetail: (Int) -> Unit) {
    val repository = remember { OrderRepository() }
    val scope = rememberCoroutineScope()
    var orders by remember { mutableStateOf<List<BatchOrderResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(selectedStatus) {
        scope.launch {
            isLoading = true
            repository.getOrders(orderStatus = selectedStatus).onSuccess { orders = it.items }
            isLoading = false
        }
    }

    val statusTabs = listOf(
        null to "全部",
        0 to "待支付",
        1 to "已支付",
        2 to "制作中",
        3 to "已完成",
        4 to "已取消"
    )

    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text("我的订单") })
                ScrollableTabRow(selectedTabIndex = statusTabs.indexOfFirst { it.first == selectedStatus }) {
                    statusTabs.forEach { (status, label) ->
                        Tab(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = status },
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
        } else if (orders.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("暂无订单", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(orders) { batch ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable {
                                // Navigate using first item's orderId
                                batch.items.firstOrNull()?.orderId?.let { onNavigateToDetail(it) }
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("批次: ${batch.batchNo}", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    when (batch.orderStatus) {
                                        0 -> "待支付"; 1 -> "已支付"; 2 -> "制作中"; 3 -> "已完成"; 4 -> "已取消"; else -> "未知"
                                    },
                                    color = when (batch.orderStatus) {
                                        0 -> MaterialTheme.colorScheme.error; 3 -> MaterialTheme.colorScheme.primary; else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            batch.items.forEach { item ->
                                Text(
                                    "${item.productName} x${item.quantity}  ¥%.2f".format(item.totalAmount),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "合计: ¥%.2f".format(batch.totalAmount),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                batch.createTime.take(19).replace("T", " "),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
