package com.josias.pesagempaginainicial.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [Pesagem::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pesagemDao(): PesagemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pesagem_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

