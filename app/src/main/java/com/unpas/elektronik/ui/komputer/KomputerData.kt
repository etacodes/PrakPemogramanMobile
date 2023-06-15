package com.unpas.elektronik.ui.komputer

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class KomputerData(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val merk: String,
    val jenis: String,
    val harga: Int,
    val dapat_diupgrade: Boolean,
    val spesifikasi: String
) {
    enum class Jenis {
        Laptop,
        Desktop,
        AIO,
    }
}