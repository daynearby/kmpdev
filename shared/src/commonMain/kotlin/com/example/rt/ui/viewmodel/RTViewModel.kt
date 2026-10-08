package com.example.rt.ui.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * 轻量级 ViewModel 基类，不依赖 AndroidX。
 *
 * - scope 使用 Dispatchers.Main + SupervisorJob，子协程异常不互相影响
 * - onCleared() 由 Composable 的 DisposableEffect 在离开组合树时调用
 */
open class RTViewModel {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    open fun onCleared() {
        scope.cancel()
    }
}