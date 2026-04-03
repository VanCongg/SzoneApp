package com.app.szone.presentation.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.*
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.navigation.NavController
import com.app.szone.presentation.navigation.NavScreen
import com.app.szone.presentation.ui.theme.SZoneTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.app.szone.presentation.viewmodel.OrderViewModel
import com.app.szone.presentation.viewmodel.CurrentUserViewModel
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// 2. Màn hình chính
@Composable
fun ShipperHomeScreen(
    navController: NavController,
    orderViewModel: OrderViewModel = koinViewModel(),
    currentUserViewModel: CurrentUserViewModel = koinViewModel()
) {
    val userUiState by currentUserViewModel.uiState.collectAsState()
    var displayName by remember { mutableStateOf("Nhân viên") }

    android.util.Log.d("ShipperHome", "=== ShipperHomeScreen Recomposed ===")
    android.util.Log.d("ShipperHome", "userUiState.user = ${userUiState.user}")
    android.util.Log.d("ShipperHome", "userUiState.user?.fullName = '${userUiState.user?.fullName}'")
    android.util.Log.d("ShipperHome", "userUiState.errorMessage = ${userUiState.errorMessage}")
    android.util.Log.d("ShipperHome", "displayName = $displayName")

    // Load current user data on first composition
    LaunchedEffect(Unit) {
        android.util.Log.d("ShipperHome", "🔄 LaunchedEffect(Unit) - Refreshing current user")
        currentUserViewModel.refresh()
    }

    // Update display name whenever user data changes
    LaunchedEffect(userUiState.user) {
        val fullName = userUiState.user?.fullName
        android.util.Log.d("ShipperHome", "🔄 LaunchedEffect triggered")
        android.util.Log.d("ShipperHome", "  - fullName from user = '$fullName'")
        android.util.Log.d("ShipperHome", "  - isNullOrBlank = ${fullName.isNullOrBlank()}")

        if (!fullName.isNullOrBlank()) {
            displayName = fullName
            android.util.Log.d("ShipperHome", "✅ Updated displayName to: '$displayName'")
        } else {
            android.util.Log.d("ShipperHome", "❌ fullName is null/blank, keeping default")
        }
    }

    ShipperHomeScreenContent(
        userName = displayName,
        onScanClick = { navController.navigate(NavScreen.ShipperScannerNavScreen) },
        onAvatarClick = { navController.navigate(NavScreen.ProfileNavScreen) },
        orderViewModel = orderViewModel,
        navController = navController
    )
}

@Composable
private fun ShipperHomeScreenContent(
    userName: String,
    onScanClick: () -> Unit,
    onAvatarClick: () -> Unit,
    orderViewModel: OrderViewModel,
    navController: NavController
) {
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFF8BBD0), Color(0xFFFCE4EC), Color(0xFFFAFAFA))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            HeaderSection(
                userName = userName,
                onScanClick = onScanClick,
                onAvatarClick = onAvatarClick
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Danh sách đơn hàng",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF880E4F)
            )

            Text(
                text = "Quét mã QR từ góc trên bên trái để lấy đơn hàng",
                fontSize = 13.sp,
                color = Color(0xFFAD1457),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // Empty state khi chưa quét QR
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Bấm nút quét QR ở góc trên bên trái để tải đơn hàng",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// 3. Phần Header (Avatar xịn xò hơn)
@Composable
fun HeaderSection(
    userName: String,
    onScanClick: () -> Unit = {},
    onAvatarClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // TẠO ĐIỂM NHẤN 2: Avatar có nền Gradient nổi bật và viền trắng
        val avatarGradient = Brush.linearGradient(
            colors = listOf(Color(0xFFFF7E5F), Color(0xFFFEB47B)) // Gradient cam hồng hoàng hôn
        )

        Box(
            modifier = Modifier
                .size(56.dp) // To hơn một chút
                .clip(CircleShape)
                .background(avatarGradient)
                .border(2.dp, Color.White, CircleShape) // Thêm viền trắng
                .clickable(onClick = onAvatarClick), // Thêm click handler
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Face, // Đổi icon nhìn thân thiện hơn
                contentDescription = "Avatar",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "Xin chào,",
                fontSize = 14.sp,
                color = Color.DarkGray
            )
            Text(
                text = userName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Nút quét QR có màu
        Card(
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            IconButton(onClick = onScanClick) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Quét mã QR",
                    tint = Color(0xFFE91E63) // Màu hồng đồng bộ
                )
            }
        }
    }
}


// --- Các hàm hỗ trợ vẽ Icon và Text ---

@Composable
fun RowItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, isBold: Boolean = false, iconColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconItem(icon, iconColor)
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isBold) Color.Black else Color.DarkGray
        )
    }
}

@Composable
fun RowItemIndented(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, isBold: Boolean = false, iconColor: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(18.dp)
                .padding(top = 2.dp),
            tint = iconColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = if (isBold) Color.Black else Color.DarkGray,
            fontWeight = if (isBold) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
fun IconItem(icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(20.dp),
        tint = color
    )
    Spacer(modifier = Modifier.width(8.dp))
}

@Preview(showBackground = true)
@Composable
private fun ShipperHomeScreenPreview() {
    SZoneTheme {
        // Note: Preview requires mock viewmodel
    }
}

