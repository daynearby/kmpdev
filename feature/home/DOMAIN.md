# home 模块域管理文档

> **所属域**: 知识域 | **加载策略**: 按需加载

---

## 模块概述

- **类型**: kotlin-multiplatform library（业务功能模块）
- **包名**: com.example.kmpdev.home
- **职责**: 首页业务，Ktorfit Api(HomeRepository /app/page)、GetFeedUseCase 缓存策略、HomeViewModel 状态管理
- **Plugin**: kotlin.multiplatform + android.kmp.library + compose + compose.compiler + androidLint + ksp + ktorfit + serialization

---

## 包结构总览

| 包 | 说明 |
|---|------|
| com.example.kmpdev.home.ui | HomeViewModel 状态管理 |
| com.example.kmpdev.home.domain.repository | 首页数据仓库接口 |
| com.example.kmpdev.home.domain.usecase | 业务用例 |
| com.example.kmpdev.home.domain.model | 数据模型 |
| com.example.kmpdev.home.di | Koin DI |

---

## 核心类详解

### 类名: homeModule
- **路径**: com.example.kmpdev.home.di
- **职责**: Koin 模块，注册首页相关依赖

### 类名: HomeRepository(interface, @POST /app/page)
- **路径**: com.example.kmpdev.home.domain.repository
- **职责**: 首页数据接口，Ktorfit 注解请求 `/app/page`

### 类名: GetFeedUseCase
- **路径**: com.example.kmpdev.home.domain.usecase
- **职责**: 获取列表业务用例，应用缓存策略

### 类名: HomeViewModel
- **路径**: com.example.kmpdev.home.ui
- **职责**: 首页状态管理（加载/数据/错误）

### 类名: Page<T> / ItemModel / TestModel(data class)
- **路径**: com.example.kmpdev.home.domain.model
- **职责**: 分页包装及首页测试数据模型

---

## 依赖关系

| 依赖模块 | 说明 |
|---------|------|
| shared | 复用平台抽象与 ViewModel 基类 |
| core | 复用网络/缓存/DI 能力 |

---

## 被依赖关系

| 被依赖模块 | 说明 |
|-----------|------|
| app | 业务装配引入首页页面 |
| androidApp | Android 壳 DI 聚合 homeModule |

---

## 版本历史

| 日期 | 版本 | 变更内容 |
|------|------|----------|
| 2026-08-06 | 2.0 | 初始版本，自动生成（Android Agent Rule v2） |