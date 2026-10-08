# Android CLI 技能安装指南

> **所属域**: 运维域 | **加载策略**: 按需加载
> **版本**: 2.0.0 | **创建日期**: 2026-07-14
> **用途**: 指导 Agent 通过 Android CLI 自动安装技能，或手动安装替代方案

---

## 一、概述

本指南提供两种安装方式：

1. **自动安装**：通过 Android CLI 命令行工具自动检测和安装所需组件
2. **手动安装**：当 CLI 不可用时，通过图形界面或手动操作安装

---

## 二、自动安装流程

### 2.1 检测阶段

Agent 在初始化时自动执行以下检测：

```powershell
# 1. 检测 Android SDK 路径
$env:ANDROID_HOME
$env:ANDROID_SDK_ROOT

# 2. 检测 ADB
adb version

# 3. 检测 Gradle
.\gradlew --version

# 4. 检测 Kotlin
kotlinc -version

# 5. 检测已安装的 SDK 组件
$sdkmanager = "$env:ANDROID_HOME\cmdline-tools\latest\bin\sdkmanager.bat"
& $sdkmanager --list_installed
```

### 2.2 安装阶段

根据检测结果，自动安装缺失组件：

#### 2.2.1 安装 Android SDK Command-line Tools

```powershell
# 创建目录
New-Item -ItemType Directory -Force -Path "$env:ANDROID_HOME\cmdline-tools"

# 下载并解压 commandlinetools
# 下载地址: https://developer.android.com/studio#command-tools
# 版本: commandlinetools-win-10406996_latest.zip
```

#### 2.2.2 安装 Build-Tools

```powershell
$sdkmanager = "$env:ANDROID_HOME\cmdline-tools\latest\bin\sdkmanager.bat"
& $sdkmanager "build-tools;34.0.0" --sdk_root="$env:ANDROID_HOME"
```

#### 2.2.3 安装 Platform

```powershell
$sdkmanager = "$env:ANDROID_HOME\cmdline-tools\latest\bin\sdkmanager.bat"
& $sdkmanager "platforms;android-34" --sdk_root="$env:ANDROID_HOME"
```

#### 2.2.4 安装 Platform-Tools

```powershell
$sdkmanager = "$env:ANDROID_HOME\cmdline-tools\latest\bin\sdkmanager.bat"
& $sdkmanager "platform-tools" --sdk_root="$env:ANDROID_HOME"
```

#### 2.2.5 安装 Emulator

```powershell
$sdkmanager = "$env:ANDROID_HOME\cmdline-tools\latest\bin\sdkmanager.bat"
& $sdkmanager "emulator" --sdk_root="$env:ANDROID_HOME"
```

#### 2.2.6 安装 Kotlin

```powershell
# 通过 SDK Manager 安装（如果可用）
$sdkmanager = "$env:ANDROID_HOME\cmdline-tools\latest\bin\sdkmanager.bat"
& $sdkmanager "kotlin;1.9.0" --sdk_root="$env:ANDROID_HOME"

# 或通过 Chocolatey 安装
choco install kotlin
```

#### 2.2.7 安装 NDK（可选）

```powershell
$sdkmanager = "$env:ANDROID_HOME\cmdline-tools\latest\bin\sdkmanager.bat"
& $sdkmanager "ndk;26.1.10909125" --sdk_root="$env:ANDROID_HOME"
```

---

## 三、手动安装方案

当 CLI 自动安装不可用时，采用以下手动安装步骤：

### 3.1 Android Studio 安装

| 步骤 | 操作 |
|------|------|
| 1 | 下载 Android Studio: https://developer.android.com/studio |
| 2 | 运行安装程序，选择"Custom"安装类型 |
| 3 | 在"Select Components"中勾选：Android SDK、Android SDK Build-Tools、Android Emulator |
| 4 | 指定 SDK 安装路径（建议：`C:\Users\<用户名>\AppData\Local\Android\Sdk`） |
| 5 | 完成安装 |

### 3.2 SDK 组件安装

通过 Android Studio SDK Manager 安装：

1. 打开 Android Studio → Settings → Appearance & Behavior → System Settings → Android SDK
2. **SDK Platforms** 标签页：
   - 勾选目标 Android 版本（如 Android 14 / API 34）
3. **SDK Tools** 标签页：
   - 勾选 "Android SDK Build-Tools"
   - 勾选 "Android SDK Platform-Tools"
   - 勾选 "Android Emulator"
   - 勾选 "Kotlin"（如需要）
   - 勾选 "NDK (Side by side)"（如需要）
4. 点击 "Apply" 安装选中组件

### 3.3 环境变量配置

配置以下环境变量：

| 变量名 | 值 | 说明 |
|--------|-----|------|
| ANDROID_HOME | `C:\Users\<用户名>\AppData\Local\Android\Sdk` | Android SDK 根目录 |
| PATH | 添加 `%ANDROID_HOME%\platform-tools` | ADB 命令 |
| PATH | 添加 `%ANDROID_HOME%\cmdline-tools\latest\bin` | SDK Manager 命令 |
| PATH | 添加 `%ANDROID_HOME%\emulator` | 模拟器命令 |

### 3.4 Gradle 安装

1. 下载 Gradle Wrapper（项目已包含）
2. 或手动下载：https://gradle.org/releases/
3. 配置环境变量 `GRADLE_HOME` 和 `PATH`

---

## 四、验证安装

安装完成后，验证以下命令是否可用：

```powershell
# 验证 ADB
adb version

# 验证 SDK Manager
sdkmanager --version

# 验证 Gradle
.\gradlew --version

# 验证 Kotlin
kotlinc -version

# 验证已安装组件
sdkmanager --list_installed
```

---

## 五、常见问题

### Q1: SDK Manager 命令找不到

**原因**: 环境变量未配置或配置错误

**解决**:
```powershell
# 检查环境变量
echo $env:ANDROID_HOME
echo $env:PATH

# 重新配置环境变量
$env:ANDROID_HOME = "C:\Users\<用户名>\AppData\Local\Android\Sdk"
$env:PATH += ";$env:ANDROID_HOME\cmdline-tools\latest\bin"
```

### Q2: ADB 命令找不到

**原因**: Platform-Tools 未安装或未添加到 PATH

**解决**:
```powershell
# 安装 Platform-Tools
sdkmanager "platform-tools"

# 添加到 PATH
$env:PATH += ";$env:ANDROID_HOME\platform-tools"
```

### Q3: Gradle 构建失败

**原因**: Gradle 版本与 AGP 版本不兼容

**解决**: 参考 [android-gradle-build-SKILL.md](docs/skills/android-gradle-build-SKILL.md) 第 1.2 节版本兼容性矩阵

### Q4: Kotlin 编译错误

**原因**: Kotlin 版本与 AGP 版本不兼容

**解决**: 参考 [android-upgrade-SKILL.md](docs/skills/android-upgrade-SKILL.md) 第 3.2 节 Kotlin/KSP 兼容性

---

## 六、版本兼容性矩阵

### 6.1 工具链版本组合

| 组合 | Gradle | AGP | Kotlin | KSP | compileSdk |
|------|--------|-----|--------|-----|------------|
| 推荐 | 8.13 | 8.13.2 | 2.0.21 | 2.0.21-1.0.28 | 34-36 |
| 稳定 | 8.5 | 8.1.x | 1.9.0 | 1.9.0-1.0.13 | 33-34 |
| 旧版 | 7.5 | 7.4.x | 1.7.x | 1.7.x | 30-33 |

### 6.2 SDK 组件版本

| 组件 | 推荐版本 | 最低版本 |
|------|---------|---------|
| Build-Tools | 34.0.0 | 30.0.3 |
| Platform | android-34 | android-30 |
| Platform-Tools | latest | 34.0.0 |
| Emulator | latest | 31.0.0 |
| NDK | 26.1.10909125 | 21.0.0 |

---

## 七、脚本工具

### 7.1 一键安装脚本

`scripts/install-android-sdk.ps1`:

```powershell
<#
.SYNOPSIS
一键安装 Android SDK 及相关组件
#>

$androidHome = "$env:USERPROFILE\AppData\Local\Android\Sdk"

# 创建目录
New-Item -ItemType Directory -Force -Path "$androidHome\cmdline-tools"

# 提示用户下载 commandlinetools
Write-Host "请下载 commandlinetools-win-10406996_latest.zip"
Write-Host "地址: https://developer.android.com/studio#command-tools"
Write-Host "解压到: $androidHome\cmdline-tools\latest"
Read-Host "按 Enter 继续..."

# 设置环境变量
$env:ANDROID_HOME = $androidHome
$env:PATH += ";$androidHome\cmdline-tools\latest\bin"

# 安装组件
$sdkmanager = "$androidHome\cmdline-tools\latest\bin\sdkmanager.bat"

Write-Host "正在安装 Build-Tools..."
& $sdkmanager "build-tools;34.0.0" --sdk_root=$androidHome

Write-Host "正在安装 Platform..."
& $sdkmanager "platforms;android-34" --sdk_root=$androidHome

Write-Host "正在安装 Platform-Tools..."
& $sdkmanager "platform-tools" --sdk_root=$androidHome

Write-Host "正在安装 Emulator..."
& $sdkmanager "emulator" --sdk_root=$androidHome

Write-Host "安装完成！"
Write-Host "请配置环境变量:"
Write-Host "ANDROID_HOME=$androidHome"
Write-Host "PATH 添加: $androidHome\platform-tools"
```

### 7.2 使用方式

```powershell
# 以管理员身份运行
powershell -ExecutionPolicy Bypass -File scripts/install-android-sdk.ps1
```

---

## 八、版本历史

| 日期 | 版本 | 变更内容 |
|------|------|----------|
| 2026-07-14 | 2.0.0 | 初始版本，提供 CLI 自动安装和手动安装两种方案，包含兼容性矩阵和脚本工具 |