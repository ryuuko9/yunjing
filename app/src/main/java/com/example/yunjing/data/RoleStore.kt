package com.example.yunjing.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "yunjing_prefs")

enum class UserRole { BUYER, MERCHANT }

class RoleStore(private val context: Context) {
    private val KEY_ROLE = stringPreferencesKey("user_role")

    val roleFlow: Flow<UserRole?> = context.dataStore.data.map { prefs ->
        prefs[KEY_ROLE]?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }
    }

    suspend fun setRole(role: UserRole) {
        context.dataStore.edit { it[KEY_ROLE] = role.name }
    }

    suspend fun clearRole() {
        context.dataStore.edit { it.remove(KEY_ROLE) }
    }
}