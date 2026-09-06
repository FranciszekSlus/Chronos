package com.tasker.chronos.data.models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class ShoppingItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val price: Double = 0.0,
    /** Legacy single category — kept for older saved JSON. */
    val categoryId: String? = null,
    val categoryIds: List<String> = emptyList(),
    val isPurchased: Boolean = false,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Serializable(with = LocalDateTimeSerializer::class)
    val purchasedAt: LocalDateTime? = null
) {
    fun resolvedCategoryIds(): List<String> {
        if (categoryIds.isNotEmpty()) return categoryIds.distinct()
        return listOfNotNull(categoryId)
    }

    fun belongsToCategory(id: String?): Boolean {
        if (id == null) return true
        return id in resolvedCategoryIds()
    }
}

@Serializable
data class ShoppingCategory(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: String = "#4CAF50"
)
