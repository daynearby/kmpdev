# Agent 配置文件

> **所属域**: 核心域 | **加载策略**: 每次对话必加载
> **版本**: 2.0.0 | **创建日期**: {{DATE}}

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

## 二、ADB 日志自动化工作流

### 2.1 触发词

| 用户输入 | Agent 行为 |
|---------|-----------|
| **"开始"** | 后台启动 adb logcat 采集 |
| **"结束"** | 停止 logcat 采集，自动分析日志（崩溃堆栈/ANR/异常），给出修复建议 |
| **"闪退"/"崩溃"** | 自动采集日志 → 定位 FATAL EXCEPTION → 分析根因 → 修复 |
| **"抓日志"** | 等同于 "开始" |
| **"分析日志"** | 读取已有日志文件，分析问题 |

### 2.2 工作流

```
用户说"开始"
  → Agent 后台启动 adb logcat 采集，日志保存到 temp/logs/
  → 提示用户 "日志采集已启动，请在设备上操作复现问题，完成后说'结束'"

用户说"结束"
  → Agent 停止 logcat
  → 读取日志文件
  → 搜索 FATAL EXCEPTION、ANR、RuntimeException、Caused by 等关键字
  → 按包名过滤，定位项目代码中的崩溃点
  → 输出根因分析 + 修复建议
```

### 2.3 手动替代方案（ADB 不可用时）

如果无法自动采集日志，Agent 应：

1. 提示用户手动执行 `adb logcat > temp/logs/logcat.log`
2. 用户操作后，将日志文件路径告知 Agent
3. Agent 读取日志文件进行自动分析

---

## 三、项目文档导航

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

### 3.1 文档分域管理规则
详见 [PROJECT_STRUCTURE.md](docs/PROJECT_STRUCTURE.md) 第 3.6 节：域定义、域标签格式、域归属规则。

---

## 四、任务管理

### 4.1 任务拆解
- 理解用户意图，分析问题，做出总结
- 制定执行大纲（含具体步骤），让用户确认后再执行
- 不确定的步骤或流程向用户确认
- 任务完成后，确认大纲全部步骤都已执行完毕

### 4.2 任务计划
- 每次对话开始时读取 [PLAN.md](docs/PLAN.md) 确认进度
- 任务完成后更新 PLAN.md，记录：目标、完成情况、日期、总结

### 4.3 全局记忆
- 每次对话开始时读取 [MEMORY.md](docs/MEMORY.md)（不存在则创建）
- 加载其中的「术语词典」和「映射规则」作为用户表达习惯
- 需要澄清时，将用户表达习惯沉淀到 MEMORY.md
- 优先级：当前对话中的明确说明 > MEMORY.md 中的规则 > 默认理解

---

## 五、场景规则加载

当任务涉及以下场景时，加载 [AGENT_RULES.md](docs/AGENT_RULES.md)：

| 场景 | 触发条件 |
|------|---------|
| 代码修改 | 修改 .kt/.java/.xml/.dart 文件 |
| 构建配置 | 修改 build.gradle / settings.gradle / versions.gradle / .gradle 文件 |
| 编译构建 | 执行构建命令 |
| 错误处理 | 编译失败或运行时崩溃 |
| Git 操作 | 提交、推送、合并代码 |

---

## 六、显示语言

显示文字使用中文。