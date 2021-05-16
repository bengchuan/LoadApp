package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.udacity.databinding.ActivityMainBinding
import com.udacity.utils.sendNotification
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_main.view.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var binding: ActivityMainBinding
    private lateinit var downloadManager: DownloadManager
    private lateinit var notificationManager: NotificationManager
    private var downloadFilename = ""
    private var downloadStatus = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(toolbar)

        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager


        // Notification
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        createChannel(
            getString(R.string.download_complete_channel_id),
            getString(R.string.download_complete_channel_name)
        )

        custom_button.setOnClickListener {
            when (downloadUrlOptions.checkedRadioButtonId) {
                // default value for uncheck RadioGroup. @see: RadioGroup.java mCheckedId
                -1 -> {
                    // show Toast to prompt user to click to download
                    Toast.makeText(
                        this,
                        R.string.please_select_file_to_download,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                else -> {
                    // set downloadFilename for notification later
                    downloadFilename =
                        findViewById<RadioButton>(downloadUrlOptions.checkedRadioButtonId).text.toString()
                    val uri =
                        getDownloadUriFromCheckedRadioId(downloadUrlOptions.checkedRadioButtonId)
                    Log.i(TAG, "Downloading from ${uri}")
                    download(uri)
                    custom_button.setState(ButtonState.Clicked)
                }
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            id?.takeIf { id > 0L }.run {
                val status = downloadManager.queryStatus(id!!)
                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL, DownloadManager.STATUS_FAILED -> {
                        notificationManager.sendNotification(
                            downloadFilename,
                            downloadStatus,
                            this@MainActivity
                        )

                        val msg =
                            if (status == DownloadManager.STATUS_SUCCESSFUL) getString(R.string.successful_message)
                            else getString(R.string.fail_message)

                        Toast.makeText(
                            this@MainActivity,
                            msg,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        // we log the error but don't show the notification
                        Log.w(TAG, "${downloadFilename} download status is ${downloadStatus}")
                    }
                }
            }
        }
    }

    private fun getDownloadUriFromCheckedRadioId(id: Int): Uri {
        return when (id) {
            R.id.glide -> Uri.parse("https://github.com/bumptech/glide")
            R.id.starter -> Uri.parse("https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip")
            R.id.retrofit -> Uri.parse("https://github.com/square/retrofit")
            R.id.error -> Uri.parse("https://google.com/this_does_not_exist")
            else -> throw RuntimeException("Invalid id for download radio group: ${id}")
        }
    }

    private fun download(uri: Uri) {
        val request =
            DownloadManager.Request(uri)
                .setTitle(getString(R.string.notification_title))
                .setDescription(getString(R.string.notification_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
        //.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    private fun DownloadManager.queryStatus(id: Long): Int {
        query(DownloadManager.Query().setFilterById(id)).use {
            if (it.moveToFirst()) {
                val statusIdx = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val status = it.getInt(statusIdx)
                return when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        Log.d(TAG, "Status is successful: ${status}")
                        downloadStatus = getString(R.string.status_successful)
                        status
                    }
                    DownloadManager.STATUS_FAILED -> {
                        Log.w(TAG, "Download status is: ${status}")
                        downloadStatus = getString(R.string.status_failed)
                        status
                    }
                    else -> {
                        //Mapping all the error to UNKNOWN ERROR.
                        Log.d(TAG, "Status failed with : ${status}")
                        downloadStatus = getString(R.string.status_unknown)
                        DownloadManager.ERROR_UNKNOWN
                    }
                }
            }
            // else
            return DownloadManager.ERROR_UNKNOWN
        }
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.download_complete_channel_desc)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }


    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
        private const val TAG = "MainActivity"
    }


}
