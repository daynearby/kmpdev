# shared 模块域管理文档

> **所属域**: 知识域 | **加载策略**: 按需加载

---

## 模块概述

- **类型**: kotlin-multiplatform library（KMP 共享）
- **包名**: com.example.rt
- **职责**: 平台抽象 Platform、跨平台轻量 ViewModel 基类 RTViewModel、通用响应模型 ResponseBody
- **Plugin**: kotlin.multiplatform + serialization + android.kmp.library + compose.multiplatform + compose.compiler

---

## 包结构总览

| 包 | 说明 |
|---|------|
| com.example.rt | 平台抽象 Platform |
| com.example.rt.ui.viewmodel | 跨平台 ViewModel 基类 |
| com.example.rt.domain.model | 通用响应模型 |
| com.example.rt.di | Koin DI |

---

## 核心类详解

### 类名: Platform(interface) + getPlatform(expect)
- **路径**: com.example.rt/Platform
- **职责**: 跨平台能力抽象接口，getPlatform expect 提供各平台实例

### 类名: RTViewModel
- **路径**: com.example.rt.ui.viewmodel
- **职责**: 跨平台轻量 ViewModel 基类，承载状态管理

### 类名: rememberRTViewModel(Composable)
- **路径**: com.example.rt.ui.viewmodel
- **职责**: Compose 侧获取 RTViewModel 的 remember 封装

### 类名: ResponseBody<T>(data class)
- **路径**: com.example.rt.domain.model
- **职责**: 通用网络响应包装模型

### 类名: ShareModule
- **路径**: com.example.rt.di
- **职责**: 空 Koin module 占位，预留共享 DI

---

## 依赖关系

| 依赖模块 | 说明 |
|---------|------|
| core | 共享层复用基础层能力 |

---

## 被依赖关系

| 被依赖模块 | 说明 |
|-----------|------|
| app | 业务装配复用平台抽象与 ViewModel |
| feature:home | 首页业务复用共享基础 |
| feature:user | 用户业务复用共享基础 |

---

## 版本历史

| 日期 | 版本 | 变更内容 |
|------|------|----------|
| 2026-08-06 | 2.0 | 初始版本，自动生成（Android Agent Rule v2） |