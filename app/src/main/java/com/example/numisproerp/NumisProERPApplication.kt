package com.numisproerp

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.numisproerp.data.database.AppDatabase

class NumisProERPApplication : Application() {

    companion object {
        private lateinit var instance: NumisProERPApplication

        fun getInstance(): NumisProERPApplication {
            return instance
        }

        fun getAppContext(): Context {
            return instance.applicationContext
        }
    }

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "numisproerp_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}
