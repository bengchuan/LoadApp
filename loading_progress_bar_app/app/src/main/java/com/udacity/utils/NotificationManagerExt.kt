package com.udacity.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.udacity.DetailActivity
import com.udacity.R

private val NOTIFICATION_ID = 0
fun NotificationManager.sendNotification(
    filename: String,
    status: String,
    applicationContext: Context
) {

    val contentIntent = Intent(applicationContext, DetailActivity::class.java)

    // Add the download file name and status
    contentIntent.putExtra(Intent.EXTRA_TITLE, filename)
    contentIntent.putExtra(Intent.EXTRA_TEXT, status)

    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.download_complete_channel_id)
    )
        .setSmallIcon(R.drawable.ic_download)
        .setContentTitle(
            applicationContext
                .getString(R.string.notification_title)
        )
        //.setContentText("${applicationContext.getString(R.string.notification_description)} ${status}")
        .setStyle(NotificationCompat.BigTextStyle())
        .setAutoCancel(true)
        .addAction(
            R.drawable.ic_download,
            applicationContext.getString(R.string.notification_button),
            contentPendingIntent
        )


    notify(NOTIFICATION_ID, builder.build())
}