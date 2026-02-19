package com.example.yunjing.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "auth_store")

data class SessionState(
    val buyerLoggedIn: Boolean,
    val merchantLoggedIn: Boolean
)

class AuthStore(private val ctx: Context) {

    private object Keys {
        val BUYER_ACCOUNT = stringPreferencesKey("buyer_account")
        val BUYER_PASSWORD = stringPreferencesKey("buyer_password")
        val BUYER_LOGGED_IN = booleanPreferencesKey("buyer_logged_in")

        val MERCHANT_ACCOUNT = stringPreferencesKey("merchant_account")
        val MERCHANT_PASSWORD = stringPreferencesKey("merchant_password")
        val MERCHANT_LOGGED_IN = booleanPreferencesKey("merchant_logged_in")
    }

    val sessionFlow: Flow<SessionState> = ctx.authDataStore.data.map { p ->
        SessionState(
            buyerLoggedIn = p[Keys.BUYER_LOGGED_IN] ?: false,
            merchantLoggedIn = p[Keys.MERCHANT_LOGGED_IN] ?: false
        )
    }

    suspend fun register(role: UserRole, account: String, password: String) {
        ctx.authDataStore.edit { p ->
            when (role) {
                UserRole.BUYER -> {
                    p[Keys.BUYER_ACCOUNT] = account
                    p[Keys.BUYER_PASSWORD] = password
                    p[Keys.BUYER_LOGGED_IN] = true
                }
                UserRole.MERCHANT -> {
                    p[Keys.MERCHANT_ACCOUNT] = account
                    p[Keys.MERCHANT_PASSWORD] = password
                    p[Keys.MERCHANT_LOGGED_IN] = true
                }
            }
        }
    }

    suspend fun login(role: UserRole, account: String, password: String): Boolean {
        val prefs = ctx.authDataStore.data.first()

        val ok = when (role) {
            UserRole.BUYER ->
                prefs[Keys.BUYER_ACCOUNT] == account && prefs[Keys.BUYER_PASSWORD] == password
            UserRole.MERCHANT ->
                prefs[Keys.MERCHANT_ACCOUNT] == account && prefs[Keys.MERCHANT_PASSWORD] == password
        }

        if (ok) {
            ctx.authDataStore.edit { p ->
                when (role) {
                    UserRole.BUYER -> p[Keys.BUYER_LOGGED_IN] = true
                    UserRole.MERCHANT -> p[Keys.MERCHANT_LOGGED_IN] = true
                }
            }
        }
        return ok
    }

    suspend fun logout(role: UserRole) {
        ctx.authDataStore.edit { p ->
            when (role) {
                UserRole.BUYER -> p[Keys.BUYER_LOGGED_IN] = false
                UserRole.MERCHANT -> p[Keys.MERCHANT_LOGGED_IN] = false
            }
        }
    }
}
