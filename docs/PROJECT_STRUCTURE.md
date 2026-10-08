# 项目结构规范（RT）

> **所属域**: 规范域 | **加载策略**: 按需加载
> **版本**: 2.0.0 | **创建日期**: 2026-08-06
> **适用项目**: RT（Kotlin Multiplatform：Android + iOS）

---

## 1. 项目概览

RT 是一个 Kotlin Multiplatform 跨平台应用（Android + iOS），使用 Compose Multiplatform 共享 UI。

- **构建栈**：Gradle 9.4.1 / AGP 9.2.1 / Kotlin 2.4.0
- **SDK**：compileSdk 36/37、minSdk 24、targetSdk 36
- **主壳模块**：`:androidApp`（Android Application，构建入口 `./gradlew :androidApp:assembleDebug`）

---

## 2. 根目录标准结构

```
RT/
├── androidApp/            # Android 应用壳（唯一 Application 入口）
├── app/                   # KMP 根组件：导航路由表、根依赖注入、Compose 根树
├── core/                  # 基础能力层：网络/DB/缓存/日志/依赖注入的公共封装
├── shared/                # 共享层：跨模块公共模型、常量、工具（KMP common）
├── feature/
│   ├── home/              # 首页功能模块
│   └── user/              # 用户功能模块
├── gradle/
│   ├── libs.versions.toml # 版本目录（依赖版本统一管理）
│   └── wrapper/
├── docs/                  # 文档体系（分 5 域，见第 3 节）
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── AGENTS.md              # Agent 核心规范文件
```

### 2.1 模块职责与依赖方向

模块间只允许**单向向下依赖**，禁止反向依赖、禁止循环依赖：

```
androidApp ──→ app ──→ feature:home / feature:user
                 ↑            ↑
                 └──── core ◄─┘
                        ↑
                     shared
```

| 模块 | 类型 | 职责 | 允许依赖 |
|------|------|------|---------|
| `:androidApp` | Android Application | 壳入口、Application 初始化 | app、feature... |
| `:app` | KMP | 根组件、Navigation KMP 路由、Koin 根装配 | core、shared、feature... |
| `:core` | KMP | 基础封装：network/DB/cache/log/DI 通用能力 | shared |
| `:shared` | KMP | 公共模型、DTO、枚举、util | （不依赖 feature） |
| `:feature:home` | KMP | 首页功能 | core、shared、app（路由契约） |
| `:feature:user` | KMP | 用户功能 | core、shared、app（路由契约） |

> **约束**：feature 模块**不**允许反向依赖或被 core 外的模块反向侵入；feature 之间不互相依赖，跳转通过 app 提供的路由契约完成。

---

## 3. docs/ 文档体系（5 域）

文档统一按 **5 个域** 组织，域归属与加载策略如下：

| 域 | 职责边界 | 存放位置 | 典型文档 | 加载策略 |
|----|---------|---------|---------|---------|
| **核心域** | Agent 行为准则、用户记忆、任务进度 | `docs/` | AGENTS.md、AGENT_RULES.md、MEMORY.md、PLAN.md | 必加载 |
| **规范域** | 开发/文档/命名/构建规范 | `docs/` | PROJECT_STRUCTURE.md、GIT_CONVENTION.md、DOCUMENT_GENERATION_RULES.md | 按需加载 |
| **知识域** | 技术实现细节、模块职责、技能参考 | `docs/skills/`、模块内 `DOMAIN.md` | kotlin-SKILL.md、navigation-SKILL.md、DOMAIN.md | 按需加载 |
| **管理域** | 迁移/优化方案、需求跟踪 | `docs/migration/`、`docs/optimize/`、`docs/future/` | 迁移方案、优化方案、需求文档 | 按需加载 |
| **运维域** | 环境搭建、CI/CD、新人上手 | `docs/ops/` | GETTING_STARTED.md、CI 配置 | 按需加载 |

### 3.1 文档域标签规范

所有文档头部（第二行起）必须带域标签：

```
> **所属域**: 核心域 | **加载策略**: 必加载
```

- 核心域文档（AGENTS.md、AGENT_RULES.md、MEMORY.md、PLAN.md）→ `必加载`
- 其余 4 域 → `按需加载`

### 3.2 存放约定

- `docs/`：核心域 + 规范域根级文档。
- `docs/skills/`：知识域技能文档（`*.SKILL.md`），每个技能一个文件。
- `docs/migration/`：迁移类方案。
- `docs/optimize/`：优化类方案。
- `docs/future/`：需求/规划类文档。
- 各 KMP 模块根目录可放 `DOMAIN.md`（模块职责说明）。

---

## 4. 模块命名与分层依赖原则

### 4.1 命名规范

- **模块名**：小写 kebab-case（`androidApp`、`core`、`shared`、`feature:home`、`feature:user`）。
- **包名**：`com/rt/<level>` 下按功能划分，如 `com.rt.shared.model`、`com.rt.feature.home`。
- **KMP 源集**：`commonMain` / `androidMain` / `iosMain`。
- 目录层级与命名需可读、见名知义。

### 4.2 分层依赖原则

1. **单向依赖**：只允许 `androidApp → app → feature → core → shared` 的向下依赖，禁止反向。
2. **依赖最小化**：各模块只暴露必要 API，内部实现不外泄；高层用 `implementation`，契约用 `api` 再确实需要。
3. **共享资产**：公共模型/工具一律放 `shared`，不散落在各 feature。

---

## 5. 文件命名规范（kebab-case）

- **文档**：`kebab-case.md`（如 `git-convention.md`、`document-generation-rules.md`）。
- **技能文档**：`<技术>-SKILL.md`（如 `jetpack-compose-SKILL.md`）。
- **代码**：Kotlin 类文件用 PascalCase（`UserRepository.kt`）；资源/drawable/layout 用 `snake_case`；KMP 目录统一 kebab-case。
- 命名表达意图：文件命名即职责（`网络封装` 命名为 `network` 相关）。

---

## 6. 新目录 / 新模块创建清单

在新建目录或模块前，按以下清单逐项检查（含文档备案）：

1. [ ] 确认业务归属域与 doc 域，明确加载策略与整理入口。
2. [ ] 确认该能力现有模块未提供，避免重复实现。
3. [ ] 命名符合 `kebab-case` / 模块命名规范。
4. [ ] `settings.gradle.kts` 注册新模块；`libs.versions.toml` 登记依赖版本。
5. [ ] 遵守单向依赖，不引入循环依赖；检查是否用到 `:app` 路由契约。
6. [ ] 按需拆分到对应文档属性域（DOMAIN.md / 管理域文档）。
7. [ ] 代码评审：工程发布前通过 `:androidApp:assembleDebug` 验证依赖与新结构。

---

* 生成规范见 [DOCUMENT_GENERATION_RULES.md](DOCUMENT_GENERATION_RULES.md)；核心执行规则见 [AGENT_RULES.md](AGENT_RULES.md)。