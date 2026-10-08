# ARouter 路由框架 Skill 文档

> **所属域**: 知识域 | **加载策略**: 按需加载

## 模块概述

ARouter 是一个 Android 路由框架，本项目采用**源码集成方式**，并**自定义了 KSP (Kotlin Symbol Processing) 注解处理器**，替代了原有的 kapt 编译方案。

### 架构特点

1. **源码集成**: 直接包含 ARouter 源码到项目中，而非通过 Maven 依赖
2. **KSP 处理器**: 自研 KSP 注解处理器，性能优于 kapt，兼容 AGP 8.x
3. **模块化支持**: 支持多模块路由分组管理
4. **动态路由**: 支持运行时动态添加路由组

### 模块结构

```
arouter-annotation/          # 注解定义模块 (Java)
├── facade/annotation/
│   ├── Route.java           # 路由注解
│   ├── Autowired.java       # 依赖注入注解
│   ├── Interceptor.java     # 拦截器注解
│   └── Param.java           # 参数注解 (已废弃)
├── facade/enums/
│   ├── RouteType.java       # 路由类型枚举
│   └── TypeKind.java        # 字段类型枚举
└── facade/model/
    └── RouteMeta.java       # 路由元数据模型

arouter-api/                 # 运行时 API 模块 (Java)
├── launcher/
│   ├── ARouter.java         # 公开 API 入口
│   └── _ARouter.java        # 内部实现
├── core/
│   ├── LogisticsCenter.java # 路由中心，加载和补全路由
│   └── Warehouse.java       # 路由存储仓库
├── facade/
│   ├── Postcard.java        # 路由卡片
│   ├── template/            # 接口模板
│   └── service/             # 服务接口
└── utils/                   # 工具类

arouter-ksp/                 # KSP 注解处理器模块 (Kotlin)
├── ArouterKspProcessor.kt       # 核心处理器
└── ArouterKspProcessorProvider.kt  # 处理器提供者
```

---

## 1. 构建环境兼容性

### 版本要求

| 组件 | 版本 | 说明 |
|------|------|------|
| AGP (Android Gradle Plugin) | 8.1.0 | 支持 AGP 8.x 系列 |
| Gradle | 8.4 | 构建工具版本 |
| Kotlin | 1.9.0 | 主语言版本 |
| KSP | 1.9.0-1.0.13 | Kotlin Symbol Processing |
| JDK | 17 (主项目) / 11 (ARouter 模块) | Java 编译版本 |

### 为什么使用源码集成？

**优势**:
1. **完全控制**: 可自定义注解处理器逻辑，无需依赖官方更新
2. **AGP 8 兼容**: 原生 ARouter 使用 kapt，在 AGP 8.x 中存在兼容性问题
3. **性能提升**: KSP 比 kapt 快约 2 倍
4. **无版本冲突**: 避免与第三方 ARouter 依赖版本冲突
5. **可调试**: 可直接调试注解处理器代码

**依赖关系**:
```groovy
// 主应用模块 build.gradle
ksp project(':arouter-ksp')           // KSP 处理器
implementation project(':arouter-api') // 运行时 API
implementation project(':arouter-annotation') // 注解定义
```

---

## 2. 注解系统详解

### 2.1 @Route 注解

**定义位置**: `arouter-annotation/src/main/java/com/alibaba/android/arouter/facade/annotation/Route.java`

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface Route {
    String path();           // 路由路径，必填，如 "/home/main"
    String group() default ""; // 路由分组，默认从 path 提取第一段
    String name() default "";  // 路由名称，用于生成 javadoc
    int extras() default Integer.MIN_VALUE; // 额外数据，位标记
    int priority() default -1;  // 优先级，数值越小优先级越高
}
```

**使用说明**:
- 用于标记 Activity、Fragment、Service、Provider 等可路由目标
- `path` 必须以 `/` 开头，格式如 `/模块名/页面名`
- `group` 为空时自动从 path 提取（如 `/home/main` 的 group 为 `home`）
- `extras` 可用于标记页面特性（如登录态要求）

**示例**:
```kotlin
@Route(path = "/home/main", extras = RouterPath.guestPage)
class HomeActivity : BaseActivity() {
    // ...
}
```

### 2.2 @Autowired 注解

**定义位置**: `arouter-annotation/src/main/java/com/alibaba/android/arouter/facade/annotation/Autowired.java`

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.CLASS)
public @interface Autowired {
    String name() default "";    // 参数名，默认使用字段名
    boolean required() default false; // 是否必填
    String desc() default "";    // 字段描述
}
```

**使用说明**:
- 用于 Activity/Fragment 中接收路由参数
- 需要在目标页面调用 `ARouter.getInstance().inject(this)`
- 支持类型: Boolean, Byte, Short, Int, Long, Float, Double, String, Parcelable, Serializable

**示例**:
```kotlin
@Route(path = "/shop/detail")
class ShopDetailActivity : BaseActivity() {
    
    @Autowired(name = "productId")
    lateinit var productId: String
    
    @Autowired
    var price: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this) // 注入参数
        // 现在可以使用 productId 和 price
    }
}
```

### 2.3 @Interceptor 注解

**定义位置**: `arouter-annotation/src/main/java/com/alibaba/android/arouter/facade/annotation/Interceptor.java`

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface Interceptor {
    int priority();          // 拦截器优先级，数值越小越先执行
    String name() default "Default"; // 拦截器名称
}
```

**使用说明**:
- 必须实现 `IInterceptor` 接口
- 在路由跳转前拦截，可进行登录校验、参数校验等
- 按 priority 从小到大顺序执行

**示例**:
```kotlin
@Interceptor(priority = 1, name = "登录拦截器")
class LoginInterceptor : IInterceptor {
    override fun process(postcard: Postcard?, callback: InterceptorCallback?) {
        // 检查登录状态
        if (!isLogin && postcard?.extra == RouterPath.guestPage) {
            // 跳转登录页
            Router.go("/user/login")
            callback?.onInterrupt(null)
        } else {
            callback?.onContinue(postcard)
        }
    }
    
    override fun init(context: Context?) {
        // 初始化逻辑
    }
}
```

---

## 3. KSP 处理器详解

### 3.1 处理器架构

**核心类**: `ArouterKspProcessor.kt`

KSP 处理器主要处理三类注解:
1. `@Route` → 生成路由分组类和根路由类
2. `@Autowired` → 生成参数注入类 (ISyringe 实现)
3. `@Interceptor` → 生成拦截器注册类

### 3.2 生成文件规则

| 注解类型 | 生成类名格式 | 示例 |
|---------|-------------|------|
| Route Group | `ARouter$$Group$${group}` | `ARouter$$Group$$home` |
| Route Root | `ARouter$$Root$${moduleName}` | `ARouter$$Root$$app` |
| Provider | `ARouter$$Providers$${moduleName}` | `ARouter$$Providers$$app` |
| Autowired | `{ClassName}$$ARouter$$Autowired` | `HomeActivity$$ARouter$$Autowired` |
| Interceptor | `ARouter$$Interceptors$${moduleName}` | `ARouter$$Interceptors$$app` |

### 3.3 路由类型解析逻辑

KSP 处理器按以下优先级判断路由类型:

```kotlin
// 解析优先级从高到低:
1. Activity (检查继承链: Activity, AppCompatActivity, FragmentActivity, ComponentActivity)
2. Fragment (检查继承链: android.app.Fragment, androidx.fragment.app.Fragment)
3. Android Service (检查继承链: Service, IntentService, JobIntentService)
4. Provider (实现 IProvider 接口或其子接口)
5. 默认为 ACTIVITY
```

**关键实现**:
```kotlin
private fun resolveRouteType(classDecl: KSClassDeclaration): String {
    val allSuperTypes = classDecl.getAllSuperTypes().toList()
    
    // 1. 优先判断 Android 组件类型
    if (isActivity) return "ACTIVITY"
    if (isFragment) return "FRAGMENT"
    if (isAndroidService) return "SERVICE"
    
    // 2. 判断是否为 ARouter Provider
    if (implementsIProvider) return "PROVIDER"
    
    // 3. 兜底默认值
    return "ACTIVITY"
}
```

### 3.4 Provider 识别逻辑

Provider 的核心定义: 类实现了 `IProvider` 接口或其子接口

```kotlin
// 收集实现了 IProvider 接口的所有 Provider 实现
if (routeType == "PROVIDER") {
    classDecl.getAllSuperTypes().forEach { superType ->
        val decl = superType.declaration as? KSClassDeclaration
        if (decl != null && decl.isIProviderInterface()) {
            providerClasses.add(ProviderMeta(...))
        }
    }
}
```

### 3.5 类型推断系统

KSP 处理器支持完整的类型推断，用于参数注入:

```kotlin
private fun resolveTypeKind(type: KSType): TypeKind {
    return when (qName) {
        "kotlin.Boolean" -> TypeKind.BOOLEAN
        "kotlin.Int" -> TypeKind.INT
        "kotlin.String" -> TypeKind.STRING
        "android.os.Parcelable" -> TypeKind.PARCELABLE
        "java.io.Serializable" -> TypeKind.SERIALIZABLE
        // ... 其他类型
    }
}
```

---

## 4. 核心运行时机制

### 4.1 初始化流程

```kotlin
// 在 Application.onCreate() 中调用
ARouter.init(this)
```

**内部流程**:
1. `ARouter.init()` → `_ARouter.init()`
2. `LogisticsCenter.init()` → 扫描并加载路由元数据
3. 从 `com.alibaba.android.arouter.routes` 包扫描生成的路由类
4. 加载 `IRouteRoot` → 填充 `Warehouse.groupsIndex`
5. 加载 `IInterceptorGroup` → 填充 `Warehouse.interceptorsIndex`
6. 加载 `IProviderGroup` → 填充 `Warehouse.providersIndex`
7. `afterInit()` → 触发拦截器初始化

### 4.2 路由加载机制

**LogisticsCenter.init() 核心逻辑**:

```java
// 1. 检查是否由插件注册
loadRouterMap();
if (registerByPlugin) {
    // 插件方式注册
} else {
    // 2. 扫描 routes 包下的所有类
    routerMap = ClassUtils.getFileNameByPackageName(mContext, ROUTE_ROOT_PAKCAGE);
    
    // 3. 根据类名前缀分类加载
    for (String className : routerMap) {
        if (className.startsWith(ROUTE_ROOT_PAKCAGE + ".ARouter$$Root")) {
            // 加载路由根节点
            ((IRouteRoot) clazz.newInstance()).loadInto(Warehouse.groupsIndex);
        } else if (className.startsWith(ROUTE_ROOT_PAKCAGE + ".ARouter$$Interceptors")) {
            // 加载拦截器
            ((IInterceptorGroup) clazz.newInstance()).loadInto(Warehouse.interceptorsIndex);
        } else if (className.startsWith(ROUTE_ROOT_PAKCAGE + ".ARouter$$Providers")) {
            // 加载 Provider
            ((IProviderGroup) clazz.newInstance()).loadInto(Warehouse.providersIndex);
        }
    }
}
```

### 4.3 路由跳转流程

```
ARouter.build(path)
  ↓
_ARouter.build(path) → 创建 Postcard
  ↓
navigation(context, postcard, requestCode, callback)
  ↓
LogisticsCenter.completion(postcard) → 补全路由元数据
  ↓
[如果路由组未加载] → 按需加载路由组
  ↓
[检查拦截器] → interceptorService.doInterceptions()
  ↓
[执行导航] → _navigation(postcard)
  ↓
[根据类型执行] → ACTIVITY: startActivity / PROVIDER: 返回实例 / FRAGMENT: 创建实例
```

### 4.4 按需加载机制

路由采用**懒加载**策略，只在首次访问某个路由组时才加载该组的详细路由信息:

```java
// LogisticsCenter.completion()
RouteMeta routeMeta = Warehouse.routes.get(postcard.getPath());
if (null == routeMeta) {
    // 路由组未加载，触发加载
    addRouteGroupDynamic(postcard.getGroup(), null);
    completion(postcard); // 重新补全
}
```

### 4.5 数据存储 (Warehouse)

```java
class Warehouse {
    // 路由组索引 (group → IRouteGroup.class)
    static Map<String, Class<? extends IRouteGroup>> groupsIndex;
    
    // 已加载的路由详情 (path → RouteMeta)
    static Map<String, RouteMeta> routes;
    
    // Provider 实例缓存
    static Map<Class, IProvider> providers;
    
    // Provider 索引
    static Map<String, RouteMeta> providersIndex;
    
    // 拦截器索引 (priority → IInterceptor.class)
    static Map<Integer, Class<? extends IInterceptor>> interceptorsIndex;
    
    // 已初始化的拦截器实例
    static List<IInterceptor> interceptors;
}
```

---

## 5. 配置指南

### 5.1 主模块配置

**build.gradle**:
```groovy
plugins {
    id 'com.android.application'
    id 'kotlin-android'
    id 'com.google.devtools.ksp'
}

android {
    defaultConfig {
        ksp {
            arg('AROUTER_MODULE_NAME', project.getName())
        }
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    ksp project(':arouter-ksp')
    implementation project(':arouter-api')
    implementation project(':arouter-annotation')
}
```

### 5.2 业务模块配置

**build.gradle**:
```groovy
plugins {
    id 'com.android.library'
    id 'kotlin-android'
    id 'com.google.devtools.ksp'
}

android {
    defaultConfig {
        ksp {
            arg('AROUTER_MODULE_NAME', project.getName())
        }
    }
}

dependencies {
    ksp project(':arouter-ksp')
    implementation project(':arouter-api')
    implementation project(':arouter-annotation')
}
```

### 5.3 ARouter-KSP 模块配置

**build.gradle**:
```groovy
plugins {
    id 'org.jetbrains.kotlin.jvm'
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation 'com.google.devtools.ksp:symbol-processing-api:1.9.22-1.0.17'
    implementation 'com.squareup:kotlinpoet:1.16.0'
    implementation 'com.squareup:kotlinpoet-ksp:1.16.0'
    
    implementation project(':arouter-annotation')
}
```

### 5.4 处理器注册文件

**位置**: `arouter-ksp/src/main/resources/META-INF/services/com.google.devtools.ksp.processing.SymbolProcessorProvider`

**内容**:
```
com.alibaba.android.arouter.ksp.ArouterKspProcessorProvider
```

---

## 6. 使用指南

### 6.1 基本路由跳转

```kotlin
// 方式一: 使用 ARouter API
ARouter.getInstance().build("/home/main").navigation()

// 方式二: 使用封装的 Router.go()
Router.go("/home/main")

// 方式三: 使用扩展方法 (推荐)
"/home/main".go()
```

### 6.2 带参数跳转

```kotlin
// 使用 with 系列方法传参
ARouter.getInstance()
    .build("/shop/detail")
    .withString("productId", "12345")
    .withInt("price", 999)
    .navigation()
```

### 6.3 接收参数

```kotlin
@Route(path = "/shop/detail")
class ShopDetailActivity : BaseActivity() {
    
    @Autowired(name = "productId")
    lateinit var productId: String
    
    @Autowired
    var price: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this)
        // 使用参数
        Log.d("ShopDetail", "productId=$productId, price=$price")
    }
}
```

### 6.4 跳转动画

```kotlin
ARouter.getInstance()
    .build("/home/main")
    .withTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    .navigation()
```

### 6.5 获取 Provider

```kotlin
// 获取 Provider 实例
val loginService = ARouter.getInstance()
    .navigation(LoginService::class.java)

// 或者使用路径
val loginService = ARouter.getInstance()
    .build("/service/login")
    .navigation() as LoginService
```

### 6.6 自定义拦截器

```kotlin
@Interceptor(priority = 10, name = "自定义拦截器")
class CustomInterceptor : IInterceptor {
    override fun process(postcard: Postcard?, callback: InterceptorCallback?) {
        // 自定义拦截逻辑
        callback?.onContinue(postcard)
    }
    
    override fun init(context: Context?) {
        // 初始化
    }
}
```

### 6.7 动态添加路由

```kotlin
// 运行时动态添加路由组
ARouter.getInstance().addRouteGroup(object : IRouteGroup {
    override fun loadInto(atlas: MutableMap<String, RouteMeta>?) {
        atlas?.put("/dynamic/page", RouteMeta.build(
            RouteType.ACTIVITY,
            DynamicActivity::class.java,
            "/dynamic/page",
            "dynamic"
        ))
    }
})
```

---

## 7. 生成代码示例

### 7.1 路由组文件

**输入**:
```kotlin
@Route(path = "/home/main")
class HomeActivity : BaseActivity()
```

**生成文件**: `ARouter$$Group$$home.kt`
```kotlin
// DO NOT EDIT THIS FILE!!! IT WAS GENERATED BY AROUTER.
package com.alibaba.android.arouter.routes

public class ARouter$$Group$$home : IRouteGroup {
  override fun loadInto(atlas: MutableMap<String, RouteMeta>?) {
    atlas?.put("/home/main", RouteMeta.build(
        RouteType.ACTIVITY, 
        HomeActivity::class.java, 
        "/home/main", 
        "home", 
        null, 
        -1, 
        -2147483648
    ))
  }
}
```

### 7.2 根路由文件

**生成文件**: `ARouter$$Root$$app.kt`
```kotlin
// DO NOT EDIT THIS FILE!!! IT WAS GENERATED BY AROUTER.
package com.alibaba.android.arouter.routes

public class ARouter$$Root$$app : IRouteRoot {
  override fun loadInto(routes: MutableMap<String, Class<out IRouteGroup>>?) {
    routes?.put("home", ARouter$$Group$$home::class.java)
    routes?.put("shop", ARouter$$Group$$shop::class.java)
    routes?.put("user", ARouter$$Group$$user::class.java)
  }
}
```

### 7.3 参数注入文件

**输入**:
```kotlin
@Route(path = "/shop/detail")
class ShopDetailActivity : BaseActivity() {
    @Autowired(name = "productId")
    lateinit var productId: String
    
    @Autowired
    var price: Int = 0
}
```

**生成文件**: `ShopDetailActivity$$ARouter$$Autowired.kt`
```kotlin
// DO NOT EDIT THIS FILE!!! IT WAS GENERATED BY AROUTER.
package com.example.shop

public class ShopDetailActivity$$ARouter$$Autowired : ISyringe {
  override fun inject(target: Any) {
    if (target !is ShopDetailActivity) return
    val substitute = target as ShopDetailActivity
    substitute.productId = substitute.intent.extras?.getCharSequence("productId")?.toString()
    substitute.price = substitute.intent.getIntExtra("price", substitute.price)
  }
}
```

### 7.4 Provider 文件

**生成文件**: `ARouter$$Providers$$app.kt`
```kotlin
// DO NOT EDIT THIS FILE!!! IT WAS GENERATED BY AROUTER.
package com.alibaba.android.arouter.routes

public class ARouter$$Providers$$app : IProviderGroup {
  override fun loadInto(providers: MutableMap<String, RouteMeta>?) {
    providers?.put("com.example.service.LoginService", RouteMeta.build(
        RouteType.PROVIDER, 
        LoginServiceImpl::class.java, 
        "/service/login", 
        "service", 
        null, 
        -1, 
        -2147483648
    ))
  }
}
```

### 7.5 拦截器文件

**生成文件**: `ARouter$$Interceptors$$app.kt`
```kotlin
// DO NOT EDIT THIS FILE!!! IT WAS GENERATED BY AROUTER.
package com.alibaba.android.arouter.routes

public class ARouter$$Interceptors$$app : IInterceptorGroup {
  override fun loadInto(interceptors: MutableMap<Int, Class<out IInterceptor>>?) {
    interceptors?.put(1, LoginInterceptor::class.java)
    interceptors?.put(10, CustomInterceptor::class.java)
  }
}
```

---

## 8. 常见问题

### Q1: KSP 处理器未生成路由文件

**原因**: 
- 未正确配置 KSP 插件
- 未添加 `AROUTER_MODULE_NAME` 参数
- 注解类未被正确识别

**解决**:
```groovy
plugins {
    id 'com.google.devtools.ksp'
}

android {
    defaultConfig {
        ksp {
            arg('AROUTER_MODULE_NAME', project.getName())
        }
    }
}

dependencies {
    ksp project(':arouter-ksp')
}
```

### Q2: 路由跳转失败 "No route matched"

**原因**:
- 未调用 `ARouter.init()`
- 路由路径不匹配
- 路由组未加载

**解决**:
```kotlin
// 1. 确保在 Application 中初始化
class MainApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ARouter.init(this)
    }
}

// 2. 开启调试日志
ARouter.openDebug()
ARouter.openLog()
```

### Q3: @Autowired 参数未注入

**原因**:
- 未调用 `ARouter.getInstance().inject(this)`
- 调用时机错误（应在 super.onCreate() 之后）

**解决**:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ARouter.getInstance().inject(this) // 必须在 super.onCreate() 之后
}
```

### Q4: 拦截器未生效

**原因**:
- 未实现 `IInterceptor` 接口
- 拦截器优先级配置错误

**解决**:
```kotlin
@Interceptor(priority = 1, name = "登录拦截器")
class LoginInterceptor : IInterceptor {
    // 必须实现 IInterceptor 接口
}
```

### Q5: Provider 获取为空

**原因**:
- Provider 类未添加 `@Route` 注解
- 服务名称不匹配

**解决**:
```kotlin
@Route(path = "/service/login")
class LoginServiceImpl : LoginService {
    override fun init(context: Context?) { }
}
```

### Q6: AGP 8.x 兼容性错误

**原因**: 原生 ARouter 使用 kapt，与 AGP 8.x 不兼容

**解决**: 本项目已采用 KSP 方案，确保使用:
```groovy
// 使用 ksp 替代 kapt
ksp project(':arouter-ksp')
// 不要使用
// kapt 'com.alibaba:arouter-compiler:...'
```

---

## 9. 最佳实践

### 9.1 路由路径规范

```
/模块名/功能名/操作
示例:
/home/main          # 首页
/user/login         # 用户登录
/shop/detail        # 商品详情
/service/login      # 登录服务 (Provider)
```

### 9.2 拦截器优先级设计

```kotlin
priority = 1   // 系统级拦截器 (日志、统计)
priority = 5   // 安全拦截器 (登录、权限)
priority = 10  // 业务拦截器 (个性化、A/B测试)
```

### 9.3 Provider 设计模式

```kotlin
// 1. 定义接口 (通常在 api 模块)
interface LoginService : IProvider {
    fun isLogin(): Boolean
    fun login(callback: LoginCallback)
}

// 2. 实现接口 (通常在 implementation 模块)
@Route(path = "/service/login")
class LoginServiceImpl : LoginService {
    override fun init(context: Context?) { }
    
    override fun isLogin(): Boolean {
        return UserInfoManager.isLogin
    }
}

// 3. 使用服务
val loginService = ARouter.getInstance().navigation(LoginService::class.java)
```

### 9.4 路由跳转封装

```kotlin
// Router 工具类
object Router {
    fun go(path: String, params: Map<String, Any>? = null) {
        val postcard = ARouter.getInstance().build(path)
        params?.forEach { (key, value) ->
            when (value) {
                is String -> postcard.withString(key, value)
                is Int -> postcard.withInt(key, value)
                // ... 其他类型
            }
        }
        postcard.navigation()
    }
}

// 使用
Router.go("/shop/detail", mapOf(
    "productId" to "12345",
    "price" to 999
))
```

### 9.5 降级服务

```kotlin
@Route(path = "/service/degrade")
class DegradeServiceImpl : DegradeService {
    override fun onLost(context: Context?, postcard: Postcard?) {
        // 路由未找到时的降级处理
        Toast.makeText(context, "页面未找到", Toast.LENGTH_SHORT).show()
    }
}
```

---

## 10. 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|----------|
| 1.0.0 | 2026-05-05 | 源码集成，KSP 处理器替代 kapt |
| - | - | 支持 AGP 8.1.0，Gradle 8.4，Kotlin 1.9.0 |
| - | - | 完整路由类型识别 (Activity/Fragment/Provider/Service) |

---

## 11. 参考链接

- [ARouter 官方文档](https://github.com/alibaba/ARouter)
- [KSP 官方文档](https://kotlinlang.org/docs/ksp-overview.html)
- [KotlinPoet 文档](https://square.github.io/kotlinpoet/)
- [项目文档](../README.md)