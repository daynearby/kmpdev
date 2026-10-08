# Android SDK 升级迁移 Skill

> **所属域**: 知识域 | **加载策略**: 按需加载

> **适用范围**: Android 项目 compileSdk/targetSdk 大版本升级（如 34→36）
>
> **创建日期**: 2026-06-11
>
> **最后更新**: 2026-06-11
>
> **关联 Skill**: [Flutter 路由迁移 Skill](flutter-migration-SKILL.md) — 若项目包含 Flutter 模块

---

## 1. 概述

本 Skill 提供 Android 项目 SDK 大版本升级的标准化流程，涵盖从环境准备到全量编译验证的完整步骤，以及升级过程中常见问题的速查解决方案。

### 1.1 适用场景

| 场景 | 说明 |
|------|------|
| Android SDK 大版本升级 | compileSdk/targetSdk 从旧版本升级到新版本 |
| 工具链同步升级 | AGP / Kotlin / KSP / Gradle 版本配套升级 |
| 依赖库兼容性修复 | AndroidX、第三方 SDK 因 SDK 升级导致的兼容问题 |
| 废弃 API 迁移 | 如 ExoPlayer→Media3、kotlin-android-extensions→ViewBinding |

### 1.2 升级前置条件检查清单

```
□ 项目当前已能正常编译 (BUILD SUCCESSFUL)
□ Git 分支干净，已创建专用升级分支
□ 明确 compileSdk / targetSdk / minSdk 的目标版本
□ 了解 AGP 最低版本要求（SDK 36 要求 AGP ≥ 8.4）
□ 若含 Flutter 模块，确认 Flutter SDK 目标版本及兼容性
```

---

## 2. 标准升级流程（7 步）

```
Step 0: 环境准备（创建分支、确认基线编译通过）
Step 1: 版本管理清理（统一版本变量，消除硬编码）
Step 2: Kotlin + KSP 升级（核心卡点，优先级最高）
Step 3: AndroidX 库升级（配套升级到目标 SDK 兼容版本）
Step 4: SDK 版本切换（compileSdk / targetSdk 正式改为目标版本）
Step 5: 废弃 API 迁移（如 ExoPlayer→Media3 或其他已废弃组件）
Step 6: 全量编译 + 功能验证
Step 7: Flutter 模块适配（若适用，见关联 Skill）
```

### 2.1 每步验证原则

- **每步完成后必须编译验证**，确认 `BUILD SUCCESSFUL`
- 编译出错时，不通过注释代码解决，必须正确定位并修复
- 编译日志归档至 `temp/logs/build_log_YYYYMMDD_HHMMSS.log`

---

## 3. 版本兼容矩阵速查

### 3.1 AGP / Gradle 兼容性

| AGP 版本 | 最低 Gradle | JDK | compileSdk 建议 |
|----------|------------|-----|----------------|
| 8.4.x | 8.7+ | 17 | 34 |
| 8.7.x | 8.7+ | 17 | 35 |
| 8.13.x | 8.13+ | 17 | 36 |

### 3.2 Kotlin / KSP 兼容性

| Kotlin | 最低 KSP 版本 | AGP 建议 |
|--------|-------------|----------|
| 1.9.x | 1.9.x-1.0.x | 8.1-8.4 |
| 2.0.x | 2.0.x-1.0.x | 8.4-8.13 |
| 2.1.x | 2.1.x-1.0.x | 8.7+ |

### 3.3 AndroidX 库最低版本（SDK 36 兼容）

| 库 | SDK 36 最低版本 | 建议版本 |
|-----|----------------|---------|
| core-ktx | 1.15.0 | 1.18.0 |
| activity-ktx | 1.9.0 | 1.13.0 |
| fragment-ktx | 1.7.0 | 1.8.6 |
| lifecycle | 2.7.0 | 2.8.7 |
| navigation | 2.7.0 | 2.8.5 |
| room | 2.6.0 | 2.6.1 |
| appcompat | 1.7.0 | 1.7.1 |
| material | 1.12.0 | 1.13.0 |

---

## 4. 依赖不兼容处理策略

### 4.1 处理原则

当遇到依赖升级后仍然不兼容的情况，按以下优先级处理：

1. **检查版本兼容矩阵**：确认 AGP/Kotlin/KSP 版本配套
2. **升级或替换依赖**：找到兼容目标 SDK 的新版本
3. **寻找替代库**：如果原库不再维护，寻找替代品
4. **版本降级回退**：如果升级无效，尝试中间版本
5. **用户确认**：以上都无效时，列出方案由用户确认

### 4.2 处理流程

```
发现依赖不兼容
    │
    ├─ 有兼容版本？ ──是──→ 升级到兼容版本 → 编译验证
    │
    └─ 无兼容版本？
        │
        ├─ 有替代库？ ──是──→ 评估迁移成本 → 与用户确认 → 执行
        │
        └─ 无替代库？
            │
            └─ 列出方案（降级/暂留/临时绕过）→ 与用户确认 → 执行
```

### 4.3 常见冲突案例

#### 案例 1：AGP 升级导致 Gradle 插件不兼容

```
现象: 特定插件不支持 AGP 新版本
处理: 暂时注释/禁用该插件，与用户确认后续方案
```

#### 案例 2：Compose Runtime 版本冲突

```
现象: Flutter 模块使用 Compose 1.7.1，主项目使用 1.8.0
处理: 使用 resolutionStrategy 强制统一版本
      configurations.all {
          resolutionStrategy.eachDependency {
              if (requested.group == 'org.jetbrains.compose.runtime') {
                  useVersion '1.8.0'
              }
          }
      }
```

#### 案例 3：minSdk 被动升级

```
现象: Flutter 3.44 要求 minSdk ≥ 24，原项目 minSdk = 23
处理: 升级 minSdk 到 24，检查是否有 API 兼容性问题
```

---

## 5. 常见 API 迁移速查

### 5.1 ExoPlayer 2.x → Media3 1.5.x

| 类别 | ExoPlayer 2.x | Media3 1.5.x |
|------|--------------|-------------|
| 根包 | `com.google.android.exoplayer2` | `androidx.media3` |
| 通用类 | `.C`, `.Format`, `.MediaItem` | `.common.C`, `.common.Format`, `.common.MediaItem` |
| 播放器 | `SimpleExoPlayer` | `ExoPlayer` |
| 监听 | `Player.EventListener` | `Player.Listener` |
| 异常 | `ExoPlaybackException` | `PlaybackException`（`type` → `errorCode`） |
| 数据源 | `DefaultDataSourceFactory` | `DefaultDataSource.Factory` |
| HTTP数据源 | `DefaultHttpDataSourceFactory` | `DefaultHttpDataSource.Factory`（Builder 模式） |
| 数据库 | `ExoDatabaseProvider` | `StandaloneDatabaseProvider` |
| 视频监听 | `VideoListener`（独立接口） | 合并入 `Player.Listener` |
| 轨道变更 | `onTracksChanged(TrackGroupArray, TrackSelectionArray)` | `onTracksChanged(Tracks)` |
| Dash | `.source.dash.DashMediaSource` | `.exoplayer.dash.DashMediaSource` |
| HLS | `.source.hls.HlsMediaSource` | `.exoplayer.hls.HlsMediaSource` |
| SmoothStreaming | `.source.smoothstreaming.SsMediaSource` | **已移除**（回退至 Progressive） |
| 字幕 | `MediaItem.Subtitle` | `MediaItem.SubtitleConfiguration`（Builder 模式） |

### 5.2 Kotlin 1.9 → 2.0

| 变更 | 说明 |
|------|------|
| `kotlin-android-extensions` 移除 | 必须改用 ViewBinding |
| `@Suppress` 注解名变更 | 部分 Suppress 名称已更改，删除或更新 |
| 协程库版本要求 | 需升级 kotlinx.coroutines 至 1.8.0+ |
| 编译器参数废弃 | 删除 kotlinOptions 中的废弃参数 |

### 5.3 Room 2.4 → 2.6

| 变更 | 说明 |
|------|------|
| kapt → KSP | 改用 `ksp "androidx.room:room-compiler"` |
| Schema 导出 | 确认 schema 目录配置正确 |

---

## 6. Flutter 混合项目特别注意事项

> **详细 Flutter 迁移指引请参阅**：[Flutter 路由迁移 Skill](flutter-migration-SKILL.md)

### 6.1 Flutter SDK 版本要求

| Flutter 版本 | Dart | 最低 AGP | Android minSdk |
|-------------|------|---------|---------------|
| 3.19.6 | 3.3.4 | 8.1 | 21 |
| 3.44.1 | 3.12.1 | 8.7 | 24 |

### 6.2 常见 Flutter 插件兼容问题

| 插件 | 问题 | 解决方案 |
|------|------|---------|
| fluttertoast 8.2.8+ | 使用 Gradle Version Catalog | 降级至 8.2.2 或手动修复 |
| shared_preferences_android | compileSdk 使用 flutter 变量失败 | 手动改为 `compileSdk = 36` |
| url_launcher_android | 同上 | 手动改为 `compileSdk = 36` |
| thinking_analytics 2.x | 使用已移除的 PluginRegistry.Registrar | 升级到 3.0+ |

### 6.3 Pub 缓存手动修复注意事项

```
- flutter_boost / fluttertoast 等插件在 pub 缓存中手动修改后
- 重新执行 flutter pub get 会覆盖修改
- 建议记录所有手动修改，使用 dependency_overrides 固定版本
- 优先升级插件到官方兼容版本
```

---

## 7. 回滚方案

### 7.1 分步回滚（推荐）

不执行全量回滚，而是按步骤逐个回退：

```powershell
# Step 1-2 回退:
git restore versions.gradle build.gradle

# Step 3 回退:
git restore versions.gradle

# Step 4 回退:
# 将 compileSdk/targetSdk 改回旧版本

# Step 5 回退:
git restore lib_exoplayer/ lib_video/
```

### 7.2 整体回滚

```powershell
git checkout master && git branch -D upgrade/sdk
```

### 7.3 回滚判定标准

| 情况 | 行动 |
|------|------|
| 单个步骤超过 50 个编译错误 | 分析前 5 个，可能根源是同一依赖冲突 |
| 同一文件多次修改仍有新错误 | 考虑升级/降级库版本，而非改代码 |
| 核心库完全不兼容且无替代方案 | 暂停升级，与用户确认是否接受功能降级 |

---

## 8. 案例实录（SDK 34→36 升级参考）

> **说明**: 以下案例来自一个 Android + Flutter 混合项目的实际升级过程，供参考。

### 8.1 项目背景

- **项目**: Android + Flutter 混合应用
- **升级路径**: compileSdk 34→36, targetSdk 34→36, minSdk 23→24
- **工具链**: AGP 8.4→8.13.2, Kotlin 1.9.0→2.0.21, KSP 1.9.0→2.0.21-1.0.28
- **Flutter**: 3.19.6→3.44.1 (Dart 3.3.4→3.12.1)

### 8.2 完整修改文件清单

| 文件 | 操作 | 所属 Step |
|------|------|----------|
| `versions.gradle` | 修改 | Step 1/2/3/4/5 |
| `app/build.gradle` | 修改 | Step 1/3 |
| `arouter-ksp/build.gradle` | 修改 | Step 2 |
| `lib_exoplayer/build.gradle` | 修改 | Step 5 |
| `lib_exoplayer/.../ExoMediaPlayer.java` | 重写 | Step 5 |
| `module_flutter/pubspec.yaml` | 修改 | Step 7 |
| `module_flutter/lib/main.dart` | 修改 | Step 7 |
| `module_flutter/lib/MainApp.dart` | 修改 | Step 7 |
| `module_flutter/.android/build.gradle` | 修改 | Step 7 |
| `gradle/libs.versions.toml` | 新建 | Step 7 |

### 8.3 升级总结

| 指标 | 数值 |
|------|------|
| 修改文件总数 | ~15 个 |
| 编译迭代次数 | 3 次（1 次基线验证 + 1 次错误修复 + 1 次成功） |
| 需要与用户确认的决策 | 4 个 (thinkingdata 禁用、minSdk 升级、Compose 冲突、flutter_boost 移除) |
| 最终编译状态 | ✅ BUILD SUCCESSFUL |

---

## 9. 版本历史

| 日期 | 版本 | 变更内容 |
|------|------|----------|
| 2026-06-11 | 1.0 | 初始版本，基于实际 SDK 升级案例创建 |