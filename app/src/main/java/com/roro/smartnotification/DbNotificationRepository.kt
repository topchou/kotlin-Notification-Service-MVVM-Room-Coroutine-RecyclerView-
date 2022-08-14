package com.roro.smartnotification

import android.app.Application
import androidx.lifecycle.LiveData

class DbNotificationRepository private constructor(private val mDatabase: DbNotificationDatabase) {
    /*
    private var dbNotificationDao: DbNotificationDao =
        mDatabase.getRoomDao()
    private val dbNotificationList: LiveData<List<DbNotification>> = dbNotificationDao.all()

    fun getNotifications(): LiveData<List<DbNotification>> {
        return dbNotificationList
    }

    fun insert(dbNotification: DbNotification) {
        dbNotificationDao.insert(dbNotification)
    }

    companion object {
        private var sInstance: DbNotificationRepository? = null

        @JvmStatic
        fun getInstance(database: DbNotificationDatabase): DbNotificationRepository? {
            if (sInstance == null) {
                synchronized(DbNotificationRepository::class.java) {
                    if (sInstance == null) {
                        sInstance = DbNotificationRepository(database)
                    }
                }
            }
            return sInstance
        }
    }

     */
}