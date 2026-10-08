# androidApp 模块域管理文档

> **所属域**: 知识域 | **加载策略**: 按需加载

---

## 模块概述

- **类型**: android-application（Android 壳）
- **包名**: com.example.rt
- **职责**: Android 入口壳，MainActivity 挂载 Compose 根组件，App(Application) 初始化 XLog/MMKV/startKoin 聚合全部模块 DI
- **Plugin**: android.application + compose.multiplatform + compose.compiler

---

## 包结构总览

| 包 | 说明 |
|---|------|
| com.example.rt | 宿主 Activity + Application |
| com.example.rt.extension | Context/String 扩展 |

---

## 核心类详解

### 类名: MainActivity
- **路径**: androidApp/src/.../com/example/rt/MainActivity.kt
- **职责**: 宿主 Activity，单点挂载 `App()` 根组件，作为 Android 壳的 UI 入口

### 类名: App
- **路径**: androidApp/src/.../com/example/rt/App.kt
- **职责**: Application 入口，初始化 XLog/MMKV/startKoin，聚合 appModule + coreModule + coreModuleAndroid + userModule + homeModule 全部模块 DI

### 类名: ContextExt
- **路径**: androidApp/src/.../com/example/rt/extension/ContextExt.kt
- **职责**: Context 扩展工具方法

### 类名: StringExt
- **路径**: androidApp/src/.../com/example/rt/extension/StringExt.kt
- **职责**: String 扩展工具方法

---

## 依赖关系

| 依赖模块 | 说明 |
|---------|------|
| core | 基础层 DI、网络、缓存、日志能力 |
| app | Compose 根组件及平台抽象入口 |
| feature:home | 首页业务功能 |
| feature:user | 用户域业务功能 |

---

## 被依赖关系

| 被依赖模块 | 说明 |
|-----------|------|
| （无） | 顶层壳模块，不被任何模块依赖 |

---

## 版本历史

| 日期 | 版本 | 变更内容 |
|------|------|----------|
| 2026-08-06 | 2.0 | 初始版本，自动生成（Android Agent Rule v2） |