package com.catcafe.app.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cat_cafe_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey(Constants.TOKEN_KEY)
        private val USER_ID_KEY = intPreferencesKey(Constants.USER_ID_KEY)
        private val USER_TYPE_KEY = intPreferencesKey(Constants.USER_TYPE_KEY)
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val userIdFlow: Flow<Int?> = context.dataStore.data.map { it[USER_ID_KEY] }

    fun getTokenBlocking(): String? = runBlocking { context.dataStore.data.first()[TOKEN_KEY] }

    suspend fun saveAuth(token: String, userId: Int, userType: Int) {
        context.dataStore.edit {
            it[TOKEN_KEY] = token
            it[USER_ID_KEY] = userId
            it[USER_TYPE_KEY] = userType
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    fun isAdminBlocking(): Boolean = runBlocking {
        context.dataStore.data.first()[USER_TYPE_KEY] == 1
    }
}
