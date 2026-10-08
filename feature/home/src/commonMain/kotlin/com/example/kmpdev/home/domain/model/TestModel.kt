package com.example.kmpdev.home.domain.model

import kotlinx.serialization.Serializable


@Serializable
data class TestModel(val item: ItemModel = ItemModel()) {
}
