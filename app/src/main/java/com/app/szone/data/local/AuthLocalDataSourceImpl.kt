//package com.app.szone.data.local
//
//package com.acteam.vocago.data.local
//
//
//import android.content.Context
//import android.content.SharedPreferences
//import androidx.core.content.edit
//import androidx.security.crypto.EncryptedSharedPreferences
//import com.acteam.vocago.domain.local.AuthLocalDataSource
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import androidx.security.crypto.MasterKey
//
//class AuthLocalDataSourceImpl(context: Context) : AuthLocalDataSource {
//    private val prefs: SharedPreferences by lazy {
//        val masterKey = MasterKey.Builder(context)
//            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
//            .build()
//        EncryptedSharedPreferences.create(
//            context,
//            "SECURE_AUTH_PREF", // Tên file
//            masterKey,
//            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
//            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
//        )
//    }
//
//    private var accessToken: String? = null
//    private var refreshToken: String? = null
//    private var credentialId: String? = null
//    private val _isAuth = MutableStateFlow(false)
//    override val isAuth: StateFlow<Boolean> = _isAuth
//
//    private val _credentialId = MutableStateFlow<String?>(null)
//    override val credentialIdFlow: StateFlow<String?> = _credentialId
//
//    companion object {
//        private const val AUTH_PREF_NAME = "AUTH_PREF_NAME"
//        private const val ACCESS_TOKEN_KEY = "ACCESS_TOKEN_KEY"
//        private const val REFRESH_TOKEN_KEY = "REFRESH_TOKEN_KEY"
//        private const val CREDENTIAL_ID_KEY = "CREDENTIAL_ID_KEY"
//    }
//
//    init {
//        accessToken = prefs.getString(ACCESS_TOKEN_KEY, null)
//        refreshToken = prefs.getString(REFRESH_TOKEN_KEY, null)
//        credentialId = prefs.getString(CREDENTIAL_ID_KEY, null)
//        _credentialId.value = credentialId
//        _isAuth.value = accessToken != null
//    }
//
//    override fun getAccessToken(): String? =
//        accessToken ?: prefs.getString(ACCESS_TOKEN_KEY, null)
//
//    override fun getRefreshToken(): String? =
//        refreshToken ?: prefs.getString(REFRESH_TOKEN_KEY, null)
//
//
//    override fun clearTokens() {
//        accessToken = null
//        refreshToken = null
//        credentialId = null
//        _credentialId.value = null
//        _isAuth.value = false
//        prefs.edit {
//            remove(ACCESS_TOKEN_KEY)
//            remove(REFRESH_TOKEN_KEY)
//            remove(CREDENTIAL_ID_KEY)
//        }
//    }
//
//    override fun getCredentialId(): String? =
//        credentialId ?: prefs.getString(CREDENTIAL_ID_KEY, null)
//
//
//    override fun getTokens(): Pair<String?, String?> =
//        Pair(
//            accessToken ?: prefs.getString(ACCESS_TOKEN_KEY, null),
//            refreshToken ?: prefs.getString(REFRESH_TOKEN_KEY, null)
//        )
//
//
//    override fun refreshTokens(accessToken: String, refreshToken: String) {
//        this.refreshToken = refreshToken
//        this.accessToken = accessToken
//        prefs.edit {
//            putString(ACCESS_TOKEN_KEY, accessToken)
//            putString(REFRESH_TOKEN_KEY, refreshToken)
//        }
//    }
//
//    override fun saveCredential(
//        accessToken: String,
//        refreshToken: String,
//        credentialId: String,
//    ) {
//        this.credentialId = credentialId
//        this.accessToken = accessToken
//        this.refreshToken = refreshToken
//        _credentialId.value = credentialId
//        _isAuth.value = true
//        prefs.edit {
//            putString(ACCESS_TOKEN_KEY, accessToken)
//            putString(REFRESH_TOKEN_KEY, refreshToken)
//            putString(CREDENTIAL_ID_KEY, credentialId)
//        }
//    }
//
//}
