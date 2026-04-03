package com.app.szone.presentation.screen.home

import android.media.AudioManager
import android.media.ToneGenerator
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.app.szone.domain.model.WarehouseModel
import com.app.szone.presentation.navigation.NavScreen
import com.app.szone.presentation.ui.theme.SZoneTheme
import com.app.szone.presentation.viewmodel.WarehouseActionState
import com.app.szone.presentation.viewmodel.WarehouseUiState
import com.app.szone.presentation.viewmodel.WarehouseViewModel
import com.app.szone.presentation.viewmodel.CurrentUserViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WarehouseScannerScreen(
    navController: NavController? = null,
    warehouseViewModel: WarehouseViewModel = koinViewModel(),
    currentUserViewModel: CurrentUserViewModel = koinViewModel()
) {
    val state by warehouseViewModel.uiState.collectAsState()
    val action by warehouseViewModel.actionState.collectAsState()
    val userState by currentUserViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showScanDialog by remember { mutableStateOf(false) }
    var orderIdInput by remember { mutableStateOf("") }

    // Get user name from CurrentUserViewModel (from login response)
    val userName = userState.user?.fullName ?: "Scanner"

    android.util.Log.d("WarehouseScanner", "🔍 Screen rendered")
    android.util.Log.d("WarehouseScanner", "  - userName: '$userName'")
    android.util.Log.d("WarehouseScanner", "  - user.fullName: '${userState.user?.fullName}'")

    LaunchedEffect(Unit) {
        android.util.Log.d("WarehouseScanner", "🔄 Refreshing warehouse info and current user")
        currentUserViewModel.refresh()  // Ensure fresh user data
        warehouseViewModel.loadWarehouseInfo()
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            warehouseViewModel.clearMessages()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            warehouseViewModel.clearMessages()
        }
    }

    LaunchedEffect(action) {
        when (action) {
            is WarehouseActionState.Success -> {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90).startTone(ToneGenerator.TONE_PROP_BEEP, 120)
                Toast.makeText(context, "Quét hàng thành công", Toast.LENGTH_SHORT).show()
                warehouseViewModel.resetActionState()
            }
            is WarehouseActionState.Error -> {
                val error = action as WarehouseActionState.Error
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                warehouseViewModel.resetActionState()
            }
            else -> Unit
        }
    }

    WarehouseScannerContent(
        userName = userName,
        state = state,
        action = action,
        showScanDialog = showScanDialog,
        orderIdInput = orderIdInput,
        onOpenScanDialog = { showScanDialog = true },
        onDismissScanDialog = { showScanDialog = false },
        onOrderIdChange = { orderIdInput = it },
        onScanConfirm = {
            showScanDialog = false
            warehouseViewModel.scanOrderArrived(orderIdInput.trim())
            orderIdInput = ""
        }
    )
}

@Composable
private fun WarehouseScannerContent(
    userName: String,
    state: WarehouseUiState,
    action: WarehouseActionState,
    showScanDialog: Boolean,
    orderIdInput: String,
    onOpenScanDialog: () -> Unit,
    onDismissScanDialog: () -> Unit,
    onOrderIdChange: (String) -> Unit,
    onScanConfirm: () -> Unit,
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFE3F2FD), Color(0xFFF5F9FF), Color.White)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "Xin chào ${userName.ifBlank { "Scanner" }}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = "Kho hiện tại: ${state.warehouse?.name ?: "Chưa có dữ liệu"}",
                    color = Color(0xFF1565C0),
                    fontWeight = FontWeight.SemiBold
                )
                Text(text = state.warehouse?.address ?: "")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onOpenScanDialog,
            shape = CircleShape,
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
            enabled = action !is WarehouseActionState.Loading && !state.isLoading
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Quét đơn", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        if (state.isLoading || action is WarehouseActionState.Loading) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator()
            }
        }

        Text("Đơn hàng đã quét trong phiên", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.scannedOrders) { id ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFF1976D2))
                        Spacer(modifier = Modifier.size(10.dp))
                        Text("Đơn #$id", modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    if (showScanDialog) {
        AlertDialog(
            onDismissRequest = onDismissScanDialog,
            title = { Text("Nhập mã đơn từ QR") },
            text = {
                OutlinedTextField(
                    value = orderIdInput,
                    onValueChange = onOrderIdChange,
                    singleLine = true,
                    label = { Text("orderId") }
                )
            },
            confirmButton = {
                TextButton(onClick = onScanConfirm) {
                    Text("Quét")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissScanDialog) { Text("Hủy") }
            }
        )
    }
}

@Composable
fun ShipperScannerScreen(navController: NavController? = null, viewModel: WarehouseViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var showScanDialog by remember { mutableStateOf(false) }
    var orderIdInput by remember { mutableStateOf("") }

    ShipperScannerContent(
        scannedOrders = state.scannedOrders,
        showScanDialog = showScanDialog,
        orderIdInput = orderIdInput,
        onOpenScanDialog = { showScanDialog = true },
        onDismissScanDialog = { showScanDialog = false },
        onOrderIdChange = { orderIdInput = it },
        onOpenOrderClick = {
            val id = orderIdInput.trim()
            showScanDialog = false
            if (id.isNotBlank()) {
                navController?.navigate(NavScreen.OrderDetailNavScreen(id))
            }
            orderIdInput = ""
        }
    )
}

@Composable
private fun ShipperScannerContent(
    scannedOrders: List<String>,
    showScanDialog: Boolean,
    orderIdInput: String,
    onOpenScanDialog: () -> Unit,
    onDismissScanDialog: () -> Unit,
    onOrderIdChange: (String) -> Unit,
    onOpenOrderClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Shipper Scanner", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Button(onClick = onOpenScanDialog) {
            Text("Quét QR đơn giao")
        }

        if (scannedOrders.isNotEmpty()) {
            Text("Gần đây", fontWeight = FontWeight.SemiBold)
            scannedOrders.take(5).forEach { id ->
                Text("#$id")
            }
        }
    }

    if (showScanDialog) {
        AlertDialog(
            onDismissRequest = onDismissScanDialog,
            title = { Text("Nhập orderId") },
            text = {
                OutlinedTextField(
                    value = orderIdInput,
                    onValueChange = onOrderIdChange,
                    singleLine = true,
                    label = { Text("orderId") }
                )
            },
            confirmButton = {
                TextButton(onClick = onOpenOrderClick) { Text("Mở đơn") }
            },
            dismissButton = {
                TextButton(onClick = onDismissScanDialog) { Text("Hủy") }
            }
        )
    }
}