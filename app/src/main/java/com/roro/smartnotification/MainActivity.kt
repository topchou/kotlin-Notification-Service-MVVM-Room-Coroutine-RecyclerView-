package com.roro.smartnotification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.roro.smartnotification.databinding.ActivityMainBinding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.Observer
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.flow.collect
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
        supportActionBar?.hide()
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
        //model = ViewModelProvider(this, SavedStateViewModelFactory(application, this)).get(
        //    DbNotificationViewModel::class.java)
        viewModel = ViewModelProvider(this).get(DbNotificationViewModel::class.java)


        //Create observer which update UI
        adapter = NotificationAdapter()


        // Set recyclerView adapter
        //adapter = NotificationAdapter(mNotificationList)


        /*
         * Note: This doesn't work,so just change to below implement. Wonder know why.

        binding.notificationList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = adapter
        }
         */

        binding.notificationList.adapter = adapter
        binding.notificationList.layoutManager = LinearLayoutManager(this)
        /*
        viewModel.notifications.observe(this,
            Observer<List<DbNotification>> {
                adapter.setListData(ArrayList(it))
                adapter.notifyDataSetChanged()
                Log.d(TAG, "onChanged notify")
            })

         */

        lifecycle.coroutineScope.launch {
            viewModel.notifications().collect {
                Log.d(TAG, "Roro, kao")
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
        val saveData = Thread {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("MMdd_hhmmss")
            val docName = dateFormat.format(calendar.getTime()) + ".txt"
            file = File(getExternalFilesDir(null), docName)
            Log.d(TAG, "getPath: ${file?.path},  getAbsolutePath: ${file?.absolutePath}")
            var fos: FileOutputStream? = null
            val split = "----------------------\n\n"
            try {
                fos = FileOutputStream(file)
                /*
                for (i in mNotificationList.indices) {
                    val sbn = mNotificationList[i]
                    val extras = sbn.notification.extras
                    val packageName = sbn.packageName
                    val app = packageName ?: "From unknown package"
                    val title = extras.getString(Notification.EXTRA_TITLE)
                    val content = extras.getString(Notification.EXTRA_TEXT)
                    val time: String = Utils.convertTimeFormat(sbn.postTime)
                    fos.write("APP: $app\n".toByteArray())
                    fos.write("Received Time: $time\n".toByteArray())
                    fos.write("Title: $title\n".toByteArray())
                    fos.write("Content: $content\n".toByteArray())
                    fos.write(split.toByteArray())
                }

                 */
                Toast.makeText(this@MainActivity, "finish save", Toast.LENGTH_SHORT).show()
            } catch (e: FileNotFoundException) {
                Toast.makeText(this@MainActivity, "Save Fail", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } catch (e: IOException) {
                Toast.makeText(this@MainActivity, "Save Fail", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
        saveData.run()

        /*
        StorageManager sm = getSystemService(StorageManager.class);
        StorageVolume volume = sm.getPrimaryStorageVolume();
        Intent intent = volume.createOpenDocumentTreeIntent();
        startActivityForResult(intent, REQUEST_PERMISSION_WRITE);
         */
    }
}