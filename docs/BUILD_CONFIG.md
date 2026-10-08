# RT 构建配置文档

> **所属域**: 规范域 | **加载策略**: 按需加载
> **版本**: 1.0.0 | **创建日期**: 2026-08-06

RT 为 Kotlin Multiplatform 跨平台应用（Android + iOS），本文档基于根目录 `build.gradle.kts`、`settings.gradle.kts`、`gradle/libs.versions.toml`、`gradle.properties` 与各模块 `build.gradle.kts` 整理。

## 一、构建环境概览

| 项 | 值 |
|----|----|
| 项目名 | RT（`rootProject.name = "RT"`） |
| Gradle | 9.4.1（wrapper，见 `gradle/wrapper/gradle-wrapper.properties`） |
| AGP | 9.2.1 |
| Kotlin | 2.4.0 |
| KSP | 2.3.9 |
| compose 编译器插件 | 随 Kotlin 2.4.0（`org.jetbrains.kotlin.plugin.compose`） |
| Android compileSdk | 37（`libs.versions.android.compileSdk`）；`:app` Android 目标使用 `release(36) { minorApiLevel = 1 }` |
| Android minSdk | 24 |
| Android targetSdk | 36 |
| JVM target | `:androidApp` = JVM_11；`:core` / `:shared` = JVM_21 |
| 仓库 | google（androidx/android/com.google）、mavenCentral、gradlePluginPortal、jitpack |
| 构建缓存 | 已开启：`org.gradle.configuration-cache=true`、`org.gradle.caching=true` |
| JVM 参数 | `org.gradle.jvmargs=-Xmx4096M -Dfile.encoding=UTF-8`、`kotlin.daemon.jvmargs=-Xmx3072M` |

### Gradle 特性

- 启用 `TYPESAFE_PROJECT_ACCESSORS`，模块间依赖使用类型安全访问器（如 `projects.core`）。
- `gradle.properties` 中 `android.nonTransitiveRClass=true`、`android.useAndroidX=true`。
- `kotlin.native.ignoreDisabledTargets=true`（Windows 上跳过 iOS 模拟器 target 编译）。

## 二、模块架构

| 模块 | 类型 | 职责 | 关键构建配置 |
|------|------|------|------|
| `:androidApp` | Android Application | Android 壳入口，applicationId `com.example.rt`，装配各业务模块 | AGP 9 + Compose Multiplatform；`compileSdk` 37；minSdk 24 / targetSdk 36；JVM target 11；`versionCode 1`、`versionName "1.0"`；release 未启用 minify |
| `:app` | KMP Android Library（AGP `com.android.kotlin.multiplatform.library`） | 业务装配，含 Compose App 根组件与路由 | 目标：`android`（compose 资源 `androidResources.enable = true`）、`iosArm64` / `iosSimulatorArm64`（framework `appKit`）；CocoaPods 与 iosX64 已注释（需 macOS） |
| `:core` | KMP | 基础层：Ktor 网络 / Ktorfit、SQLDelight、MMKV、Koin、XLog 日志等 | JVM target 21；SQLDelight 插件生成 |
| `:shared` | KMP | 共享 domain 层 | JVM target 21；含 commonTest / iosTest |
| `:feature:home` | KMP | 首页业务功能 | compileSdk 使用 `libs.versions.android.compileSdk` |
| `:feature:user` | KMP | 用户业务功能 | 同上 |
| `:iosApp` | Xcode 工程 | iOS 壳（`iosApp/iosApp.xcodeproj`），引用 KMP framework | 非 Gradle 模块，需 macOS + Xcode |

依赖关系（数据流方向）：`:androidApp` → `:app` → `:feature:home` / `:shared`；`:app` → `:core` / `:shared`；业务模块 → `:core`。

## 三、框架依赖检测表

以下依赖均在 `gradle/libs.versions.toml` 中声明，且被对应模块实际引用：

| 框架 | 版本 | 使用状态 | 说明 |
|------|------|---------|------|
| Compose Multiplatform | 1.11.1 | 已使用 | `:app` / `:androidApp` 启用 compose 插件 |
| Material3 | 1.11.0-alpha07 | 已使用 | `compose.material3` |
| Navigation Compose（KMP） | 2.9.1 | 已使用 | `org.jetbrains.androidx.navigation:navigation-compose` |
| Ktor | 3.5.1 | 已使用 | `:core`，含 okhttp / darwin 引擎、content-negotiation、logging |
| Ktorfit | 2.7.5 | 已使用 | `:core`，KSP 代码生成 |
| SQLDelight | 2.1.0 | 已使用 | `:core`，android-driver / native-driver，`.sq` 文件生成 |
| MMKV | 2.4.0 / mmkv-kotlin 1.3.2 | 已使用 | `:core` 缓存层 |
| Koin | 4.0.0 | 已使用 | `:core` / `:app` / `:androidApp`（koin-core / koin-compose / koin-android） |
| Kermit | 2.0.4 | 已使用 | `:core` 日志 |
| kmp-xlog | 1.5.0 | 已使用 | `:core` 日志 |
| Coil 3 | 3.4.0 | 已使用 | coil-compose + coil-network-okhttp |
| kotlinx-serialization | 1.7.3 | 已使用 | 序列化插件已应用 |
| kotlinx-coroutines | 1.11.0 | 已使用 | core / android |
| AndroidX Lifecycle（KMP） | 2.9.0 | 已使用 | `lifecycle-viewmodel-compose` / `lifecycle-runtime-compose` |
| Kotest + Mockative | 5.9.0 / 1.5.0 | 已使用 | `:shared` 测试 |

> 注：`androidx.lifecycle:lifecycle-runtime-compose/ktx`（2.11.0-beta01）仅供 `:androidApp` 壳使用，commonMain 不引用。

## 四、构建命令

在项目根目录执行（Windows 用 `.\gradlew`，macOS / Linux 用 `./gradlew`）：

| 命令 | 说明 |
|------|------|
| `.\gradlew :androidApp:assembleDebug` | 构建 Android Debug APK（主应用） |
| `.\gradlew :androidApp:assembleRelease` | 构建 Android Release APK |
| `.\gradlew :androidApp:installDebug` | 安装 Debug 包到已连接设备 |
| `.\gradlew :shared:allTests` / `:shared:testDebugUnitTest` | 运行共享模块测试 |
| `.\gradlew clean` | 清理构建产物 |
| `.\gradlew :androidApp:dependencies` | 查看依赖树（排查依赖冲突） |
| `.\gradlew tasks` | 列出可用任务 |

首次构建需先执行 Gradle Sync（Android Studio 自动触发），或 `.\gradlew --version` 校验 wrapper。

## 五、签名配置

- 当前 `androidApp/build.gradle.kts` **未配置自定义 signingConfigs**，release 构建使用默认调试签名。
- `release` buildType 仅关闭 minify（`isMinifyEnabled = false`），未启用 R8 混淆。
- 发布正式包前需在 `androidApp` 模块补充 `signingConfigs`（storeFile / storePassword / keyAlias / keyPassword）与 `versionCode` / `versionName` 升级。

## 六、版本历史

| 版本 | 变更 | 提交 |
|------|------|------|
| 1.0.0 | 项目初始化：KMP 骨架、模块划分、核心依赖接入 | `cfd5e50` / `9b25774` |
| 1.0.0 | 修复 Koin 依赖初始化遗漏 | `cdbfab3` |

依赖版本号集中管理于 `gradle/libs.versions.toml` 的 `[versions]`，升级统一在此修改并同步各模块引用。
