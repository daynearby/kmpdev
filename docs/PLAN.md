# 任务计划（RT）

> **所属域**: 核心域 | **加载策略**: 必加载
> **版本**: 2.0.0 | **创建日期**: 2026-08-06
> **说明**: 记录 RT 项目各阶段任务进度，会话开始先读取，任务完成后更新。

---

## 1. 进行中任务

| 任务 | 负责人 | 状态 | 目标完成日 | 备注 |
|------|--------|------|-----------|------|
| 项目规范文档体系初始化（6 份文档） | Agent | 已完成 | 2026-08-06 | 生成 AGENT_RULES / PROJECT_STRUCTURE / GIT_CONVENTION / MEMORY / PLAN / DOCUMENT_GENERATION_RULES |
| 模块脚手架搭建 | — | 待启动 | — | androidApp / app / core / shared / feature:home / feature:user |
| 技术栈落地验证 | — | 待启动 | — | Ktorfit + SQLDelight + MMKV + Koin 在 shared/core 打通 |

---

## 2. 待办

- [x] 初始化 6 份规范文档并验收（字节、结构、域标签）
- [x] 建立模块目录与 Gradle 模块注册（`settings.gradle.kts`、`libs.versions.toml`）
- [ ] 构建冒烟：`.\gradlew :androidApp:assembleDebug` 通过
- [ ] 首页 feature:home 骨架 + 导航路由（Navigation KMP）
- [ ] 用户 feature:user 骨架
- [ ] Ktorfit + SQLDelight + MMKV 接入 core
- [ ] 依赖注入（Koin）根装配到 app
- [ ] 日志（kermit/kmp-xlog）接入
- [ ] 各模块 DOMAIN.md 与技能文档按需补齐

---

## 3. 已完成记录

| 日期 | 完成内容 | 验证结果 |
|------|---------|---------|
| 2026-08-06 | 项目初始化：AGENTS.md v2.0.0、文档分域目录（docs/skills、docs/migration、docs/optimize、docs/future） | 已就绪 |
| 2026-08-06 | 初始化 6 份规范文档（本次任务） | 已验收 |
| 2026-08-06 | 部署 9 个技能文档到 docs/skills/ | 已完成 |
| 2026-08-06 | 生成 4 份项目文档（README/BUILD_CONFIG/GETTING_STARTED/reference-resource） | 已完成 |
| 2026-08-06 | 生成 6 个模块 DOMAIN.md | 已完成 |

---

## 4. 总结（最近一轮）

- **已完成**：核心规范文档体系建立（行为规则、结构规范、Git 规范、记忆、计划、文档生成规则）。
- **未完成**：模块代码脚手架与技术栈实际落地尚未启动。
- **待改进**：后续每个功能任务完成后需回到本文件更新状态，保持进度实时。

---

* 记忆与偏好见 [MEMORY.md](MEMORY.md)；执行规则见 [AGENT_RULES.md](AGENT_RULES.md)。