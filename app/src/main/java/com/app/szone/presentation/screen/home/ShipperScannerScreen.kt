@file:Suppress("DEPRECATION", "UNCHECKED_CAST")

package com.app.szone.presentation.screen.home

import android.Manifest
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.app.szone.presentation.viewmodel.OrderViewModel
import com.app.szone.presentation.viewmodel.CurrentUserViewModel
import com.app.szone.presentation.navigation.NavScreen
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.coroutines.launch
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import androidx.compose.ui.tooling.preview.Preview
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipperScannerScreen(
    navController: NavController,
    orderViewModel: OrderViewModel = koinViewModel(),
    currentUserViewModel: CurrentUserViewModel = koinViewModel()
) {
    // Wrap in remember to make stable for composition
    val stableNavController = remember(navController) { navController }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Get user info from ViewModel
    val userUiState by currentUserViewModel.uiState.collectAsState()
    val userName = userUiState.user?.fullName ?: "Nhân viên"
    val userPhone = userUiState.user?.phone ?: ""

    // Observe order state from ViewModel
    val uiState by orderViewModel.uiState.collectAsState()
    var hasScanned by remember { mutableStateOf(false) }

    // Check camera permission
    var hasCameraPermission by remember { mutableStateOf(false) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            android.util.Log.d("ShipperScanner", "✅ Camera permission granted")
        } else {
            Toast.makeText(context, "Quyền camera bị từ chối", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        android.util.Log.d("ShipperScanner", "🔄 Screen init - Checking camera permission")
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            hasCameraPermission = true
            android.util.Log.d("ShipperScanner", "✅ Camera permission already granted")
        } else {
            android.util.Log.d("ShipperScanner", "🔐 Requesting camera permission...")
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Auto navigate to OrderDetail when order loaded
    LaunchedEffect(uiState.order) {
        if (uiState.order != null && hasScanned) {
            stableNavController.navigate(NavScreen.OrderDetailNavScreen(uiState.order!!.id))
            hasScanned = false // Reset for next scan
        }
    }

    // Show error toast
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            hasScanned = false
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quét QR đơn hàng", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { stableNavController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (hasCameraPermission) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.Black)
            ) {
                // QR Camera Preview
                CameraPreviewWithQRScanner(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    lifecycleOwner = lifecycleOwner,
                    context = context,
                    onQRCodeScanned = { qrCode ->
                        if (qrCode.isNotBlank() && !uiState.isLoading && !hasScanned) {
                            hasScanned = true
                            scope.launch {
                                orderViewModel.loadOrder(qrCode, userName, userPhone)
                            }
                        }
                    }
                )

                // Info Section (Bottom)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Thông tin Shipper",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            userName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Text(
                            userPhone,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        if (uiState.isLoading) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF0079C1)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Đang tải đơn hàng...", fontSize = 12.sp, color = Color.Gray)
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
                    .padding(padding)
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
                    "Vui lòng cấp quyền camera để quét QR code",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        // In a real app, this would open system settings
                        Toast.makeText(context, "Vui lòng bật quyền camera trong cài đặt", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0079C1))
                ) {
                    Text("Cấp quyền Camera")
                }
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
                                        // Debounce: chỉ scan lại sau 2 giây
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

@Suppress("OptIn")
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
@Preview
@OptIn(ExperimentalMaterial3Api::class)
fun ShipperScannerScreenPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Quét QR đơn hàng", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Placeholder cho Camera Preview
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Black),
                    color = Color.Black
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Camera Preview\n(Preview Mode)",
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                }

                // Info Section (Bottom)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Thông tin Shipper",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            "Trịnh Minh Nhật",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Text(
                            "0971549981",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
@OptIn(ExperimentalMaterial3Api::class)
fun ShipperScannerScreenLoadingPreview() {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Quét QR đơn hàng", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.Black)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Black),
                    color = Color.Black
                ) {}

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Thông tin Shipper",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            "Trịnh Minh Nhật",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Text(
                            "0971549981",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF0079C1)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Đang tải đơn hàng...", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}


