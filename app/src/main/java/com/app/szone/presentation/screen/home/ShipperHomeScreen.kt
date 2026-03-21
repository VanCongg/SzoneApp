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

// 1. Định nghĩa dữ liệu (Giữ nguyên)
data class Order(
    val id: String,
    val cod: Long,
    val shippingFee: Long,
    val recipientName: String,
    val phone: String,
    val address: String
)

// 2. Màn hình chính
@Composable
fun ShipperHomeScreen(
    navController: NavController,
) {
    // TẠO ĐIỂM NHẤN 1: Nền Gradient chuyển từ hồng đậm sang hồng nhạt
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFF8BBD0), Color(0xFFFCE4EC), Color(0xFFFAFAFA))
    )

    val orders = listOf(
        Order("123456789", 124000, 15000, "lpnconlonton", "0984123449", "số 4 đường Tân Bình, Phú Nhuận, HCM"),
        Order("123456789", 330000, 15000, "Viết Duy", "0984123449", "số 4 đường Tân Bình, Phú Nhuận, HCM")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient) // Áp dụng nền Gradient
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            HeaderSection(
                onScanClick = {
                    navController.navigate(NavScreen.ShipperScannerNavScreen)
                },
                onAvatarClick = {
                    navController.navigate(NavScreen.ProfileNavScreen)
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Danh sách đơn hàng",
                fontSize = 24.sp, // Chữ to hơn một chút
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF880E4F) // Màu chữ đỏ mận sang trọng
            )

            Text(
                text = "Nhấn vào thẻ để xem chi tiết",
                fontSize = 13.sp,
                color = Color(0xFFAD1457),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 20.dp) // Thêm khoảng trống dưới cùng
            ) {
                items(orders) { order ->
                    OrderCard(order = order, onClick = {
                        navController.navigate(NavScreen.OrderDetailNavScreen(order.id))
                    })
                }
            }
        }
    }
}

// 3. Phần Header (Avatar xịn xò hơn)
@Composable
fun HeaderSection(
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
                text = "Nguyễn Huy",
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

// 4. Component Thẻ Đơn Hàng (Sáng sủa, đổ bóng đẹp)
@Composable
fun OrderCard(order: Order, onClick: () -> Unit) {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    val themeColor = Color(0xFFD81B60) // Màu chủ đạo cho các icon trong thẻ

    Card(
        shape = RoundedCornerShape(16.dp), // Bo góc tròn trịa hơn
        colors = CardDefaults.cardColors(
            containerColor = Color.White // Nền trắng tinh giúp chữ nổi bật
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp), // Đổ bóng đậm hơn chút
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(
            ) }
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Dòng 1: ID
            RowItem(
                icon = Icons.Default.ConfirmationNumber,
                text = "Đơn: #${order.id}",
                isBold = true,
                iconColor = themeColor
            )

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFFF0F0F0) // Dòng kẻ mờ ngăn cách
            )

            // Dòng 2: Tiền và Phí ship
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconItem(icon = Icons.Default.Payments, color = Color(0xFF4CAF50)) // Icon tiền màu xanh lá
                Text(text = "Thu: ${formatter.format(order.cod)}đ", fontSize = 15.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.width(16.dp))

                IconItem(icon = Icons.Default.LocalShipping, color = Color(0xFFFF9800)) // Icon xe tải màu cam
                Text(text = "Ship: ${formatter.format(order.shippingFee)}đ", fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dòng 3: Người nhận
            RowItemIndented(icon = Icons.Default.Person, text = order.recipientName, isBold = true, iconColor = Color.Gray)

            Spacer(modifier = Modifier.height(6.dp))

            // Dòng 4: SĐT
            RowItemIndented(icon = Icons.Default.Phone, text = order.phone, iconColor = Color.Gray)

            Spacer(modifier = Modifier.height(6.dp))

            // Dòng 5: Địa chỉ
            RowItemIndented(icon = Icons.Default.LocationOn, text = order.address, iconColor = Color.Gray)
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
