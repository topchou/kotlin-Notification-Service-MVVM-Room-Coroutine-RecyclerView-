package com.roro.smartnotification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.roro.smartnotification.databinding.ActivityMainBinding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private lateinit var notificationManager: NotificationManager
    private lateinit var viewModel: DbNotificationViewModel
    private lateinit var adapter: NotificationAdapter
    private var notifyID = 0
    private var file: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Hide ActionBar
        //supportActionBar?.hide()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        //Start service for catching system Notifications in background
        val intent = Intent(this, NotificationService::class.java)
        intent.action = Utils.START_SERVICE
        startService(intent)

        // setting notification listener permission
        if (!isNotificationListenerEnable()) {
            askNotificationListenerPermission()
        }

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //Get the view model
        viewModel = ViewModelProvider(this).get(DbNotificationViewModel::class.java)

        //Create observer which update UI
        adapter = NotificationAdapter()

        /*
         * Note: This doesn't work,so just change to below implement. Wonder know why.

        binding.notificationList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = adapter
        }
         */

        binding.notificationList.adapter = adapter
        binding.notificationList.layoutManager = LinearLayoutManager(this)

        lifecycle.coroutineScope.launch {
            viewModel.notifications().collect {
                Log.d(TAG, "Roro, notifications onChange")
                adapter.setListData(it)
                adapter.notifyDataSetChanged()
            }
        }

        binding.createNotificationBtn.setOnClickListener {
            createTestNotification()
        }

        binding.exportBtn.setOnClickListener {
            //export data to storage
            export()
        }

        binding.share.setOnClickListener {
            share()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.clearAll -> {
                GlobalScope.launch {
                    viewModel.clearAll()
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun share() {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.data = Uri.parse("mailto:")
        emailIntent.type = "message/rfc822"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email_address)))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
        emailIntent.putExtra(Intent.EXTRA_TEXT, "-")

        // Note: Accessing into storage make sure -> add FileProvider declaration in Manifest
        if (file != null) {
            val uri = FileProvider.getUriForFile(this@MainActivity, "$packageName.provider", file!!)
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            emailIntent.putExtra(Intent.EXTRA_STREAM, uri)
        }
        startActivity(Intent.createChooser(emailIntent, "Send mail..."))
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
        val resumeActivity = Intent(this, NotificationService::class.java)
        resumeActivity.action = Utils.CHECK_DIFF
        startService(resumeActivity)
    }

    /*
     * Check the "enabled_notification_listener" permission is allowed or not.
     */
    private fun isNotificationListenerEnable(): Boolean {
        var enable = false
        val packageName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (flat != null) {
            enable = flat.contains(packageName)
        }
        return enable
    }

    /*
     * Show dialog for asking for permission, can only be dismissed by btn
     */
    private fun askNotificationListenerPermission() {
        val enableNotificationListenerAlertDialog: AlertDialog =
            buildNotificationServiceAlertDialog()
        enableNotificationListenerAlertDialog.setCanceledOnTouchOutside(false)
        enableNotificationListenerAlertDialog.show()
    }

    private fun buildNotificationServiceAlertDialog(): AlertDialog {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(R.string.alert_dialog_permission_title)
        alertDialogBuilder.setMessage(R.string.alert_dialog_permission_content)

        //Intent to activate settings page
        alertDialogBuilder.setPositiveButton("Yes"
        ) { dialog, id ->
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
        }

        // Show toast if user not allow the permission.
        alertDialogBuilder.setNegativeButton("No"
        ) { dialog, id ->
            Toast.makeText(this@MainActivity,
                "App cannot work without notification permission",
                Toast.LENGTH_SHORT).show()
        }
        return alertDialogBuilder.create()
    }

    private fun createTestNotification() {
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            Utils.PRIMARY_CHANNEL_ID,
            "Channel",
            NotificationManager.IMPORTANCE_HIGH)
        channel.description = "000"
        channel.enableLights(true)
        channel.enableVibration(true)
        notificationManager.createNotificationChannel(channel)
        notifyID++
        val mNotifyBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, Utils.PRIMARY_CHANNEL_ID)
                .setContentTitle("New Message $notifyID")
                .setContentText("You've received new messages.")
                .setSmallIcon(R.drawable.ic_icon)
        notificationManager.notify(
            notifyID,
            mNotifyBuilder.build())
    }

    private fun export() {
        //implement extract
        val enableExportAlertDialog = buildExportAlertDialog()
        enableExportAlertDialog!!.setCanceledOnTouchOutside(false)
        enableExportAlertDialog.show()
    }

    private fun buildExportAlertDialog(): AlertDialog? {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(R.string.export_title)
        alertDialogBuilder.setMessage(R.string.export_message)
        alertDialogBuilder.setPositiveButton("Yes"
        ) { dialog, id ->
            exportToLocal()
        }
        alertDialogBuilder.setNegativeButton("No"
        ) { dialog, id ->
            Toast.makeText(this@MainActivity, "Stop Export ", Toast.LENGTH_SHORT).show()
        }
        return alertDialogBuilder.create()
    }

    private fun exportToLocal() {
        lifecycle.coroutineScope.launch {
            Log.d(TAG, "Roro launch exportToLocal")
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("MMdd_hhmmss")
            val docName = dateFormat.format(calendar.getTime()) + ".txt"
            file = File(getExternalFilesDir(null), docName)
            Log.d(TAG, "Roro getPath: ${file?.path},  getAbsolutePath: ${file?.absolutePath}")
            var fos: FileOutputStream
            val split = "----------------------\n\n"
            var list = viewModel.notifications().flattenToList()
            Log.d(TAG, "Roro list size = ${list.size}")
            try {
                fos = FileOutputStream(file)

                for (notification in list) {
                    val packageName = notification.packageName
                    val title = notification.title
                    val content = notification.content
                    val time = notification.time
                    fos.write("APP packageName: $packageName\n".toByteArray())
                    fos.write("Received Time: $time\n".toByteArray())
                    fos.write("Title: $title\n".toByteArray())
                    fos.write("Content: $content\n".toByteArray())
                    fos.write(split.toByteArray())
                }
                Toast.makeText(this@MainActivity, "Roro finish save", Toast.LENGTH_SHORT).show()
            } catch (e: FileNotFoundException) {
                Toast.makeText(this@MainActivity, "Roro Save Fail", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } catch (e: IOException) {
                Toast.makeText(this@MainActivity, "Roro Save Fail", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private suspend fun <T> Flow<List<T>>.flattenToList() =
        flatMapConcat { it.asFlow() }.toList()
}