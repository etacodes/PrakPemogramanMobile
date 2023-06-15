package com.unpas.elektronik.ui.komputer

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [KomputerData::class],
    version = 1
)
abstract class KomputerDatabase : RoomDatabase(){

    abstract fun komputerDao() : KomputerDao

    companion object {

        @Volatile private var instance : KomputerDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            KomputerDatabase::class.java,
            "komputer.db"
        ).build()

    }
}
