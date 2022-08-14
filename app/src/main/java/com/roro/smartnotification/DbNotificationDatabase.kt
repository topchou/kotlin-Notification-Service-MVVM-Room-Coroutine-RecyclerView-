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
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance =
                        Room.databaseBuilder(context,
                            DbNotificationDatabase::class.java,
                            "notification_table").build()
                }
                return instance as DbNotificationDatabase
            }


        }
        /*
        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDataBase(context).also { instance = it }
        }

        private fun buildDataBase(context: Context) = Room.databaseBuilder(context,
            DbNotificationDatabase::class.java,
            "notification_table").build()

         */
    }


    abstract fun getRoomDao(): DbNotificationDao
}