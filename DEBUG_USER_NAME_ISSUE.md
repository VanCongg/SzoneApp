# 🔍 Debug Guide - Tên User Là "warehousescanner"

## 🎯 Vấn đề
User name hiển thị là "warehousescanner" thay vì tên thực (ví dụ: "Trịnh Minh Nhật")

## 📊 Debug Flow

```
Login (nhập email/password)
  ↓
LoginViewModel.login()
  ├─ 🔐 LOGIN STARTED: email=...
  └─ AuthRepository.login()
     ├─ 📡 API RESPONSE: success=true
     ├─ ✅ User: Trịnh Minh Nhật (API trả về đúng)
     ├─ 💾 Tokens saved to DataStore
     └─ ✅ User saved to DB: fullName='Trịnh Minh Nhật'
  ↓
ShipperHomeScreen init
  └─ CurrentUserViewModel()
     └─ loadCurrentUser()
        ├─ 📖 getCurrentUser() called
        ├─ ✅ Found user in DB: fullName='Trịnh Minh Nhật'
        └─ displayName = 'Trịnh Minh Nhật'
```

## 🐛 Kiểm Tra Step-by-Step

### Step 1: Login Success
```
🔐 LOGIN ATTEMPTED: email=trinhminhnhatym@gmail.com
📡 API RESPONSE: success=true
✅ LOGIN SUCCESS
  - User: Trịnh Minh Nhật
  - Role (raw): SHIPPER
  - Token: eyJhbGciOiJIUzI1NiIs...
💾 Tokens saved to DataStore
User saved to DB: fullName=Trịnh Minh Nhật
```

**❌ Nếu không thấy logs trên:**
- Đăng nhập failed
- Check logs `❌ EXCEPTION` hoặc `❌ LOGIN FAILED`
- Xem `LOGIN_DEBUG_GUIDE.md`

---

### Step 2: Navigate to Home Screen
```
ShipperHome screen init
  userUiState.user = null (chưa load từ DB)
  displayName = "Nhân viên" (default)
```

**This is normal - user data chưa load từ database**

---

### Step 3: CurrentUserViewModel Load User
```
🔄 START: loadCurrentUser()
📖 getCurrentUser() called
✅ Found user in DB:
  - id: 9f6dd513-80db-4611-890f-c853c490df82
  - fullName: 'Trịnh Minh Nhật'
  - email: trinhminhnhatym@gmail.com
  - phone: 0971549981
  - roleName: SHIPPER
✅ Converted to domain model:
  - fullName: 'Trịnh Minh Nhật'
```

**Nếu thấy fullName='warehousescanner' ở bước này → BUG trong room database**

---

### Step 4: ShipperHomeScreen Update Name
```
LaunchedEffect triggered - fullName from user = Trịnh Minh Nhật
User name updated to: Trịnh Minh Nhật
```

---

## ❌ Các Scenarios Lỗi

### Scenario 1: fullName = "warehousescanner" ở DB
**Log:**
```
✅ Found user in DB:
  - fullName: 'warehousescanner'  ← SAI!
```

**Nguyên nhân:**
- Mapping UserDto → UserEntity sai
- hoặc API trả về `fullName="warehousescanner"` (BE bug)

**Fix:**
```kotlin
// Check UserMapper.kt line 9-10
// Phải là:
fullName = fullName,  ✓ (correct)

// Không phải:
fullName = roleName,  ✗ (wrong!)
```

---

### Scenario 2: User not found in DB
**Log:**
```
🔄 START: loadCurrentUser()
📖 getCurrentUser() called
❌ User not found in DB
```

**Nguyên nhân:**
- User chưa được lưu vào DB lúc login
- DB bị xóa/clear

**Fix:**
1. Check log: `User saved to DB` có xuất hiện không?
2. Nếu không → AuthRepository.login() có gọi `userDao.insertUser()` không?

---

### Scenario 3: fullName = null
**Log:**
```
✅ Found user in DB:
  - fullName: 'null'  ← SAI!
```

**Nguyên nhân:**
- API trả về `fullName=null`
- hoặc mapping null
- hoặc database field schema sai

**Fix:**
```kotlin
// In AuthRepositoryImpl.login() line 42
// Check: fullName từ API là gì?
android.util.Log.d("AuthRepo", "  - User: ${authData.user.fullName}")

// Should show fullName từ API, không phải null
```

---

### Scenario 4: fullName = "warehousescanner" nhưng chỉ lúc debug
**Dấu hiệu:**
- Lúc test debug cây có tên sai, nhưng lúc deploy OK

**Fix:**
- Clear app data: `adb shell pm clear com.app.szone`
- Rebuild & test lại

---

## 🔧 Kiểm Tra Chi Tiết

### 1. Xem UserMapper
```kotlin
// File: app/src/main/java/com/app/szone/data/mapping/UserMapper.kt
fun UserEntity.toDomain(): User {
    return User(
        id = id,
        email = email,
        fullName = fullName,  // ← PHẢI LÀ FULLNAME, KHÔNG PHẢI ROLENAME!
        phone = phone,
        roleName = roleName,
        // ...
    )
}
```

### 2. Xem UserEntity Schema
```kotlin
// File: app/src/main/java/com/app/szone/data/local/entity/UserEntity.kt
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val fullName: String,  // ← Column name
    val phone: String,     // ← NOT phoneNumber
    val roleName: String,
    // ...
)
```

### 3. Xem UserDto (API)
```kotlin
// File: app/src/main/java/com/app/szone/data/model/ApiModels.kt
@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val fullName: String,  // ← Phải match với API response
    val phoneNumber: String,  // ← Note: phoneNumber, không phải phone
    val roleName: String,
    // ...
)
```

---

## 📝 Checklist Debug

- [ ] Login successful? Check: `✅ LOGIN SUCCESS` log
- [ ] User saved to DB? Check: `User saved to DB` log
- [ ] fullName value correct lúc save? Check: `fullName=...` value
- [ ] getCurrentUser() find user? Check: `✅ Found user in DB` log
- [ ] fullName mapping correct? Check UserMapper.kt
- [ ] DB schema correct? Check UserEntity.kt
- [ ] API response correct? Check: API return fullName value
- [ ] Clear app data & rebuild? Try: `adb shell pm clear com.app.szone`

---

## 🚀 Test Steps

1. **Clear previous data:**
   ```bash
   adb shell pm clear com.app.szone
   ```

2. **Rebuild app:**
   ```bash
   ./gradlew installDebug
   ```

3. **Open Logcat filter:**
   ```
   AuthRepo|CurrentUserVM|ShipperHome
   ```

4. **Login with test account:**
   - Email: trinhminhnhatym@gmail.com
   - Password: (ask user)

5. **Watch logs flow** → Should show proper fullName

6. **Check screen:** Should show "Xin chào, Trịnh Minh Nhật"

---

## 💡 Report Issue With These Logs

When reporting bug, capture:

```
🔐 LOGIN STARTED: email=...
✅ LOGIN SUCCESS
  - User: ??? (should be actual name)
User saved to DB: fullName=???
🔄 START: loadCurrentUser()
✅ Found user in DB:
  - fullName: '???' (check if correct)
```

And share these files:
- `UserEntity.kt` - DB schema
- `UserMapper.kt` - Mapping logic
- `ApiModels.kt` (UserDto) - API model

---

**Last Updated**: 2026-04-03
**Logs Added**: AuthRepositoryImpl.getCurrentUser() + CurrentUserViewModel.loadCurrentUser()

