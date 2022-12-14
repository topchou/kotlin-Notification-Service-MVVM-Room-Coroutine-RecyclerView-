package com.roro.smartnotification

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class NotificationAdapter() :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TAG = "NotificationViewHolder"
    private lateinit var context: Context
    private lateinit var packageManager: PackageManager
    var list = ArrayList<DbNotification>()

    fun setListData(list: ArrayList<DbNotification>) {
        this.list = list
    }

    fun setListData(list: List<DbNotification>) {
        Log.d(TAG, "Roro setListData")
        this.list = ArrayList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d(TAG, "onCreateViewHolder")
        val view = LayoutInflater.from(parent.context)
            .inflate((R.layout.notification_list), parent, false)
        context = parent.context
        packageManager = context.packageManager
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NotificationViewHolder -> {
                Log.d(TAG, "isViewHolder")

                // Get Notification Info
                val notification = list[position]
                val time = notification.time
                val packageName = notification.packageName
                val title = notification.title
                val content = notification.content
                var icon: Drawable? = null
                try {
                    icon = packageManager.getApplicationIcon(packageName)
                } catch (e: PackageManager.NameNotFoundException) {
                    e.toString()
                }

                icon?.let { holder.iconView.setImageDrawable(it) }
                holder.packageNameView.text = packageName ?: "unknown package"
                holder.titleView.text = title ?: "with no title"
                holder.contentView.text = content ?: "with no content"
                //holder.timeView.text = Utils.convertTimeFormat(time)
                holder.timeView.text = time

                holder.delButton.setOnClickListener(View.OnClickListener {
                    Log.d(TAG, "Roro, notification id : ${notification.id}")
                    deleteCheck(notification)
                })


            }

        }
        Log.d(TAG, "ViewHolder")
    }

    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconView: ImageView = view.findViewById(R.id.icon)
        val timeView: TextView = view.findViewById(R.id.notification_time)
        val packageNameView: TextView = view.findViewById(R.id.notification_package_name)
        val titleView: TextView = view.findViewById(R.id.notification_title)
        val contentView: TextView = view.findViewById(R.id.notification_content)
        val delButton: ImageView = view.findViewById(R.id.delete_btn)

    }

    private fun deleteCheck(dbNotification: DbNotification) {
        val enableNotificationListenerAlertDialog = buildDeleteAlertDialog(dbNotification)
        enableNotificationListenerAlertDialog!!.setCanceledOnTouchOutside(false)
        enableNotificationListenerAlertDialog.show()
    }

    private fun buildDeleteAlertDialog(dbNotification: DbNotification): AlertDialog? {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle(R.string.delete_btn_title)
        alertDialogBuilder.setMessage(R.string.delete_btn_content)
        alertDialogBuilder.setPositiveButton("Yes"
        ) { dialog, id -> //delete
            val dao = DbNotificationDatabase.getInstance(context.applicationContext).getRoomDao()
            GlobalScope.launch {
                dao.delete(dbNotification)
            }
            /*
            val post_time_as_ID: Long = list.get(position).getPostTime()
            val deleteNotification = Intent(context, NotificationService::class.java)
            deleteNotification.action = Utils.DELETE
            deleteNotification.putExtra("DeleteID", post_time_as_ID)
            context.startService(deleteNotification)
            list.removeAt(position)
            notifyDataSetChanged()
            Log.d(TAG, "delete position $position")

             */
        }
        alertDialogBuilder.setNegativeButton("No"
        ) { dialog, id -> }
        return alertDialogBuilder.create()
    }


}