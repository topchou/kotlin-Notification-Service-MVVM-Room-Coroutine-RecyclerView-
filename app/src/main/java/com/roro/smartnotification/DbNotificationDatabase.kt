package com.roro.smartnotification

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [(DbNotification::class)], version = 1, exportSchema = false)
abstract class DbNotificationDatabase : RoomDatabase() {
    companion object {
        @Volatile
        private var INSTANCE: DbNotificationDatabase? = null
        fun getInstance(context: Context): DbNotificationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance =
                    Room.databaseBuilder(context,
                        DbNotificationDatabase::class.java,
                        "notification_table").build()
                INSTANCE = instance

                instance
            }


        }
    }


    abstract fun getRoomDao(): DbNotificationDao
}