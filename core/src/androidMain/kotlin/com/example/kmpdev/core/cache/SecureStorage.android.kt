package com.example.kmpdev.core.cache

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 *  使用AndroidKeyStore + AES/GCM 产生密钥，并且保存密钥，加密解密使用改密钥
 *  再通过存储 token这些敏感数据getSharedPreferences的Context.MODE_PRIVATE模式进行保存
 */
actual class SecureStorage constructor(context: Context) {

    private companion object {
        const val KEY_ALIAS = "kmpdev_secure_key"
        const val GCM_IV_LENGTH = 12
    }

    private val TAG = SecureStorage::class.java.simpleName

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val prefs = context.getSharedPreferences("secure_storage", Context.MODE_PRIVATE)

    private fun getSecretKey(): SecretKey {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val generator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
            )
            generator.init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
            generator.generateKey()
        }
        return (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    private fun encrypt(plain: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(iv + encrypted, Base64.NO_WRAP)
    }

    private fun decrypt(cipherText: String): String {
        val combined = Base64.decode(cipherText, Base64.NO_WRAP)
        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val encrypted = combined.copyOfRange(GCM_IV_LENGTH, combined.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), gcmSpec)
        return String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }

    actual suspend fun setString(key: String, value: String) = withContext(Dispatchers.IO) {
        prefs.edit { putString(key, encrypt(value)) }
    }

    actual suspend fun getString(key: String): String? = withContext(Dispatchers.IO) {
        try {
            prefs.getString(key, null)?.let { decrypt(it) }
        } catch (e: Exception) {
            Logger.e("decrypt error:${e.message}", tag = TAG)
            null
        }
    }

    actual suspend fun remove(key: String) = withContext(Dispatchers.IO) {
        prefs.edit { remove(key) }
    }

    actual suspend fun clear() = withContext(Dispatchers.IO) {
        prefs.edit { clear() }
    }

}