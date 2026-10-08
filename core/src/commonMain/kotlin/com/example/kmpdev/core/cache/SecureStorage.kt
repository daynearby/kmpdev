package com.example.kmpdev.core.cache

expect class SecureStorage {
    suspend fun setString(key: String, value: String)
    suspend fun getString(key: String): String?
    suspend fun remove(key: String)
    suspend fun clear()
}