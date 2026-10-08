# Flutter 路由框架迁移 Skill

> **所属域**: 知识域 | **加载策略**: 按需加载

> **适用范围**: Flutter 混合开发项目中的路由框架更换（flutter_boost ↔ go_router）
>
> **创建日期**: 2026-06-11
>
> **最后更新**: 2026-06-11

---

## 1. 概述

本 Skill 提供 Flutter 混合开发项目中路由框架迁移的标准化流程，涵盖从 flutter_boost 迁移到 go_router 以及反向迁移的完整步骤。

### 1.1 适用场景

| 场景 | 说明 |
|------|------|
| flutter_boost → go_router | 移除 flutter_boost 依赖，使用 Flutter 标准 go_router |
| go_router → flutter_boost | 回退到 flutter_boost 路由方案 |
| 路由框架版本升级 | 同框架大版本升级时的迁移指南 |

### 1.2 两种框架对比

| 特性 | flutter_boost | go_router |
|------|--------------|-----------|
| 原生端路由控制 | ✅ 原生端可主动打开 Flutter 页面 | ❌ 需要额外的 MethodChannel 通信 |
| 混合栈管理 | ✅ 原生和 Flutter 页面统一管理 | ❌ 仅管理 Flutter 内部路由 |
| 官方支持 | ❌ 社区维护 | ✅ Flutter 官方维护 |
| Dart 版本兼容 | 依赖社区更新 | 与 Flutter SDK 同步更新 |
| 学习成本 | 较高（需理解 BoostNavigator 机制） | 较低（标准声明式路由） |

---

## 2. 迁移前准备

### 2.1 信息收集清单

在执行任何迁移操作之前，必须确认以下信息：

```
□ Flutter 模块路径: [如: {PROJECT_ROOT}/module_flutter/]
□ 主项目路径: [如: {PROJECT_ROOT}/]
□ 当前使用的路由框架: [flutter_boost / go_router]
□ 目标路由框架: [flutter_boost / go_router]
□ Git 历史中原始版本可追溯: [是 / 否]
□ 原生端是否依赖 flutter_boost 原生库: [是 / 否]
□ 是否有 go_router 迁移的未提交修改: [是 / 否]
```

### 2.2 Git 历史检查

```bash
# 检查 Flutter 模块的 git 状态
cd module_flutter
git status
git log --all --oneline -10

# 检查关键文件的修改历史
git log --all --oneline -- lib/main.dart
git log --all --oneline -- pubspec.yaml
git log --all --oneline -- lib/MainApp.dart
git log --all --oneline -- lib/routes/GoRoutes.dart

# 查看原始版本（如果迁移未提交）
git show HEAD:lib/main.dart
git show HEAD:lib/MainApp.dart
```

### 2.3 文件扫描清单

必须扫描以下文件类型：

| 类别 | 搜索模式 | 说明 |
|------|----------|------|
| go_router 引用 | `go_router` / `GoRoutes` / `GoRouter` | Flutter 端所有引用 |
| flutter_boost 引用 | `flutter_boost` / `BoostNavigator` / `FlutterBoostApp` | Flutter 端所有引用 |
| 原生端引用 | `FlutterBoost` / `flutter_boost` | Android/iOS 原生端引用 |
| 路由页面引用 | `BoostNavigator.instance.push` / `context.go` / `GoRouter.of` | 页面跳转调用 |

---

## 3. go_router → flutter_boost 迁移流程

### 3.1 步骤总览

```
步骤 1: Git 历史分析
    │
    ▼
步骤 2: Flutter 端修改
    │
    ├── Routes.dart: 新增 routeFactory 方法
    ├── main.dart: 恢复 FlutterBoostApp 初始化
    ├── MainApp.dart: 恢复原始初始化逻辑
    ├── 页面文件: 替换 GoRoutes 调用为 BoostNavigator
    └── NativeChannelUtils: 添加 initialRoute 处理
    │
    ▼
步骤 3: 清理工作
    │
    ├── 删除 GoRoutes.dart
    └── pubspec.yaml: 移除 go_router 依赖
    │
    ▼
步骤 4: 编译验证
    │
    ├── flutter pub get
    ├── flutter analyze
    └── Gradle 编译
```

### 3.2 步骤 2.1：Routes.dart 新增 routeFactory

在 `Routes.dart` 末尾新增 `routeFactory` 静态方法，供 `FlutterBoostApp` 使用：

```dart
/// FlutterBoost 路由工厂方法
static Route<dynamic>? routeFactory(RouteSettings settings, String? uniqueId) {
  FlutterBoostRouteFactory? func = routerMap[settings.name];
  if (func == null) {
    return null;
  }
  return func(settings, uniqueId);
}
```

### 3.3 步骤 2.2：main.dart 恢复 FlutterBoostApp

**关键变更点**：

1. **导入变更**:
   ```dart
   // 移除
   import 'package:module_flutter/routes/GoRoutes.dart';
   
   // 新增
   import 'package:flutter_boost/flutter_boost.dart';
   import 'package:module_flutter/routes/Routes.dart';
   import 'package:module_flutter/view/demo/nonexistent_page.dart';
   ```

2. **main() 函数变更**:
   ```dart
   void main() {
     tz.initializeTimeZones();
     // 新增 flutter_boost 初始化
     PageVisibilityBinding.instance.addGlobalObserver(AppLifecycleObserver());
     CustomFlutterBinding();
     MainApp.init();
     runApp(...);
   }
   ```

3. **新增类**:
   ```dart
   class AppLifecycleObserver extends GlobalPageVisibilityObserver {
     @override
     void onPageShow(Route<dynamic> route) {
       bus.emit(EventBus.EVENT_PAGE_SHOW, route.settings.name);
     }
   
     @override
     void onPageHide(Route<dynamic> route) {
       bus.emit(EventBus.EVENT_PAGE_HIDE, route.settings.name);
     }
   }
   
   class CustomFlutterBinding extends WidgetsFlutterBinding
       with BoostFlutterBinding {}
   ```

4. **build() 方法变更**:
   ```dart
   // 移除 MaterialApp.router + routerConfig
   // 替换为 FlutterBoostApp
   @override
   Widget build(BuildContext context) {
     return FlutterBoostApp(
       routeFactory,
       appBuilder: appBuilder,
     );
   }
   ```

5. **appBuilder 中使用 MaterialApp 替代 MaterialApp.router**:
   ```dart
   Widget appBuilder(Widget home) {
     return RefreshConfiguration(
       // ... 配置保持不变 ...
       child: MaterialApp(
         home: home,  // 关键：使用 home 参数而非 routerConfig
         // ... 其他配置保持不变 ...
       ),
     );
   }
   ```

### 3.4 步骤 2.3：MainApp.dart 恢复原始逻辑

```dart
// 移除
import 'package:module_flutter/routes/GoRoutes.dart';

// 恢复
import 'package:module_flutter/util/DeviceInfo.dart';

class MainApp {
  static init() {
    // ... 保持不变 ...

    /// 初始化设备信息（原始版本中的逻辑）
    if (!PlatformUtils.isAndroid) {
      DeviceInfo.getDeviceInfo();
    }
    DeviceInfo.initPreferences();
  }

  // 移除 openFlutterPage 方法（flutter_boost 不需要此入口）
}
```

### 3.5 步骤 2.4：页面文件替换 GoRoutes 调用

在 go_router 迁移期间，页面文件通常使用 `as` 别名同时导入两套路由：

```dart
// 需要移除的导入
import 'package:module_flutter/routes/GoRoutes.dart' as go_routes;
import 'package:module_flutter/routes/Routes.dart' as old_routes;

// 替换为
import 'package:flutter_boost/flutter_boost.dart';
import 'package:module_flutter/routes/Routes.dart';
```

**API 映射表**:

| go_router API | flutter_boost API |
|---------------|-------------------|
| `go_routes.GoRoutes.push(path, {...})` | `BoostNavigator.instance.push(path, arguments: {...})` |
| `go_routes.GoRoutes.pushReplacement(path, {...})` | `BoostNavigator.instance.pushReplacement(path, arguments: {...})` |
| `go_routes.GoRoutes.pop()` | `BoostNavigator.instance.pop()` |
| `go_routes.GoRoutes.pushForResult(path, {...})` | `BoostNavigator.instance.push(path, arguments: {...})` |

> ⚠️ **注意**: `BoostNavigator` 的 `push` 方法返回 `Future<T?>`，可直接用 `.then()` 接收返回值，无需单独的 `pushForResult` 方法。

### 3.6 步骤 2.5：NativeChannelUtils 添加 initialRoute 处理

如果原生端通过 `MethodChannel` 传递路由（而非通过 `FlutterBoost.open()`），需要在 `NativeChannelUtils` 中处理：

```dart
// _handlerPlatformInvoke 方法中新增 case
case 'initialRoute':
  var param = Map<String, dynamic>.from(methodCall.arguments);
  String route = param.vString('route');
  Map<String, dynamic> arguments = Map<String, dynamic>.from(param['arguments'] ?? {});
  if (route.isNotEmpty) {
    BoostNavigator.instance.push(route, arguments: arguments);
  }
  break;
```

### 3.7 步骤 3：清理工作

```bash
# 删除 GoRoutes.dart
rm lib/routes/GoRoutes.dart

# pubspec.yaml 中移除 go_router 依赖
# 删除: go_router: 14.6.2
```

### 3.8 步骤 4：编译验证

```bash
# 1. Flutter 依赖更新
cd module_flutter
flutter pub get

# 2. Flutter 静态分析（确保无 error 级别问题）
flutter analyze

# 3. Gradle 编译
cd ..
./gradlew clean assembleDevOfficialDebug
```

> ✅ 验证标准: `flutter analyze` 无 error 级别 issue；`BUILD SUCCESSFUL`

---

## 4. flutter_boost → go_router 迁移流程（反向迁移）

### 4.1 步骤总览

```
步骤 1: 评估原生端依赖
步骤 2: 创建 GoRoutes.dart 替代路由表
步骤 3: 修改 main.dart 使用 MaterialApp.router
步骤 4: 修改 MainApp.dart 添加 openFlutterPage 入口
步骤 5: 替换页面文件中的 BoostNavigator 调用
步骤 6: pubspec.yaml 添加 go_router 依赖
步骤 7: 编译验证
```

### 4.2 注意事项

> ⚠️ **重要提醒**: 在决定迁移前，务必确认：
> 1. 原生端是否依赖 `FlutterBoost.instance()` 的功能（如 `sendEventToFlutter`、`open`）
> 2. 原生端路由传递方式是否需要调整
> 3. go_router 的声明式路由是否能满足所有页面跳转需求
>
> 如果原生端深度依赖 flutter_boost 的原生 API，迁移成本较高，建议先完成原生端去 flutter_boost 化。

---

## 5. 通用原则

### 5.1 不可变性原则

- **绝不通过注释屏蔽错误**: 遇到编译错误应理解原因并修复，而非注释代码
- **保持业务逻辑一致**: 迁移不改变页面跳转逻辑和参数传递
- **先编译验证后提交**: 每次修改后必须执行编译验证

### 5.2 Git 管理原则

- 迁移前确保工作区干净（或至少知道哪些是迁移前的修改）
- 迁移完成后统一提交，避免散落的未提交修改
- 提交信息应清晰描述迁移范围：`refactor: migrate go_router back to flutter_boost`

### 5.3 验证清单

| 验证项 | 方法 | 通过标准 |
|--------|------|----------|
| Flutter pub get | `flutter pub get` | 无依赖冲突 |
| Flutter analyze | `flutter analyze` | 无 error 级别 issue |
| Gradle 编译 | `./gradlew assembleDevOfficialDebug` | BUILD SUCCESSFUL |
| 页面跳转 | 运行时测试 | 页面正常打开、参数正确传递 |
| 返回操作 | 运行时测试 | 页面正常关闭、返回值正确 |

---

## 6. 本次迁移记录

### 6.1 迁移背景

- **日期**: 2026-06-11
- **方向**: go_router → flutter_boost（回退）
- **原因**: go_router 迁移不完整（Git 未提交），原生端已完全移除 flutter_boost 原生依赖

### 6.2 修改文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `lib/routes/Routes.dart` | 修改 | 新增 `routeFactory` 静态方法 |
| `lib/main.dart` | 重写 | `MaterialApp.router` → `FlutterBoostApp` |
| `lib/MainApp.dart` | 重写 | 恢复原始版本，移除 `openFlutterPage` |
| `lib/view/mall/GoodsDetailPage.dart` | 修改 | 替换 `GoRoutes` 调用为 `BoostNavigator` |
| `lib/util/NativeChannelUtils.dart` | 修改 | 新增 `initialRoute` MethodChannel handler |
| `lib/routes/GoRoutes.dart` | 删除 | 不再需要 |
| `pubspec.yaml` | 修改 | 移除 `go_router: 14.6.2` |

### 6.3 编译结果

- `flutter pub get`: ✅ 成功（go_router 已移除）
- `flutter analyze`: ✅ 通过（0 error）
- `Gradle assembleDevOfficialDebug`: ✅ BUILD SUCCESSFUL

---

## 7. 版本历史

| 日期 | 版本 | 变更内容 |
|------|------|----------|
| 2026-06-11 | 1.0 | 初始版本，基于 go_router→flutter_boost 回退迁移创建 |