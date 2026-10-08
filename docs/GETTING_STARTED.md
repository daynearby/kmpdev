# RT 新人上手指南

> **所属域**: 运维域 | **加载策略**: 按需加载
> **版本**: 1.0.0 | **创建日期**: 2026-08-06

RT 是一个 Kotlin Multiplatform（KMP）跨平台应用，覆盖 Android 与 iOS。本文档面向新成员，从零说明环境准备、项目结构、构建运行与常见操作。

## 一、环境要求

| 环境 | 版本要求 | 说明 |
|------|---------|------|
| JDK | **17+（推荐 21）** | 构建需要；无 JDK 时安装 Android Studio 自带的 JetBrains Runtime（JBR 17+）即可。`:core` / `:shared` 的 JVM target 为 21，`:androidApp` 为 11，统一使用 JDK 21 兼容运行 |
| Android SDK | compileSdk 37、minSdk 24、targetSdk 36 | 通过 Android Studio SDK Manager 安装 `platforms;android-37` 及 build-tools |
| Android Studio | 最新稳定版（含 AGP 9.x 兼容支持） | 官方下载：<https://developer.android.com/studio> |
| Gradle | 9.4.1（项目 wrapper，无需手动安装） | 直接使用 `gradlew` / `gradlew.bat` |
| Xcode + macOS | iOS 构建需要 | 仅 `:iosApp` / iOS target 需要，Windows 上自动跳过（`kotlin.native.ignoreDisabledTargets=true`） |

> **无 JDK 时**：安装 Android Studio（自带 JBR 21），并在 `File > Project Structure > SDK Location` 确认 JDK 指向 JBR 即可，无需单独安装 JDK。

## 二、快速上手流程

```
git clone <仓库地址>
  → 用 Android Studio 打开项目根目录
  → 等待 Gradle Sync 完成（首次会下载依赖，需联网）
  → 选择 :androidApp 配置运行到模拟器/真机
```

1. **克隆**：`git clone <仓库URL> && cd RT`
2. **打开**：Android Studio 打开项目根目录（含 `settings.gradle.kts` 的目录），IDE 会自动识别为 Gradle 工程并触发 Sync。
3. **同步**：Sync 成功后，在 Run Configuration 选择 `androidApp`，点击 Run 部署到 Android 设备。
4. **命令行构建**（可选）：在项目根目录执行 `.\gradlew :androidApp:assembleDebug`，APK 输出到 `androidApp/build/outputs/apk/debug/`。

## 三、项目结构概览

```
RT/
├── androidApp/          # Android 壳入口（applicationId: com.example.rt）
├── app/                 # KMP 业务装配，Compose App 根组件与路由
├── core/                # KMP 基础层：Ktor/Ktorfit、SQLDelight、MMKV、Koin、XLog
├── shared/              # KMP 共享 domain
├── feature/
│   ├── home/            # 首页业务功能
│   └── user/            # 用户业务功能
├── iosApp/              # iOS 壳（Xcode 工程，引用 KMP framework）
├── gradle/
│   └── libs.versions.toml  # 依赖版本统一管理
├── docs/                # 项目文档（见下方导航）
├── build.gradle.kts     # 根构建脚本（插件声明）
├── settings.gradle.kts  # 模块注册
└── gradlew / gradlew.bat  # Gradle wrapper
```

各 KMP 模块源码目录约定：`commonMain`（共享逻辑）、`androidMain`（Android 实现）、`iosMain`（iOS 实现）、`commonTest`（共享测试）。

## 四、常见命令

| 命令 | 说明 |
|------|------|
| `.\gradlew :androidApp:assembleDebug` | 构建 Debug APK |
| `.\gradlew :androidApp:installDebug` | 构建并安装到设备 |
| `.\gradlew :shared:testDebugUnitTest` | 运行共享模块单元测试 |
| `.\gradlew clean` | 清理构建产物 |
| `.\gradlew :androidApp:dependencies` | 查看依赖树 |

> Windows 使用 `.\gradlew`，macOS / Linux 使用 `./gradlew`。命令在项目根目录执行。

## 五、如何新增模块

1. **新建目录**：按 `feature/<name>` 或顶层模块命名规范创建模块目录（如 `feature/order`），命名使用 kebab-case。
2. **注册模块**：在 `settings.gradle.kts` 添加 `include(":feature:order")`。
3. **创建 `build.gradle.kts`**：参考 `feature/home/build.gradle.kts` 复制 KMP 配置（android + ios 目标、sourceSets、SQLDelight/Compose 插件按需声明）。
4. **依赖版本**：新增第三方依赖统一在 `gradle/libs.versions.toml` 中声明（`[versions]` + `[libraries]`），模块内使用 `libs.xxx` 引用。
5. **装配**：在 `:app` 的 commonMain 依赖中加入 `implementation(projects.feature.order)` 并按路由注册页面。
6. **文档**：按 [README.md](README.md) 中的文档规范补充模块 `DOMAIN.md`（待模板）。

## 六、相关文档导航

- [README.md](README.md)：文档总索引（5 域导航）
- [BUILD_CONFIG.md](BUILD_CONFIG.md)：构建配置、依赖版本、构建命令
- [reference-resource-documents.md](reference-resource-documents.md)：官方参考资源链接
- [AGENT_RULES.md](AGENT_RULES.md)：代码修改 / 构建 / Git 操作规范
- 技能文档：`docs/skills/*.md`（Kotlin / Compose / Navigation / Gradle 构建等），完整索引见 [../androidAgentRulev2/SKILLS_MAP.md](../androidAgentRulev2/SKILLS_MAP.md)
- 根目录 [AGENTS.md](../AGENTS.md)：Agent 配置与文档导航
