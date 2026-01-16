package com.tasker.chronos.data.models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class ShoppingItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val price: Double = 0.0,
    val categoryId: String? = null,
    val isPurchased: Boolean = false,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Serializable(with = LocalDateTimeSerializer::class)
    val purchasedAt: LocalDateTime? = null
)

@Serializable
data class ShoppingCategory(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: String = "#4CAF50"
)