package com.unpas.elektronik.ui.periferal

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PeriferalData::class],
    version = 1
)
abstract class PeriferalDatabase : RoomDatabase(){

    abstract fun periferalDao() : PeriferalDao

    companion object {

        @Volatile private var instance : PeriferalDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            PeriferalDatabase::class.java,
            "periferal.db"
        ).build()

    }
}
