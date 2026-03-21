package com.app.szone.presentation.screen.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.szone.presentation.viewmodel.DeliveryUpdateState
import com.app.szone.presentation.viewmodel.OrderViewModel
import org.koin.compose.viewmodel.koinViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onBackClick: () -> Unit = {},
    viewModel: OrderViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId = orderId, shipperName = "Nguyen Huy", shipperPhone = "0984123449")
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(updateState) {
        when (val current = updateState) {
            is DeliveryUpdateState.Success -> {
                Toast.makeText(context, current.message, Toast.LENGTH_SHORT).show()
                viewModel.resetUpdateState()
                onBackClick()
            }
            is DeliveryUpdateState.Error -> {
                Toast.makeText(context, current.message, Toast.LENGTH_SHORT).show()
                viewModel.resetUpdateState()
            }
            else -> Unit
        }
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFF8BBD0), Color(0xFFFCE4EC), Color(0xFFFAFAFA))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết đơn hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            }

            state.order?.let { order ->
                val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
                val total = order.totalMoney

                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Đơn #${order.id}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Người nhận: ${order.recipient.name}")
                        Text("SĐT: ${order.recipient.phoneNumber}")
                        Text("Địa chỉ: ${order.recipient.address}")
                    }
                }

                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Shop: ${order.shop.name}", fontWeight = FontWeight.SemiBold)
                        Text("SĐT shop: ${order.shop.phoneNumber}")
                        Text("Địa chỉ shop: ${order.shop.address}")
                    }
                }

                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Tiền hàng: ${formatter.format(order.price)} đ")
                        Text("Phí ship: ${formatter.format(order.shippingFee)} đ")
                        Text(
                            "Tổng cần thu: ${formatter.format(total)} đ",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                }

                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Danh sách sản phẩm", fontWeight = FontWeight.Bold)
                        order.productList.forEachIndexed { index, p ->
                            Text("${index + 1}. ${p.name} | ${p.sku} | SL: ${p.quantity}")
                        }
                    }
                }

                if (state.canShowActions) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.confirmFail(order.id) },
                            modifier = Modifier.weight(1f),
                            enabled = updateState !is DeliveryUpdateState.Loading
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null)
                            Text(" Giao thất bại")
                        }
                        Button(
                            onClick = { viewModel.confirmSuccess(order.id, order.shop.id) },
                            modifier = Modifier.weight(1f),
                            enabled = updateState !is DeliveryUpdateState.Loading,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Text(" Giao thành công")
                        }
                    }
                }
            }
        }
    }
}
