# {{PROJECT_NAME}} 构建配置文档

> **所属域**: 规范域 | **加载策略**: 按需加载

---

## 一、构建环境概览

| 配置项 | 值 |
|--------|-----|
| Gradle | {{GRADLE_VERSION}} |
| Android Gradle Plugin | {{AGP_VERSION}} |
| Kotlin | {{KOTLIN_VERSION}} |
| KSP | {{KSP_VERSION}} |
| JVM Target | Java 17 |
| compileSdk / targetSdk | {{COMPILE_SDK}} |
| minSdk | {{MIN_SDK}} |

---

## 二、模块架构

### 2.1 模块分类总览（共 {{MODULE_COUNT}} 个模块）

{{MODULE_ARCHITECTURE}}

---

## 三、框架依赖检测

| 框架 | 检测结果 | 技能文档 |
|------|---------|---------|
| Jetpack Compose | {{HAS_COMPOSE}} | [jetpack-compose-SKILL.md](docs/skills/jetpack-compose-SKILL.md) |
| Navigation | {{HAS_NAVIGATION}} | [navigation-SKILL.md](docs/skills/navigation-SKILL.md) |
| Room | {{HAS_ROOM}} | [room-database-SKILL.md](docs/skills/room-database-SKILL.md) |
| Retrofit | {{HAS_RETROFIT}} | [retrofit-network-SKILL.md](docs/skills/retrofit-network-SKILL.md) |
| ARouter | {{HAS_AROUTER}} | [arouter-SKILL.md](docs/skills/arouter-SKILL.md) |
| Flutter | {{HAS_FLUTTER}} | [flutter-add-to-app-SKILL.md](docs/skills/flutter-add-to-app-SKILL.md) |

---

## 四、构建命令

```bash
# Debug 构建
.\gradlew :app:assembleDebug

# Release 构建
.\gradlew :app:assembleRelease

# 清理构建
.\gradlew clean :app:assembleDebug

# 查看依赖树
.\gradlew :app:dependencies

# 查看 KSP 生成代码
.\gradlew :app:kspDebugKotlin

# 性能分析
.\gradlew build --profile
```

---

## 五、签名配置

| 类型 | 配置来源 |
|------|----------|
| **debug** | Android 默认 debug 签名 |
| **release** | `keystore.properties` 文件 |

---

## 六、版本历史

| 日期 | 版本 | 变更内容 |
|------|------|----------|
| {{DATE}} | 2.0 | 初始版本，自动生成（Android Agent Rule v2） |