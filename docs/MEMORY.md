# 全局记忆（RT）

> **所属域**: 核心域 | **加载策略**: 必加载
> **版本**: 1.0.0 | **创建日期**: 2026-08-06
> **说明**: 本文件记录 Agent（AI 编码助手）与用户协作时的术语词典、表达映射、偏好与注意事项，随协作持续完善更新。

---

## 1. 术语词典

| 术语 / 简称 | 含义 / 全称 |
|------------|------------|
| RT | 当前项目名，Kotlin Multiplatform 跨平台应用（Android + iOS） |
| KMP | Kotlin Multiplatform，Kotlin 多平台 |
| Compose Multiplatform | 共享 UI 框架（JetBrains 官方） |
| Navigation KMP | `org.jetbrains.androidx.navigation` 多平台导航库 |
| Ktor / Ktorfit | 网络请求库 / 基于 Ktor 的 KMP 类型安全 HTTP 客户端 |
| SQLDelight | KMP 数据库 ORM（生成类型安全 SQL 代码） |
| MMKV | 高性能 Key-Value 本地缓存（腾讯开源） |
| Koin | 轻量依赖注入库 |
| kermit / kmp-xlog | 日志库（kermit 跨平台、kmp-xlog 高性能） |
| Coil | 图片加载库（JetBrains） |
| kotlinx-serialization | Kotlin 官方序列化库 |
| kotlinx-coroutines | Kotlin 协程库 |
| shell / androidApp | 主 Android Application 壳模块（构建入口） |
| feature | 功能模块（feature:home、feature:user） |

---

## 2. 映射规则

用于将用户的自然语言表述映射为标准操作/对象，避免理解偏差：

| 用户表达 | Agent 处理 |
|---------|-----------|
| 提到「壳 / 主模块 / 入口」 | 指 `:androidApp`，构建验证走 `./gradlew :androidApp:assembleDebug` |
| 「导航 / 路由 / 跳转」 | 指 Navigation KMP（`org.jetbrains.androidx.navigation`），路由表在 `app` 根组件 |
| 「网络 / 请求」 | 指 Ktor + Ktorfit 封装（`core` 网络层） |
| 「数据库 / DB」 | 指 SQLDelight（`core` 数据层） |
| 「缓存 / KV」 | 指 MMKV（`core` 缓存层） |
| 「日志」 | 指 kermit / kmp-xlog（`core` 日志层） |
| 「依赖注入 / DI」 | 指 Koin |
| 「共享 / 共模」 | 指 `shared` 模块公共模型 |
| 「功能模块 home / user」 | 指 `feature:home` / `feature:user` |

---

## 3. 用户偏好

- **语言**：中文交流（文档、说明、会议输出默认中文）。
- **执行优先**：偏好先把任务拆细、确认大纲后再动手；涉及多文件同时推进。
- **文档规范**：新文档头部带「域标签 + 加载策略」，遵循 `DOCUMENT_GENERATION_RULES.md`。
- **代码规范**：遵循 `.kt` 技能文档与 KMP 平台分离，不引栈外技术。
- **提交风格**：遵循 `GIT_CONVENTION.md`（`type(scope): subject`），仅在明确要求时 commit。

---

## 4. 注意事项

- **优先级**：当前对话中的明确说明 > 本记忆规则 > 默认上下文理解。
- 新增偏好在确认后写入对应条目，保持记忆时效。

---

* 任务进度见 [PLAN.md](PLAN.md)；行为规则见 [AGENT_RULES.md](AGENT_RULES.md)；项目结构见 [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)。