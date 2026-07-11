package com.example.kmpdev.core.cache

import com.ctrip.flight.mmkv.MMKV_KMP
import com.ctrip.flight.mmkv.defaultMMKV
import com.example.kmpdev.core.cache.CacheManager.Companion.MAX_CACHEABLE_JSON_BYTES
import com.example.kmpdev.core.cache.CacheManager.Companion.MAX_CACHE_BYTES
import com.example.kmpdev.core.logger.Logger
import kotlinx.serialization.json.Json
import kotlin.math.ceil
import kotlin.time.Clock

/**
 *  putRawJson/getRawJson 用作网络请求的json数据缓存，到了限制的缓存大小,会进行清理trimCache()
 *  其他put、get可以正常使用，需要自行进行控制修改、删除
 */
class MMKVCacheManagerImpl(
    val mmkv: MMKV_KMP,
    val versionCode: Long
) : CacheManager {

    private val keyPrefixMeta = "cache:meta:"
    private val keyPrefixBody = "cache:body:"
    private fun metaKey(key: String) = "$keyPrefixMeta$key"
    private fun bodyKey(key: String) = "$keyPrefixBody$key"
    private fun baseKeyFromMeta(k: String) = k.removePrefix(keyPrefixMeta)

    // 用来存储 原始 JSON（CACHE_THEN_NETWORK ）作为缓存

    override suspend fun putRawJson(key: String, rawJson: String, ttlSeconds: Long) {
        // 超过阈值 → 跳过缓存（body 为原始文本，直接测长度）
        if (rawJson.encodeToByteArray().size > MAX_CACHEABLE_JSON_BYTES) {
            Logger.w("CacheManager", "Body size ${rawJson.length} chars exceeds limit, skip: $key")
            return
        }
        val meta = CacheMeta(
            cachedAt = Clock.System.now().toEpochMilliseconds(),
            ttlSeconds = ttlSeconds,
            versionCode = versionCode
        )
        mmkv.set(metaKey(key), Json.encodeToString(meta))    // ★ mmkv-kotlin: set()
        mmkv.set(bodyKey(key), rawJson)                      // ★ mmkv-kotlin: set()
    }

    override suspend fun getRawJson(key: String): String? {
        val metaRaw = mmkv.getString(metaKey(key))

        val meta = try {
            Json.decodeFromString<CacheMeta>(metaRaw)
        } catch (_: Exception) {
            remove(key)
            return null
        }
        if (meta.versionCode != versionCode) {
            remove(key)
            return null
        }
        val age = (Clock.System.now().toEpochMilliseconds() - meta.cachedAt) / 1000
        if (age > meta.ttlSeconds) {
            return null
        }
        return mmkv.getString(bodyKey(key))
    }


    override suspend fun put(key: String, data: Any) {
        when (data) {
            is String -> mmkv.set(key, data)
            is Boolean -> mmkv.set(key, data)
            is Int -> mmkv.set(key, data)
            is Long -> mmkv.set(key, data)
            is Float -> mmkv.set(key, data)
            is Double -> mmkv.set(key, data)
            is ByteArray -> mmkv.set(key, data)
            is UInt -> mmkv.set(key, data)
            is ULong -> mmkv.set(key, data)
            else -> throw RuntimeException("put方法不支持该类型数据${data::class},或者换成String？")
        }
    }

    override suspend fun put(key: String, data: Set<String>) {
        mmkv.set(key, data)
    }

    override fun getString(key: String, default: String): String {
        return mmkv.getString(key, default)
    }

    override fun getBoolean(key: String, default: Boolean): Boolean {
        return mmkv.getBoolean(key, default)
    }

    override fun getInt(key: String, default: Int): Int {
        return mmkv.getInt(key, default)
    }


    override fun getLong(key: String, default: Long): Long {
        return mmkv.getLong(key, default)
    }

    override fun getFloat(key: String, default: Float): Float {
        return mmkv.getFloat(key, default)
    }

    override fun getDouble(key: String, default: Double): Double {
        return mmkv.getDouble(key, default)
    }

    override fun getByteArray(key: String, default: ByteArray?): ByteArray? {
        return mmkv.getByteArray(key, default)
    }

    override fun getUInt(key: String, default: UInt): UInt {
        return mmkv.getUInt(key, default)
    }

    override fun getULong(key: String, default: ULong): ULong {
        return mmkv.getULong(key, default)
    }

    override fun getStringSet(
        key: String,
        default: Set<String>?
    ): Set<String>? {
        return mmkv.getStringSet(key, default)
    }

    override suspend fun remove(key: String) {
        mmkv.removeValueForKey(metaKey(key))
        mmkv.removeValueForKey(bodyKey(key))
    }

    override suspend fun clearAll() {
        mmkv.clearAll()
        mmkv.set("cache:version", versionCode)
    }

    override suspend fun evictExpired() {
        val now = Clock.System.now().toEpochMilliseconds()
        for (k in mmkv.allKeys().orEmpty()) {                // ★ allKeys(): List<String>?
            if (!k.startsWith(keyPrefixMeta)) continue
            val metaRaw = mmkv.getString(k) ?: continue
            try {
                val meta = Json.decodeFromString<CacheMeta>(metaRaw)
                if ((now - meta.cachedAt) / 1000 > meta.ttlSeconds) {
                    remove(baseKeyFromMeta(k))
                }
            } catch (_: Exception) {
                remove(baseKeyFromMeta(k))
            }
        }
    }

    override suspend fun getMeta(key: String): CacheMeta? {
        val raw = mmkv.getString(metaKey(key)) ?: return null
        return try {
            Json.decodeFromString(raw)
        } catch (_: Exception) {
            null
        }
    }


    override suspend fun trimCache() {
        val totalSize = mmkv.totalSize
        if (totalSize <= MAX_CACHE_BYTES) return

        val allMetaKeys = mmkv.allKeys().orEmpty()
            .filter { it.startsWith(keyPrefixMeta) }
        if (allMetaKeys.isEmpty()) return

        // 解析每条 meta，提取 cachedAt
        val entries = allMetaKeys.mapNotNull { k ->
            val json = mmkv.getString(k, "")
            if (json.isEmpty()) {
                // 空值 meta 视为脏数据，直接清理
                mmkv.removeValueForKey(k)
                val bodyK = keyPrefixBody + baseKeyFromMeta(k)
                mmkv.removeValueForKey(bodyK)
                null
            } else {
                try {
                    val meta = Json.decodeFromString<CacheMeta>(json)
                    Pair(baseKeyFromMeta(k), meta.cachedAt)
                } catch (_: Exception) {
                    // 解析失败的 meta，直接清理
                    mmkv.removeValueForKey(k)
                    null
                }
            }
        }

        //  cachedAt 升序排序（最旧的在前）
        val sorted = entries.sortedBy { it.second }

        //保留一半（向上取整），删除最旧的另一半
        val keepCount = ceil(sorted.size / 2.0).toInt()
        val toRemove = sorted.dropLast(keepCount)

        toRemove.forEach { (baseKey, _) -> remove(baseKey) }

        // 回收磁盘空间
        mmkv.trim()

        val sizeAfter = mmkv.totalSize
        val freedKb = (totalSize - sizeAfter) / 1024
        Logger.i(
            "CacheManager",
            "trimCache: evicted ${toRemove.size}/${sorted.size} entries, " +
                    "${freedKb}KB freed (${totalSize / 1024}KB → ${sizeAfter / 1024}KB)"
        )
    }

    override suspend fun clearCacheOnVersionChange(versionCode: Int) {
        val mmkv = defaultMMKV()
        val savedVersion = mmkv.getInt("cache:version", 0)
        if (savedVersion != versionCode) {
            mmkv.clearAll()
            mmkv.set("cache:version", versionCode)
        }
    }
}