# Room 数据库技能文档

> **所属域**: 知识域 | **加载策略**: 按需加载
> **版本**: 2.0.0 | **创建日期**: 2026-07-14

## 一、概述

Room 是 Android Jetpack 提供的 SQLite 数据库抽象层，简化了数据库操作。本技能文档涵盖实体定义、DAO、数据库初始化、迁移等核心内容。

---

## 二、依赖配置

```groovy
dependencies {
    // Room 核心库
    implementation "androidx.room:room-runtime:2.6.1"
    
    // KSP 注解处理器
    ksp "androidx.room:room-compiler:2.6.1"
    
    // Room Kotlin 扩展
    implementation "androidx.room:room-ktx:2.6.1"
    
    // Room RxJava 支持（可选）
    implementation "androidx.room:room-rxjava3:2.6.1"
    
    // Room Paging 支持（可选）
    implementation "androidx.room:room-paging:2.6.1"
}
```

---

## 三、实体定义

### 3.1 基础实体

```kotlin
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "email", index = true)
    val email: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
```

### 3.2 复合主键

```kotlin
@Entity(
    tableName = "user_roles",
    primaryKeys = ["user_id", "role_id"]
)
data class UserRole(
    @ColumnInfo(name = "user_id")
    val userId: Long,
    
    @ColumnInfo(name = "role_id")
    val roleId: Long
)
```

### 3.3 外键约束

```kotlin
@Entity(
    tableName = "posts",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["user_id"])]
)
data class Post(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "user_id")
    val userId: Long,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "content")
    val content: String
)
```

---

## 四、DAO 定义

### 4.1 基础操作

```kotlin
@Dao
interface UserDao {
    // 查询所有用户
    @Query("SELECT * FROM users ORDER BY created_at DESC")
    suspend fun getAllUsers(): List<User>
    
    // 根据 ID 查询
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): User?
    
    // 根据名称查询
    @Query("SELECT * FROM users WHERE name LIKE :name")
    suspend fun searchUsers(name: String): List<User>
    
    // 插入用户
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long
    
    // 更新用户
    @Update
    suspend fun updateUser(user: User)
    
    // 删除用户
    @Delete
    suspend fun deleteUser(user: User)
    
    // 删除所有用户
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
    
    // 统计用户数量
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}
```

### 4.2 观察数据

```kotlin
@Dao
interface UserDao {
    // 返回 Flow（自动更新）
    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<User>>
    
    // 返回 LiveData（自动更新）
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserByIdLiveData(id: Long): LiveData<User?>
}
```

### 4.3 关系查询

```kotlin
// 定义关系数据类
data class UserWithPosts(
    @Embedded
    val user: User,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "user_id"
    )
    val posts: List<Post>
)

// DAO 查询
@Dao
interface UserDao {
    @Transaction
    @Query("SELECT * FROM users")
    suspend fun getUsersWithPosts(): List<UserWithPosts>
}
```

---

## 五、数据库初始化

### 5.1 创建数据库

```kotlin
@Database(
    entities = [User::class, Post::class, UserRole::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

### 5.2 使用 Hilt 注入

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }
    
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
}
```

---

## 六、数据库迁移

### 6.1 创建迁移

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 添加新列
        database.execSQL("ALTER TABLE users ADD COLUMN age INTEGER DEFAULT 0")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 创建新表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS settings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                key TEXT NOT NULL,
                value TEXT NOT NULL
            )
        """.trimIndent())
    }
}
```

### 6.2 注册迁移

```kotlin
Room.databaseBuilder(
    context,
    AppDatabase::class.java,
    "app_database"
)
    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
    .build()
```

### 6.3 破坏性迁移（开发阶段）

```kotlin
Room.databaseBuilder(
    context,
    AppDatabase::class.java,
    "app_database"
)
    .fallbackToDestructiveMigration()
    .build()
```

---

## 七、使用示例

### 7.1 在 ViewModel 中使用

```kotlin
class UserViewModel @ViewModelInject constructor(
    private val userDao: UserDao
) : ViewModel() {
    
    // 观察用户列表
    val users: Flow<List<User>> = userDao.getAllUsersFlow()
    
    // 获取用户数量
    suspend fun getUserCount(): Int {
        return userDao.getUserCount()
    }
    
    // 添加用户
    suspend fun addUser(name: String, email: String) {
        val user = User(name = name, email = email)
        userDao.insertUser(user)
    }
    
    // 删除用户
    suspend fun removeUser(user: User) {
        userDao.deleteUser(user)
    }
}
```

### 7.2 在 Compose 中观察

```kotlin
@Composable
fun UserListScreen(viewModel: UserViewModel = viewModel()) {
    val users by viewModel.users.collectAsState(initial = emptyList())
    
    LazyColumn {
        items(users) { user ->
            Text("${user.name} - ${user.email}")
        }
    }
}
```

---

## 八、常见问题

### Q1: Room 编译错误

**原因**: KSP 配置错误或实体定义问题

**解决**: 确保添加了 `ksp "androidx.room:room-compiler:2.6.1"` 依赖

### Q2: 迁移失败

**原因**: 迁移脚本错误或版本号不匹配

**解决**: 检查迁移脚本的 SQL 语法，确保版本号连续

### Q3: 查询结果为空

**原因**: 查询条件错误或数据库为空

**解决**: 添加日志验证查询条件，检查数据库初始化

---

## 九、最佳实践

1. **单一数据库实例**: 使用单例模式或依赖注入确保只有一个数据库实例
2. **后台执行**: 所有数据库操作在协程或后台线程中执行
3. **使用 Flow**: 对于需要自动更新的数据，使用 Flow 观察
4. **迁移测试**: 在发布前测试数据库迁移
5. **导出 Schema**: 设置 `exportSchema = true` 便于调试
6. **索引优化**: 为频繁查询的列添加索引