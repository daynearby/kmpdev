# API/Implementation 依赖分离 Skill 文档

> **所属域**: 知识域 | **加载策略**: 按需加载

## 模块概述

Gradle 提供了两种依赖声明方式：`api` 和 `implementation`。`api` 会将依赖传递给下游消费者，而 `implementation` 不会。合理使用 `implementation` 可以显著减少编译依赖图，提升增量编译速度，并降低模块间耦合。

### 核心原则

- `api`：仅用于模块对外暴露的公开 API 类型（接口、基类、数据模型等）
- `implementation`：用于模块内部实现的依赖，不应泄露给下游模块

---

## 1. API vs Implementation 原理

### 1.1 编译可见性

```
模块 A (api → 模块 B) → 模块 C
```
- 模块 C 可以访问模块 B 的所有 public 类（通过 `api` 传递）

```
模块 A (implementation → 模块 B) → 模块 C
```
- 模块 C **不能**访问模块 B 的任何类（编译期隔离）

### 1.2 性能优势

| 维度 | api | implementation |
|------|-----|----------------|
| 下游编译可见性 | 可见 | 不可见 |
| 下游重编译范围 | 依赖链全部重编译 | 仅直接依赖者重编译 |
| 增量编译 | 较差 | 较好 |
| 模块解耦 | 低 | 高 |

---

## 2. 操作流程

### 2.1 前置检查

1. 确认当前 Gradle 版本和 AGP 版本兼容
2. 确认 Kotlin 编译器版本（如使用 K2 编译器需注意兼容性）
3. 备份当前 `build.gradle` 文件

### 2.2 执行步骤

#### Step 1：分析依赖关系

确定哪些依赖是"内部实现细节"，哪些是"公开 API"：

```groovy
// 示例：lib_base/build.gradle 迁移前
dependencies {
    api "androidx.appcompat:appcompat:${versions.appcompat}"        // 公开：BasicActivity 继承 AppCompatActivity
    api "androidx.fragment:fragment-ktx:${versions.fragment_ktx}"   // 公开：BasicFragment 继承 Fragment
    api "androidx.recyclerview:recyclerview:${versions.recyclerview}" // 公开：BetterRecyclerView 继承 RecyclerView
    api "androidx.lifecycle:lifecycle-viewmodel-ktx:${versions.lifecycle}" // 公开：BaseViewModel
    api "com.google.android.material:material:${versions.material}" // 公开：Material 组件
    implementation "com.github.bumptech.glide:glide:${versions.glide}" // 内部：图片加载实现
    implementation "com.squareup.retrofit2:retrofit:${versions.retrofit}" // 内部：网络实现
}
```

**判断标准**：下游模块是否需要直接 import 该库的类？

- 需要 → `api`
- 不需要 → `implementation`

#### Step 2：执行 api → implementation 变更

将不需要暴露的依赖改为 `implementation`。

#### Step 3：编译识别缺失依赖

执行完整编译（`gradlew assemble`），编译错误会显示下游模块缺少哪些依赖。

#### Step 4：补充下游模块缺失依赖

根据编译错误信息，在下游模块的 `build.gradle` 中显式添加缺失的依赖。

#### Step 5：验证编译

再次执行完整编译，确认 BUILD SUCCESSFUL。

---

## 3. 实际案例：lib_base api → implementation

### 3.1 变更内容

**lib_base/build.gradle** 变更：

```groovy
// 保留 api（公开 API，下游需要直接使用）
api "androidx.appcompat:appcompat:${versions.appcompat}"
api "androidx.recyclerview:recyclerview:${versions.recyclerview}"
api "com.google.android.material:material:${versions.material}"

// 改为 implementation（内部实现）
implementation "com.github.bumptech.glide:glide:${versions.glide}"
implementation "com.squareup.retrofit2:retrofit:${versions.retrofit}"
implementation "org.greenrobot:eventbus:${versions.eventbus}"
implementation "com.airbnb.android:lottie:${versions.lottie}"
implementation "com.facebook.rebound:rebound:${versions.rebound}"
implementation "com.scwang.smart:refresh-layout-kernel:${versions.smart_refresh}"
implementation "me.samlss:broccoli:${versions.broccoli}"
implementation "androidx.constraintlayout:constraintlayout:${versions.constraint_layout}"
implementation "com.github.chrisbanes:PhotoView:${versions.photoView}"
implementation "com.tencent:mmkv-static:${versions.mmkv}"
implementation "net.lingala.zip4j:zip4j:${versions.zip4j}"
implementation "androidx.startup:startup-runtime:${versions.app_startup}"
implementation "com.squareup.okhttp3:okhttp:${versions.okhttp}"
implementation "com.squareup.okhttp3:logging-interceptor:${versions.okhttp_logging_interceptor}"
implementation "com.squareup.retrofit2:converter-gson:${versions.retrofit}"
```

### 3.2 下游模块补充依赖

**lib_components/build.gradle** 需要补充：

```groovy
// lib_base 的 api 改为 implementation 后，这些依赖需显式声明
implementation "androidx.appcompat:appcompat:${versions.appcompat}"
implementation "androidx.fragment:fragment-ktx:${versions.fragment_ktx}"
implementation "androidx.recyclerview:recyclerview:${versions.recyclerview}"
implementation "com.google.android.material:material:${versions.material}"
implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:${versions.lifecycle}"
```

### 3.3 实际案例：lib_components api → implementation

**lib_components/build.gradle** 变更：

```groovy
// 改为 implementation
implementation project(path: ':lib_base')
implementation project(path: ':lib_resources')
implementation project(path: ':lib_user_info')
implementation project(path: ':lib_repository')
// ... 其他子模块
```

**下游 app 模块** 需要补充对 `lib_base`、`lib_resources` 等的显式依赖。

---

## 4. K2 编译器兼容处理

Kotlin 2.0 的 K2 编译器更严格，可能在迁移过程中暴露以下问题：

### 4.1 CustomTarget → BitmapCustomTarget

**问题**：Glide 的 `CustomTarget` 在 K2 下类型推断失败。

**修复**：
```kotlin
// 迁移前
object : CustomTarget<Bitmap>() { ... }

// 迁移后
object : BitmapCustomTarget() { ... }
```

### 4.2 SingleLiveEvent Kotlin → Java

**问题**：Kotlin 实现的 `SingleLiveEvent` 在 K2 下存在类型擦除问题。

**修复**：将 `SingleLiveEvent` 改为 Java 实现，确保泛型正确处理。

### 4.3 函数引用歧义

**问题**：K2 编译器对函数引用更严格，可能导致歧义。

**修复**：使用 lambda 替代函数引用，或显式指定类型。

---

## 5. 编译验证步骤

### 5.1 清理构建

```powershell
.\gradlew clean
```

### 5.2 完整编译

```powershell
.\gradlew assemble
```

### 5.3 错误分析

编译失败时，按以下顺序分析：
1. 检查是否有 "Unresolved reference" 错误 → 缺少依赖
2. 检查是否有 "Cannot access class" 错误 → 依赖不可见
3. 检查是否有 K2 编译器特有错误 → 类型推断/函数引用问题

### 5.4 日志记录

编译日志保存在 `temp/logs/build_log_YYYYMMDD_HHMMSS.log`，最后 200 行输出。

---

## 6. 常见问题与解决方案

### Q1：编译报错 "Unresolved reference: Glide"

**原因**：Glide 已从 `api` 改为 `implementation`，下游模块无法访问。

**解决**：在下游模块的 `build.gradle` 中添加：
```groovy
implementation "com.github.bumptech.glide:glide:${versions.glide}"
```

### Q2：编译报错 "Cannot access class '...'"

**原因**：下游模块间接依赖的库不再传递。

**解决**：使用编译错误驱动法，逐个补充缺失依赖。

### Q3：K2 编译器报类型推断错误

**原因**：K2 编译器的类型推断算法与 K1 不同。

**解决**：
- 使用具体类型替代泛型通配符
- 为 lambda 参数显式声明类型
- 考虑将问题类改为 Java 实现

### Q4：ARouter 注解处理失败

**原因**：KSP 处理器需要显式配置 `AROUTER_MODULE_NAME`。

**解决**：确保每个模块的 `defaultConfig` 中配置：
```groovy
ksp {
    arg('AROUTER_MODULE_NAME', project.getName())
}
```

### Q5：依赖版本冲突

**原因**：多个模块依赖同一库的不同版本。

**解决**：在 `versions.gradle` 中统一管理版本号，所有模块使用同一版本。

---

## 7. 最佳实践

### 7.1 依赖声明顺序

```groovy
dependencies {
    // 1. 项目内部模块
    implementation project(':lib_base')
    
    // 2. AndroidX 核心库
    implementation "androidx.core:core-ktx:${versions.core_ktx}"
    
    // 3. UI 库
    implementation "com.google.android.material:material:${versions.material}"
    
    // 4. 网络库
    implementation "com.squareup.retrofit2:retrofit:${versions.retrofit}"
    
    // 5. 工具库
    implementation "org.greenrobot:eventbus:${versions.eventbus}"
    
    // 6. 测试库
    testImplementation "junit:junit:${versions.junit}"
}
```

### 7.2 版本统一管理

所有版本号定义在 `versions.gradle` 中，避免硬编码：

```groovy
// versions.gradle
def versions = [
    glide: "4.15.1",
    retrofit: "2.9.0",
    // ...
]
ext.versions = versions
```

### 7.3 渐进式迁移

不要一次性迁移所有模块，按以下顺序：
1. 最底层模块（`lib_resources`）
2. 基础模块（`lib_base`）
3. 组件模块（`lib_components`）
4. 业务模块（`app`, `app_bbs` 等）

---

## 8. 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|----------|
| 1.0.0 | 2026-06-11 | 初始版本，总结 lib_base/lib_components 的 api→implementation 迁移流程 |

---

## 9. 参考链接

- [Gradle 依赖管理文档](https://docs.gradle.org/current/userguide/java_library_plugin.html)
- [Android Gradle Plugin 依赖配置](https://developer.android.com/studio/build/dependencies)
- [Kotlin K2 编译器迁移指南](https://kotlinlang.org/docs/k2-compiler-migration-guide.html)
- [项目构建配置](../BUILD_CONFIG.md)