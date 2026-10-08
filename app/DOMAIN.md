# app 模块域管理文档

> **所属域**: 知识域 | **加载策略**: 按需加载

---

## 模块概述

- **类型**: kotlin-multiplatform library（KMP 业务装配）
- **包名**: com.example.kmpdev.app
- **职责**: Compose 根组件 App()、Material3 主题 RTTheme、平台抽象 platform()
- **Plugin**: kotlin.multiplatform + android.kmp.library + compose.multiplatform + compose.compiler + kotlin.serialization + androidLint

---

## 包结构总览

| 包 | 说明 |
|---|------|
| com.example.kmpdev.app | App/Platform/Greeting |
| com.example.kmpdev.app.ui | Theme/Type/NoRipple |

---

## 核心类详解

### 类名: App()
- **路径**: app/src/.../com/example/kmpdev/app/(App)
- **职责**: 根组件入口，编排各 feature 页面与导航装配

### 类名: RTTheme
- **路径**: app/src/.../com/example/kmpdev/app/ui/Theme
- **职责**: Material3 主题定义（颜色/暗色模式等）

### 类名: RTTypography
- **路径**: app/src/.../com/example/kmpdev/app/ui/Type
- **职责**: Material3 排版字体定义

### 类名: NoRipple
- **路径**: app/src/.../com/example/kmpdev/app/ui/NoRipple
- **职责**: 去除点击涟漪效果的 Modifier 实现

### 类名: Greeting
- **路径**: com.example.kmpdev.app/Greeting
- **职责**: 简单欢迎文案组件

### 类名: platform()
- **路径**: com.example.kmpdev.app/Platform
- **职责**: expect 平台抽象，各平台提供实际实现

---

## 依赖关系

| 依赖模块 | 说明 |
|---------|------|
| feature:home | 首页业务功能页面 |
| shared | 跨平台共享模型与 ViewModel 基类（间接 core） |

---

## 被依赖关系

| 被依赖模块 | 说明 |
|-----------|------|
| androidApp | Android 壳挂载 App() 根组件 |

---

## 版本历史

| 日期 | 版本 | 变更内容 |
|------|------|----------|
| 2026-08-06 | 2.0 | 初始版本，自动生成（Android Agent Rule v2） |