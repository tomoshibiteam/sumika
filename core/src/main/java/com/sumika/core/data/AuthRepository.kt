package com.sumika.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ユーザー情報モデル
 */
data class User(
    val id: String,
    val email: String,
    val displayName: String
)

/**
 * 認証結果
 */
sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/**
 * モック認証リポジトリ
 * DataStoreを使用してローカルに認証情報を保存
 */
@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")
        
        private object Keys {
            val USER_ID = stringPreferencesKey("user_id")
            val USER_EMAIL = stringPreferencesKey("user_email")
            val USER_DISPLAY_NAME = stringPreferencesKey("user_display_name")
            val USER_PASSWORD_HASH = stringPreferencesKey("user_password_hash")
            val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        }
    }
    
    /**
     * 現在のユーザー情報
     */
    val currentUser: Flow<User?> = context.authDataStore.data.map { prefs ->
        val isLoggedIn = prefs[Keys.IS_LOGGED_IN] ?: false
        if (isLoggedIn) {
            val id = prefs[Keys.USER_ID] ?: return@map null
            val email = prefs[Keys.USER_EMAIL] ?: return@map null
            val displayName = prefs[Keys.USER_DISPLAY_NAME] ?: return@map null
            User(id, email, displayName)
        } else {
            null
        }
    }
    
    /**
     * 認証状態
     */
    val isAuthenticated: Flow<Boolean> = context.authDataStore.data.map { prefs ->
        prefs[Keys.IS_LOGGED_IN] ?: false
    }
    
    /**
     * 新規登録
     */
    suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): AuthResult {
        // バリデーション
        if (!isValidEmail(email)) {
            return AuthResult.Error("有効なメールアドレスを入力してください")
        }
        if (password.length < 6) {
            return AuthResult.Error("パスワードは6文字以上で設定してください")
        }
        if (displayName.isBlank()) {
            return AuthResult.Error("名前を入力してください")
        }
        
        // 既存ユーザーチェック
        val existingEmail = context.authDataStore.data.map { it[Keys.USER_EMAIL] }.first()
        if (existingEmail == email) {
            return AuthResult.Error("このメールアドレスは既に登録されています")
        }
        
        // ユーザー作成
        val userId = UUID.randomUUID().toString()
        val passwordHash = hashPassword(password)
        
        context.authDataStore.edit { prefs ->
            prefs[Keys.USER_ID] = userId
            prefs[Keys.USER_EMAIL] = email
            prefs[Keys.USER_DISPLAY_NAME] = displayName
            prefs[Keys.USER_PASSWORD_HASH] = passwordHash
            prefs[Keys.IS_LOGGED_IN] = true
        }
        
        return AuthResult.Success(User(userId, email, displayName))
    }
    
    /**
     * ログイン
     */
    suspend fun signIn(email: String, password: String): AuthResult {
        val storedEmail = context.authDataStore.data.map { it[Keys.USER_EMAIL] }.first()
        val storedPasswordHash = context.authDataStore.data.map { it[Keys.USER_PASSWORD_HASH] }.first()
        
        if (storedEmail == null || storedPasswordHash == null) {
            return AuthResult.Error("アカウントが見つかりません")
        }
        
        if (storedEmail != email) {
            return AuthResult.Error("メールアドレスまたはパスワードが間違っています")
        }
        
        if (storedPasswordHash != hashPassword(password)) {
            return AuthResult.Error("メールアドレスまたはパスワードが間違っています")
        }
        
        // ログイン成功
        context.authDataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = true
        }
        
        val userId = context.authDataStore.data.map { it[Keys.USER_ID] }.first() ?: ""
        val displayName = context.authDataStore.data.map { it[Keys.USER_DISPLAY_NAME] }.first() ?: ""
        
        return AuthResult.Success(User(userId, email, displayName))
    }
    
    /**
     * ログアウト
     */
    suspend fun signOut() {
        context.authDataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = false
        }
    }
    
    /**
     * メールアドレスの形式チェック
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * 簡易パスワードハッシュ（SHA-256）
     */
    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
