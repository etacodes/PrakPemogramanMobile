package com.unpas.elektronik.ui.periferal

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete

@Dao
interface PeriferalDao {
    @Query("SELECT * FROM PeriferalData")
    suspend fun getAllPeriferals(): List<PeriferalData>

    @Insert
    suspend fun insertPeriferal(periferal: PeriferalData)

    @Update
    suspend fun updatePeriferal(periferal: PeriferalData)

    @Delete
    suspend fun deletePeriferal(periferal: PeriferalData)
}