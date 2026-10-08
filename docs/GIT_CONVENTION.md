# Git 提交规范（RT）

> **所属域**: 规范域 | **加载策略**: 按需加载
> **版本**: 2.0.0 | **创建日期**: 2026-08-06
> **适用项目**: RT（Kotlin Multiplatform）

统一采用 **Conventional Commits**，保证提交历史清晰、可回溯、可支撑自动 changelog / version。

---

## 1. Commit Message 格式

```
<type>(<scope>): <subject>

<body (可选，空一行)>

# <footer 可选：BREAKING CHANGE / 关联 issue>
```

- **type**：必填。
- **scope**：可选，填影响模块（`androidApp`、`app`、`core`、`shared`、`home`、`user`、`docs`、`build` 等）。
- **subject**：必填，简明概括，**一般小写、不定式，中文可用完成式**，控制在 50 字符内。
- 冒号后要空一格。

### 1.1 type 取值

| type | 含义 |
|------|------|
| `feat` | 新功能 |
| `fix` | 修复缺陷 |
| `refactor` | 重构（功能不变） |
| `perf` | 性能优化 |
| `docs` | 文档新增/调整 |
| `style` | 样式/格式（不影响逻辑） |
| `build` | 构建脚本 / 依赖 |
| `chore` | 杂项（框架、配置、维护） |
| `test` | 测试相关 |
| `revert` | 回滚 |

### 1.2 示例

```
feat(home): 首页新增推荐位模块
fix(core): 修复 SQLDelight 连接未关闭导致的泄漏
refactor(shared): 抽取通用缓存封装
docs: 新增 GIT_CONVENTION 规范
build: 升级 AGP 至 9.2.1
BREAKING CHANGE: Home 路由契约签名调整
```

---

## 2. 分支命名规范

采用 `type/描述` 形式，描述用 kebab-case：

| 场景 | 格式 | 示例 |
|------|------|------|
| 功能 | `feat/<描述>` | `feat/home-list` |
| 修复 | `fix/<描述>` | `fix/user-login-crash` |
| 重构 | `refactor/<描述>` | `refactor/network-layer` |
| 文档 | `docs/<描述>` | `docs/project-structure` |
| 主干 | `main`（保护分支，禁止直接提交） | — |
| 开发集成分支 | `develop` | — |

---

## 3. 提交工作流

```
1. 本地编码 + 自测（通过 :androidApp:assembleDebug）
2. git status / git diff 审阅改动
3. git add <仅本次相关文件>（禁止 git add -A 盲目全加）
4. git commit -m "<type>(<scope>): <subject>"
5. 推送到对应 feature 分支 → 发起 PR 合并到 develop/main
```

### 3.1 规范要点

- **只暂存计划内文件**：避免把无关改动/调试文件一起提交。
- **禁止提交敏感信息**：密钥、token、`local.properties`、`.env` 一律进 `.gitignore`。
- **一次提交一个语义**：大改动拆多个小提交。
- **提交前自查**：是否含调试残留、未用代码、格式问题。

---

## 4. 提交信息：遵循规则（Do / Don't）

| ✅ 做 | ❌ 不要 |
|-------|--------|
| subject 简洁、聚焦单一改动 | 不做大而全的一次提交 |
| 描述清楚「为什么」改动 | 不写模糊的「修一下」「改代码」 |
| 变更涉及 breaking 时加 footer | 不在 message 中夹带无关信息 |
| 复用已有技术栈缩写（如 `feat(home)`）| 不发表无 scope 的含糊 commit |

---

## 5. Tag 规范（版本发布）

| 规则 | 说明 |
|------|------|
| 格式 | `v<major>.<minor>.<patch>`，如 `v1.2.0` |
| 前缀 | 统一 `v`，全小写 |
| 触发 | 每次发版 / 里程碑收口时打 tag |
| 记录 | 与 changelog 同步；可用于定位版本对应构建 |

- **SemVer**：主版本（不兼容）、次版本（新增功能向下兼容）、补丁（修复）。
- 分支合并并验收后才可打 tag。

---

* 相关：代码改动规则见 [AGENT_RULES.md](AGENT_RULES.md)；模块结构见 [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)。