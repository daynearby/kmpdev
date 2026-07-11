package com.example.kmpdev.core.cache

/**
 * mmkv，基于腾讯的mmkv
 * putRawJson/getRawJson 用作网络请求的json数据缓存，到了限制的缓存大小会进行清理trimCache()
 * 其他put、get可以正常使用，需要自行进行控制修改、删除
 */
interface CacheManager {
    companion object {
        const val MAX_CACHEABLE_JSON_BYTES = 500 * 1024  // 单条json最大字节数，当前看来最大单条json：200KByte
        const val MAX_CACHE_BYTES = 50L * 1024 * 1024     // 缓存最大占用空间 50MB
    }

    // 读写原始json数据
    suspend fun putRawJson(key: String, rawJson: String,ttlSeconds : Long)
    suspend fun getRawJson(key: String): String?


    /**
     * 其他类型数据保存，不会被trimCache 清理，需要自己进行管理
     */
    suspend fun  put(key: String, data: Any)
    suspend fun  put(key: String, data: Set<String>)

    fun getString(key: String, default: String = ""): String

    fun getBoolean(key: String, default: Boolean = false): Boolean

    fun getInt(key: String, default: Int = 0): Int

    fun getLong(key: String, default: Long = 0): Long

    fun getFloat(key: String, default: Float = 0f): Float

    fun getDouble(key: String, default: Double = 0.0): Double

    fun getByteArray(key: String, default: ByteArray? = null): ByteArray?

    fun getUInt(key: String, default: UInt = 0u): UInt

    fun getULong(key: String, default: ULong = 0u): ULong

    fun getStringSet(key: String, default: Set<String>? = null): Set<String>?

    // 通用方法
    suspend fun remove(key: String)
    suspend fun clearAll()
    // 处理过期的数据
    suspend fun evictExpired()
    suspend fun getMeta(key: String) : CacheMeta?

    /**
     *  清理mmkv保存的文件，一些长时间
     *   - 检查 mmkv.totalSize > maxCacheBytes
     *   - 若超限：扫所有 cache:meta:*（其他的key的数据不处理） → 解析 cachedAt
     *   - 删除 ceil(count/2) 条 → mmkv.trim() 回收
     *   - 建议 App 启动延迟 2s 后异步调用，不阻塞冷启动关键路径
     */
    suspend fun trimCache()

    /**
     * 应用启动时候，版本不一样，可能需要清楚所有的信息，谨慎操作，一般不处理
     */
    suspend fun clearCacheOnVersionChange(versionCode: Int)
}