# S-Zone App - Shipper & Warehouse Scanner

Android delivery management application for shippers and warehouse staff using MVVM + Clean Architecture.

## 📱 Features

### Shipper Module
- **Order Scanning**: Scan QR codes to retrieve order details
- **Delivery Status**: Mark orders as delivered or failed
- **Order Tracking**: View product lists, recipient info, shipping fees

### Warehouse Scanner Module
- **QR Scanning**: Scan incoming orders to warehouse
- **Order Arrival**: Register package arrival at warehouse
- **Session History**: View all scanned items in current session

### Authentication
- **Secure Login**: Email/password authentication with role-based access
- **Token Management**: Secure token storage with DataStore
- **Auto Refresh**: Automatic token refresh when needed

## 🏗️ Architecture

```
├── data/                    # Data Layer
│   ├── local/              # Room Database & DataStore
│   ├── remote/             # Retrofit API Services
│   ├── repository/         # Repository Implementations
│   └── model/              # DTOs
├── domain/                 # Domain Layer
│   ├── model/              # Business Entities
│   ├── repository/         # Repository Interfaces
│   └── usecase/            # Business Logic
└── presentation/           # Presentation Layer
    ├── screen/             # Jetpack Compose UI
    ├── viewmodel/          # MVVM ViewModels
    └── navigation/         # Navigation Graph
```

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVVM + Clean Architecture
- **DI**: Koin
- **Network**: Retrofit + OkHttp
- **Local Storage**: Room + DataStore
- **State Management**: StateFlow
- **Build**: Gradle 8.x

## 📦 Key Dependencies

```gradle
// Compose
androidx.compose.material3
androidx.compose.ui

// Network
com.squareup.retrofit2:retrofit
com.squareup.okhttp3:okhttp

// Local Storage
androidx.room:room-runtime
androidx.datastore:datastore-preferences

// DI
io.insert-koin:koin-android
io.insert-koin:koin-androidx-compose

// Serialization
org.jetbrains.kotlinx:kotlinx-serialization-json
```

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest)
- JDK 11+
- Android SDK 34+
- Gradle 8.x

### Setup

1. **Clone Repository**
```bash
git clone <repository-url>
cd AppCuaNhat
```

2. **Configure local.properties**
```properties
sdk.dir=/path/to/android/sdk
api.base_url=http://192.168.1.119:8000
```

3. **Build & Run**
```bash
./gradlew build
./gradlew assembleDebug
```

Or use Android Studio: File → Run

## 🔐 API Endpoints

### Authentication
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/logout` - User logout

### Shipper
- `GET /api/v1/orders/:orderId/shipper` - Get order details
- `POST /api/v1/orders/:orderId/delivery-success` - Mark as delivered
- `POST /api/v1/orders/:orderId/delivery-fail` - Mark as failed

### Warehouse
- `GET /api/v1/scanners/:scannerId/warehouse` - Get warehouse info
- `POST /api/v1/orders/:orderId/arrived-warehouse` - Register arrival

## 📝 Login Credentials

Test accounts available:
- **Shipper**: shipper@example.com / password
- **Warehouse**: warehouse@example.com / password

## 🐛 Known Issues & Fixes

### Issue: FullName display showing incorrect name
**Fix**: Applied in v1.0.1
- Clears stale database data on login
- Forces user data refresh on screen entry
- See `FULLNAME_FIX_SUMMARY.md` for details

## 📋 Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/app/szone/
│   │   │   ├── data/
│   │   │   ├── domain/
│   │   │   ├── presentation/
│   │   │   ├── di/              # Koin Dependency Injection
│   │   │   └── utils/
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   ├── debug/
│   │   └── AndroidManifest.xml
│   └── test/
├── build.gradle.kts
└── proguard-rules.pro
```

## 🧪 Testing

Run tests:
```bash
./gradlew test
./gradlew connectedAndroidTest
```

## 📱 Build & Release

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

## 🔄 API Configuration

The API base URL is read from `local.properties`:
```kotlin
// In NetworkModule.kt
api_base_url=http://<your-backend-ip>:8000
```

For local development with Android Emulator:
```
api_base_url=http://10.0.2.2:8000
```

For physical device:
```
api_base_url=http://192.168.x.x:8000
```

## 🎯 Recent Updates

### v1.0.1 - FullName Fix
- Fixed issue where user fullName displayed incorrectly
- Clear stale database on login
- Force refresh user data on screen entry
- Removed unused LoggedInUser entity

### v1.0.0 - Initial Release
- Shipper delivery management
- Warehouse order scanning
- Secure authentication
- MVVM architecture

## 📞 Support

For issues or questions:
1. Check `FULLNAME_FIX_SUMMARY.md` for known fixes
2. Review Logcat output for error details
3. Ensure API backend is running and accessible

## 📄 License

Proprietary - S-Zone Delivery System

---

**Last Updated**: April 3, 2026
**Status**: ✅ Active Development

