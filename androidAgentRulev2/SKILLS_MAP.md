# Android Agent Rule v2 技能文档索引

> **所属域**: 知识域 | **加载策略**: 按需加载
> **版本**: 2.0.0 | **创建日期**: 2026-07-14
> **用途**: 本项目所有可迁移技能文档的索引。Agent 在遇到对应场景时，读取相应的技能文档获取知识。

---

## 一、技能文档总览（12 个）

| 技能文档 | 适用场景 | 类型 | 优先级 |
|---------|---------|------|--------|
| [kotlin-SKILL.md](docs/skills/kotlin-SKILL.md) | 编写/修改 Kotlin 代码 | 通用 | 高 |
| [android-gradle-build-SKILL.md](docs/skills/android-gradle-build-SKILL.md) | Gradle 构建问题、多模块配置 | 通用 | 高 |
| [android-upgrade-SKILL.md](docs/skills/android-upgrade-SKILL.md) | SDK/AGP/Kotlin 版本升级 | 通用 | 高 |
| [api-implementation-separation-SKILL.md](docs/skills/api-implementation-separation-SKILL.md) | 依赖优化、api→implementation 迁移 | 通用 | 中 |
| [jetpack-compose-SKILL.md](docs/skills/jetpack-compose-SKILL.md) | Jetpack Compose UI 开发 | 通用 | 高 |
| [navigation-SKILL.md](docs/skills/navigation-SKILL.md) | Navigation 组件页面导航 | 通用 | 高 |
| [room-database-SKILL.md](docs/skills/room-database-SKILL.md) | Room 数据库配置和使用 | 通用 | 高 |
| [retrofit-network-SKILL.md](docs/skills/retrofit-network-SKILL.md) | Retrofit 网络请求配置 | 通用 | 高 |
| [datastore-SKILL.md](docs/skills/datastore-SKILL.md) | SharedPreferences 迁移到 DataStore | 通用 | 中 |
| [arouter-SKILL.md](docs/skills/arouter-SKILL.md) | ARouter 路由框架使用 | 框架（可选） | 高 |
| [flutter-add-to-app-SKILL.md](docs/skills/flutter-add-to-app-SKILL.md) | Flutter 混合开发集成 | 框架（可选） | 中 |
| [flutter-migration-SKILL.md](docs/skills/flutter-migration-SKILL.md) | Flutter 路由框架迁移 | 框架（可选） | 低 |

---

## 二、按场景速查

| 你要做什么 | 看哪个文档 |
|-----------|-----------|
| 修改 Kotlin 代码 | [kotlin-SKILL.md](docs/skills/kotlin-SKILL.md) |
| Gradle 编译报错 | [android-gradle-build-SKILL.md](docs/skills/android-gradle-build-SKILL.md) |
| 升级 compileSdk/targetSdk | [android-upgrade-SKILL.md](docs/skills/android-upgrade-SKILL.md) |
| 修复依赖版本冲突 | [android-gradle-build-SKILL.md](docs/skills/android-gradle-build-SKILL.md) 第6节 |
| 优化编译速度 | [api-implementation-separation-SKILL.md](docs/skills/api-implementation-separation-SKILL.md) |
| 使用 Jetpack Compose | [jetpack-compose-SKILL.md](docs/skills/jetpack-compose-SKILL.md) |
| 使用 Navigation 组件 | [navigation-SKILL.md](docs/skills/navigation-SKILL.md) |
| 使用 Room 数据库 | [room-database-SKILL.md](docs/skills/room-database-SKILL.md) |
| 使用 Retrofit 网络 | [retrofit-network-SKILL.md](docs/skills/retrofit-network-SKILL.md) |
| 迁移到 DataStore | [datastore-SKILL.md](docs/skills/datastore-SKILL.md) |
| 使用 ARouter 路由 | [arouter-SKILL.md](docs/skills/arouter-SKILL.md) |
| 集成 Flutter 到 Android | [flutter-add-to-app-SKILL.md](docs/skills/flutter-add-to-app-SKILL.md) |
| Flutter 路由框架迁移 | [flutter-migration-SKILL.md](docs/skills/flutter-migration-SKILL.md) |
| 升级 AGP 版本 | [android-upgrade-SKILL.md](docs/skills/android-upgrade-SKILL.md) 第3节 |
| KSP/KAPT 注解处理器 | [android-gradle-build-SKILL.md](docs/skills/android-gradle-build-SKILL.md) 第3节 |

---

## 三、文档分类

### 3.1 通用技能（9 个）

所有 Android 项目都适用，**必须迁移**：

| 文档 | 核心内容 | 关键技术点 |
|------|---------|-----------|
| `kotlin-SKILL.md` | Kotlin 语法规范、协程、扩展函数 | 协程、Flow、扩展方法 |
| `android-gradle-build-SKILL.md` | Gradle 版本体系、插件机制、KSP/KAPT、多模块配置、编译问题速查 | AGP、KSP、多模块依赖 |
| `android-upgrade-SKILL.md` | SDK 升级7步流程、版本兼容矩阵、API 迁移速查、回滚方案 | SDK 升级、版本兼容 |
| `api-implementation-separation-SKILL.md` | api/implementation 原理、操作流程、K2 兼容、最佳实践 | 编译优化、依赖管理 |
| `jetpack-compose-SKILL.md` | Compose 基础组件、布局、状态管理、Material3 | Compose 布局、状态管理 |
| `navigation-SKILL.md` | Navigation 3 导航、深链接、多返回栈、场景 | Navigation 3、深链接 |
| `room-database-SKILL.md` | Room 实体定义、DAO、数据库初始化、迁移 | Room、KSP、数据库迁移 |
| `retrofit-network-SKILL.md` | Retrofit 接口定义、OkHttp 配置、拦截器、错误处理 | Retrofit、OkHttp |
| `datastore-SKILL.md` | DataStore 初始化、数据存取、SharedPreferences 迁移 | DataStore、迁移策略 |

### 3.2 框架技能（3 个，可选）

仅在项目使用对应框架时适用：

| 文档 | 前提条件 | 关键技术点 |
|------|---------|-----------|
| `arouter-SKILL.md` | 项目使用 ARouter 路由框架 | ARouter、KSP 注解处理器 |
| `flutter-add-to-app-SKILL.md` | 项目包含 Flutter 混合开发 | Flutter Add-to-App、MethodChannel |
| `flutter-migration-SKILL.md` | Flutter 项目中需要迁移路由框架 | go_router、flutter_boost |

---

## 四、技能依赖关系

```
┌────────────────────────────────────────────────────────────────────┐
│                        基础技能层                                   │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐ │
│  │   kotlin-SKILL   │  │android-gradle-  │  │android-upgrade-  │ │
│  │                  │  │ build-SKILL      │  │ SKILL            │ │
│  └────────┬─────────┘  └────────┬─────────┘  └────────┬─────────┘ │
└───────────┼──────────────────────┼──────────────────────┼──────────┘
            │                      │                      │
┌───────────┼──────────────────────┼──────────────────────┼──────────┐
│           ▼                      ▼                      ▼          │
│                        开发技能层                                   │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐ │
│  │  jetpack-compose │  │   navigation-    │  │   room-database  │ │
│  │   -SKILL         │  │   SKILL          │  │   -SKILL         │ │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘ │
│  ┌──────────────────┐  ┌──────────────────┐                       │
│  │  retrofit-network│  │   datastore-     │                       │
│  │   -SKILL         │  │   SKILL          │                       │
│  └──────────────────┘  └──────────────────┘                       │
│  ┌──────────────────┐                                              │
│  │api-implementation│                                              │
│  │ -separation-SKILL│                                              │
│  └──────────────────┘                                              │
└────────────────────────────────────────────────────────────────────┘
                                    │
┌───────────────────────────────────┼────────────────────────────────┐
│                                   ▼                                │
│                        框架技能层                                   │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐ │
│  │   arouter-       │  │flutter-add-to-   │  │flutter-migration │ │
│  │   SKILL          │  │ app-SKILL        │  │   -SKILL         │ │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘ │
└────────────────────────────────────────────────────────────────────┘
```

---

## 五、迁移优先级建议

### 5.1 首次迁移（最小必要集）

| 优先级 | 技能文档 | 原因 |
|--------|---------|------|
| P0 | `kotlin-SKILL.md` | 基础开发语言规范 |
| P0 | `android-gradle-build-SKILL.md` | 构建基础，解决编译问题 |
| P0 | `android-upgrade-SKILL.md` | 版本升级必备 |
| P1 | `jetpack-compose-SKILL.md` | UI 开发必备 |
| P1 | `navigation-SKILL.md` | 页面导航必备 |
| P1 | `room-database-SKILL.md` | 数据存储必备 |
| P1 | `retrofit-network-SKILL.md` | 网络请求必备 |

### 5.2 进阶迁移

| 优先级 | 技能文档 | 原因 |
|--------|---------|------|
| P2 | `api-implementation-separation-SKILL.md` | 编译优化 |
| P2 | `datastore-SKILL.md` | 存储现代化 |
| P3 | `arouter-SKILL.md` | ARouter 路由项目 |
| P3 | `flutter-add-to-app-SKILL.md` | Flutter 混合项目 |
| P4 | `flutter-migration-SKILL.md` | Flutter 路由迁移 |

---

## 六、版本历史

| 日期 | 版本 | 变更内容 |
|------|------|----------|
| 2026-07-14 | 2.0.0 | 升级到 v2 版本，新增 5 个技能文档（Compose、Navigation、Room、Retrofit、DataStore），重构分类体系 |
| 2026-06-27 | 1.0.0 | 初始版本，收录 7 个技能文档（4 通用 + 3 框架） |