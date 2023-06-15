package com.unpas.elektronik.ui.smartphone

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SmartphoneData::class],
    version = 1
)
abstract class SmartphoneDatabase : RoomDatabase(){

    abstract fun smartphoneDao() : SmartphoneDao

    companion object {

        @Volatile private var instance : SmartphoneDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            SmartphoneDatabase::class.java,
            "smartphone.db"
        ).build()

    }
}
