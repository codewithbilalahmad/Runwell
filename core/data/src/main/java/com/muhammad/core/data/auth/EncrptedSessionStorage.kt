package com.muhammad.core.data.auth

import android.content.SharedPreferences
import androidx.core.content.edit
import com.muhammad.core.domain.AuthInfo
import com.muhammad.core.domain.SessionStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class EncryptedSessionStorage(
    private val sharedPreferences: SharedPreferences,
) : SessionStorage{
    override suspend fun get(): AuthInfo? {
        return withContext(Dispatchers.IO){
            val json = sharedPreferences.getString(KEY_AUTH_INFO, null)
            json?.let {data ->
                Json.decodeFromString<AuthInfoSerializable>(data).toAuthInfo()
            }
        }
    }

    override suspend fun set(info: AuthInfo?) {
        withContext(Dispatchers.IO) {
            if(info == null){
                sharedPreferences.edit(commit = true) { remove(KEY_AUTH_INFO) }
                return@withContext
            }
            val json = Json.encodeToString<AuthInfoSerializable>(info.toAuthInfoSerializable())
            sharedPreferences.edit(commit = true) { putString(KEY_AUTH_INFO, json) }
        }
    }
    companion object{
        private const val KEY_AUTH_INFO = "KEY_AUTH_INFO"
    }
}