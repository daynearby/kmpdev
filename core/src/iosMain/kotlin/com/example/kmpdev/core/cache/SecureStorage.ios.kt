package com.example.kmpdev.core.cache

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 *
 * 需链接系统 `Security` / `CoreFoundation` 框架。
 * 系统框架在 Kotlin/Native 中通常通过 `platform.Security.*` 自动链接；
 * 若链接报错，在 `iosMain` 的 `binaries` 或 `linkerOpts` 加 `-framework Security`。
 */
actual class SecureStorage   constructor() {

   /* private val service = "com.example.kmpdev.secure"

    private fun stringToData(value: String): NSData =
        (value as NSString).dataUsingEncoding(NSUTF8StringEncoding)!!

    private fun dataToString(data: NSData): String? =
        NSString.create(data = data, encoding = NSUTF8StringEncoding) as? String

    *//**
     * ★ 构建 Keychain 查询字典。
     *   返回 CFDictionaryRef? — K/N 中 CoreFoundation 类型是 ObjC 对象，
     *   不是 CPointed 子类型，不能用 CPointer<> 包装。
     *//*
    private fun buildQuery(
        account: String,
        data: NSData? = null,
        returnData: Boolean = false,
        matchAll: Boolean = false,
    ): CFDictionaryRef? = memScoped {
        val keys = mutableListOf<CFTypeRef?>(kSecClass, kSecAttrService)
        val values = mutableListOf<CFTypeRef?>(kSecClassGenericPassword, service as CFTypeRef)
        if (!matchAll) {
            keys += kSecAttrAccount; values += account as CFTypeRef
        }
        data?.let { keys += kSecValueData; values += it as CFTypeRef }
        if (returnData) {
            keys += kSecReturnData; values += kCFBooleanTrue
        }
        if (matchAll) {
            keys += kSecMatchLimit; values += kSecMatchLimitAll
        }
        CFDictionaryCreate(
            kCFAllocatorDefault,
            keys.toTypedArray().toCValues().ptr,
            values.toTypedArray().toCValues().ptr,
            keys.size.toLong(), null, null
        )
    }
*/
    actual suspend fun setString(key: String, value: String) = withContext(Dispatchers.IO) {
       /* memScoped {
            SecItemDelete(buildQuery(key))   // 覆盖写：先删旧值
            SecItemAdd(
                buildQuery(key, data = stringToData(value)),
                null
            )
        }*/
    }

    actual suspend fun getString(key: String): String? = withContext(Dispatchers.IO) {
       /* memScoped {
            // ★ CFTypeRefVar 由 CoreFoundation cinterop 绑定自动生成，
            //   通过 alloc<>() 分配，SecItemCopyMatching 写入引用
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(buildQuery(key, returnData = true), result.ptr)
            if (status == errSecSuccess) {
                val nsData = result.value as? NSData ?: return@withContext null
                dataToString(nsData)
            } else null
        }*/
        ""
    }

    actual suspend fun remove(key: String) = withContext(Dispatchers.IO) {
       // memScoped { SecItemDelete(buildQuery(key)) }
    }

    actual suspend fun clear() = withContext(Dispatchers.IO) {
      //  memScoped { SecItemDelete(buildQuery("", matchAll = true)) }
    }

}