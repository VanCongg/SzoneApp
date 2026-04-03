package com.app.szone.presentation.screen.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.app.szone.presentation.ui.theme.SZoneTheme
import com.app.szone.presentation.navigation.NavScreen
import com.app.szone.presentation.viewmodel.LogoutUiState
import com.app.szone.presentation.viewmodel.LogoutViewModel
import com.app.szone.presentation.viewmodel.WarehouseViewModel
import com.app.szone.presentation.viewmodel.CurrentUserViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    onLogout: () -> Unit = {},
    logoutViewModel: LogoutViewModel = koinViewModel(),
    warehouseViewModel: WarehouseViewModel = koinViewModel(),
    currentUserViewModel: CurrentUserViewModel = koinViewModel()
) {
    val logoutState by logoutViewModel.uiState.collectAsState()
    val warehouseState by warehouseViewModel.uiState.collectAsState()
    val userState by currentUserViewModel.uiState.collectAsState()

    android.util.Log.d("ProfileScreen", "🔍 Profile Screen rendered")
    android.util.Log.d("ProfileScreen", "  - user.fullName: '${userState.user?.fullName}'")
    android.util.Log.d("ProfileScreen", "  - user.email: ${userState.user?.email}")

    LaunchedEffect(Unit) {
        warehouseViewModel.loadCachedWarehouse()
    }

    // Xử lý logout thành công
    LaunchedEffect(logoutState) {
        if (logoutState is LogoutUiState.Success) {
            android.util.Log.d("ProfileScreen", "✅ Logout success - calling onLogout callback")
            // Call onLogout callback - this handles navigation from parent
            // The callback defined in SetupNavGraph handles the navigation
            onLogout()
        }
    }

    // Xử lý logout error
    LaunchedEffect(logoutState) {
        if (logoutState is LogoutUiState.Error) {
            android.util.Log.e("ProfileScreen", "❌ Logout error: ${(logoutState as LogoutUiState.Error).message}")
        }
    }

    ProfileScreenContent(
        logoutState = logoutState,
        userName = userState.user?.fullName ?: "Người dùng",
        userEmail = userState.user?.email ?: "email@example.com",
        warehouseName = warehouseState.warehouse?.name,
        warehouseAddress = warehouseState.warehouse?.address,
        onLogoutClick = { logoutViewModel.logout() },
        onBackClick = { navController.popBackStack() }
    )
}

@Composable
private fun ProfileScreenContent(
    logoutState: LogoutUiState,
    userName: String,
    userEmail: String,
    warehouseName: String?,
    warehouseAddress: String?,
    onLogoutClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFF8BBD0), Color(0xFFFCE4EC), Color(0xFFFAFAFA))
    )

    val avatarGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFF7E5F), Color(0xFFFEB47B))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(avatarGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = "Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = userName,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = userEmail,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Kho phụ trách", fontWeight = FontWeight.SemiBold, color = Color(0xFF1565C0))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(warehouseName ?: "Chưa có dữ liệu kho")
                    Text(warehouseAddress ?: "", color = Color.Gray, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (logoutState is LogoutUiState.Error) {
                Text(
                    text = logoutState.message,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                enabled = logoutState !is LogoutUiState.Loading
            ) {
                if (logoutState is LogoutUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Đăng xuất",
                        tint = Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 8.dp)
                    )
                    Text(
                        text = "Đăng xuất",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, Color(0xFFE91E63)),
                enabled = logoutState !is LogoutUiState.Loading
            ) {
                Text(
                    text = "Quay lại",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE91E63)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    SZoneTheme {
        ProfileScreenContent(
            logoutState = LogoutUiState.Idle,
            userName = "Trịnh Minh Nhật",
            userEmail = "trinhminhnhatym@gmail.com",
            warehouseName = "Kho Tân Bình",
            warehouseAddress = "12 Nguyễn Trãi, Quận 1, TP.HCM",
            onLogoutClick = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenLoadingPreview() {
    SZoneTheme {
        ProfileScreenContent(
            logoutState = LogoutUiState.Loading,
            userName = "Trịnh Minh Nhật",
            userEmail = "trinhminhnhatym@gmail.com",
            warehouseName = "Kho Tân Bình",
            warehouseAddress = "12 Nguyễn Trãi, Quận 1, TP.HCM",
            onLogoutClick = {},
            onBackClick = {}
        )
    }
}

