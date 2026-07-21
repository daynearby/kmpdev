package com.example.rt.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember


/**
 * 在 Composable 中创建并记住 ViewModel，自动绑定生命周期。
 * Composable 离开组合树时自动调用 onCleared()。
 */
@Composable
inline fun <reified T : RTViewModel> rememberRTViewModel(
    noinline creator: () -> T
): T {
    val vm = remember { creator() }
    DisposableEffect(vm) {
        onDispose { vm.onCleared() }
    }
    return vm
}