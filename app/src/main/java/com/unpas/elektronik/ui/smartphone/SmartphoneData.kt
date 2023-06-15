package com.unpas.elektronik.ui.smartphone

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SmartphoneData(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val model: String,
    val warna: String,
    val storage: Int,
    val tanggal_rilis: String,
    val sistem_operasi: String
) {
    enum class SistemOperasi {
        Android,
        IOS,
    }
}