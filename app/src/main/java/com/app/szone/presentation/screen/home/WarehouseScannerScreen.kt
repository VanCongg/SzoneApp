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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.app.szone.presentation.navigation.NavScreen
import com.app.szone.presentation.viewmodel.WarehouseActionState
import com.app.szone.presentation.viewmodel.WarehouseViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WarehouseScannerScreen(navController: NavController? = null, viewModel: WarehouseViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val action by viewModel.actionState.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showScanDialog by remember { mutableStateOf(false) }
    var orderIdInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadWarehouseInfo()
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(action) {
        when (val current = action) {
            is WarehouseActionState.Success -> {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90).startTone(ToneGenerator.TONE_PROP_BEEP, 120)
                Toast.makeText(context, current.message, Toast.LENGTH_SHORT).show()
                viewModel.resetActionState()
            }
            is WarehouseActionState.Error -> {
                Toast.makeText(context, current.message, Toast.LENGTH_SHORT).show()
                viewModel.resetActionState()
            }
            else -> Unit
        }
    }

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
                Text(text = "Xin chào ${state.userName.ifBlank { "Scanner" }}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
            onClick = { showScanDialog = true },
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
            onDismissRequest = { showScanDialog = false },
            title = { Text("Nhập mã đơn từ QR") },
            text = {
                OutlinedTextField(
                    value = orderIdInput,
                    onValueChange = { orderIdInput = it },
                    singleLine = true,
                    label = { Text("orderId") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showScanDialog = false
                        viewModel.scanOrder(orderIdInput.trim())
                        orderIdInput = ""
                    }
                ) {
                    Text("Quét")
                }
            },
            dismissButton = {
                TextButton(onClick = { showScanDialog = false }) { Text("Hủy") }
            }
        )
    }
}

@Composable
fun ShipperScannerScreen(navController: NavController? = null, viewModel: WarehouseViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var showScanDialog by remember { mutableStateOf(false) }
    var orderIdInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Shipper Scanner", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Button(onClick = { showScanDialog = true }) {
            Text("Quét QR đơn giao")
        }

        if (state.scannedOrders.isNotEmpty()) {
            Text("Gần đây", fontWeight = FontWeight.SemiBold)
            state.scannedOrders.take(5).forEach { id ->
                Text("#$id")
            }
        }
    }

    if (showScanDialog) {
        AlertDialog(
            onDismissRequest = { showScanDialog = false },
            title = { Text("Nhập orderId") },
            text = {
                OutlinedTextField(
                    value = orderIdInput,
                    onValueChange = { orderIdInput = it },
                    singleLine = true,
                    label = { Text("orderId") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val id = orderIdInput.trim()
                    showScanDialog = false
                    if (id.isNotBlank()) {
                        navController?.navigate(NavScreen.OrderDetailNavScreen(id))
                    }
                    orderIdInput = ""
                }) { Text("Mở đơn") }
            },
            dismissButton = {
                TextButton(onClick = { showScanDialog = false }) { Text("Hủy") }
            }
        )
    }
}
