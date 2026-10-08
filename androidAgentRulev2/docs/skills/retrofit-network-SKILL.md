# Retrofit 网络技能文档

> **所属域**: 知识域 | **加载策略**: 按需加载
> **版本**: 2.0.0 | **创建日期**: 2026-07-14

## 一、概述

Retrofit 是 Android 平台最流行的 HTTP 客户端库，基于 OkHttp 构建。本技能文档涵盖接口定义、OkHttp 配置、拦截器、错误处理等核心内容。

---

## 二、依赖配置

```groovy
dependencies {
    // Retrofit 核心库
    implementation "com.squareup.retrofit2:retrofit:2.9.0"
    
    // Gson 转换器
    implementation "com.squareup.retrofit2:converter-gson:2.9.0"
    
    // OkHttp
    implementation "com.squareup.okhttp3:okhttp:4.3.0"
    
    // OkHttp 日志拦截器
    implementation "com.squareup.okhttp3:logging-interceptor:4.3.0"
    
    // Kotlin Coroutines 适配器
    implementation "com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2"
}
```

---

## 三、接口定义

### 3.1 基础接口

```kotlin
interface ApiService {
    @GET("users")
    suspend fun getUsers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<UserListResponse>
    
    @GET("users/{id}")
    suspend fun getUserById(
        @Path("id") id: Long
    ): Response<UserResponse>
    
    @POST("users")
    suspend fun createUser(
        @Body user: CreateUserRequest
    ): Response<UserResponse>
    
    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") id: Long,
        @Body user: UpdateUserRequest
    ): Response<UserResponse>
    
    @DELETE("users/{id}")
    suspend fun deleteUser(
        @Path("id") id: Long
    ): Response<Void>
}
```

### 3.2 请求体

```kotlin
data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String
)

data class UpdateUserRequest(
    val name: String? = null,
    val email: String? = null
)
```

### 3.3 响应体

```kotlin
data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val createdAt: String
)

data class UserListResponse(
    val data: List<UserResponse>,
    val total: Int,
    val page: Int,
    val limit: Int
)
```

---

## 四、OkHttp 配置

### 4.1 基础配置

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    })
    .build()
```

### 4.2 认证拦截器

```kotlin
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${tokenManager.getToken()}")
            .addHeader("Content-Type", "application/json")
            .build()
        return chain.proceed(request)
    }
}
```

### 4.3 重试拦截器

```kotlin
class RetryInterceptor(private val maxRetries: Int = 3) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response = chain.proceed(request)
        var retryCount = 0
        
        while (!response.isSuccessful && retryCount < maxRetries) {
            retryCount++
            response.close()
            response = chain.proceed(request)
        }
        
        return response
    }
}
```

### 4.4 完整配置

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(AuthInterceptor(tokenManager))
    .addInterceptor(RetryInterceptor(maxRetries = 3))
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    })
    .build()
```

---

## 五、Retrofit 初始化

### 5.1 创建 Retrofit 实例

```kotlin
val retrofit = Retrofit.Builder()
    .baseUrl(BuildConfig.BASE_URL)
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val apiService = retrofit.create(ApiService::class.java)
```

### 5.2 使用 Hilt 注入

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(tokenManager: TokenManager): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
```

---

## 六、错误处理

### 6.1 统一错误处理

```kotlin
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String, val code: Int = -1) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

suspend fun <T> safeApiCall(call: suspend () -> Response<T>): Result<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            response.body()?.let { Result.Success(it) } 
                ?: Result.Error("Empty response")
        } else {
            Result.Error(
                message = response.message(),
                code = response.code()
            )
        }
    } catch (e: Exception) {
        Result.Error(
            message = e.message ?: "Unknown error",
            code = -1
        )
    }
}
```

### 6.2 在 Repository 中使用

```kotlin
class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao
) {
    
    suspend fun getUsers(page: Int = 1, limit: Int = 20): Result<UserListResponse> {
        return safeApiCall {
            apiService.getUsers(page = page, limit = limit)
        }
    }
    
    suspend fun getUserById(id: Long): Result<UserResponse> {
        return safeApiCall {
            apiService.getUserById(id = id)
        }
    }
}
```

---

## 七、上传文件

### 7.1 单文件上传

```kotlin
interface ApiService {
    @Multipart
    @POST("upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody
    ): Response<UploadResponse>
}

// 使用
val file = File("path/to/file")
val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
val filePart = MultipartBody.Part.createFormData("file", file.name, requestBody)
val descriptionPart = "文件描述".toRequestBody("text/plain".toMediaTypeOrNull())

val response = apiService.uploadFile(filePart, descriptionPart)
```

### 7.2 多文件上传

```kotlin
interface ApiService {
    @Multipart
    @POST("upload/multiple")
    suspend fun uploadFiles(
        @Part files: List<MultipartBody.Part>
    ): Response<UploadResponse>
}
```

---

## 八、下载文件

### 8.1 下载到文件

```kotlin
interface ApiService {
    @GET("download/{filename}")
    @Streaming
    suspend fun downloadFile(
        @Path("filename") filename: String
    ): Response<ResponseBody>
}

// 使用
val response = apiService.downloadFile("file.zip")
response.body()?.let { body ->
    val inputStream = body.byteStream()
    val outputFile = File("path/to/output/file.zip")
    inputStream.use { input ->
        outputFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }
}
```

---

## 九、常见问题

### Q1: 网络请求失败

**原因**: 网络不可用、超时、服务器错误

**解决**: 添加网络状态检查，增加超时时间，实现重试机制

### Q2: 认证失败

**原因**: Token 过期或无效

**解决**: 在 AuthInterceptor 中处理 Token 刷新逻辑

### Q3: JSON 解析错误

**原因**: 响应格式与数据类不匹配

**解决**: 检查数据类字段名是否与 JSON 一致，使用 `@SerializedName` 注解映射

---

## 十、最佳实践

1. **使用 suspend**: 所有 Retrofit 接口方法使用 `suspend` 关键字
2. **统一错误处理**: 使用 `safeApiCall` 封装网络请求
3. **日志分级**: 只在 Debug 模式下启用详细日志
4. **超时设置**: 设置合理的连接、读取、写入超时时间
5. **拦截器顺序**: 认证拦截器在前，日志拦截器在后
6. **依赖注入**: 使用 Hilt 管理 Retrofit 和 OkHttp 实例