package com.roro.smartnotification

import java.text.SimpleDateFormat
import java.util.*

class Utils {

    companion object {
        private val simpleDateFormat = SimpleDateFormat("MM/dd - hh:mm:ss")
        const val PRIMARY_CHANNEL_ID = "TestNotification"
        const val START_SERVICE = "START_SERVICE"
        const val STOP_SERVICE = "STOP_SERVICE"
        const val CHECK_DIFF = "CHECK_DIFF"
        const val CLEAR = "CLEAR"
        const val DELETE = "DELETE"

        fun convertTimeFormat(time: Long): String {
            val date = Date(time)
            return simpleDateFormat.format(date)
        }
    }

}