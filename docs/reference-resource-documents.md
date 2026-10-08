# RT 参考资源文档

> **所属域**: 知识域 | **加载策略**: 按需加载
> **版本**: 1.0.0 | **创建日期**: 2026-08-06

RT 项目涉及的 Kotlin Multiplatform 生态官方文档链接汇总，按技术栈分组。仅收录官方维护的地址，遇到问题时优先查阅。

## 一、Kotlin Multiplatform 基础

- Kotlin Multiplatform 官方文档：<https://kotlinlang.org/docs/multiplatform.html>
- KMP 项目结构（target / source set 层级）：<https://kotlinlang.org/docs/multiplatform-discover-project.html>
- 源码集层次结构：<https://kotlinlang.org/docs/multiplatform-hierarchy.html>
- 原生二进制 / framework 构建：<https://kotlinlang.org/docs/multiplatform-build-native-binaries.html>
- Kotlin 序列化（kotlinx.serialization）：<https://kotlinlang.org/docs/serialization.html>
- 协程指南（kotlinx.coroutines）：<https://kotlinlang.org/docs/coroutines-guide.html>

## 二、Compose Multiplatform 与 UI

- Compose Multiplatform 官网：<https://www.jetbrains.com/compose-multiplatform/>
- Compose Multiplatform 资源（compose.resources）使用：<https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-resources-usage.html>
- Compose Navigation（Navigation3 / 多平台导航）：<https://github.com/JetBrains/compose-multiplatform/tree/master/components/Navigation>
- Material Design 3：<https://m3.material.io/>

## 三、网络层

- Ktor 官方文档：<https://ktor.io/>
- Ktor 客户端引擎（OkHttp / Darwin）：<https://ktor.io/docs/client-engines.html>
- Ktorfit（Ktor 上的 Retrofit 风格）官方文档：<https://foso.github.io/Ktorfit/>

## 四、数据层

- SQLDelight 官方文档：<https://cashapp.github.io/sqldelight/>
- SQLDelight Gradle 插件配置：<https://cashapp.github.io/sqldelight/2.1.0/multiplatform_sqlite/>
- MMKV（腾讯）GitHub：<https://github.com/Tencent/MMKV>
- MMKV-KMP（KMP 封装）GitHub：<https://github.com/ctripcorp/MMKV-KMP>
- Coil 3 官方文档：<https://coil-kt.github.io/coil/>

## 五、依赖注入与日志

- Koin 官方文档：<https://insert-koin.io/>
- Koin Compose 集成：<https://insert-koin.io/docs/reference/koin-compose/overview/>
- Kermit（KMP 日志）GitHub：<https://github.com/touchlab/Kermit>
- kmp-xlog（基于腾讯 XLog 的 KMP 日志）GitHub：<https://github.com/piasy/kmp-xlog>

## 六、Android 构建

- Android Gradle Plugin（AGP）官方文档：<https://developer.android.com/build>
- Gradle 官方文档：<https://docs.gradle.org/>
- Android Studio 下载：<https://developer.android.com/studio>
- Android KMP（com.android.kotlin.multiplatform.library）说明：<https://developer.android.com/kotlin/multiplatform/migrate>

## 七、测试

- Kotest 官方文档：<https://kotest.io/>
- Mockative 官方文档：<https://mockative.martinbonnin.com/>

> 版本对应关系可在 [BUILD_CONFIG.md](BUILD_CONFIG.md) 的「框架依赖检测表」中查询。
