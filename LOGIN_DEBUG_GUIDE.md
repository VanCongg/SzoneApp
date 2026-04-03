# 🔍 Login Debug Guide - Hướng Dẫn Xem Log Đăng Nhập

## 📋 Các Log Points Đã Thêm

### 1. **LoginViewModel Logs**
- 🔐 `LOGIN STARTED` - Bắt đầu đăng nhập
- 📡 `LOGIN RESULT` - Kết quả từ Use Case
- ✅ `LOGIN SUCCESS` - Đăng nhập thành công
- ✅ `ROLE VALID` - Role được chấp nhận
- ❌ `UNKNOWN ROLE` - Role không hợp lệ
- ❌ `LOGIN ERROR` - Lỗi từ server

### 2. **AuthRepository Logs** (More Detailed)
- 🔐 `LOGIN ATTEMPT` - Gửi request tới API
- 📡 `API RESPONSE` - Nhận response từ server
- 📦 `Response data` - Full response content
- ✅ `LOGIN SUCCESS` - Chi tiết đăng nhập thành công
- 💾 `Tokens saved to DataStore` - Token đã lưu
- ❌ `UNKNOWN ROLE` - Role không được chấp nhận
- ❌ `LOGIN FAILED` - Đăng nhập thất bại
- ❌ `EXCEPTION` - Có exception xảy ra

## 🛠️ Cách Xem Logcat

### Android Studio
1. Mở **Logcat** (View → Tool Windows → Logcat hoặc Alt+6)
2. Filter: `LoginVM|AuthRepo`
3. Click vào test email/password
4. Xem logs theo trình tự

### Alternative: Terminal
```bash
# Xem all logs
adb logcat

# Xem chỉ login logs
adb logcat | grep -E "(LoginVM|AuthRepo)"
```

## 📊 Log Flow Mong Đợi (Success)

```
🔐 LOGIN STARTED: email=trinhminhnhatym@gmail.com
📡 LOGIN RESULT: Resource.Success(...)
📡 API RESPONSE: success=true, code=null, message=Login succesfully!
✅ LOGIN SUCCESS
  - User: Trịnh Minh Nhật
  - Email: trinhminhnhatym@gmail.com
  - Phone: 0971549981
  - Role (raw): SHIPPER
  - Role (parsed): SHIPPER
  - Token: eyJhbGciOiJIUzI1NiIsI...
💾 Tokens saved to DataStore
✅ ROLE VALID: SHIPPER, fullName=Trịnh Minh Nhật
```

## ❌ Log Flow - Các Lỗi Thường Gặp

### 1. **Network Error (CLEARTEXT Communication)**
```
❌ EXCEPTION: CLEARTEXT communication to 192.168.1.119 not permitted
```
**Fix**: Thêm network security config

---

### 2. **Unknown Role**
```
❌ UNKNOWN ROLE: CUSTOMER
❌ ROLE VALID: false
```
**Fix**: Chỉ SHIPPER/WAREHOUSE_SCANNER được accept

---

### 3. **API Connection Refused**
```
❌ EXCEPTION: Failed to connect to 192.168.1.119:8000
```
**Fix**: 
- Check BASE_URL trong local.properties
- Verify backend running
- Check network connectivity

---

### 4. **Invalid Credentials**
```
📡 API RESPONSE: success=false, code=401, message=Email hoặc mật khẩu không chính xác
❌ LOGIN ERROR: code=401, message=...
```
**Fix**: Check email/password format

---

### 5. **Server Error (500)**
```
📡 API RESPONSE: success=false, code=500
❌ LOGIN ERROR: code=500
```
**Fix**: Check backend logs

---

## 🚀 Kiểm Tra Step-by-Step

### Step 1: Validate Input
```
🔐 LOGIN STARTED: email=...
```
If không thấy → Email format invalid

### Step 2: API Call
```
📡 LOGIN RESULT: Resource.Success(...)
```
If `Resource.Error` → Network issue

### Step 3: Response Parse
```
📦 Response data: ApiResponse(success=true, ...)
```
If success=false → API error

### Step 4: Role Check
```
✅ ROLE VALID: SHIPPER
```
If `UNKNOWN ROLE` → Role not accepted

### Step 5: Token Save
```
💾 Tokens saved to DataStore
```
If missing → DataStore issue

## 📱 Test Credentials

From response nhận được:
```json
{
  "email": "trinhminhnhatym@gmail.com",
  "password": "??" // Ask user
}
```

Available roles:
- ✅ **SHIPPER** - Giao hàng
- ✅ **WAREHOUSE_SCANNER** - Quét kho
- ❌ **CUSTOMER** - Không được accept
- ❌ **ADMIN** - Không được accept

## 💡 Tips

1. **Clear App Data** trước khi test lại:
   ```bash
   adb shell pm clear com.app.szone
   ```

2. **Real Device**: Dùng IP thực (192.168.1.119) không phải 10.0.2.2

3. **Emulator**: Dùng 10.0.2.2 để connect tới localhost

4. **Export Logs**: 
   ```bash
   adb logcat > login_debug.log
   ```

## 📝 Report Issue

Khi báo lỗi, hãy cung cấp:
1. **Full log output** (5-10 lines)
2. **Email & password** (blurred)
3. **Device**: Emulator/Real
4. **Base URL**: local.properties
5. **Backend status**: Running?

---

**Last Updated**: 2026-04-03
**Logs Added**: AuthRepositoryImpl + LoginViewModel

