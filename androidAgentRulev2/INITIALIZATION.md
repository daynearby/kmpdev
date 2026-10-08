# Android Agent Rule v2 项目初始化执行规则

> **用途**: 定义 Agent 执行项目初始化的完整流程
> **触发条件**: 用户说"根据规则初始化一下项目"等指令
> **所属域**: 核心域 | **加载策略**: 按需加载（触发初始化时加载）
> **版本**: 2.0.0 | **创建日期**: 2026-07-14

---

## 零、前置说明

本文件是 Agent 的执行手册。Agent 读取本文件后，必须严格按照以下阶段逐步执行。

---

## 一、环境检查阶段

### 1.1 自动检测清单

Agent 自动检查以下内容：

| 检查项 | 检查方式 | 通过条件 |
|--------|---------|---------|
| 项目根目录 | 检查当前工作目录 | 目录存在 |
| Android 项目 | 检查 `build.gradle` 或 `build.gradle.kts` | 至少一个存在 |
| 模块注册 | 检查 `settings.gradle` 或 `settings.gradle.kts` | 至少一个存在 |
| 规则文件夹 | 检查 `androidAgentRulev2/` 目录 | 目录存在且包含 INITIALIZATION.md |
| 已初始化检查 | 检查 `docs/` 目录 | 如已存在，进入冲突处理 |

### 1.2 Android CLI 环境检测

| 检查项 | 检测命令 | 预期结果 |
|--------|---------|---------|
| Android SDK | 读取 `ANDROID_HOME`/`ANDROID_SDK_ROOT` | 环境变量存在 |
| ADB | `adb version` | 版本号输出 |
| Gradle | `gradlew --version` | 版本号输出 |
| Kotlin | `kotlinc -version` | 版本号输出 |
| SDK Components | `sdkmanager --list_installed` | 组件列表 |

### 1.3 CLI 安装建议

如果 CLI 环境检测不通过，Agent 应：

1. 读取 [ANDROID_CLI_INSTALL.md](ANDROID_CLI_INSTALL.md) 获取安装指南
2. 根据检测结果给出针对性安装建议
3. 提供自动安装（CLI）和手动安装两种方案

### 1.4 冲突处理

如果 `docs/` 目录已存在：

```
Agent: 检测到项目中已存在 docs/ 目录，可能已完成过初始化。
       请选择：
       1. 跳过初始化（仅查看当前状态）
       2. 覆盖更新（保留已有文件，仅补充缺失文件）
       3. 完全重新初始化（⚠️ 将覆盖所有 docs/ 内容）
```

---

## 二、信息收集阶段

### 2.1 自动检测项

Agent 从项目文件中自动读取，**无需询问用户**：

| 信息 | 读取来源 | 读取方式 |
|------|---------|---------|
| Kotlin 版本 | `build.gradle` / `versions.gradle` | 搜索 `kotlin` 版本定义 |
| AGP 版本 | `build.gradle` | 搜索 `com.android.tools.build:gradle` |
| Gradle 版本 | `gradle/wrapper/gradle-wrapper.properties` | 搜索 `distributionUrl` |
| compileSdk | `app/build.gradle` | 搜索 `compileSdk` |
| minSdk | `app/build.gradle` | 搜索 `minSdk` |
| 模块列表 | `settings.gradle` | 搜索 `include` 语句 |
| 是否包含 Flutter | 检查 `module_flutter` 目录 | 目录存在性检查 |
| 是否使用 Compose | 搜索 `compose` 依赖 | 依赖存在性检查 |
| 是否使用 Navigation | 搜索 `navigation` 依赖 | 依赖存在性检查 |
| 是否使用 Room | 搜索 `room` 依赖 | 依赖存在性检查 |
| 是否使用 Retrofit | 搜索 `retrofit` 依赖 | 依赖存在性检查 |

### 2.2 需要询问用户

| 信息 | 默认值 |
|------|--------|
| 项目名称 | 无，必填 |
| 项目描述 | "Android 应用" |
| 包名前缀 | 从 `applicationId` 自动提取 |
| 是否启用 Flutter 混合 | 根据检测结果推荐 |
| 是否启用 ARouter | 根据检测结果推荐 |

### 2.3 信息确认

展示收集到的信息，等待用户确认：

```
Agent: 项目信息收集完成，请确认：
       项目名称: {{PROJECT_NAME}}
       项目描述: {{PROJECT_DESC}}
       包名前缀: {{PACKAGE_NAME}}
       Kotlin: {{KOTLIN_VERSION}} | AGP: {{AGP_VERSION}} | Gradle: {{GRADLE_VERSION}}
       compileSdk: {{COMPILE_SDK}} | minSdk: {{MIN_SDK}}
       模块数量: {{MODULE_COUNT}} 个
       检测到框架: {{DETECTED_FRAMEWORKS}}
```

---

## 三、安全确认阶段

展示完整操作清单，等待用户确认：

```
Agent: 即将执行以下操作，请确认：

       【创建目录】
       - docs/、docs/skills/、docs/migration/、docs/optimize/、docs/future/
       - temp/logs/、temp/script/

       【部署 AGENTS.md】（更新为正式版，去除初始化相关内容）
       - AGENTS.md（项目根目录）

       【部署规则文件】（从规则文件夹复制，6 个）
       - docs/AGENT_RULES.md
       - docs/PROJECT_STRUCTURE.md
       - docs/GIT_CONVENTION.md
       - docs/MEMORY.md
       - docs/PLAN.md
       - docs/DOCUMENT_GENERATION_RULES.md

       【部署技能文档】（从规则文件夹复制，根据检测结果选择）
       - 通用技能（9 个）: kotlin、gradle-build、upgrade、api-separation
                          compose、navigation、room、retrofit、datastore
       - 框架技能（可选）: arouter（{{USE_AROUTER}}）
                          flutter-add-to-app（{{USE_FLUTTER}}）
                          flutter-migration（{{USE_FLUTTER}}）

       【生成文件】（根据项目信息填充模板）
       - docs/README.md（文档索引）
       - docs/GETTING_STARTED.md（新人指南）
       - docs/BUILD_CONFIG.md（构建配置）
       - docs/reference-resource-documents.md（参考资源）

       【为每个模块生成】
       - <module>/DOMAIN.md（共 {{MODULE_COUNT}} 个）

       是否确认执行？
```

---

## 四、文件部署阶段

### 4.1 AGENTS.md 更新

初始化完成后，将项目根目录的 `AGENTS.md` 替换为正式版（去除初始化相关内容）：

| 源文件 | 目标文件 |
|--------|---------|
| `androidAgentRulev2/templates/AGENTS.md` | `AGENTS.md`（项目根目录） |

> 正式版 `AGENTS.md` 不再包含「初始化触发指令」章节，后续使用无需再触发初始化。

### 4.2 规则文件部署

从 `androidAgentRulev2/docs/` 复制到项目 `docs/`：

| 源文件 | 目标文件 |
|--------|---------|
| `androidAgentRulev2/docs/AGENT_RULES.md` | `docs/AGENT_RULES.md` |
| `androidAgentRulev2/docs/PROJECT_STRUCTURE.md` | `docs/PROJECT_STRUCTURE.md` |
| `androidAgentRulev2/docs/GIT_CONVENTION.md` | `docs/GIT_CONVENTION.md` |
| `androidAgentRulev2/docs/MEMORY.md` | `docs/MEMORY.md` |
| `androidAgentRulev2/docs/PLAN.md` | `docs/PLAN.md` |
| `androidAgentRulev2/docs/DOCUMENT_GENERATION_RULES.md` | `docs/DOCUMENT_GENERATION_RULES.md` |

### 4.3 技能文档部署（优化方案）

根据项目检测结果，智能选择部署的技能文档：

#### 4.3.1 通用技能（全部部署）

| 源文件 | 目标文件 |
|--------|---------|
| `androidAgentRulev2/docs/skills/kotlin-SKILL.md` | `docs/skills/kotlin-SKILL.md` |
| `androidAgentRulev2/docs/skills/android-gradle-build-SKILL.md` | `docs/skills/android-gradle-build-SKILL.md` |
| `androidAgentRulev2/docs/skills/android-upgrade-SKILL.md` | `docs/skills/android-upgrade-SKILL.md` |
| `androidAgentRulev2/docs/skills/api-implementation-separation-SKILL.md` | `docs/skills/api-implementation-separation-SKILL.md` |
| `androidAgentRulev2/docs/skills/jetpack-compose-SKILL.md` | `docs/skills/jetpack-compose-SKILL.md` |
| `androidAgentRulev2/docs/skills/navigation-SKILL.md` | `docs/skills/navigation-SKILL.md` |
| `androidAgentRulev2/docs/skills/room-database-SKILL.md` | `docs/skills/room-database-SKILL.md` |
| `androidAgentRulev2/docs/skills/retrofit-network-SKILL.md` | `docs/skills/retrofit-network-SKILL.md` |
| `androidAgentRulev2/docs/skills/datastore-SKILL.md` | `docs/skills/datastore-SKILL.md` |

#### 4.3.2 框架技能（条件部署）

| 源文件 | 目标文件 | 部署条件 |
|--------|---------|---------|
| `androidAgentRulev2/docs/skills/arouter-SKILL.md` | `docs/skills/arouter-SKILL.md` | 项目使用 ARouter |
| `androidAgentRulev2/docs/skills/flutter-add-to-app-SKILL.md` | `docs/skills/flutter-add-to-app-SKILL.md` | 项目包含 Flutter |
| `androidAgentRulev2/docs/skills/flutter-migration-SKILL.md` | `docs/skills/flutter-migration-SKILL.md` | 项目包含 Flutter |

**部署方式**: 使用 Read 读取源文件，使用 Write 写入目标位置。

### 4.4 模板文件生成

读取 `androidAgentRulev2/templates/` 中的模板，替换占位符后生成：

| 模板文件 | 目标文件 |
|---------|---------|
| `androidAgentRulev2/templates/BUILD_CONFIG.md` | `docs/BUILD_CONFIG.md` |
| `androidAgentRulev2/templates/GETTING_STARTED.md` | `docs/GETTING_STARTED.md` |
| `androidAgentRulev2/templates/reference-resource-documents.md` | `docs/reference-resource-documents.md` |

### 4.5 索引文件生成

根据实际模块列表和框架配置，使用 `androidAgentRulev2/templates/README.md` 模板生成 `docs/README.md`。

---

## 五、模块扫描与 DOMAIN.md 生成阶段

### 5.1 模块识别

从 `settings.gradle` 提取所有模块，按规则分类：

| 分类 | 识别规则 | 示例 |
|------|---------|------|
| 主入口 | 名称 `app` | `:app` |
| 业务组件 | 名称以 `app_` 开头 | `:app_user` |
| 公共组件 | 名称以 `lib_` 开头 | `:lib_components` |
| 基础组件 | 名称以 `lib_` 开头 | `:lib_base` |
| 路由模块 | 名称含 `router` / `arouter` | `:lib_router` |
| Flutter 模块 | 名称含 `flutter` | `:flutter`, `:module_flutter` |

### 5.2 模块分析

对每个模块：
1. 读取 `build.gradle` → 获取 plugin 类型、namespace、依赖
2. 扫描 `src/main/java/` 或 `src/main/kotlin/` → 获取包结构
3. 识别核心类 → Activity、Fragment、ViewModel、Compose 组件等
4. 提取依赖关系 → 从 dependencies 块提取

### 5.3 DOMAIN.md 生成

使用 `androidAgentRulev2/templates/DOMAIN.md` 模板，填充分析结果后写入各模块根目录。

---

## 六、结果汇报阶段

```
Agent: ✅ 项目初始化完成！

       【更新 AGENTS.md】
       - AGENTS.md（项目根目录，已替换为正式版，去除初始化内容）

       【创建的目录】（7 个）
       - docs/、docs/skills/、docs/migration/、docs/optimize/、docs/future/
       - temp/logs/、temp/script/

       【部署的规则文件】（6 个）
       - docs/AGENT_RULES.md、docs/PROJECT_STRUCTURE.md 等

       【部署的技能文档】（{{SKILL_COUNT}} 个）
       - 通用技能（9 个）: kotlin、gradle-build、upgrade、api-separation
                          compose、navigation、room、retrofit、datastore
       - 框架技能（{{FRAMEWORK_COUNT}} 个）: {{FRAMEWORK_LIST}}

       【生成的项目文档】（4 个）
       - docs/README.md、docs/BUILD_CONFIG.md、docs/GETTING_STARTED.md
       - docs/reference-resource-documents.md

       【生成的模块文档】（{{MODULE_COUNT}} 个）
       - app/DOMAIN.md 等

       【后续使用】
       - 技能文档索引见 androidAgentRulev2/SKILLS_MAP.md
       - 日常开发规则请参考 docs/AGENT_RULES.md
       - Android CLI 安装指南见 androidAgentRulev2/ANDROID_CLI_INSTALL.md
```

---

## 七、占位符定义

| 占位符 | 来源 | 说明 |
|--------|------|------|
| `{{PROJECT_NAME}}` | 用户输入 | 项目名称 |
| `{{PROJECT_DESC}}` | 用户输入 | 项目描述 |
| `{{PACKAGE_NAME}}` | 自动提取 | 包名前缀 |
| `{{KOTLIN_VERSION}}` | 自动检测 | Kotlin 版本 |
| `{{AGP_VERSION}}` | 自动检测 | AGP 版本 |
| `{{GRADLE_VERSION}}` | 自动检测 | Gradle 版本 |
| `{{COMPILE_SDK}}` | 自动检测 | compileSdk |
| `{{MIN_SDK}}` | 自动检测 | minSdk |
| `{{MODULE_COUNT}}` | 自动检测 | 模块数量 |
| `{{SKILL_COUNT}}` | 自动计算 | 部署的技能文档数量 |
| `{{FRAMEWORK_COUNT}}` | 自动计算 | 部署的框架技能数量 |
| `{{FRAMEWORK_LIST}}` | 自动检测 | 检测到的框架列表 |
| `{{DETECTED_FRAMEWORKS}}` | 自动检测 | 检测到的框架 |
| `{{USE_AROUTER}}` | 用户选择 | 是否使用 ARouter |
| `{{USE_FLUTTER}}` | 用户选择 | 是否使用 Flutter |
| `{{DATE}}` | 当前日期 | 初始化日期 |

---

## 八、安全约束

1. **绝不覆盖已有文件**：部署前检查目标文件是否存在，已存在则询问用户
2. **绝不修改源代码**：初始化过程不修改任何 .kt/.java/.xml/.dart 文件
3. **绝不修改构建配置**：不修改 build.gradle、settings.gradle 等
4. **绝不执行构建**：初始化过程不触发任何编译操作
5. **可中断**：用户可随时要求停止，已执行的操作不影响项目正常开发

---

## 九、迁移方案对比（v1 vs v2）

| 维度 | v1 版本 | v2 版本 | 优化点 |
|------|---------|---------|--------|
| 技能文档数量 | 7 个 | 12 个 | 新增 5 个核心技能（Compose、Navigation、Room、Retrofit、DataStore） |
| 技能分类 | 简单分类（通用/框架） | 三层架构（基础/开发/框架） | 更清晰的技能依赖关系 |
| CLI 支持 | 无 | 完整 CLI 安装机制 | 支持自动检测和安装 |
| 部署策略 | 全部部署 | 条件部署（按需） | 根据项目检测结果智能选择 |
| 参考资源 | 无 | 集成参考资源文档 | 统一管理技术栈参考链接 |
| 版本管理 | 简单版本号 | 语义化版本 + 变更日志 | 清晰的版本演进记录 |