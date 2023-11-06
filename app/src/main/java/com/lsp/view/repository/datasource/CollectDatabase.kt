package com.lsp.view.repository.datasource

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lsp.view.repository.datasource.dao.CollectDao
import com.lsp.view.repository.datasource.model.Collect

@Database(entities = [Collect::class], version = 1,exportSchema = false)
abstract class CollectDatabase: RoomDatabase(){
    abstract fun collectDao():CollectDao
    companion object{
        @Volatile
        private var Instance:CollectDatabase? = null

        fun getDatabase(context: Context):CollectDatabase{
            return Instance?: synchronized(this) {
                Room.databaseBuilder(context,CollectDatabase::class.java,"collect_database").build().also { Instance = it }
            }
        }

    }
}