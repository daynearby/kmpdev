# Jetpack Compose 开发技能文档

> **所属域**: 知识域 | **加载策略**: 按需加载
> **版本**: 2.0.0 | **创建日期**: 2026-07-14

## 一、概述

Jetpack Compose 是 Android 官方的现代声明式 UI 框架。本技能文档涵盖 Compose 基础组件、布局系统、状态管理、Material3 设计规范等核心内容。

---

## 二、基础组件

### 2.1 文本组件

```kotlin
Text(
    text = "Hello, Compose!",
    fontSize = 18.sp,
    fontWeight = FontWeight.Bold,
    color = Color.Black,
    textAlign = TextAlign.Center,
    maxLines = 2,
    overflow = TextOverflow.Ellipsis
)
```

### 2.2 按钮组件

```kotlin
// Material3 按钮
Button(
    onClick = { /* 点击事件 */ },
    enabled = true,
    shape = RoundedCornerShape(8.dp),
    colors = ButtonDefaults.buttonColors(
        containerColor = Color.Blue,
        contentColor = Color.White
    ),
    modifier = Modifier.padding(16.dp)
) {
    Text("点击按钮")
}

// 文字按钮
TextButton(onClick = {}) {
    Text("文字按钮")
}

// 图标按钮
IconButton(onClick = {}) {
    Icon(Icons.Filled.Favorite, contentDescription = null)
}
```

### 2.3 图片组件

```kotlin
Image(
    painter = painterResource(R.drawable.ic_launcher),
    contentDescription = "图标",
    modifier = Modifier.size(48.dp),
    contentScale = ContentScale.Crop
)

// 网络图片（配合 Coil）
AsyncImage(
    model = "https://example.com/image.jpg",
    contentDescription = "网络图片",
    placeholder = painterResource(R.drawable.placeholder),
    error = painterResource(R.drawable.error)
)
```

---

## 三、布局系统

### 3.1 基础布局

```kotlin
// Column - 垂直布局
Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text("第一行")
    Text("第二行")
}

// Row - 水平布局
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
) {
    Text("左侧")
    Text("右侧")
}

// Box - 层叠布局
Box(modifier = Modifier.size(200.dp)) {
    Text("底层文字", modifier = Modifier.align(Alignment.Center))
    Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.align(Alignment.TopEnd))
}
```

### 3.2 ConstraintLayout

```kotlin
ConstraintLayout(modifier = Modifier.fillMaxSize()) {
    val (image, text) = createRefs()
    
    Image(
        painter = painterResource(R.drawable.ic_launcher),
        contentDescription = null,
        modifier = Modifier.constrainAs(image) {
            top.linkTo(parent.top, margin = 16.dp)
            start.linkTo(parent.start, margin = 16.dp)
        }
    )
    
    Text(
        text = "约束布局",
        modifier = Modifier.constrainAs(text) {
            top.linkTo(image.bottom, margin = 8.dp)
            start.linkTo(image.start)
        }
    )
}
```

### 3.3 Scrollable 布局

```kotlin
// 垂直滚动
ScrollableColumn(modifier = Modifier.fillMaxSize()) {
    repeat(100) {
        Text("Item $it", modifier = Modifier.padding(8.dp))
    }
}

// 水平滚动
ScrollableRow(modifier = Modifier.height(100.dp)) {
    repeat(20) {
        Text("Item $it", modifier = Modifier.padding(8.dp))
    }
}
```

---

## 四、状态管理

### 4.1 State 基础

```kotlin
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    
    Column {
        Text("计数: $count")
        Button(onClick = { count++ }) {
            Text("增加")
        }
    }
}
```

### 4.2 ViewModel 集成

```kotlin
class CounterViewModel : ViewModel() {
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count
    
    fun increment() {
        _count.value++
    }
}

@Composable
fun CounterScreen(viewModel: CounterViewModel = viewModel()) {
    val count by viewModel.count.collectAsState()
    
    Column {
        Text("计数: $count")
        Button(onClick = { viewModel.increment() }) {
            Text("增加")
        }
    }
}
```

### 4.3 State Hoisting

```kotlin
@Composable
fun Parent() {
    var name by remember { mutableStateOf("") }
    
    Column {
        NameInput(name = name, onNameChange = { name = it })
        Text("Hello, $name!")
    }
}

@Composable
fun NameInput(name: String, onNameChange: (String) -> Unit) {
    TextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("输入姓名") }
    )
}
```

---

## 五、Material3 主题

### 5.1 创建主题

```kotlin
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6750A4),
    secondary = Color(0xFF9D4EDD),
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    secondary = Color(0xFF9D4EDD),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

@Composable
fun MyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
```

### 5.2 使用主题

```kotlin
@Composable
fun ThemedButton() {
    Button(
        onClick = {},
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            "主题按钮",
            style = MaterialTheme.typography.titleMedium
        )
    }
}
```

---

## 六、列表与懒加载

### 6.1 LazyColumn

```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    // 单个项目
    item {
        Text("标题", style = MaterialTheme.typography.headlineMedium)
    }
    
    // 多个项目
    items(100) { index ->
        ListItem(
            headlineContent = { Text("Item $index") },
            supportingContent = { Text("描述信息") },
            leadingContent = {
                Icon(Icons.Filled.Favorite, contentDescription = null)
            }
        )
    }
}
```

### 6.2 LazyRow

```kotlin
LazyRow(
    modifier = Modifier.fillMaxWidth(),
    contentPadding = PaddingValues(16.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
) {
    items(20) { index ->
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text("Item $index", color = Color.White)
        }
    }
}
```

---

## 七、动画

### 7.1 基础动画

```kotlin
@Composable
fun AnimatedVisibilityDemo() {
    var visible by remember { mutableStateOf(true) }
    
    Column {
        Button(onClick = { visible = !visible }) {
            Text(if (visible) "隐藏" else "显示")
        }
        
        AnimatedVisibility(visible = visible) {
            Text("动画显示的内容")
        }
    }
}
```

### 7.2 属性动画

```kotlin
@Composable
fun AnimateSizeDemo() {
    var expanded by remember { mutableStateOf(false) }
    
    Column {
        Button(onClick = { expanded = !expanded }) {
            Text(if (expanded) "收起" else "展开")
        }
        
        Box(
            modifier = Modifier
                .background(Color.Blue)
                .animateSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "动态大小",
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
```

---

## 八、常见问题

### Q1: Compose 预览不显示

**原因**: 缺少 `@Preview` 注解或 Android Studio 配置问题

**解决**:
```kotlin
@Preview(showBackground = true, name = "预览名称")
@Composable
fun MyComposablePreview() {
    MyAppTheme {
        MyComposable()
    }
}
```

### Q2: 状态更新但 UI 不刷新

**原因**: 使用了不可变数据或忘记使用 `mutableStateOf`

**解决**: 使用 `mutableStateOf` 或 `StateFlow` + `collectAsState()`

### Q3: 性能问题（卡顿）

**原因**: 重组过于频繁或布局嵌套过深

**解决**:
- 使用 `remember` 和 `derivedStateOf` 减少计算
- 避免在 Composable 中创建对象
- 使用 `LazyColumn` 代替 `Column` + `Scrollable`

---

## 九、最佳实践

1. **状态提升**: 将状态移到使用它的 Composable 之上
2. **单一职责**: 每个 Composable 只负责一个功能
3. **使用 remember**: 缓存计算结果和对象创建
4. **ViewModel**: 用于管理 UI 相关状态和业务逻辑
5. **Material3**: 使用 Material3 组件保持设计一致性
6. **预览**: 为每个组件编写 `@Preview` 注解