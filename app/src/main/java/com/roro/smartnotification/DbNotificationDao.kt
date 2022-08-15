package com.roro.smartnotification

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/*
 * Define each entity as an annotated data class,
 * and the interactions with that entity as an annotated interface, called a data access object (DAO)
 */
@Dao
interface DbNotificationDao {
    @Insert
    fun insertAll(vararg dbNotifications: DbNotification?)

    @Insert
    fun insert(dbNotification: DbNotification)

    @Delete
    fun delete(dbNotification: DbNotification?)

    @Query("SELECT * FROM notification_table ORDER BY id DESC")
    fun all(): Flow<List<DbNotification>>

    @Query("DELETE FROM notification_table")
    fun deleteAll()

}