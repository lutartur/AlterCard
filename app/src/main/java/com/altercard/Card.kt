package com.altercard

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class Card(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val number: String,
    val barcodeData: String? = null,
    val barcodeFormat: String? = null,
    val customBackgroundColor: Int? = null,
    val customTextColor: Int? = null,
    val lastModified: Long = 0L,
    val isDeleted: Boolean = false
)
