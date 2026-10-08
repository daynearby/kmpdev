# Flutter Add-to-App 模块技能文档

> **所属域**: 知识域 | **加载策略**: 按需加载

## 模块概述

1.Flutter Add-to-App 模块实现了在现有 Android 应用中嵌入 Flutter 功能的能力。采用官方推荐的 add-to-app 源码集成模式，通过 `:flutter` 库项目作为桥梁，实现 Android 与 Dart 代码的无缝交互。
2.引入Flutter源码（../module_flutter)实现Flutter与android原生代码的工程独立，并且可以直接编辑。
3.不能修改.ios,.android 文件夹下面的内容，只能通过修改pubspec.yaml中的内容实现依赖版本升级、降级。

## 架构设计

### 整体架构
```
┌─────────────────────────────────────────────────────────────┐
│                     Android 宿主应用                          │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                     app/                              │   │
│  │  ┌──────────┐    ┌───────────────────┐               │   │
│  │  │ MainApp  │───>│ FlutterEngineCache│               │   │
│  │  │.onCreate()│   │ (ltc_flutter_engine)│             │   │
│  │  └──────────┘    └────────┬──────────┘               │   │
│  └───────────────────────────┼──────────────────────────┘   │
│                              │                               │
└──────────────────────────────┼───────────────────────────────┘
                               │
┌──────────────────────────────┼───────────────────────────────┐
│                        Flutter 层                             │
│                               │                               │
│  ┌──────────────┐    ┌───────┴───────┐                       │
│  │ flutter/     │    │ module_flutter│                       │
│  │ (Android Lib)│    │  (Dart Code)  │                       │
│  │ ┌──────────┐ │    │ ┌──────────┐  │                       │
│  │ │flutter.  │ │    │ │ main.dart│  │                       │
│  │ │jar       │ │    │ │ routes/  │  │                       │
│  │ └──────────┘ │    │ │ util/    │  │                       │
│  └──────────────┘    │ └──────────┘  │                       │
│                       └──────────────┘                       │
└─────────────────────────────────────────────────────────────┘
```

### 模块结构
```
project/flutter/                              # Android 库项目 (桥接层)
├── build.gradle                             # 标准 Android Library 配置
├── src/main/
│   └── AndroidManifest.xml                  # 空清单
└── libs/
    └── flutter.jar                          # Flutter Embedding JAR

module_flutter/                              # Flutter Dart 项目
├── lib/
│   ├── main.dart                            # Flutter 入口
│   ├── routes/
│   │   └── Routes.dart                      # GoRouter 路由配置
│   ├── util/
│   │   └── NativeChannelUtils.dart          # 原生通信工具
│   └── extension/
│       └── Extensions.dart                  # 路由扩展
├── pubspec.yaml                             # Flutter 依赖
└── build/
    └── flutter_assets/                      # Flutter 资源
```

---

## 1. flutter/ 库项目

### 功能职责
作为 Android 与 Flutter 之间的桥梁，提供 flutter.jar 依赖和基础配置。

### build.gradle 配置，改模块是自动生成，不可修改
```groovy
apply plugin: "com.android.library"
apply plugin: "kotlin-android"

android {
    namespace "com.example.module_flutter"
    compileSdk 35

    defaultConfig {
        minSdk 24
        targetSdk 35
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation files("libs/flutter.jar")
}
```

### 关键说明
- **flutter.jar** 来源: `{FLUTTER_SDK_PATH}/bin/cache/artifacts/engine/android-arm64-release/flutter.jar`
- **不使用 includeBuild** - 避免 Gradle 类加载器隔离问题
- **标准 Android Library** - 可以作为普通 Gradle 子项目编译

---

## 2. module_flutter/ Dart 项目

### 2.1 入口文件 (main.dart)

```dart
import 'package:flutter/material.dart';
import 'routes/Routes.dart';
import 'util/NativeChannelUtils.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  
  // 初始化原生通信
  NativeChannelUtils.init();
  
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      title: 'App',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        useMaterial3: true,
      ),
      routerConfig: Routes.router,  // 使用 GoRouter
    );
  }
}
```

### 2.2 GoRouter 路由配置 (Routes.dart)

```dart
import 'package:go_router/go_router.dart';

class Routes {
  // GoRouter 实例
  static final GoRouter router = GoRouter(
    initialLocation: '/',
    routes: [
      GoRoute(
        path: '/',
        builder: (context, state) => const HomePage(),
      ),
      GoRoute(
        path: '/user/address',
        builder: (context, state) => const UserAddressPage(),
      ),
      GoRoute(
        path: '/user/settings',
        builder: (context, state) => const UserSettingsPage(),
      ),
      GoRoute(
        path: '/shop/detail',
        builder: (context, state) {
          final extra = state.extra as Map<String, dynamic>?;
          return ProductDetailPage(productId: extra?['productId']);
        },
      ),
    ],
  );

  // 导航辅助方法
  static void push(String path, [Map<String, dynamic>? extra]) {
    router.push(path, extra: extra);
  }

  static void pop() {
    if (router.canPop()) {
      router.pop();
    }
  }

  static void replace(String path, [Map<String, dynamic>? extra]) {
    router.replace(path, extra: extra);
  }
}
```

### 2.3 原生通信工具 (NativeChannelUtils.dart)

```dart
import 'package:flutter/services.dart';

class NativeChannelUtils {
  // Channel 定义
  static const _fromPlatform = MethodChannel('flutter.channel.shared.data');
  static const _toPlatform = MethodChannel('app.channel.shared.data');

  // 初始化
  static void init() {
    _fromPlatform.setMethodCallHandler(_handleMethodCall);
  }

  // 处理来自 Android 的调用
  static Future<dynamic> _handleMethodCall(MethodCall call) async {
    switch (call.method) {
      case 'eventToFlutter':
        return _handleEvent(call.arguments);
      default:
        throw PlatformException(
          code: 'Unimplemented',
          details: 'The method ${call.method} is not implemented',
        );
    }
  }

  // 处理事件数据
  static void _handleEvent(dynamic arguments) {
    final Map<String, dynamic> data = arguments as Map<String, dynamic>;
    
    // 根据事件类型处理
    switch (data['event']) {
      case 'token_update':
        // Token 更新
        break;
      case 'user_info_update':
        // 用户信息更新
        break;
      case 'login_state_change':
        // 登录状态变更
        break;
    }
  }

  // 发送到 Android
  static Future<dynamic> callNativeMethod(
    String method,
    Map<String, dynamic> arguments,
  ) async {
    try {
      return await _toPlatform.invokeMethod(method, arguments);
    } on PlatformException catch (e) {
      print('PlatformException: ${e.message}');
      return null;
    }
  }
}
```

---

## 3. Android 端集成

### 3.1 settings.gradle.kts 配置

```kotlin
val flutterModulePath = settingsDir.parentFile.parentFile.toString() + "/dev/module_flutter"

// 1. 包含 :flutter 库项目
include(":flutter")
project(":flutter").projectDir = File(settingsDir, "flutter")

// 2. 读取 .flutter-plugins 并包含原生插件
val flutterPluginsFile = File(flutterModulePath, ".flutter-plugins")
if (flutterPluginsFile.exists()) {
    flutterPluginsFile.readLines().filter { it.contains("=") }.forEach { line ->
        val parts = line.split("=")
        if (parts.size == 2) {
            val pluginName = parts[0].trim()
            val pluginPath = parts[1].trim().replace("\"", "")
            val pluginAndroidDir = File(flutterModulePath, "$pluginPath/android")
            if (pluginAndroidDir.exists()) {
                include(":$pluginName")
                project(":$pluginName").projectDir = pluginAndroidDir
            }
        }
    }
}
```

### 3.2 MainApp.kt - FlutterEngine 初始化

```kotlin
class MainApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        if (isMainProcess()) {
            // ... 其他初始化
            
            // 初始化 Flutter
            initFlutterBoost()
        }
    }

    private fun initFlutterBoost() {
        // 创建 FlutterEngine
        val flutterEngine = FlutterEngine(this)
        
        // 执行 Dart 入口
        flutterEngine.dartExecutor.executeDartEntrypoint(
            DartExecutor.DartEntrypoint.createDefault()
        )
        
        // 缓存引擎
        FlutterEngineCache.getInstance().put(
            FlutterChannelUtils.ENGINE_ID, 
            flutterEngine
        )
        
        // 初始化通信通道
        FlutterChannelUtils.initFlutterEngine(flutterEngine, applicationContext)
    }
}
```

### 3.3 FlutterChannelUtils.kt - 通信管理

```kotlin
object FlutterChannelUtils {
    const val ENGINE_ID = "ltc_flutter_engine"

    private var flutterEngine: FlutterEngine? = null
    private var appContext: Context? = null

    // 初始化
    fun initFlutterEngine(engine: FlutterEngine, context: Context) {
        flutterEngine = engine
        appContext = context
        
        // 设置 MethodChannel 处理器
        MethodChannel(
            engine.dartExecutor.binaryMessenger, 
            "flutter.channel.shared.data"
        ).setMethodCallHandler { call, result ->
            when (call.method) {
                "eventToFlutter" -> {
                    handleEvent(call.arguments)
                    result.success(null)
                }
                else -> result.notImplemented()
            }
        }
    }

    // 发送数据到 Flutter
    fun sendEventToFlutter(data: Map<String, Any?>) {
        flutterEngine?.let {
            MethodChannel(
                it.dartExecutor.binaryMessenger,
                "flutter.channel.shared.data"
            ).invokeMethod("eventToFlutter", data)
        }
    }

    // 启动 Flutter 页面
    fun startFlutterPage(
        pageName: String,
        arguments: Map<String, Any> = mapOf(),
        requestCode: Int = -1
    ) {
        val context = appContext ?: return
        
        val intent = FlutterActivity
            .withCachedEngine(ENGINE_ID)
            .backgroundMode(FlutterActivityLaunchConfigs.BackgroundMode.opaque)
            .build(context)
            .apply {
                putExtra("route", pageName)
                putExtra("arguments", java.util.HashMap(arguments))
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
        
        if (requestCode != -1 && context is Activity) {
            context.startActivityForResult(intent, requestCode)
        } else {
            context.startActivity(intent)
        }
    }
}
```

### 3.4 LtcFlutterActivity.kt - Flutter Activity

```kotlin
class LtcFlutterActivity : FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        // 设置 MethodChannel
        setupMethodChannel(flutterEngine)
        
        // 初始化用户信息
        initInfo()
        
        // 初始化设备信息
        initDeviceInfo()
    }

    private fun setupMethodChannel(flutterEngine: FlutterEngine) {
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            "app.channel.shared.data"
        ).setMethodCallHandler { call, result ->
            when (call.method) {
                "callUserLogin" -> handleLogin(call.arguments, result)
                "callShare" -> handleShare(call.arguments, result)
                "callPayVipOfiOS" -> handlePay(call.arguments, result)
                "call_launch_game" -> handleLaunchGame(call.arguments, result)
                "call_show_toast" -> handleShowToast(call.arguments, result)
                else -> result.notImplemented()
            }
        }
    }

    private fun handleLogin(arguments: Any?, result: MethodChannel.Result) {
        // 处理登录请求
        result.success(true)
    }

    private fun handleShare(arguments: Any?, result: MethodChannel.Result) {
        // 处理分享请求
        result.success(true)
    }
}
```

---

## MethodChannel 通信协议

### Android → Flutter (flutter.channel.shared.data)

| 方法 | 参数 | 用途 |
|------|------|------|
| eventToFlutter | Map<String, dynamic> | 通用事件通知 |

### Flutter → Android (app.channel.shared.data)

| 方法 | 参数 | 返回值 | 用途 |
|------|------|--------|------|
| callUserLogin | Map | Boolean | 触发登录 |
| callRefreshUserInfo | Map | Boolean | 刷新用户信息 |
| callShare | Map | Boolean | 触发分享 |
| callPayVipOfiOS | Map | Boolean | 触发支付 |
| call_launch_game | Map | Boolean | 启动游戏 |
| call_show_toast | Map | void | 显示 Toast |
| buglyReport | Map | void | Bugly 上报 |

---

## 数据流程

### Flutter 页面启动流程
```
用户点击 Flutter 页面
  │
  ▼
Router.go("/flutter/xxx")
  │
  ▼
RouterCallback.handleFlutterRouter()
  │
  ▼
FlutterChannelUtils.startFlutterPage(route, params)
  │
  ▼
FlutterActivity.withCachedEngine(ENGINE_ID)
  │
  ▼
LtcFlutterActivity.onCreate()
  │
  ▼
configureFlutterEngine(flutterEngine)
  │
  ▼
传递 route 和 params 给 Dart 侧
  │
  ▼
GoRouter 导航到对应页面
```

### MethodChannel 通信流程
```
Flutter 侧调用 NativeChannelUtils.callNativeMethod()
  │
  ▼
MethodChannel.invokeMethod(method, arguments)
  │
  ▼
Android 侧 MethodChannel.MethodCallHandler 接收
  │
  ▼
根据 call.method 分发处理
  │
  ▼
result.success(returnValue) 返回结果
```

---

## 开发工作流

### 1. 修改 Dart 代码
```bash
cd module_flutter
flutter pub get
flutter build bundle --debug
```

### 2. 重新构建 Android
```bash
cd ../rastar
./gradlew clean :app:assembleDevOfficialDebug
```

### 3. 热重载开发模式 (可选)
```bash
# 在 Android Studio 中运行 app
# 然后在终端执行:
flutter attach --debug
```

---

## 依赖管理

### pubspec.yaml
```yaml
environment:
  sdk: '>=3.11.0 <4.0.0'
  flutter: ">=3.11.0"

dependencies:
  flutter:
    sdk: flutter
  go_router: ^14.8.1
  device_info_plus: ^13.0.0
  thinking_analytics: ^3.3.1
```

### 版本兼容性
| 组件 | 版本 | 说明 |
|------|------|------|
| Flutter SDK | 3.41.6 | 稳定版 |
| Dart SDK | 3.11.4 | 与 Flutter 匹配 |
| go_router | 14.8.1 | 路由框架 |
| AGP | 8.13.0 | 构建工具 |
| Gradle | 8.13 | 构建工具 |

---

## 常见问题

### Q1: Flutter 页面空白
**原因**: flutter_assets 未生成
**解决**: 
```bash
cd module_flutter
flutter build bundle --debug
```

### Q2: MethodChannel 通信失败
**原因**: Channel 名称不匹配
**解决**: 确认 Android 端和 Flutter 端使用相同的 Channel 名称:
- Android → Flutter: `flutter.channel.shared.data`
- Flutter → Android: `app.channel.shared.data`

### Q3: FlutterEngine 未初始化
**原因**: MainApp.onCreate() 未正确调用 initFlutterBoost()
**解决**: 确保在 isMainProcess() 条件内调用 initFlutterBoost()

### Q4: ClassNotFoundException: com.android.builder.model.BuildType
**原因**: 使用了 includeBuild 导致类加载器隔离
**解决**: 改用 flutter.jar 直接集成方式

---

## 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|----------|
| 5.40.10 | 2026-04-16 | Flutter add-to-app 集成，go_router 替换 flutter_boost |
| 5.40.9 | 2025-12-20 | Flutter 模块初步集成 |