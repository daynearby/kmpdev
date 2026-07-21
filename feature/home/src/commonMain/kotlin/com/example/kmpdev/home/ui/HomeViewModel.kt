package com.example.kmpdev.home.ui

import com.example.kmpdev.home.domain.model.TestModel
import com.example.kmpdev.home.domain.usecase.GetFeedUseCase
import com.example.rt.ui.viewmodel.RTViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(val feedUseCase: GetFeedUseCase) : RTViewModel() {
    private val _state = MutableStateFlow<List<TestModel>>(emptyList())
    val state: StateFlow<List<TestModel>> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        scope.launch {
            feedUseCase().collect { result ->
                _state.update {  result.data }
            }
        }
    }


    fun refresh() = loadData()

}