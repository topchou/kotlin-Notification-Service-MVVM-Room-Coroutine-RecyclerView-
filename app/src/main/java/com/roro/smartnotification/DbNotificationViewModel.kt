package com.roro.smartnotification

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.flow.Flow

class DbNotificationViewModel(application: Application) :
    AndroidViewModel(application) {
    private var notificationDao: DbNotificationDao =
        DbNotificationDatabase.getInstance(getApplication())?.getRoomDao()

    fun notifications(): Flow<List<DbNotification>> {
        return notificationDao.all()
    }

    /*
    class Factory(private val mApplication: Application) :
        ViewModelProvider.NewInstanceFactory() {
        private val mRepository: DbNotificationRepository? = DbNotificationRepository.getInstance()
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return DbNotificationViewModel(mApplication, mRepository!!) as T
        }
    }

     */
}