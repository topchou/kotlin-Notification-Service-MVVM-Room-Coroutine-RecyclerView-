package com.roro.smartnotification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class NotificationService : NotificationListenerService() {
    private val TAG = "NotificationService"
    private val CHANNEL_ID = "NotificationDB"
    private val channelName = "NotificationDB"
    private val ONGOING_NOTIFICATION_ID = 1001

    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, channelName, importance)
        channel.description = "Training DB"

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel)
        }


        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE)

        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(getText(R.string.foreground_notification_title))
            .setContentText(getText(R.string.foreground_notification_message))
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setColor(Color.RED)
            .setContentIntent(pendingIntent)
            .setChannelId(CHANNEL_ID)
            .setTicker(getText(R.string.ticker_text))
            .build()

        // Notification ID cannot be 0.
        startForeground(ONGOING_NOTIFICATION_ID, notification)

    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "NotificationService onBind")
        return super.onBind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Utils.START_SERVICE -> {
                return super.onStartCommand(intent, flags, startId)
            }

            Utils.STOP_SERVICE -> {
                Log.d(TAG, "StopForeground")
            }

            Utils.CLEAR -> {

            }

            Utils.DELETE -> {

            }

        }
        return START_STICKY
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.d(TAG, "POST")

        val notification = sbn.notification
        val extras = notification.extras
        val time = Utils.convertTimeFormat(sbn.postTime)
        val packageName = sbn.packageName
        val title = extras.getString(Notification.EXTRA_TITLE)
        val content = extras.getString(Notification.EXTRA_TEXT)
        if (title == null && content == null) return
        val dbNotification = DbNotification(packageName, title, content, time)

        val dao = DbNotificationDatabase.getInstance(application).getRoomDao()

        GlobalScope.launch {
            dao.insert(dbNotification)
            Log.d(TAG, "Roro insert")
        }

    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d(TAG, "remove")
        super.onNotificationRemoved(sbn)
    }
}