## Tóm tắt các thay đổi để fix tên shipper không load và thêm quét QR

### ✅ Vấn đề đã giải quyết:

1. **Tên shipper không load**: 
   - Tạo `CurrentUserViewModel` để lấy thông tin user từ database thay vì dùng savedStateHandle
   - ShipperHomeScreen giờ tự động load tên user khi khởi tạo

2. **Quét QR để lấy đơn hàng**:
   - Tạo `ShipperScannerScreen` với giao diện quét QR
   - Người dùng nhập mã đơn hàng rồi bấm "Tìm đơn hàng"
   - API `GET /api/v1/orders/:orderId/shipper` được gọi với tên + SĐT shipper
   - Hiển thị chi tiết đơn hàng (người nhận, cửa hàng, sản phẩm, chi phí)
   - Có 2 nút: "Xem chi tiết" và "Cập nhật giao hàng"

### 📁 Files được tạo/sửa:

#### Files tạo mới:
- `CurrentUserViewModel.kt` - ViewModel để quản lý thông tin user hiện tại
- `ShipperScannerScreen.kt` - Màn hình quét QR và hiển thị chi tiết đơn hàng

#### Files được sửa:
- `ShipperHomeScreen.kt` - Thay vì load dữ liệu cứng, giờ dùng CurrentUserViewModel
- `ViewModelModule.kt` - Đăng ký CurrentUserViewModel vào Koin

### 🎯 Quy trình hoạt động:

#### Trên màn hình Shipper Home:
1. Tên shipper được tự động load từ database khi mở màn hình
2. Bấm icon quét QR (góc trên bên phải) → Sang ShipperScannerScreen
3. Lưu ý: Màn hình Home hiện tại chỉ hiển thị thông báo hướng dẫn quét QR

#### Trên màn hình Scanner:
1. Nhập/quét mã đơn hàng vào ô input
2. Bấm nút "Tìm đơn hàng" 
3. API gọi: `GET /api/v1/orders/{orderId}/shipper` với body `{name, phoneNumber}`
4. Nếu thành công:
   - Hiển thị thông tin người nhận (tên, SĐT, địa chỉ)
   - Hiển thị thông tin cửa hàng (tên, địa chỉ)
   - Hiển thị chi phí (tiền hàng + phí ship + tổng)
   - Hiển thị danh sách sản phẩm (tên, SKU, số lượng)
   - Có 2 nút hành động

### 🚀 Cách sử dụng ShipperScannerScreen:

Màn hình này đã tích hợp:
- `OrderViewModel.loadOrder(orderId, shipperName, shipperPhone)` - gọi API lấy thông tin đơn hàng
- `CurrentUserViewModel` - tự động cung cấp tên + SĐT shipper hiện tại
- Xử lý trạng thái loading, error, success
- Hiển thị error message nếu không tìm thấy đơn hàng

### 📊 API được sử dụng:

**Get Order Details**
- Method: GET
- URL: `http://localhost:8000/api/v1/orders/:orderId/shipper`
- Body: `{ "name": "Tên shipper", "phoneNumber": "SĐT shipper" }`
- Header: `Authorization: Bearer <token>`

Response:
```json
{
  "success": true,
  "data": {
    "order": {
      "id": "...",
      "recipient": {"name": "...", "phoneNumber": "...", "address": "..."},
      "shop": {"id": "...", "name": "...", "phoneNumber": "...", "address": "..."},
      "shippingFee": 15000,
      "price": 390000,
      "productList": [...]
    }
  }
}
```

### ✨ Tính năng nổi bật:

1. **Tự động load tên shipper**: Không cần fix cứng, lấy từ database tự động
2. **Giao diện Scanner chuyên nghiệp**: Có loading, error handling
3. **Hiển thị đầy đủ thông tin đơn hàng**: Người nhận, cửa hàng, sản phẩm, chi phí
4. **Dễ bảo trì**: Sử dụng MVVM + Clean Architecture + Koin DI

### 🔧 Điều chỉnh cần làm tiếp theo:

Nếu muốn thêm chức năng quét QR thực tế, bạn cần:
1. Thêm dependency ML Kit Barcode Scanning
2. Tích hợp `CameraController` vào ShipperScannerScreen
3. Parse QR code để lấy orderId
4. Tự động gọi API khi quét thành công

Hiện tại, bạn có thể gõ mã đơn hàng thủ công vào ô input.

### 🐛 Build Status:
✅ BUILD SUCCESSFUL - Không có lỗi compilation
⚠️ Có vài warnings từ Koin deprecation (có thể fix sau)

---

Toàn bộ implementation đã hoàn tất. Ứng dụng giờ sẵn sàng kiểm thử với backend!

