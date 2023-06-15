package com.unpas.elektronik.ui.komputer

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete

@Dao
interface KomputerDao {
    @Query("SELECT * FROM KomputerData")
    suspend fun getAllKomputers(): List<KomputerData>

    @Insert
    suspend fun insertKomputer(komputer: KomputerData)

    @Update
    suspend fun updateKomputer(komputer: KomputerData)

    @Delete
    suspend fun deleteKomputer(komputer: KomputerData)
}