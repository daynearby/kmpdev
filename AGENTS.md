# Agent 配置文件

> **所属域**: 核心域 | **加载策略**: 每次对话必加载
> **版本**: 2.0.0 | **创建日期**: 2026-08-06

本文件定义了 AI 辅助开发的核心规范和流程。详细规则请参考对应的专项文档。

> **兼容性**: 本规则包独立于任何特定 IDE，适用于 Trae、CodeBuddy、Cursor 等主流 AI 编程助手。

---

## 一、技能文档速查

Agent 在遇到以下场景时，**优先读取对应的技能文档**获取知识：

| 场景 | 技能文档 | 触发条件 |
|------|---------|---------|
| **Kotlin 代码** | [docs/skills/kotlin-SKILL.md](docs/skills/kotlin-SKILL.md) | 修改 .kt 文件 |
| **Gradle 构建** | [docs/skills/android-gradle-build-SKILL.md](docs/skills/android-gradle-build-SKILL.md) | 构建失败、依赖冲突、KSP 问题 |
| **SDK 升级** | [docs/skills/android-upgrade-SKILL.md](docs/skills/android-upgrade-SKILL.md) | 升级 compileSdk/AGP/Kotlin |
| **依赖优化** | [docs/skills/api-implementation-separation-SKILL.md](docs/skills/api-implementation-separation-SKILL.md) | 优化编译速度、api→implementation |
| **Compose 开发** | [docs/skills/jetpack-compose-SKILL.md](docs/skills/jetpack-compose-SKILL.md) | 使用 Jetpack Compose 开发 UI |
| **Navigation** | [docs/skills/navigation-SKILL.md](docs/skills/navigation-SKILL.md) | 使用 Navigation 组件进行页面导航 |
| **Room 数据库** | [docs/skills/room-database-SKILL.md](docs/skills/room-database-SKILL.md) | Room 数据库配置和使用 |
| **Retrofit 网络** | [docs/skills/retrofit-network-SKILL.md](docs/skills/retrofit-network-SKILL.md) | Retrofit 网络请求配置 |
| **DataStore** | [docs/skills/datastore-SKILL.md](docs/skills/datastore-SKILL.md) | SharedPreferences 迁移到 DataStore |
| **ARouter 路由** | [docs/skills/arouter-SKILL.md](docs/skills/arouter-SKILL.md) | 路由配置、注解处理、跳转问题 |
| **Flutter 集成** | [docs/skills/flutter-add-to-app-SKILL.md](docs/skills/flutter-add-to-app-SKILL.md) | Flutter 混合开发问题 |
| **Flutter 路由迁移** | [docs/skills/flutter-migration-SKILL.md](docs/skills/flutter-migration-SKILL.md) | Flutter 路由框架更换 |

> 完整索引见 [androidAgentRulev2/SKILLS_MAP.md](androidAgentRulev2/SKILLS_MAP.md)

---

## 二、项目文档导航

文档按 **5 个域** 组织（详见 [docs/README.md](docs/README.md)），遇到不确定的规范时，按场景查阅对应文档：

| 场景 | 域 | 文档 |
|------|-----|------|
| 项目结构、模块分类、文件命名 | 规范域 | [PROJECT_STRUCTURE.md](docs/PROJECT_STRUCTURE.md) |
| 构建配置、变体、依赖 | 规范域 | [BUILD_CONFIG.md](docs/BUILD_CONFIG.md) |
| Git 规范、提交格式 | 规范域 | [GIT_CONVENTION.md](docs/GIT_CONVENTION.md) |
| Kotlin 代码风格 | 规范域 | [docs/skills/kotlin-SKILL.md](docs/skills/kotlin-SKILL.md) |
| ADB 日志采集与分析 | 规范域 | [AGENTS.md](AGENTS.md) 第二章 |
| 文档创建/更新规则 | 规范域 | [PROJECT_STRUCTURE.md](docs/PROJECT_STRUCTURE.md) 第 3 节 |
| 文档生成规则、影响范围评估 | 规范域 | [DOCUMENT_GENERATION_RULES.md](docs/DOCUMENT_GENERATION_RULES.md) |
| 代码修改、构建、Git 操作 | 核心域 | [AGENT_RULES.md](docs/AGENT_RULES.md) |
| 模块职责 | 知识域 | 各模块目录下的 `DOMAIN.md` |
| 新人上手 | 运维域 | [GETTING_STARTED.md](docs/GETTING_STARTED.md) |
| 技能文档索引 | 知识域 | [androidAgentRulev2/SKILLS_MAP.md](androidAgentRulev2/SKILLS_MAP.md) |
| 参考资源 | 知识域 | [reference-resource-documents.md](docs/reference-resource-documents.md) |

### 2.1 文档分域管理规则
详见 [PROJECT_STRUCTURE.md](docs/PROJECT_STRUCTURE.md) 第 3.6 节：域定义、域标签格式、域归属规则。

## 三、显示语言

显示文字使用中文。