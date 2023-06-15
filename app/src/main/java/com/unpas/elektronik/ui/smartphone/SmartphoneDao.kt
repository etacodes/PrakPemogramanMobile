package com.unpas.elektronik.ui.smartphone

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete

@Dao
interface SmartphoneDao {
    @Query("SELECT * FROM SmartphoneData")
    suspend fun getAllSmartphones(): List<SmartphoneData>

    @Insert
    suspend fun insertSmartphone(smartphone: SmartphoneData)

    @Update
    suspend fun updateSmartphone(smartphone: SmartphoneData)

    @Delete
    suspend fun deleteSmartphone(smartphone: SmartphoneData)
}