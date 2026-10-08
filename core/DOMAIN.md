# core 模块域管理文档

> **所属域**: 知识域 | **加载策略**: 按需加载

---

## 模块概述

- **类型**: kotlin-multiplatform library（KMP 基础层，最庞大）
- **包名**: com.example.kmpdev.core
- **职责**: 网络层(Ktor/Ktorfit/缓存策略/Auth)、MMKV 缓存、日志(XLog/Kermit)、SQLDelight 数据库壳、Koin DI、工具配置
- **Plugin**: kotlin.multiplatform + compose + serialization + android.kmp.library + sqldelight + ksp + ktorfit + androidLint

---

## 包结构总览

| 包 | 说明 |
|---|------|
| com.example.kmpdev.core.app | AppVersion/RequestParams 等应用级配置 |
| com.example.kmpdev.core.cache | 缓存能力（CacheManager/策略） |
| com.example.kmpdev.core.database | SQLDelight 数据库壳 |
| com.example.kmpdev.core.di | Koin DI 装配 |
| com.example.kmpdev.core.extension | 扩展函数 |
| com.example.kmpdev.core.logger | 日志封装 |
| com.example.kmpdev.core.net | 网络层主体 |
| com.example.kmpdev.core.network | 含 auth/error/interceptors 子包的网络实现 |
| com.example.kmpdev.core.util | 工具类 |

---

## 核心类详解

### 类名: coreModule
- **路径**: com.example.kmpdev.core.di
- **职责**: Koin 模块，注册 core 层全部依赖

### 类名: CacheManager / MMKVCacheManagerImpl
- **路径**: package com.example.kmpdev.core.cache
- **职责**: 缓存抽象接口及其基于 MMKV 的实现

### 类名: CacheThenNetworkStrategy
- **路径**: com.example.kmpdev.core.cache
- **职责**: 先缓存后网络的请求策略实现

### 类名: RequestResult / NetworkError(sealed)
- **路径**: com.example.kmpdev.core.network
- **职责**: 请求结果封装与密封网络错误类型

### 类名: createHttpClient / createKtorfit
- **路径**: com.example.kmpdev.core.net / network
- **职责**: Ktor HttpClient 与 Ktorfit 工厂创建

### 类名: TokenManager / TokenRefreshManager / AuthRetryHandler / AuthCoordinator
- **路径**: com.example.kmpdev.core.network.auth
- **职责**: Token 存储、刷新管理、鉴权重试拦截、认证协调

### 类名: ErrorMessageResolver
- **路径**: com.example.kmpdev.core.network.error
- **职责**: 错误码/异常到用户可读提示的映射

### 类名: Logger
- **路径**: com.example.kmpdev.core.logger
- **职责**: XLog/Kermit 统一日志封装

### 类名: AppConfig / AppVersion(expect)
- **路径**: com.example.kmpdev.core.app
- **职责**: 应用配置与跨平台版本信息抽象

---

## 依赖关系

| 依赖模块 | 说明 |
|---------|------|
| （无本地模块） | 不依赖任何本地模块，纯第三方库依赖 |

---

## 被依赖关系

| 被依赖模块 | 说明 |
|-----------|------|
| shared | 共享层复用基础能力 |
| feature:home | 首页业务复用网络/缓存/DI |
| feature:user | 用户域复用基础层 Token 机制 |
| app | 业务装配复用基础层 |
| androidApp | Android 壳初始化 core 层 DI |

---

## 版本历史

| 日期 | 版本 | 变更内容 |
|------|------|----------|
| 2026-08-06 | 2.0 | 初始版本，自动生成（Android Agent Rule v2） |