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
import com.app.szone.domain.model.OrderModel
import com.app.szone.domain.model.ProductModel
import com.app.szone.domain.model.RecipientModel
import com.app.szone.domain.model.ShopModel
import com.app.szone.presentation.ui.theme.SZoneTheme
import com.app.szone.presentation.state.DeliveryUpdateState
import com.app.szone.presentation.viewmodel.OrderUiState
import com.app.szone.presentation.viewmodel.OrderViewModel
import com.app.szone.presentation.viewmodel.CurrentUserViewModel
import org.koin.compose.viewmodel.koinViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onBackClick: () -> Unit = {},
    viewModel: OrderViewModel = koinViewModel(),
    currentUserViewModel: CurrentUserViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val userState by currentUserViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val shipperName = userState.user?.fullName ?: "Shipper"
    val shipperPhone = userState.user?.phone ?: ""

    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId = orderId, shipperName = shipperName, shipperPhone = shipperPhone)
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(updateState) {
        when (updateState) {
            is DeliveryUpdateState.Success -> {
                Toast.makeText(context, "Giao hàng thành công", Toast.LENGTH_SHORT).show()
                viewModel.resetUpdateState()
                onBackClick()
            }
            is DeliveryUpdateState.Error -> {
                val error = updateState as DeliveryUpdateState.Error
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                viewModel.resetUpdateState()
            }
            else -> Unit
        }
    }

    OrderDetailContent(
        state = state,
        updateState = updateState,
        onBackClick = onBackClick,
        onFailClick = { id -> viewModel.confirmFail(id) },
        onSuccessClick = { id, shopId -> viewModel.confirmSuccess(id, shopId) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderDetailContent(
    state: OrderUiState,
    updateState: DeliveryUpdateState,
    onBackClick: () -> Unit,
    onFailClick: (String) -> Unit,
    onSuccessClick: (String, String) -> Unit,
) {
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
                            onClick = { onFailClick(order.id) },
                            modifier = Modifier.weight(1f),
                            enabled = updateState !is DeliveryUpdateState.Loading
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null)
                            Text(" Giao thất bại")
                        }
                        Button(
                            onClick = { onSuccessClick(order.id, order.shop.id) },
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

@Preview(showBackground = true)
@Composable
private fun OrderDetailScreenPreview() {
    val fakeOrder = OrderModel(
        id = "123456789",
        recipient = RecipientModel(
            name = "Nguyen Van A",
            phoneNumber = "0984123449",
            address = "So 4 Tan Binh, Phu Nhuan, HCM"
        ),
        shop = ShopModel(
            id = "shop-1",
            name = "SZone Shop",
            phoneNumber = "0909123456",
            address = "12 Cong Hoa, Tan Binh, HCM"
        ),
        shippingFee = 15000,
        price = 220000,
        productList = listOf(
            ProductModel(name = "Ao phong", sku = "SKU-01", quantity = 1),
            ProductModel(name = "Quan jean", sku = "SKU-02", quantity = 2)
        )
    )

    SZoneTheme {
        OrderDetailContent(
            state = OrderUiState(order = fakeOrder, canShowActions = true),
            updateState = DeliveryUpdateState.Idle,
            onBackClick = {},
            onFailClick = { _ -> },
            onSuccessClick = { _, _ -> }
        )
    }
}

