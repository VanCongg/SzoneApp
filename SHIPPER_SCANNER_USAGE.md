# 📱 Hướng dẫn sử dụng Shipper Scanner - Quét QR Đơn Hàng

## 🎯 Mục đích
Màn hình này cho phép Shipper quét mã QR (hoặc nhập thủ công) để lấy thông tin chi tiết đơn hàng từ hệ thống, sau đó cập nhật trạng thái giao hàng (thành công/thất bại).

---

## 🚀 Quy trình hoạt động

### 1️⃣ Từ Màn hình Home
```
Shipper Home Screen
    ↓
Bấm icon "Quét QR" (góc trên bên phải)
    ↓
ShipperScannerScreen
```

### 2️⃣ Nhập/Quét mã QR
- **Input field**: Nhập mã đơn hàng (ví dụ: `ORD123456789`)
- **Nút "Tìm đơn hàng"**: Bấm để gọi API lấy thông tin

### 3️⃣ API được gọi
```
GET /api/v1/orders/:orderId/shipper
Body: {
  "name": "Tên Shipper (từ database)",
  "phoneNumber": "SĐT Shipper (từ database)"
}
Header: Authorization: Bearer <token>
```

### 4️⃣ Hiển thị thông tin
Khi thành công, màn hình hiển thị:
- **Người nhận**: Tên, SĐT, Địa chỉ giao
- **Cửa hàng**: Tên, Địa chỉ shop
- **Chi phí**: Tiền hàng + Phí giao + Tổng cộng
- **Sản phẩm**: Danh sách với tên, SKU, số lượng

### 5️⃣ Hành động
Hai nút ở cuối:
- **"Xem chi tiết"**: Mở màn hình OrderDetailScreen
- **"Cập nhật giao hàng"**: Mở màn hình chi tiết để cập nhật trạng thái

---

## 📊 Dữ liệu hiển thị

### Nguồn dữ liệu
| Thông tin | Nguồn |
|-----------|-------|
| Tên Shipper | Database (CurrentUserViewModel) |
| SĐT Shipper | Database (CurrentUserViewModel) |
| Thông tin đơn hàng | API Response |
| Token | DataStore |

### Xử lý lỗi
| Tình huống | Xử lý |
|-----------|-------|
| Mã QR trống | Không gọi API, hiển thị empty state |
| API không tìm thấy | Hiển thị error message |
| Token hết hạn (401) | Hiển thị error, User cần login lại |
| Lỗi kết nối | Hiển thị error message |

---

## 🔄 Luồng dữ liệu (Data Flow)

```
ShipperScannerScreen
    ↓
OrderViewModel.loadOrder()
    ↓
OrderRepositoryImpl.getOrderDetails()
    ↓
OrderService.getOrderToShipper()
    ↓
API: GET /orders/:orderId/shipper
    ↓
Response → OrderDto → OrderModel
    ↓
UI Update (uiState.order)
    ↓
Hiển thị thông tin trên màn hình
```

---

## 🛠️ Thành phần kỹ thuật

### ViewModel
- **OrderViewModel**: Quản lý state đơn hàng (loading, success, error)
- **CurrentUserViewModel**: Lấy thông tin shipper hiện tại từ database

### UseCase
- **GetOrderDetailsUseCase**: Gọi repository để lấy thông tin đơn hàng

### Repository
- **OrderRepositoryImpl.getOrderDetails()**: 
  - Gọi API
  - Lưu vào Room Database (offline support)
  - Trả về Resource<OrderModel>

### Service (Retrofit)
- **OrderService.getOrderToShipper()**: 
  - HTTP GET request
  - Tự động thêm Bearer token via AuthInterceptor

---

## 💾 Cơ chế Cache (Offline Support)

Khi quét thành công:
1. **API call**: Gọi `getOrderToShipper()`
2. **Save to DB**: `orderDao.upsertOrder(orderEntity)`
3. **On Error**: Nếu API fail nhưng có dữ liệu cũ trong DB, sẽ hiển thị dữ liệu cached

Ưu điểm:
- ✅ Shipper có thể xem đơn hàng đã quét ngay cả khi mất mạng
- ✅ Tăng hiệu suất UX

---

## 🔐 Bảo mật

### Token Management
- Token được lưu ở **DataStore** (encrypted)
- Tự động thêm vào header bằng **AuthInterceptor**
- Không cần manual add header

### Data Protection
- Không lưu password
- Không log sensitive data
- Token hết hạn → User phải login lại

---

## 📝 Ví dụ Response từ API

```json
{
  "success": true,
  "message": "Get order to shipper successfully",
  "data": {
    "order": {
      "id": "ORD-001",
      "recipient": {
        "name": "Nguyễn Văn A",
        "phoneNumber": "0912345678",
        "address": "Số 7 Phan Chu Trinh, Hà Nội"
      },
      "shop": {
        "id": "SHOP-001",
        "name": "Shop Thời Trang ABC",
        "phoneNumber": "0987654321",
        "address": "Số 10 Trần Hưng Đạo, Hà Nội"
      },
      "shippingFee": 15000,
      "price": 390000,
      "productList": [
        {
          "name": "Áo thun Silomon tay cộc",
          "sku": "nâu-XL",
          "quantity": 2
        },
        {
          "name": "Quần âu May 10 form đứng",
          "sku": "đen-XL",
          "quantity": 1
        }
      ]
    }
  }
}
```

---

## 🐛 Troubleshooting

### Tên Shipper không hiển thị
**Nguyên nhân**: User chưa đăng nhập hoặc database trống
**Giải pháp**: 
1. Đảm bảo đã đăng nhập thành công
2. Kiểm tra `GetCurrentUserUseCase` có lấy được dữ liệu

### "Đơn hàng không tồn tại"
**Nguyên nhân**: Mã QR sai hoặc đơn hàng không tồn tại trên server
**Giải pháp**: 
1. Kiểm tra lại mã QR
2. Đảm bảo orderId đúng format

### API Timeout
**Nguyên nhân**: Kết nối mạng yếu hoặc server chậm
**Giải pháp**: 
1. Kiểm tra kết nối WiFi/Data
2. Thử lại
3. Nếu còn lỗi, database sẽ trả dữ liệu cached

### Token hết hạn (401 error)
**Nguyên nhân**: Session đã hết hạn
**Giải pháp**: 
1. Quay lại màn hình login
2. Đăng nhập lại
3. Token refresh token sẽ được lưu tự động

---

## 🚀 Tính năng sắp tới (Future Enhancements)

- [ ] 📷 Tích hợp quét QR bằng camera (ML Kit Barcode Scanning)
- [ ] 🔊 Beep/Vibrate khi quét thành công
- [ ] 📋 Lịch sử quét hôm nay
- [ ] 🖨️ In phiếu giao hàng
- [ ] 📍 Tích hợp GPS để verify địa chỉ
- [ ] 📸 Chụp ảnh xác nhận giao hàng

---

## 📞 Support

Nếu gặp lỗi:
1. Kiểm tra logcat: `adb logcat | grep "szone"`
2. Xem stack trace ở error message
3. Đảm bảo API endpoint đúng trong `local.properties`

**API Endpoint**: `http://localhost:8000/api/v1/`

---

**Last Updated**: 02/04/2026
**Status**: ✅ Ready for Testing

