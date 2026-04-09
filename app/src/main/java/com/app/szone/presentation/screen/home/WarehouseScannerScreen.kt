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
import androidx.compose.material.icons.filled.Face
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.draw.clip

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
        navController = navController,
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
    navController: NavController?,
    onQRCodeScanned: (String) -> Unit,
    onRetryPermission: () -> Unit,
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFF0F4F8), Color.White)
    )

    if (hasCameraPermission) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .statusBarsPadding() // Đảm bảo không bị dính vào tai thỏ
        ) {
            // --- HÀNG 1: AVATAR & TÊN ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .clickable { navController?.navigate(NavScreen.ProfileNavScreen) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "Profile",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Xin chào,",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = userName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                }
            }

            // --- HÀNG 2: THÔNG TIN KHO ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn, // Bạn có thể dùng Icons.Default.Home nếu chưa import LocationOn
                        contentDescription = null,
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Kho: ${state.warehouse?.name ?: "Đang tải..."}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1565C0)
                        )
                        if (state.warehouse?.address != null) {
                            Text(
                                state.warehouse.address,
                                fontSize = 12.sp,
                                color = Color.Gray,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- HÀNG 3: CAMERA ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, Color.White, RoundedCornerShape(24.dp))
            ) {
                CameraPreviewWithQRScanner(
                    modifier = Modifier.fillMaxSize(),
                    lifecycleOwner = lifecycleOwner,
                    context = context,
                    onQRCodeScanned = onQRCodeScanned
                )

                // Overlay hướng dẫn quét (Tùy chọn)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Đưa mã QR vào khung hình",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            // --- HÀNG 4: DANH SÁCH ĐÃ QUÉT (NẾU CẦN) ---
            if (state.scannedOrders.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 120.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Vừa quét (${state.scannedOrders.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                            items(state.scannedOrders.reversed()) { id ->
                                Text(
                                    "✅ Đơn #$id",
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // ... (Giữ nguyên phần UI xin quyền Camera của bạn)
    }
}
@androidx.annotation.OptIn(ExperimentalGetImage::class)
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

@ExperimentalGetImage
@OptIn(ExperimentalGetImage::class)
@Suppress("OptIn", "GetImage")
private fun processImageProxy(
    imageProxy: ImageProxy,
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    onQRCodeScanned: (String) -> Unit
) {
    try {
        @Suppress("GetImage")
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

