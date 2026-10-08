# kmpdev

一个Kotlin-Multiplatform项目，支持android、iOS，后面还会支持鸿蒙（快手已经在上面实践了），一套代码多端用，并且性能跟原生差不多，只有类型转换的一小开销，作为跨平台方向更加合适。

另外腾讯的Kuikly是基于Kotlin MultiPlatform(KMP)构建的跨端开发框架，面向客户端开发的全新跨端解决方案， 可以使用 Kotlin 原生开发语言创建Android、iOS、鸿蒙、Web、小程序应用。

## 项目方向

 方案1，全部UI使用Compose，业务逻辑、基础功能使用 Kotlin Multiplatform，（:core + :shared + 业务:feature:*模块 + :app + :androidApp / :iosApp）
 方案2，UI使用各平台控件，这样可以保持对应平台的UI特性，业务逻辑、基础功能使用 Kotlin Multiplatform，（:core + :shared + 业务:feature:* 模块 +  android UI / iOS UI 直接调用）
> 现在先试试方案1，尝试完再尝试方案2。
> 方案1 compose UI在iOS上渲染也还行，但毕竟是Skia 引擎确实不如原生，质感不好，截图到去对比就比较明显。
> 方案2可以保持对应平台原生UI特性，似乎这个更加理想。

### 为什么不是用Flutter？

用过Flutter，在复杂交互的UI上面不够满意，只能做一些简单交互的UI，并且帧率不够稳定，连60帧都稳定不了（之前还有个bug，120帧的手机也只能跑60帧，原来是Flutter framework 固定了60fps，好像最近2年才解决）

### KMP vs Flutter (共享逻辑) 性能对比

| 对比维度 | KMP (Kotlin Multiplatform) | Flutter |
| :--- | :--- | :--- |
| **实现原理** | **编译时多态**：`expect/actual` 在编译期将共享逻辑分别编译为各平台的**原生机器码**（iOS **上直接运行在系统上，而非 JVM**）或**字节码**（Android）。 | **自带渲染引擎**：共享的Dart代码运行在Dart VM上，UI通过**Skia 引擎**直接绘制，不依赖原生组件。 |
| **UI 层性能** | **原生级别**：如果使用原生UI组件（如 UIKit/SwiftUI），性能与纯原生应用无异。 | **高性能自绘**：渲染性能优秀且稳定，但在复杂交互或动画场景下，可能略逊于直接调用原生API的KMP。 |
| **逻辑层性能** | **极高**：直接执行原生机器码，**无额外运行时开销**。 | **良好**：在Dart VM上运行，但无桥接（Bridge）开销。 |
| **启动性能** | **快**：接近原生应用。 | **相对较慢**：需要初始化Dart VM，导致启动时间较长，比原生慢。 |
| **内存与CPU效率** | **更优**：最终编译为对应指令集的机器码（LLVM 技术），只有一些类型转换的开销。| **相对较高**：因自带渲染引擎和Dart VM，内存和CPU占用通常高于原生和 KMP，在多引擎模式下内存占用会更高。 |
| **包体积** | **更小**：增加Kotlin代码和运行时库，体积增量小。 | **更大**：必须打包整个Dart引擎，android单指令集打包会增加3MB，并且还在增加。 |
| **电池效率** | **更优**：由于资源消耗更低，通常更省电。 | **相比之下**：更高的CPU和内存占用，会更耗电、发热明显。 |

> 但是世事无绝对，选择哪种方案取决于具体场景和目标（当然是基于共用一套代码的原则）：
>
> * 如果目标是**极致的性能、原生体验和资源效率**，并且希望尽可能复用现有的 Kotlin/Android 技能，**KMP + 原生 UI** 是更优的选择。iOS也开始搞**Swift共享逻辑 + 原生UI**。
> * 如果目标是**快速开发和线上验证、统一的 UI 外观**，**Flutter** 可以更高效，依赖库的基础功能不符合预期还是要自己改，flutter的第三方库都比较基础，没有经过大范围验证，基本都是会自己本地维护一套，甚至重写；并且时间长了各种库停止维护，依赖库替换升级还是要花不少时间。

## 项目架构

采用 **MVVM（Model-View-ViewModel）+ Clean Architecture** 组合设计模式：

| 模式 | 发挥作用 | 层面 |
|:---|:---|:---|
| **MVVM** | UI 与业务逻辑解耦，状态驱动视图更新 | UI Layer ↔ Domain Layer |
| **Clean Architecture** | 分层隔离，依赖规则（外层依赖内层） | Domain ↔ Data ↔ Platform |
| **Repository Pattern** | 数据源抽象，统一数据访问入口 | Domain ↔ Data |
| **UDF（单向数据流）** | 数据只能单向流动，状态变化可预测 | 跨所有层 |

### 方案1的结构

```
:androidApp / :iosApp        ← 壳工程（仅初始化 + 调用 App()）
         │
         ▼
       :app                   ← 总装模块：根 NavHost + Koin 注入聚合 + App() 入口
         │
    ┌────┼────┐
    ▼    ▼    ▼
:feature:*  …  …              ← 业务功能模块，依赖 :shared
         │
         ▼
      :shared                 ← 业务共享：Repository接口 + 导航抽象 + 公共路由 + UI 基类
         │
         ▼
       :core                  ← 基础设施：日志/数据库/缓存/网络/策略，纯kmp。
```

### 方案2的结构

```
android原生 / iOS原生        ←  使用原生UI、路由、图片加载
         │
         ▼
    ┌────┼────┐
    ▼    ▼    ▼
:feature:*  …  …              ← 业务功能模块，Koin 注入聚合，提供数据监听、更新接口，依赖 :shared
         │
         ▼
      :shared                 ← 业务共享：Repository接口、抽象接口
         │
         ▼
       :core                  ← 基础设施：日志/数据库/缓存/网络/策略，纯kmp。
```

## 技术选型

| 分类 | 方案 | 版本 | 选型理由 |
|:---|:---|:---|:---|
| **构建工具** | Gradle | 9.1.0+ |9.0破坏性更新，这又关联kotlin、android studio、apg版本，窒息 |
| **AGP** | Android Gradle Plugin | 9.0.1 | 要兼容新的安卓版本，（android16->APG8.13.0,android17->AGP9.1.1） |
| **UI 框架** | Compose Multiplatform | 1.11.1 | UI完全共享，Kotlin UI |
| **DI** | Koin | 4.0.0 | KMP生态最成熟的DI框架 |
| **日志门面** | Kermit | 2.0.4 | Touchlab 出品，社区成熟，默认对接各平台原生日志 |
| **本地日志存储** | kmp-xlog | 1.5.0 | 基于腾讯Mars XLog封装 |
| **数据库** | SQLDelight | 2.0.2 | 成熟框架，现在没用，或许后面会等ROOM成熟 |
| **KV 缓存** | MMKV-Kotlin | 1.3.2 | 携程基于微信的mmkv封装的门面，还需要引入mmkv的依赖 |
| **安全存储** | 自己实现（通过expect/actual） | — | AndroidKeyStore / Keychain |
| **网络请求** | Ktor Client + Ktorfit | 3.5.1 / 2.7.5 | Ktor 底层引擎 + Ktorfit 类型安全注解包装（Retrofit 风格），KSP 代码生成 |
| **序列化** | kotlinx.serialization | 1.7.3 | 与 Ktor 3.5.1 兼容，多平台支持 |
| **协程** | kotlinx.coroutines | 1.11.0 | 2026年5月最新稳定版，多平台支持 |
| **图片加载** | Coil 3 | 3.4.0 | 原生支持KMP，Compose集成良好 |
| **导航** | Navigation Compose (KMP) | 跟随 CMP 版本 | org.jetbrains.androidx.navigation:navigation-compose，类型安全路由 |
| **测试** | Kotest + mockative | 5.9 / 1.5 | 先写着 |
| **错误上报** | Bugly /（海外） Sentry | 最新版 | 先写着 |

### 网络缓存与token刷新

协程 + Ktor(android okhttp,iOS NSURLSession) + Ktorfit(包装) + MMKV本地缓存

发成请求流程 
```
UseCase → CacheThenNetworkStrategy.execute(policy, cacheKey, ttl, fetcher)
│
├─ policy == NO_CACHE                     ← 登录/支付/下单等
│   └─ AuthRetryHandler.execute {         ← ★ 同样包裹 401 重试
│           fetcher()                     ← 不走缓存，直接网络
│       }
│       ├─ 成功 → emit FromNetwork
│       ├─ AuthException → return（loginRequired 已触发弹窗）
│       └─ 其他失败 → classifyError → emit Error
│
└─ policy == CACHE_THEN_NETWORK           ← 用户资料/帖子列表/banner 等
    │
    ├─ Step 1: 查缓存
    │   └─ cacheManager.getRawJson(key)
    │       ├─ 命中 → emit FromCache（UI 立即展示）
    │       └─ 未命中 → 无操作
    │
    ├─ Step 2: inFlight 单个相同地址请求控制
    │   └─ 已有同 key 请求请求中 → 跳过
    │
    ├─ Step 3: 网络请求
    │   └─ CacheWriteConfig.wrap {
    │           AuthRetryHandler.execute {     ← ★ 401 自动重试包裹（core 内）
    │               fetcher()                  ← Ktorfit 原始调用
    │           }
    │       }
    │       │
    │       │   → HttpAuthInterceptor.onRequest 附加 Bearer Token
    │       │   → Ktorfit → HttpClient
    │       │     ├─ HttpRequestRetry（5xx/超时 指数退避，max 3次）
    │       │     ├─ HttpCachePlugin.onResponse（透明写缓存）
    │       │     └─ 响应
    │       │         ├─ 成功 → emit FromNetwork
    │       │         │         └─ HttpCachePlugin 透明写入 MMKV 缓存
    │       │         │
    │       │         └─ 401 → AuthRetryHandler 捕获
    │       │             ├─ tokenManager.clearAccessToken()
    │       │             ├─ TokenRefreshManager.getValidAccessToken()
    │       │             │   └─ Mutex.withLock {              ← 并发 401 互斥
    │       │             │         doubleCheck → 已有新 token? → return
    │       │             │         TokenRefresher.refresh()    ← :feature:user 实现
    │       │             │         ├─ 成功 → saveTokens
    │       │             │         └─ 失败 → clearAccessToken()
    │       │             │       }
    │       │             ├─ 刷新成功 → 重试 fetcher() ✓         ← ★ 重试原始请求
    │       │             │   └─ HttpCachePlugin 写入新响应缓存
    │       │             └─ 刷新失败 → AuthCoordinator.notifyAuthExpired()
    │       │                 └─ loginRequired = true → App() 弹登录对话框
    │       │
    │       └─ 失败（非 401）
    │           ├─ 有缓存 → emit NetworkErrorWithCache（Toast 提示）
    │           └─ 无缓存 → emit Error（ErrorBanner + 重试按钮）
    │
    └─ Step 4: 清除 inFlight[key]
```

>
> 在透明写缓存中用到了协程的上下文进行传递参数

```
/**
 *   - `companion object Key` 是标准协程上下文 Key 模式（参考 `CoroutineName`），
 *     Key 本身是类型标识，不需要"赋值"。元素通过 `withContext(this)` 注入，
 *     插件通过 `coroutineContext[CacheWriteConfig.Key]` 读取。
 *   - `withContext` 每次创建独立的作用域，多个并发的 `wrap()` 互不干扰。
 *   - `AbstractCoroutineContextElement(CacheWriteConfig)` 中的 `CacheWriteConfig`
 *     是 companion object 引用（即 `CacheWriteConfig.Key`），这是 Kotlin 标准写法。
 */
class CacheWriteConfig(
    val cacheKey: String,
    val ttlSeconds: Long
) : AbstractCoroutineContextElement(CacheWriteConfig) {
    companion object Key : CoroutineContext.Key<CacheWriteConfig>

    suspend fun <T> wrap(block: suspend () -> T): T =
        withContext(this) { block() }
}


// 在需要用的地方进行获取，这样就能获取到CacheWriteConfig对象
  val config = currentCoroutineContext()[CacheWriteConfig.Key]
  
```


## 进度

> iOS没搞，mac mini太旧，新版涨价离谱，后面买了再验证
>

| 模块 | 验证项 | 验证方式 | 预期结果 |状态|
|:---|:---|:---|:---|:---|
| **日志** | Kermit 输出 | Android 模拟器运行，查看 Logcat | 看到 Kermit 输出的日志 |✅|
| | kmp-xlog 持久化 | 检查日志文件目录 | 日志文件生成成功 |✅|
| **数据库** | SQLDelight 表创建 | 运行初始化代码 | 数据库文件生成，表结构正确 ||
| | 数据 CRUD | 调用 Repository API | 增删改查正常 ||
| **KV 缓存** | MMKV 读写 | 调用 CacheManager API | 数据正确读写 |✅|
| | 持久化验证 | 重启应用后读取 | 数据依然存在 |⏳|
| **安全存储** | 自封装安全存储 | 调用 SecureStorage API | 敏感数据加密存储 |✅|
| **网络** | Ktor 请求 | 调用 HttpClient | 请求成功，响应正确 |✅|
| | API 统一封装 | 模拟网络异常 | 正确返回 Error 状态 |⏳|
| **导航** | Navigation Compose KMP 类型安全 | 编译检查路由 | 编译期类型检查通过 |⏳|
| | 拦截器鉴权 | 未登录点击需登录页面 | 自动重定向到登录页 |⏳|
