# Agent 代码操作规则

> **所属域**: 核心域 | **加载策略**: 必加载
> **版本**: 2.0.0 | **创建日期**: 2026-08-06
> **适用项目**: RT（Kotlin Multiplatform：Android + iOS）

本文件定义代码修改、构建、错误处理、Git 操作的核心执行规则。与 `AGENTS.md`、`GIT_CONVENTION.md` 配合使用。

---

## 一、执行流程

所有代工任务必须遵循以下流程，不可跳步：

```
1. 理解意图    → 读取相关代码/文档，明确用户要什么
2. 制定大纲   → 拆解为子任务，标注依赖（DAG），输出执行大纲
3. 用户确认   → 涉及多文件/多步骤时，先让用户确认再动手
4. 并发执行   → 无依赖子任务同轮并发派发；有依赖的等上游完成即触发
5. 验证       → 构建/测试/自查，确认无回归
```

> **铁律**：含 ≥2 个步骤的任务，必须先拆细再执行，不默认串行。见全局 AGENTS.md「并发执行铁律」。

---

## 二、代码修改规则

### 2.1 通用约束

| 约束 | 说明 |
|------|------|
| 遵循他人约定 | 先看周边文件风格，模仿；不新增注释 |
| 复用现有组件 | 新增前先查是否已有类似组件/工具类 |
| 技术栈一致性 | RT 统一使用：Compose Multiplatform、Navigation KMP、Ktorfit、SQLDelight、MMKV、Koin、kermit/xlog、Coil、kotlinx-serialization、kotlinx-coroutines。**禁止引入栈外替代方案** |
| 分模块落位 | KMP 公共代码放 `shared`/`app`，平台差异用 `expect/actual`，Android 壳仅放 Application 入口。见 `PROJECT_STRUCTURE.md` |
| 无注释 | 除非用户明确要求，否则不写注释 |

### 2.2 修改流程

1. 先在目标模块 `DOMAIN.md`（如有）确认模块职责与边界。
2. 查看对应技能文档（如修改 `.kt` 先读 `docs/skills/kotlin-SKILL.md`）。
3. 小步修改，保持单一职责；跨模块改动先确认依赖方向（feature 依赖 shared，禁止反向）。
4. 修改后同步更新关联文档（若涉及模块职责需更新 `DOMAIN.md`）。

### 2.3 Compose / KMP 注意事项

- 公共代码用 `commonMain`，Android 用 `androidMain`，iOS 用 `iosMain`，平台差异声明 `expect/actual`。
- 依赖注入统一用 Koin；网络统一用 Ktor + Ktorfit；日志统一用 kermit/kmp-xlog。
- 新增依赖必须同步更新 `gradle/libs.versions.toml` 与模块 `build.gradle.kts`。

---

## 三、编译与构建规则

### 3.1 构建命令

| 场景 | 命令 |
|------|------|
| Android 调试包 | `.\gradlew :androidApp:assembleDebug` |
| 全量构建 | `./gradlew build` |
| 清理 | `./gradlew clean` |

### 3.2 构建环境

| 项 | 值 |
|----|----|
| Gradle | 9.4.1 |
| AGP | 9.2.1 |
| Kotlin | 2.4.0 |
| compileSdk | 36 / 37 |
| targetSdk | 36 |
| minSdk | 24 |

### 3.3 构建与判断规则

- 主壳入口为 `:androidApp`（Android Application），**任何验证均以 `:androidApp:assembleDebug` 通过为完成标志**。
- 构建输出出现 `BUILD SUCCESSFUL`（退出码 0）判定成功；出现 `FAILED`/`error`/`Permission denied` 立即终止并定位日志。
- 长任务构建带超时判断，超时上报最后一次状态快照，不无限等待。

### 3.4 新增依赖

- 依赖版本优先放入 `gradle/libs.versions.toml`，禁止硬编码版本号。
- 尽量用 `implementation` 局部暴露即可的，高层用 `api` 合理隔离；见 `api-implementation-separation-SKILL.md`。

---

## 四、错误处理流程

```
出现编译/运行时错误
  → 定位根因（日志/堆栈/关键字）
  → 判断归属模块
  → 修复 → 重新构建 :androidApp:assembleDebug → 验证通过
```

### 4.1 编译错误

- 定位报错文件与行号，结合技能文档（Gradle 构建、KSP、依赖冲突）处理。
- 依赖冲突 / KSP 问题见 `docs/skills/android-gradle-build-SKILL.md`。

### 4.2 运行时崩溃（闪退/ANR）

- 通过 ADB logcat 采集日志（见 `AGENTS.md` 第二章「ADB 日志自动化工作流」）。
- 按包名过滤，定位 `FATAL EXCEPTION` / `ANR` / `Caused by`，分析根因后修复。

---

## 五、Git 操作规则

- 提交信息格式严格遵循 `docs/GIT_CONVENTION.md`（`type(scope): subject`）。
- 仅在用户明确要求时执行 commit / push / 合并。
- 提交前检查 `git status`、`git diff`，只暂存计划内的文件，绝不提交秘钥/敏感信息。
- 分支命名规则见 `GIT_CONVENTION.md`。

---

## 六、安全红线（不可违反）

| 红线 | 说明 |
|------|------|
| 不提交密钥 | API Key / token / 密文/ 生产密钥严禁入库 |
| 不打印敏感信息 | 日志中禁止输出 token、密码、身份证号等 |
| 不越权改配置 | 未经确认不擅自改 `build.gradle` 平台配置之外的全局配置 |
| 不在用户未要求时提交 | Git 提交必须由用户显式触发 |
| 平台隔离 | 不把 Android 专属 API 泄漏到 iOS 代码，避免 `androidMain` 之外误用 Android 类 |

---

## 相关文档

| 文档 | 作用 |
|------|------|
| [AGENTS.md](AGENTS.md) | 全局规范与技能速查、ADB 工作流 |
| [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) | 项目结构、模块分层、命名规范 |
| [GIT_CONVENTION.md](GIT_CONVENTION.md) | Git 提交规范 |
| [MEMORY.md](MEMORY.md) | 术语词典、用户偏好 |
| [PLAN.md](PLAN.md) | 任务进度跟踪 |