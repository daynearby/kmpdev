package com.example.kmpdev.home.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ItemModel(
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
)
