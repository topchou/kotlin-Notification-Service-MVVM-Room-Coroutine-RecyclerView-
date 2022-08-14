package com.roro.smartnotification

import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Entity
import com.roro.smartnotification.DbNotification.Companion.TABLE_NAME

@Entity(tableName = TABLE_NAME)
class DbNotification {
    companion object {
        const val TABLE_NAME = "notification_table"
    }

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @ColumnInfo(name = "packageName")
    var packageName: String = ""

    @ColumnInfo(name = "title")
    var title: String = ""

    @ColumnInfo(name = "content")
    var content: String = ""

    @ColumnInfo(name = "post_time")
    var time: String = ""

    constructor(packageName: String, title: String?, content: String?, time: String) {
        //this.id = id

        this.packageName = packageName

        if (title != null) {
            this.title = title
        }
        if (content != null) {
            this.content = content
        }

        this.time = time
    }

}