# RT 文档索引

> **所属域**: 规范域 | **加载策略**: 按需加载
> **版本**: 1.0.0 | **创建日期**: 2026-08-06

RT 是基于 Kotlin Multiplatform（KMP）的跨平台应用，一套 Kotlin 业务代码同时支持 **Android 与 iOS**，UI 采用 Compose Multiplatform，业务分层为 `:core`（基础能力）+ `:shared`（共享 domain）+ `:feature:*`（业务功能）+ `:app`（业务装配）+ `:androidApp` / `:iosApp`（平台壳）。

文档按 5 个域组织管理，核心域必加载，其余按需加载：

## 一、核心域（Agent 行为准则、用户记忆、任务进度）

| 文档 | 说明 | 状态 |
|------|------|------|
| [AGENTS.md](../AGENTS.md) | Agent 配置文件（技能速查、ADB 日志工作流、文档导航） | 已存在 |
| [AGENT_RULES.md](AGENT_RULES.md) | 代码修改 / 构建 / Git 操作规范 | 已存在 |
| [MEMORY.md](MEMORY.md) | 全局记忆（术语词典、映射规则） | 已存在 |
| [PLAN.md](PLAN.md) | 任务计划与进度跟踪 | 已存在 |

## 二、规范域（开发规范、构建、命名、代码风格）

| 文档 | 说明 | 状态 |
|------|------|------|
| [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) | 项目结构、模块分类、文件命名 | 已存在 |
| [BUILD_CONFIG.md](BUILD_CONFIG.md) | 构建配置、模块架构、依赖清单、构建命令 | 已创建 |
| [GIT_CONVENTION.md](GIT_CONVENTION.md) | Git 规范与提交格式 | 已存在 |
| [DOCUMENT_GENERATION_RULES.md](DOCUMENT_GENERATION_RULES.md) | 文档生成规则、影响范围评估 | 已存在 |

## 三、知识域（技术实现细节、模块职责、技能参考）

| 文档 | 说明 | 状态 |
|------|------|------|
| [reference-resource-documents.md](reference-resource-documents.md) | KMP / Compose / Ktor / SQLDelight 等官方参考链接 | 已创建 |
| [skills/android-gradle-build-SKILL.md](skills/android-gradle-build-SKILL.md) | Gradle 构建技能 | 已存在 |
| [skills/android-upgrade-SKILL.md](skills/android-upgrade-SKILL.md) | SDK / AGP / Kotlin 升级技能 | 已存在 |
| [skills/api-implementation-separation-SKILL.md](skills/api-implementation-separation-SKILL.md) | api→implementation 依赖优化技能 | 已存在 |
| [skills/jetpack-compose-SKILL.md](skills/jetpack-compose-SKILL.md) | Compose 开发技能 | 已存在 |
| [skills/navigation-SKILL.md](skills/navigation-SKILL.md) | Navigation 组件技能 | 已存在 |
| [skills/kotlin-SKILL.md](skills/kotlin-SKILL.md) | Kotlin 代码技能 | 已存在 |
| [skills/retrofit-network-SKILL.md](skills/retrofit-network-SKILL.md) | Retrofit 网络技能 | 已存在 |
| [skills/room-database-SKILL.md](skills/room-database-SKILL.md) | Room 数据库技能 | 已存在 |
| [skills/datastore-SKILL.md](skills/datastore-SKILL.md) | DataStore 迁移技能 | 已存在 |
| [../androidAgentRulev2/SKILLS_MAP.md](../androidAgentRulev2/SKILLS_MAP.md) | 技能文档完整索引 | 已存在 |
| [../androidAgentRulev2/ANDROID_CLI_INSTALL.md](../androidAgentRulev2/ANDROID_CLI_INSTALL.md) | Android 命令行工具安装 | 已存在 |
| [../androidAgentRulev2/INITIALIZATION.md](../androidAgentRulev2/INITIALIZATION.md) | 规则包初始化说明 | 已存在 |
| 各模块 `DOMAIN.md` | `:androidApp` / `:app` / `:core` / `:shared` / `:feature:home` / `:feature:user` 模块职责 | 已存在 |

## 四、管理域（迁移计划、优化方案、需求跟踪）

| 目录 / 文档 | 说明 | 状态 |
|------|------|------|
| [migration/](migration/) | 迁移方案文档目录 | 目录已建，待补充 |
| [optimize/](optimize/) | 优化方案文档目录 | 目录已建，待补充 |
| [future/](future/) | 需求 / 远期规划文档目录 | 目录已建，待补充 |

## 五、运维域（环境搭建、CI/CD、新人上手）

| 文档 | 说明 | 状态 |
|------|------|------|
| [GETTING_STARTED.md](GETTING_STARTED.md) | 新人上手指南（环境要求、构建运行、新增模块） | 已创建 |

## 文档导航入口

- 根目录 `AGENTS.md`：技能文档速查表 + ADB 日志自动化工作流 + 场景规则加载。
- 遇到文档管理问题（新增 / 更新 / 删除）时，先阅读 [DOCUMENT_GENERATION_RULES.md](DOCUMENT_GENERATION_RULES.md) 与本文件的域归属。
