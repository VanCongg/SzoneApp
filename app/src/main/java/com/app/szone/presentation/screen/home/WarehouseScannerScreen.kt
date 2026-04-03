package com.app.szone.presentation.screen.home

import android.Manifest
import android.media.AudioManager
import android.media.ToneGenerator
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.app.szone.presentation.navigation.NavScreen
import com.app.szone.presentation.viewmodel.WarehouseActionState
import com.app.szone.presentation.viewmodel.WarehouseUiState
import com.app.szone.presentation.viewmodel.WarehouseViewModel
import com.app.szone.presentation.viewmodel.CurrentUserViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import androidx.camera.core.ImageProxy
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

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
    val lifecycleOwner = LocalLifecycleOwner.current

    // Get user name from CurrentUserViewModel (from login response)
    val userName = userState.user?.fullName ?: "Scanner"
    var hasCameraPermission by remember { mutableStateOf(false) }
    var hasScanned by remember { mutableStateOf(false) }

    android.util.Log.d("WarehouseScanner", "🔍 Screen rendered")
    android.util.Log.d("WarehouseScanner", "  - userName: '$userName'")
    android.util.Log.d("WarehouseScanner", "  - user.fullName: '${userState.user?.fullName}'")

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            android.util.Log.d("WarehouseScanner", "✅ Camera permission granted")
        } else {
            Toast.makeText(context, "Quyền camera bị từ chối", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        android.util.Log.d("WarehouseScanner", "🔄 Refreshing warehouse info and current user")
        currentUserViewModel.refresh()
        warehouseViewModel.loadWarehouseInfo()

        // Request camera permission
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            hasCameraPermission = true
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
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
                warehouseViewModel.resetActionState()
                hasScanned = false
            }
            is WarehouseActionState.Error -> {
                val error = action as WarehouseActionState.Error
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                warehouseViewModel.resetActionState()
                hasScanned = false
            }
            else -> Unit
        }
    }

    WarehouseScannerContent(
        userName = userName,
        state = state,
        action = action,
        hasCameraPermission = hasCameraPermission,
        lifecycleOwner = lifecycleOwner,
        context = context,
        onQRCodeScanned = { qrCode ->
            if (qrCode.isNotBlank() && !action.isLoading && !state.isLoading && !hasScanned) {
                hasScanned = true
                warehouseViewModel.scanOrderArrived(qrCode.trim())
            }
        },
        onRetryPermission = {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    )
}

private val WarehouseActionState.isLoading: Boolean
    get() = this is WarehouseActionState.Loading

@Composable
private fun WarehouseScannerContent(
    userName: String,
    state: WarehouseUiState,
    action: WarehouseActionState,
    hasCameraPermission: Boolean,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    context: android.content.Context,
    onQRCodeScanned: (String) -> Unit,
    onRetryPermission: () -> Unit,
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFE3F2FD), Color(0xFFF5F9FF), Color.White)
    )

    if (hasCameraPermission) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
        ) {
            // Header info
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.padding(16.dp)
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

            // Camera preview - main scanning area
            CameraPreviewWithQRScanner(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                lifecycleOwner = lifecycleOwner,
                context = context,
                onQRCodeScanned = onQRCodeScanned
            )

            // Scanned orders list
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Đơn hàng đã quét trong phiên", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    if (state.scannedOrders.isEmpty()) {
                        Text(
                            "Quét mã QR trên màn hình để bắt đầu",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .padding(top = 12.dp)
                        ) {
                            items(state.scannedOrders) { id ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("✅ Đơn #$id", fontSize = 14.sp, color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Permission denied UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FBFC))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Cần quyền truy cập camera",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Vui lòng cấp quyền camera để quét mã QR",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetryPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                Text("Cấp quyền Camera")
            }
        }
    }
}

@Composable
fun CameraPreviewWithQRScanner(
    modifier: Modifier,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    context: android.content.Context,
    onQRCodeScanned: (String) -> Unit
) {
    val barcodeScanner = BarcodeScanning.getClient()
    var lastScannedCode by remember { mutableStateOf("") }
    var lastScanTime by remember { mutableStateOf(0L) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener(
                {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = androidx.camera.core.Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(
                                ContextCompat.getMainExecutor(ctx)
                            ) { imageProxy ->
                                processImageProxy(
                                    imageProxy,
                                    barcodeScanner,
                                    onQRCodeScanned = { qrCode ->
                                        val currentTime = System.currentTimeMillis()
                                        // Debounce: only scan again after 2 seconds
                                        if (qrCode != lastScannedCode || (currentTime - lastScanTime) > 2000) {
                                            lastScannedCode = qrCode
                                            lastScanTime = currentTime
                                            onQRCodeScanned(qrCode)
                                        }
                                    }
                                )
                            }
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (exc: Exception) {
                        exc.printStackTrace()
                    }
                },
                ContextCompat.getMainExecutor(context)
            )
            previewView
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalGetImage::class)
@Suppress("OptIn", "GetImage")
private fun processImageProxy(
    imageProxy: ImageProxy,
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    onQRCodeScanned: (String) -> Unit
) {
    try {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue
                    if (!rawValue.isNullOrEmpty()) {
                        onQRCodeScanned(rawValue)
                    }
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } catch (e: Exception) {
        e.printStackTrace()
        imageProxy.close()
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