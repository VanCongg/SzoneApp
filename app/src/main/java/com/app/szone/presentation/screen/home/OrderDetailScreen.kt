package com.app.szone.presentation.screen.home

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.szone.presentation.state.DeliveryUpdateState
import com.app.szone.presentation.viewmodel.OrderUiState
import com.app.szone.presentation.viewmodel.OrderViewModel
import com.app.szone.presentation.viewmodel.CurrentUserViewModel
import org.koin.compose.viewmodel.koinViewModel
import java.text.NumberFormat
import java.util.*

// ✅ Track which action is in progress
sealed class ActionInProgress {
    object None : ActionInProgress()
    object Success : ActionInProgress()
    object Fail : ActionInProgress()
}

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

    val shipperName = userState.user?.fullName.orEmpty()
    val shipperPhone = userState.user?.phone.orEmpty()

    // ✅ Track which action is in progress (Success, Fail, or None)
    var actionInProgress by remember { mutableStateOf<ActionInProgress>(ActionInProgress.None) }

    LaunchedEffect(orderId, shipperName, shipperPhone) {
        // ✅ Reset updateState first to prevent triggering old state
        viewModel.resetUpdateState()

        // Wait until profile data is ready to avoid invalid request params.
        if (shipperName.isBlank() || shipperPhone.isBlank()) {
            android.util.Log.d("OrderDetailScreen", "⏳ Waiting user profile before loading order")
            return@LaunchedEffect
        }

        // ✅ Chỉ load nếu order chưa có trong state
        if (state.order?.id != orderId) {
            android.util.Log.d("OrderDetailScreen", "📡 Loading order: $orderId")
            viewModel.loadOrder(orderId = orderId, shipperName = shipperName, shipperPhone = shipperPhone)
        } else {
            android.util.Log.d("OrderDetailScreen", "✅ Order already loaded: $orderId, skip API call")
        }
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
        // ✅ Only process if an action is in progress
        if (actionInProgress == ActionInProgress.None) return@LaunchedEffect

        when (updateState) {
            is DeliveryUpdateState.Success -> {
                val message = when (actionInProgress) {
                    ActionInProgress.Success -> "Giao hàng thành công"
                    ActionInProgress.Fail -> "Đã cập nhật giao thất bại"
                    ActionInProgress.None -> null
                }
                message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                viewModel.clearMessages()
                viewModel.resetUpdateState()
                actionInProgress = ActionInProgress.None
                onBackClick()
            }
            is DeliveryUpdateState.Error -> {
                val error = updateState as DeliveryUpdateState.Error
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                viewModel.resetUpdateState()
                actionInProgress = ActionInProgress.None
            }
            else -> Unit
        }
    }

    OrderDetailContent(
        state = state,
        actionInProgress = actionInProgress,
        onBackClick = onBackClick,
        onFailClick = { id ->
            // ✅ Only proceed if no action is in progress
            if (actionInProgress == ActionInProgress.None) {
                actionInProgress = ActionInProgress.Fail
                viewModel.confirmFail(id)
            }
        },
        onSuccessClick = { id, shopId ->
            // ✅ Only proceed if no action is in progress
            if (actionInProgress == ActionInProgress.None) {
                actionInProgress = ActionInProgress.Success
                viewModel.confirmSuccess(id, shopId)
            }
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderDetailContent(
    state: OrderUiState,
    actionInProgress: ActionInProgress,
    onBackClick: () -> Unit,
    onFailClick: (String) -> Unit,
    onSuccessClick: (String, String) -> Unit,
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFFCE4EC), Color(0xFFF3E5F5), Color(0xFFFFFFFF))
    )

    // SỬA LỖI: Bọc toàn bộ trong Box có Gradient để lấp khoảng trống phía dưới hệ thống
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Chi tiết đơn hàng", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            bottomBar = {
                if (state.canShowActions && state.order != null) {
                    // ✅ Nút nằm trên navigation bar, cách đáy 4dp
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onFailClick(state.order.id) },
                            modifier = Modifier.weight(1f).height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFE57373)),
                            enabled = actionInProgress == ActionInProgress.None  // ✅ Only enable if no action in progress
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFD32F2F))
                            Spacer(Modifier.width(8.dp))
                            Text("Thất bại", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onSuccessClick(state.order.id, state.order.shop.id) },
                            modifier = Modifier.weight(1f).height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                            enabled = actionInProgress == ActionInProgress.None  // ✅ Only enable if no action in progress
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Thành công", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            // Đặt Scaffold là Transparent để nhìn xuyên qua lớp Box Gradient
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // SỬA LỖI: Xóa background(gradient) ở đây để không bị chồng lấp sai cách
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().clip(CircleShape))
                }

                state.order?.let { order ->
                    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))

                    DetailCard(title = "Thông tin khách hàng", icon = Icons.Default.Person, iconColor = Color(0xFF1976D2)) {
                        InfoRow(label = "Đơn hàng", value = "#${order.id.take(8)}", isHighlight = true)
                        InfoRow(label = "Người nhận", value = order.recipient.name)
                        InfoRow(label = "Điện thoại", value = order.recipient.phoneNumber)
                        InfoRow(label = "Địa chỉ", value = order.recipient.address)
                    }

                    DetailCard(title = "Thông tin cửa hàng", icon = Icons.Default.Store, iconColor = Color(0xFFF57C00)) {
                        InfoRow(label = "Tên shop", value = order.shop.name)
                        InfoRow(label = "Điện thoại", value = order.shop.phoneNumber)
                        InfoRow(label = "Địa chỉ", value = order.shop.address)
                    }

                    DetailCard(title = "Danh sách sản phẩm (${order.productList.size})", icon = Icons.Default.Inventory, iconColor = Color(0xFF7B1FA2)) {
                        order.productList.forEach { p ->
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text("•", Modifier.padding(end = 8.dp))
                                Column {
                                    Text(p.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Phân loại: ${p.sku} | SL: ${p.quantity}", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    DetailCard(title = "Chi tiết thanh toán", icon = Icons.Default.Payments, iconColor = Color(0xFF388E3C)) {
                        PaymentRow("Tiền hàng", "${formatter.format(order.price)} đ")
                        PaymentRow("Phí vận chuyển", "${formatter.format(order.shippingFee)} đ")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tổng thu hộ (COD)", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            Text(
                                "${formatter.format(order.price + order.shippingFee)} đ",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = Color(0xFFAD1457)
                            )
                        }
                    }

                    // Spacer cuối để nội dung không bị dính sát vào mép của BottomBar
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
@Composable
fun DetailCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.DarkGray)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color(0xFFEEEEEE))
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()) {
        Text(text = "$label: ", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.width(85.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlight) Color(0xFFAD1457) else Color.Black
        )
    }
}

@Composable
fun PaymentRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}