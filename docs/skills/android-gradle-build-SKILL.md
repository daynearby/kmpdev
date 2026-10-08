# Android Gradle 编译技能文档

> **所属域**: 知识域 | **加载策略**: 按需加载

## 概述

本文档总结 Android Gradle 编译的核心知识，包括版本依赖关系、插件应用机制、注解处理器（KSP/KAPT/APT）、多模块配置及编译顺序等。适用于 Android 项目的日常开发和问题排查。

---

## 1. Gradle 版本依赖体系

### 1.1 三层版本关系

```
┌─────────────────────────────────────────────────────────┐
│                    构建工具链                             │
│                                                         │
│  ┌───────────┐    ┌───────────┐    ┌───────────────┐   │
│  │  Gradle   │───>│    AGP    │───>│   Kotlin/GSP  │   │
│  │  Wrapper  │    │  Plugin   │    │    Plugin     │   │
│  └───────────┘    └───────────┘    └───────────────┘   │
│       │                │                   │            │
│   gradle-8.5      gradle:8.1.1       ksp:1.9.0-1.0.13  │
│   wrapper.jar                      kotlin-gradle:1.9.0 │
└─────────────────────────────────────────────────────────┘
```

### 1.2 版本兼容性矩阵

| Gradle | AGP       | Kotlin    | KSP              | 说明               |
|--------|-----------|-----------|------------------|--------------------|
| 8.0+   | 8.0-8.2   | 1.8.0+     | 1.8.x-1.9.x      | 常见配置           |
| 8.5    | 8.1.x     | 1.9.0      | 1.9.0-1.0.13     | 示例版本           |
| 7.5    | 7.4.x     | 1.7.x      | 1.7.x            | 旧版本             |

**关键规则**：
- Gradle Wrapper 版本 >= AGP 要求的最低版本
- KSP 版本格式: `Kotlin版本-KSP修订版本`，如 `1.9.0-1.0.13`
- AGP 和 Kotlin 插件版本需要兼容

### 1.3 版本配置文件

```
项目根目录/
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties   # Gradle Wrapper版本
├── versions.gradle                     # 所有依赖版本统一管理
├── build.gradle                        # 根构建配置，应用versions.gradle
└── gradle.properties                   # JVM参数、AndroidX开关等
```

**gradle-wrapper.properties**：
```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip
```

**versions.gradle**：
```groovy
def versions = [
    android_gradle_plugin: '8.1.1',
    kotlin               : '1.9.0',
    ksp                  : '1.9.0-1.0.13',
    // ... 其他依赖
]
ext.versions = versions
```

---

## 2. Gradle Plugin 应用机制

### 2.1 插件类型

| 类型 | 声明位置 | 作用域 | 示例 |
|------|---------|--------|------|
| 应用插件 | `buildscript.dependencies.classpath` | 整个项目 | AGP、Kotlin、KSP |
| 声明插件 | 模块 `build.gradle` 的 `plugins {}` 块 | 单个模块 | `com.android.library` |

### 2.2 根 build.gradle 配置

```groovy
// build.gradle (根目录)
buildscript {
    apply from: 'versions.gradle'          // 引入版本管理
    addRepos(repositories)                 // 配置仓库
    dependencies {
        // 这些插件对整个项目可用
        classpath "com.android.tools.build:gradle:$versions.android_gradle_plugin"
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$versions.kotlin"
        classpath "com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:$versions.ksp"
    }
}

allprojects {
    addRepos(repositories)                 // 所有子模块共享仓库配置
}
```

### 2.3 模块 build.gradle 应用插件

```groovy
// 模块 build.gradle
apply plugin: 'com.android.library'       // 或 'com.android.application'
apply plugin: 'kotlin-android'
apply plugin: 'com.google.devtools.ksp'   // KSP插件

android {
    namespace 'com.example.xxx'
    compileSdk 34
    
    defaultConfig {
        minSdk 21
        targetSdk 34
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = '1.8'
    }
}
```

### 2.4 关键注意事项

1. **不能修改 `.android/` 目录下的 build.gradle** - Flutter自动生成的文件，其gradle配置继承主项目
2. **子模块不重复声明 classpath** - 在根 build.gradle 声明后，子模块直接用 `apply plugin`
3. **namespace 替代 package** - AGP 7.0+ 使用 namespace，AndroidManifest.xml 中不再需要 package 属性

---

## 3. 注解处理器：APT → KAPT → KSP

### 3.1 演进历程

```
APT (Java) ──> KAPT (Kotlin兼容) ──> KSP (Kotlin原生，更快)
  │                │                      │
  Java编译期       生成Java_stub          直接分析Kotlin AST
  处理注解         再用APT处理            编译速度提升2-3倍
```

### 3.2 三者的区别

| 特性 | APT | KAPT | KSP |
|------|-----|------|-----|
| 语言支持 | Java | Kotlin+Java | Kotlin+Java |
| 原理 | Java编译器API | 生成Java stub + APT | 直接读取Kotlin AST |
| 速度 | 基准 | 较慢(需生成stub) | 快2-3倍 |
| 配置 | `annotationProcessor` | `kapt` | `ksp` |
| 典型框架 | Dagger2(旧版) | Room(旧版)、Dagger2 | Room(新版)、ARouter-KSP |

### 3.3 项目中的配置

**根 build.gradle**：
```groovy
buildscript {
    dependencies {
        classpath "com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:$versions.ksp"
    }
}
```

**模块 build.gradle**：
```groovy
apply plugin: 'com.google.devtools.ksp'

dependencies {
    // KSP 方式（推荐）
    ksp 'com.alibaba.android:arouter-ksp-compiler:1.0.0'
    
    // KAPT 方式（旧项目兼容）
    kapt 'com.alibaba.android:arouter-compiler:1.5.2'
    
    // APT 方式（纯Java项目）
    annotationProcessor 'com.jakewharton:butterknife-compiler:10.2.3'
}
```

### 3.4 混合使用场景

实际项目中常同时存在 KSP 和 KAPT：
- **ARouter** - 可使用自研 KSP 编译器
- **Room** - 使用 KAPT 或 KSP
- **其他** - 根据框架支持情况选择

```groovy
// 模块示例
dependencies {
    ksp project(':arouter-ksp')           // KSP编译ARouter路由
    kapt "androidx.room:room-compiler:$versions.room"  // KAPT编译Room
}
```

---

## 4. 多模块配置与依赖顺序

### 4.1 模块分层与依赖方向

```
┌─────────────────────────────────────────────────────────┐
│                      业务模块层                           │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
│  │ app_user │ │ app_shop │ │ app_game │ │ app_bbs  │   │
│  │    │     │ │    │     │ │    │     │ │    │     │   │
│  └────┼─────┘ └────┼─────┘ └────┼─────┘ └────┼─────┘   │
└───────┼────────────┼────────────┼────────────┼──────────┘
        │            │            │            │
┌───────┼────────────┼────────────┼────────────┼──────────┐
│       ▼            ▼            ▼            ▼          │
│                     公共组件层                            │
│              ┌──────────────────┐                        │
│              │  lib_components  │                        │
│              └────────┬─────────┘                        │
└───────────────────────┼─────────────────────────────────┘
                        │
┌───────────────────────┼─────────────────────────────────┐
│                       ▼                                  │
│                     基础组件层                             │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐          │
│  │lib_  │ │lib_  │ │lib_  │ │lib_  │ │lib_  │          │
│  │base  │ │db    │ │video │ │img   │ │push  │          │
│  └──┬───┘ └──┬───┘ └──┬───┘ └──┬───┘ └──┬───┘          │
└─────┼────────┼────────┼────────┼────────┼───────────────┘
      │        │        │        │        │
      └────────┴────────┴────────┴────────┘
                         │
┌────────────────────────┼────────────────────────────────┐
│                        ▼                                 │
│                      app/ (主应用)                        │
│           依赖所有业务模块和公共组件                        │
└──────────────────────────────────────────────────────────┘
```

**依赖原则**：
- 上层模块依赖下层模块
- 同层模块之间不直接依赖（通过公共接口或事件总线通信）
- 基础组件不依赖业务模块

### 4.2 settings.gradle 模块声明顺序

```groovy
// settings.gradle

// 1. 主应用
include ':app'

// 2. 业务组件（依赖公共组件层和基础组件层）
include ':app_user', ':app_shop', ':app_game', ':app_bbs'

// 3. 公共组件层（依赖基础组件层）
include ':lib_components', ':lib_user_info', ':lib_game_manager'

// 4. 基础组件层
include ':lib_base', ':lib_resources', ':lib_exoplayer', 
        ':lib_video', ':lib_image_picker', ':lib_guider',
        ':lib_animation', ':lib_easyat', ':lib_file_transfer',
        ':lib_repository', ':lib_db', ':lib_mas_sdk',
        ':lib_msp_push', ':lib_jpush', ':lib_turbojpeg'

// 5. ARouter相关（注解处理器）
include ':arouter-annotation', ':arouter-api', ':arouter-ksp'

rootProject.name = 'MyApp'
```

**注意**：声明顺序不影响编译顺序，Gradle 会根据依赖关系自动解析。

### 4.3 模块依赖声明方式

**app 模块 (主应用)**：
```groovy
dependencies {
    // 业务模块
    implementation project(':app_user')
    implementation project(':app_shop')
    implementation project(':app_game')
    implementation project(':app_bbs')
    
    // 公共组件
    implementation project(':lib_components')
    implementation project(':lib_user_info')
    implementation project(':lib_game_manager')
    
    // Flutter
    implementation project(':flutter')
}
```

**业务模块依赖公共组件**：
```groovy
// app_user/build.gradle
dependencies {
    implementation project(':lib_components')
    implementation project(':lib_user_info')
    implementation project(':lib_base')
}
```

**公共组件依赖基础组件**：
```groovy
// lib_components/build.gradle
dependencies {
    implementation project(':lib_base')
    implementation project(':lib_db')
    implementation project(':lib_repository')
}
```

### 4.4 依赖作用域

| 作用域 | 说明 | 使用场景 |
|--------|------|---------|
| `implementation` | 仅当前模块可用，不传递 | 大多数依赖 |
| `api` | 传递给依赖当前模块的其他模块 | 需要暴露的公共API |
| `compileOnly` | 仅编译时需要，不打包 | provided依赖 |
| `runtimeOnly` | 仅运行时需要 | 运行时动态加载 |
| `ksp`/`kapt` | 注解处理器 | 代码生成 |
| `testImplementation` | 测试依赖 | 单元测试 |

**最佳实践**：
- 默认使用 `implementation`
- 只在需要暴露API时使用 `api`
- 减少 `api` 使用可以加快增量编译速度

---

## 5. Gradle 编译顺序

### 5.1 编译阶段

```
1. Initialization (初始化)
   └── 解析 settings.gradle，确定包含哪些模块

2. Configuration (配置)
   └── 执行所有模块的 build.gradle
   └── 构建任务依赖图 (DAG)

3. Execution (执行)
   └── 按依赖顺序执行任务
```

### 5.2 任务执行顺序

Gradle 按依赖关系确定执行顺序，不是按声明顺序：

```
示例：编译 app 模块时

lib_base          (无依赖，最先编译)
    ↓
lib_db            (依赖 lib_base)
lib_resources     (依赖 lib_base)
    ↓
lib_components    (依赖 lib_base, lib_db, lib_repository)
    ↓
app_user          (依赖 lib_components, lib_user_info)
    ↓
app               (依赖所有业务模块，最后编译)
```

### 5.3 查看依赖关系

```bash
# 查看模块依赖树
./gradlew :app:dependencies

# 查看特定配置依赖
./gradlew :app:dependencies --configuration implementation

# 查看依赖冲突
./gradlew :app:dependencies | grep "FAILED"

# 查看任务执行顺序
./gradlew :app:assembleDebug --dry-run
```

---

## 6. 常见编译问题与解决

### Q1: `Plugin with id 'com.android.library' not found`

**原因**：
- 子模块有自己的 `buildscript` 声明了不同版本的 AGP
- Gradle 类加载器隔离问题

**解决**：
- 子模块不要声明 `buildscript`，复用根项目的插件
- 确保根 build.gradle 的 `buildscript.dependencies` 包含 AGP classpath

### Q2: `Cannot run program "git"`

**原因**：`versions.gradle` 中调用 `git rev-list` 获取版本号，但系统 PATH 没有 git

**解决**：
- 将 git 添加到系统 PATH
- 或在 `gitVersionCode()` 中添加错误处理，返回默认值

### Q3: KSP/KAPT 生成的代码找不到

**原因**：
- 没有正确应用 KSP/KAPT 插件
- 注解处理器配置错误

**解决**：
```groovy
// 确保应用插件
apply plugin: 'com.google.devtools.ksp'

// 使用正确的依赖声明
ksp 'com.xxx:compiler:1.0.0'  // 不要用 implementation
```

### Q4: 模块依赖循环

**原因**：模块 A 依赖 B，模块 B 又依赖 A

**解决**：
- 提取公共接口到新的模块 C
- A 和 B 都依赖 C
- 使用事件总线（EventBus）解耦

### Q5: 依赖版本冲突

**原因**：不同模块引入了同一库的不同版本

**解决**：
```groovy
// 在根 build.gradle 中强制统一版本
configurations.all {
    resolutionStrategy {
        force "com.squareup.okhttp3:okhttp:$versions.okhttp"
    }
}
```

---

## 7. 构建优化建议

### 7.1 gradle.properties 配置

```properties
# 启用守护进程
org.gradle.daemon=true

# 并行编译
org.gradle.parallel=true

# 按需配置（只配置需要的模块）
org.gradle.configureondemand=true

# JVM 内存
org.gradle.jvmargs=-Xmx8g -Dfile.encoding=UTF-8

# AndroidX
android.useAndroidX=true

# 禁用测试（如果不需要）
android.injected.testOnly=false
```

### 7.2 模块拆分原则

1. **按功能拆分** - 业务模块独立（app_user、app_shop等）
2. **按层级拆分** - 基础层、公共层、业务层
3. **控制模块数量** - 过多模块会增加配置时间
4. **避免过深依赖链** - 依赖层级建议不超过4层

### 7.3 编译加速

```bash
# 1. 只编译目标模块
./gradlew :app:assembleDebug

# 2. 排除不需要的模块
./gradlew :app:assembleDebug -x :app_game:build

# 3. 使用构建缓存
./gradlew build --build-cache

# 4. 查看性能报告
./gradlew build --profile
```

---

## 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|----------|
| 1.0.0 | 2026-05-06 | 初始版本，总结Gradle编译核心知识 |