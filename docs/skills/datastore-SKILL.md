# DataStore 技能文档

> **所属域**: 知识域 | **加载策略**: 按需加载
> **版本**: 2.0.0 | **创建日期**: 2026-07-14

## 一、概述

DataStore 是 Android Jetpack 提供的现代化数据存储方案，替代 SharedPreferences。本技能文档涵盖 DataStore 初始化、数据存取、SharedPreferences 迁移等核心内容。

---

## 二、依赖配置

```groovy
dependencies {
    // Preferences DataStore
    implementation "androidx.datastore:datastore-preferences:1.0.0"
    
    // Proto DataStore（可选，用于复杂数据结构）
    implementation "androidx.datastore:datastore:1.0.0"
    implementation "com.google.protobuf:protobuf-javalite:3.21.7"
    
    // DataStore KTX
    implementation "androidx.datastore:datastore-preferences-ktx:1.0.0"
}
```

---

## 三、Preferences DataStore

### 3.1 创建 DataStore

```kotlin
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
```

### 3.2 定义键

```kotlin
object SettingsKeys {
    val USER_NAME = stringPreferencesKey("user_name")
    val USER_ID = longPreferencesKey("user_id")
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    val THEME_MODE = intPreferencesKey("theme_mode")
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
}
```

### 3.3 读取数据

```kotlin
// 读取单个值
val userNameFlow: Flow<String?> = context.dataStore.data
    .map { preferences ->
        preferences[SettingsKeys.USER_NAME]
    }

// 读取多个值
data class UserSettings(
    val userName: String?,
    val isLoggedIn: Boolean,
    val themeMode: Int
)

val userSettingsFlow: Flow<UserSettings> = context.dataStore.data
    .map { preferences ->
        UserSettings(
            userName = preferences[SettingsKeys.USER_NAME],
            isLoggedIn = preferences[SettingsKeys.IS_LOGGED_IN] ?: false,
            themeMode = preferences[SettingsKeys.THEME_MODE] ?: 0
        )
    }
```

### 3.4 写入数据

```kotlin
suspend fun saveUserName(userName: String) {
    context.dataStore.edit { preferences ->
        preferences[SettingsKeys.USER_NAME] = userName
    }
}

suspend fun saveUserSettings(settings: UserSettings) {
    context.dataStore.edit { preferences ->
        preferences[SettingsKeys.USER_NAME] = settings.userName ?: ""
        preferences[SettingsKeys.IS_LOGGED_IN] = settings.isLoggedIn
        preferences[SettingsKeys.THEME_MODE] = settings.themeMode
    }
}

suspend fun clearAllSettings() {
    context.dataStore.edit { preferences ->
        preferences.clear()
    }
}
```

### 3.5 删除数据

```kotlin
suspend fun removeUserName() {
    context.dataStore.edit { preferences ->
        preferences.remove(SettingsKeys.USER_NAME)
    }
}
```

---

## 四、Proto DataStore

### 4.1 创建 Proto 文件

```proto
// app/src/main/proto/user.proto
syntax = "proto3";

option java_package = "com.example.app";
option java_multiple_files = true;

message UserPreferences {
  string user_name = 1;
  int64 user_id = 2;
  bool is_logged_in = 3;
  int32 theme_mode = 4;
  repeated string favorite_topics = 5;
}
```

### 4.2 配置 Protobuf（build.gradle）

```groovy
plugins {
    id 'com.google.protobuf' version '0.9.4'
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.21.7"
    }
    
    generateProtoTasks {
        all().each { task ->
            task.builtins {
                java {
                    option 'lite'
                }
            }
        }
    }
}

dependencies {
    implementation "com.google.protobuf:protobuf-javalite:3.21.7"
}
```

### 4.3 创建 Proto DataStore

```kotlin
object UserPreferencesSerializer : Serializer<UserPreferences> {
    override val defaultValue: UserPreferences = UserPreferences.getDefaultInstance()
    
    override suspend fun readFrom(input: InputStream): UserPreferences {
        try {
            return UserPreferences.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto", e)
        }
    }
    
    override suspend fun writeTo(t: UserPreferences, output: OutputStream) {
        t.writeTo(output)
    }
}

val Context.userPreferencesDataStore: DataStore<UserPreferences> by dataStore(
    fileName = "user_preferences.pb",
    serializer = UserPreferencesSerializer
)
```

### 4.4 读写 Proto 数据

```kotlin
// 读取
val userPreferencesFlow: Flow<UserPreferences> = context.userPreferencesDataStore.data

// 写入
suspend fun updateUserName(userName: String) {
    context.userPreferencesDataStore.updateData { preferences ->
        preferences.toBuilder()
            .setUserName(userName)
            .build()
    }
}

// 删除字段
suspend fun clearUserName() {
    context.userPreferencesDataStore.updateData { preferences ->
        preferences.toBuilder()
            .clearUserName()
            .build()
    }
}
```

---

## 五、SharedPreferences 迁移

### 5.1 创建迁移器

```kotlin
val sharedPreferencesMigration = SharedPreferencesMigration(
    context = context,
    sharedPreferencesName = "old_settings"
)

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    produceMigrations = { context ->
        listOf(sharedPreferencesMigration)
    }
)
```

### 5.2 自定义迁移逻辑

```kotlin
val migration = object : Migration<Preferences> {
    override suspend fun migrate(currentData: Preferences): Preferences {
        return currentData.toMutablePreferences().apply {
            // 迁移逻辑
            val oldToken = getString("old_token_key")
            if (oldToken != null) {
                this[SettingsKeys.ACCESS_TOKEN] = oldToken
            }
            // 删除旧键
            remove("old_token_key")
        }
    }
}
```

### 5.3 完整迁移配置

```kotlin
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(context, "old_settings"),
            SharedPreferencesMigration(context, "user_settings")
        )
    }
)
```

---

## 六、在 ViewModel 中使用

```kotlin
class SettingsViewModel @ViewModelInject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    val userSettings: Flow<UserSettings> = context.dataStore.data
        .map { preferences ->
            UserSettings(
                userName = preferences[SettingsKeys.USER_NAME],
                isLoggedIn = preferences[SettingsKeys.IS_LOGGED_IN] ?: false,
                themeMode = preferences[SettingsKeys.THEME_MODE] ?: 0
            )
        }
    
    fun saveUserName(userName: String) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[SettingsKeys.USER_NAME] = userName
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences.remove(SettingsKeys.USER_NAME)
                preferences.remove(SettingsKeys.USER_ID)
                preferences[SettingsKeys.IS_LOGGED_IN] = false
            }
        }
    }
}
```

---

## 七、在 Compose 中观察

```kotlin
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val userSettings by viewModel.userSettings.collectAsState(
        initial = UserSettings(null, false, 0)
    )
    
    Column {
        Text("用户名: ${userSettings.userName ?: "未登录"}")
        
        OutlinedTextField(
            value = userSettings.userName ?: "",
            onValueChange = { viewModel.saveUserName(it) },
            label = { Text("输入用户名") }
        )
        
        Button(onClick = { viewModel.logout() }) {
            Text("退出登录")
        }
    }
}
```

---

## 八、常见问题

### Q1: DataStore 数据不更新

**原因**: 没有使用 Flow 观察或在错误的线程读取

**解决**: 使用 `collectAsState()` 在 Compose 中观察，或在协程中收集

### Q2: 迁移失败

**原因**: SharedPreferences 文件不存在或格式错误

**解决**: 添加迁移前检查，使用 try-catch 处理异常

### Q3: Proto 编译错误

**原因**: Protobuf 插件配置错误

**解决**: 确保添加了 `com.google.protobuf` 插件和正确的依赖

---

## 九、最佳实践

1. **单一 DataStore**: 应用中按功能分类创建多个 DataStore（如 settings、user）
2. **使用 Flow**: 通过 Flow 观察数据变化，自动更新 UI
3. **封装 Repository**: 将 DataStore 操作封装在 Repository 中
4. **迁移策略**: 使用 SharedPreferencesMigration 平滑迁移旧数据
5. **协程作用域**: 使用 `viewModelScope` 或 `lifecycleScope` 确保操作在正确的生命周期执行
6. **避免阻塞**: DataStore 操作是异步的，不要在主线程阻塞等待结果