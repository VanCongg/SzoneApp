# 🔍 Debug Guide - Vấn đề: Vẫn thấy "Xin chào, Shipper"

## 🎯 Vấn đề
Sau khi login, tên shipper vẫn hiển thị "Nhân viên" hoặc "SHIPPER" thay vì fullName từ response

## 🔧 Debug Steps

### 1️⃣ **Kiểm tra Logcat**
```bash
adb logcat | grep -E "ShipperHome|CurrentUserVM|AuthRepo"
```

**Expected Output**:
```
D/AuthRepo: User saved to DB: fullName=Trịnh Minh Nhật, phone=0971549981, role=SHIPPER
D/CurrentUserVM: User loaded: fullName=Trịnh Minh Nhật, phone=0971549981, role=SHIPPER
D/ShipperHome: userUiState.user = User(id=..., fullName=Trịnh Minh Nhật, ...)
D/ShipperHome: LaunchedEffect triggered - fullName from user = Trịnh Minh Nhật
D/ShipperHome: User name updated to: Trịnh Minh Nhật
```

### 2️⃣ **Kiểm tra Response Login**

Hãy add log vào AuthService để xem API response:

```kotlin
// OrderService.kt hoặc interceptor
android.util.Log.d("API", "Response: $response")
```

**Expected**:
```json
{
  "data": {
    "user": {
      "fullName": "Trịnh Minh Nhật",
      "roleName": "SHIPPER"
    }
  }
}
```

### 3️⃣ **Kiểm tra Database**

```bash
adb shell
cd /data/data/com.app.szone/databases
sqlite3 szone_db.db
SELECT id, fullName, phone, roleName FROM users LIMIT 1;
```

**Expected Output**:
```
9f6dd513-80db-4611-890f-c853c490df82|Trịnh Minh Nhật|0971549981|SHIPPER
```

### 4️⃣ **Kiểm tra Timing Issue**

Có thể CurrentUserViewModel chưa load xong khi screen hiển thị:

```kotlin
// ShipperHomeScreen.kt đã thêm LaunchedEffect
LaunchedEffect(userUiState.user) {
    val fullName = userUiState.user?.fullName
    if (!fullName.isNullOrBlank()) {
        displayName = fullName
    }
}
```

## ❌ Các Nguyên Nhân Có Thể

### 1. **CurrentUserViewModel không load được user từ database**
- **Dấu hiệu**: `D/CurrentUserVM: User loaded: fullName=null`
- **Nguyên nhân**: Database trống (user chưa lưu)
- **Fix**: Xác nhận `AuthRepositoryImpl.login()` đã gọi `userDao.insertUser()`

### 2. **GetCurrentUserUseCase trả về lỗi**
- **Dấu hiệu**: `D/CurrentUserVM: User loaded: fullName=null` + error log
- **Nguyên nhân**: Exception trong UserDao.getUser()
- **Fix**: Kiểm tra database connection, Room schema

### 3. **UserDto.fullName null từ API**
- **Dấu hiệu**: `D/AuthRepo: User saved to DB: fullName=null`
- **Nguyên nhân**: API response không có fullName field
- **Fix**: Xác nhận API trả về `fullName`, không phải `name`

### 4. **Mismatch giữa UserDto.fullName và API response key**
- **Dấu hiệu**: Deserialization error
- **Nguyên nhân**: API trả về `"full_name"` nhưng UserDto expect `"fullName"`
- **Fix**: Thêm `@SerialName` nếu cần:
  ```kotlin
  @Serializable
  data class UserDto(
      @SerialName("full_name")
      val fullName: String,
      // ...
  )
  ```

### 5. **UI không update sau khi user load**
- **Dấu hiệu**: Logcat hiện fullName, nhưng UI vẫn hiện "Nhân viên"
- **Nguyên nhân**: Recomposition issue
- **Fix**: `LaunchedEffect(userUiState.user)` đã thêm để force update

## 📊 Debug Checklist

- [ ] Run app trên emulator
- [ ] Login với tài khoản SHIPPER
- [ ] Mở Logcat
- [ ] Lọc: `grep -E "ShipperHome|CurrentUserVM|AuthRepo"`
- [ ] Kiểm tra tất cả các log
- [ ] Nếu `fullName` trong log → UI issue
- [ ] Nếu `fullName=null` → Database/API issue
- [ ] Xem database bằng `adb shell + sqlite3`
- [ ] Verify API response có `fullName` field

## 🔨 Fixes to Try

### Fix 1: Force Refresh ViewModel
```kotlin
// Thêm vào ShipperHomeScreen
LaunchedEffect(Unit) {
    currentUserViewModel.refresh()
}
```

### Fix 2: Thêm Delay (nếu timing issue)
```kotlin
LaunchedEffect(userUiState.user) {
    delay(500) // Chờ 500ms
    val fullName = userUiState.user?.fullName
    if (!fullName.isNullOrBlank()) {
        displayName = fullName
    }
}
```

### Fix 3: Check UserEntity mapping
```kotlin
// UserEntity.kt - Verify schema
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val fullName: String,  // ✓ Phải có
    val phone: String,
    val roleName: String,
    // ...
)
```

### Fix 4: Xác nhận AuthRepositoryImpl.login()
```kotlin
// AuthRepositoryImpl.kt line 47
// Phải có:
userDao.insertUser(authData.user.toEntity())
// Và mapping phải lấy fullName:
fullName = authData.user.fullName ✓
```

## 📋 Quick Checklist

```
☐ Build app
☐ Install: adb install app/build/outputs/apk/debug/app-debug.apk
☐ Open app
☐ Login với SHIPPER account
☐ Đợi home screen load
☐ Mở Logcat
☐ Filter: ShipperHome
☐ Kiểm tra: userUiState.user?.fullName có value không?
  - YES → UI update issue (cần fix LaunchedEffect)
  - NO → Database/API issue (cần debug AuthRepo)
```

## 🚀 Expected Behavior

```
1. Login success
2. AuthRepo logs: "User saved to DB: fullName=..."
3. Home screen opens
4. CurrentUserVM loads user
5. CurrentUserVM logs: "User loaded: fullName=..."
6. ShipperHome logs: "LaunchedEffect triggered"
7. ShipperHome logs: "User name updated to: ..."
8. UI hiển thị: "Xin chào, Tên thực của shipper"
```

---

**Next Steps**:
1. Build lại code (vừa thêm debug logs)
2. Cài trên device/emulator
3. Login
4. Mở Logcat và capture logs
5. Chia sẻ logs để debug

**Build Status**: ✅ SUCCESS
**Debug Logs**: ✅ ADDED

