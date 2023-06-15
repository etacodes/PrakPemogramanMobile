package com.unpas.elektronik.ui.periferal

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PeriferalData(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val nama: String,
    val harga: Int,
    val deskripsi: String,
    val jenis: String,
) {
    enum class Jenis {
        Mouse,
        Keyboard
    }
}