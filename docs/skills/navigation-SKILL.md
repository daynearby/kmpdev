# Navigation 组件技能文档

> **所属域**: 知识域 | **加载策略**: 按需加载
> **版本**: 2.0.0 | **创建日期**: 2026-07-14

## 一、概述

Jetpack Navigation 组件提供了声明式的页面导航方案，支持深链接、多返回栈、场景（Dialog/BottomSheet）等高级功能。

---

## 二、基础配置

### 2.1 添加依赖

```groovy
dependencies {
    implementation "androidx.navigation:navigation-compose:2.9.8"
}
```

### 2.2 创建导航图

```kotlin
@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("detail/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            DetailScreen(id = id, navController = navController)
        }
    }
}
```

---

## 三、导航操作

### 3.1 导航到新页面

```kotlin
// 基础导航
navController.navigate("detail/123")

// 带参数导航
navController.navigate("detail/$productId")

// 带可选参数
navController.navigate("search?query=$query&page=1")
```

### 3.2 返回

```kotlin
// 返回上一页
navController.popBackStack()

// 返回指定页面
navController.popBackStack("home", inclusive = false)

// 返回到根页面
navController.popBackStack(navController.graph.startDestinationId, inclusive = false)
```

### 3.3 替换当前页面

```kotlin
// 替换当前页面（不会添加到返回栈）
navController.navigate("login") {
    popUpTo("home") { inclusive = true }
}
```

---

## 四、参数传递

### 4.1 路径参数

```kotlin
// 定义
composable("detail/{id}") { backStackEntry ->
    val id = backStackEntry.arguments?.getString("id") ?: ""
    DetailScreen(id = id)
}

// 调用
navController.navigate("detail/123")
```

### 4.2 查询参数

```kotlin
// 定义
composable(
    "search?query={query}&page={page}",
    arguments = listOf(
        navArgument("query") { defaultValue = "" },
        navArgument("page") { defaultValue = "1" }
    )
) { backStackEntry ->
    val query = backStackEntry.arguments?.getString("query") ?: ""
    val page = backStackEntry.arguments?.getString("page")?.toInt() ?: 1
    SearchScreen(query = query, page = page)
}

// 调用
navController.navigate("search?query=android&page=2")
```

### 4.3 Bundle 参数

```kotlin
// 定义
composable("detail") { backStackEntry ->
    val args = backStackEntry.arguments
    val product = args?.getParcelable<Product>("product")
    DetailScreen(product = product)
}

// 调用
navController.navigate("detail") {
    arguments = bundleOf("product" to product)
}
```

---

## 五、场景（Scenes）

### 5.1 Dialog 场景

```kotlin
dialog("settings") {
    SettingsDialog(onDismiss = { navController.popBackStack() })
}

// 调用
navController.navigate("settings")
```

### 5.2 BottomSheet 场景

```kotlin
bottomSheet("filter") {
    FilterBottomSheet(onApply = { navController.popBackStack() })
}

// 调用
navController.navigate("filter")
```

### 5.3 List-Detail 场景

```kotlin
navigation(startDestination = "list") {
    composable("list") {
        ItemListScreen(onItemClick = { id ->
            navController.navigate("detail/$id")
        })
    }
    composable("detail/{id}") { backStackEntry ->
        val id = backStackEntry.arguments?.getString("id") ?: ""
        ItemDetailScreen(id = id)
    }
}
```

---

## 六、深链接

### 6.1 配置 Intent Filter

```xml
<!-- AndroidManifest.xml -->
<activity
    android:name=".MainActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="myapp"
            android:host="example.com"
            android:pathPrefix="/detail" />
    </intent-filter>
</activity>
```

### 6.2 处理深链接

```kotlin
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    
    // 处理深链接
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val intent = (context as? Activity)?.intent
        intent?.data?.let { uri ->
            navController.navigate(uri.toString())
        }
    }
    
    NavHost(navController = navController, startDestination = "home") {
        composable(
            "detail/{id}",
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "myapp://example.com/detail/{id}"
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            DetailScreen(id = id)
        }
    }
}
```

---

## 七、多返回栈

### 7.1 创建嵌套导航

```kotlin
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "main") {
        navigation(startDestination = "home", route = "main") {
            composable("home") { HomeScreen() }
            composable("profile") { ProfileScreen() }
        }
        
        navigation(startDestination = "login", route = "auth") {
            composable("login") { LoginScreen() }
            composable("register") { RegisterScreen() }
        }
    }
}
```

### 7.2 切换导航图

```kotlin
// 从 main 切换到 auth
navController.navigate("auth") {
    popUpTo("main") { inclusive = true }
}

// 从 auth 切换回 main
navController.navigate("main") {
    popUpTo("auth") { inclusive = true }
}
```

---

## 八、导航监听

### 8.1 监听导航事件

```kotlin
val navController = rememberNavController()

LaunchedEffect(navController) {
    navController.currentBackStackEntryFlow.collect { backStackEntry ->
        val route = backStackEntry.destination.route
        // 记录页面访问
        Analytics.logEvent("page_view", mapOf("route" to route))
    }
}
```

### 8.2 监听返回事件

```kotlin
val navController = rememberNavController()

BackHandler {
    if (navController.currentDestination?.route == "home") {
        // 退出应用
        (context as? Activity)?.finish()
    } else {
        navController.popBackStack()
    }
}
```

---

## 九、常见问题

### Q1: 导航到同一页面重复添加

**原因**: 没有清除返回栈

**解决**:
```kotlin
navController.navigate("detail") {
    launchSingleTop = true
}
```

### Q2: 深链接不生效

**原因**: Intent Filter 配置错误或缺少 deepLinks 定义

**解决**: 检查 AndroidManifest.xml 和 NavHost 中的 deepLinks 配置

### Q3: 返回栈混乱

**原因**: 导航操作不当

**解决**: 使用 `popUpTo` 和 `inclusive` 参数控制返回栈

---

## 十、最佳实践

1. **单一 NavHost**: 应用中只使用一个 NavHost
2. **路由命名**: 使用清晰的路由名称（如 "home", "detail/{id}"）
3. **参数校验**: 对导航参数进行非空校验
4. **深链接测试**: 使用 `adb shell am start -a android.intent.action.VIEW -d "myapp://example.com/detail/123"` 测试
5. **返回栈管理**: 使用 `popUpTo` 清理不需要的页面
6. **ViewModel 生命周期**: 使用 `ViewModelStoreOwner` 控制 ViewModel 生命周期