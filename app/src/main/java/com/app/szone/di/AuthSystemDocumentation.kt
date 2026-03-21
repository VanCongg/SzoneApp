package com.app.szone.di

/**
 * ========================================================
 * HỆ THỐNG LOGIN/LOGOUT HOÀN CHỈNH THEO CLEAN ARCHITECTURE + MVVM
 * ========================================================
 *
 * TẦNG DOMAIN:
 * - AuthModels.kt: User, LoginRequest, AuthResponse models
 * - Resource<T>: Wrapper class để xử lý trạng thái API (Success, Error, Loading)
 * - AuthRepository (Interface): Định nghĩa các hành động liên quan đến auth
 * - AuthUseCases.kt: LoginUseCase, LogoutUseCase, GetCurrentUserUseCase, v.v...
 *
 * TẦNG DATA:
 * - ApiModels.kt: LoginRequestDto, AuthResponseDto, UserDto (DTOs từ API)
 * - UserEntity.kt: Entity cho Room Database
 * - AuthDataStore.kt: Lưu/lấy Token từ DataStore
 * - UserDao.kt: DAO để quản lý User trong Database
 * - AuthService.kt: Retrofit Service để gọi API
 * - AuthRepositoryImpl.kt: Triển khai AuthRepository
 * - UserMapper.kt: Mapper để chuyển đổi DTO <-> Domain Model <-> Entity
 * - AuthInterceptor.kt: Thêm Authorization header vào tất cả requests
 *
 * TẦNG PRESENTATION:
 * - LoginViewModel.kt: Quản lý UI State của login
 *   - LoginUiState: Idle, Loading, Success, Error
 *   - login(email, password): Gọi usecase & kiểm tra role
 * - LogoutViewModel.kt: Quản lý UI State của logout
 * - LoginScreen.kt: UI đăng nhập với Compose
 * - ProfileScreen.kt: UI hồ sơ người dùng + nút logout
 *
 * DI MODULES (KOIN):
 * - LocalModule: Database, DAO, DataStore
 * - NetworkModule: Retrofit + OkHttpClient + AuthInterceptor
 * - RepositoryModule: AuthRepository instance
 * - UseCaseModule: Tất cả UseCases
 * - ViewModelModule: LoginViewModel, LogoutViewModel
 * - AppModule: Kết nối tất cả modules
 *
 * ========================================================
 * LUỒNG LOGIN:
 * ========================================================
 * 1. User nhập email & password trong LoginScreen
 * 2. Click "Đăng nhập" → Chọn role (Shipper/Warehouse)
 * 3. LoginViewModel.login(email, password) gọi LoginUseCase
 * 4. UseCase gọi AuthRepository.login()
 * 5. Repository gọi AuthService.login() (API call)
 * 6. API trả về token + user info + role
 * 7. Repository kiểm tra role: SHIPPER / WAREHOUSE_SCANNER / CUSTOMER
 * 8. Nếu role không hợp lệ → Error "Không có quyền truy cập"
 * 9. Token được lưu vào DataStore (AuthDataStore)
 * 10. User được lưu vào Room Database
 * 11. LoginViewModel emit Success state với role
 * 12. LoginScreen navigate đến MainNavScreen (Shipper) hoặc WarehouseNavScreen
 *
 * ========================================================
 * LUỒNG LOGOUT:
 * ========================================================
 * 1. User click "Đăng xuất" trong ProfileScreen
 * 2. LogoutViewModel.logout() gọi LogoutUseCase
 * 3. UseCase gọi AuthRepository.logout()
 * 4. Repository gọi AuthService.logout(bearerToken)
 * 5. API nhận yêu cầu logout (có header Authorization)
 * 6. Token được xóa từ DataStore
 * 7. User được xóa từ Room Database
 * 8. LogoutViewModel emit Success state
 * 9. ProfileScreen navigate quay lại LoginScreen
 *
 * ========================================================
 * SECURITY & TOKEN MANAGEMENT:
 * ========================================================
 * - Token lưu trong DataStore (encrypted by system)
 * - Token được thêm vào tất cả API requests qua AuthInterceptor
 * - Khi logout, token được xóa ngay lập tức
 * - Xử lý 401/403 errors từ API
 *
 * ========================================================
 * DATABASE ENTITIES:
 * ========================================================
 * Room Database (@Database version 1):
 *   - UserEntity: Lưu info user (id, email, fullName, phone, roleName, avatar, status)
 *   - Cho phép lấy user info nhanh mà không cần gọi API
 *
 * DataStore:
 *   - ACCESS_TOKEN_KEY: Token để authenticated requests
 *   - REFRESH_TOKEN_KEY: Dùng refresh token (nếu cần implement)
 *
 * ========================================================
 * ERROR HANDLING:
 * ========================================================
 * API Errors được map sang error messages:
 * - 401: "Email hoặc mật khẩu không chính xác"
 * - 403: "Tài khoản của bạn bị khóa"
 * - 500: "Lỗi máy chủ, vui lòng thử lại sau"
 * - Những role không phải SHIPPER/WAREHOUSE_SCANNER: "Không có quyền truy cập"
 *
 * ========================================================
 * KOIN DEPENDENCY INJECTION:
 * ========================================================
 * Tất cả dependencies được quản lý bởi Koin:
 * - ViewModel inject qua @koinViewModel() trong Composables
 * - Repository inject vào UseCases
 * - UseCase inject vào ViewModels
 * - AuthDataStore inject vào Repository
 * - AuthService inject vào Repository
 * - Database/DAO inject vào Repository
 *
 * ========================================================
 */
val authSystemDocumentation = "See above comments for detailed auth system documentation"

