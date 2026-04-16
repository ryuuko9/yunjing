package com.example.yunjing.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "auth_store")

data class SessionState(
    val buyerLoggedIn: Boolean,
    val merchantLoggedIn: Boolean
)

class AuthStore(private val ctx: Context) {

    private object Keys {
        val BUYER_ACCOUNT = stringPreferencesKey("buyer_account")
        val MERCHANT_ACCOUNT = stringPreferencesKey("merchant_account")
        val BUYER_USER_ID = longPreferencesKey("buyer_user_id")
        val MERCHANT_USER_ID = longPreferencesKey("merchant_user_id")
        val BUYER_LOGGED_IN = booleanPreferencesKey("buyer_logged_in")
        val MERCHANT_LOGGED_IN = booleanPreferencesKey("merchant_logged_in")
    }

    val sessionFlow: Flow<SessionState> = ctx.authDataStore.data.map { p ->
        SessionState(
            buyerLoggedIn = p[Keys.BUYER_LOGGED_IN] ?: false,
            merchantLoggedIn = p[Keys.MERCHANT_LOGGED_IN] ?: false
        )
    }

    suspend fun saveLogin(role: UserRole, account: String, userId: Long?) {
        ctx.authDataStore.edit { p ->
            when (role) {
                UserRole.BUYER -> {
                    p[Keys.BUYER_ACCOUNT] = account
                    if (userId != null) {
                        p[Keys.BUYER_USER_ID] = userId
                    } else {
                        p.remove(Keys.BUYER_USER_ID)
                    }
                    p[Keys.BUYER_LOGGED_IN] = true
                    p[Keys.MERCHANT_LOGGED_IN] = false
                }
                UserRole.MERCHANT -> {
                    p[Keys.MERCHANT_ACCOUNT] = account
                    if (userId != null) {
                        p[Keys.MERCHANT_USER_ID] = userId
                    } else {
                        p.remove(Keys.MERCHANT_USER_ID)
                    }
                    p[Keys.MERCHANT_LOGGED_IN] = true
                    p[Keys.BUYER_LOGGED_IN] = false
                }
            }
        }
    }

    suspend fun logout(role: UserRole) {
        ctx.authDataStore.edit { p ->
            when (role) {
                UserRole.BUYER -> {
                    p[Keys.BUYER_LOGGED_IN] = false
                    p.remove(Keys.BUYER_USER_ID)
                }
                UserRole.MERCHANT -> {
                    p[Keys.MERCHANT_LOGGED_IN] = false
                    p.remove(Keys.MERCHANT_USER_ID)
                }
            }
        }
    }

    fun accountFlow(role: UserRole): Flow<String> {
        return ctx.authDataStore.data.map { p ->
            when (role) {
                UserRole.BUYER -> p[Keys.BUYER_ACCOUNT] ?: "未登录"
                UserRole.MERCHANT -> p[Keys.MERCHANT_ACCOUNT] ?: "未登录"
            }
        }
    }

    fun userIdFlow(role: UserRole): Flow<Long?> {
        return ctx.authDataStore.data.map { p ->
            when (role) {
                UserRole.BUYER -> p[Keys.BUYER_USER_ID]
                UserRole.MERCHANT -> p[Keys.MERCHANT_USER_ID]
            }
        }
    }
}
