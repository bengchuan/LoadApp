package com.udacity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        // get value from intent
        val filename = intent?.extras?.getString(Intent.EXTRA_TITLE, "") ?: ""
        val status = intent?.extras?.getString(Intent.EXTRA_TEXT, "") ?: ""
        val success = status == getString(R.string.status_successful)

        val textColor =
            if (success) getColor(R.color.colorPrimary) else getColor(R.color.colorAccent)
        val icon =
            if (success) getDrawable(R.drawable.ic_checked) else getDrawable(R.drawable.ic_unavailable)

        label_filename_value.text = filename
        label_status_value.text = status
        label_status_value.setTextColor(textColor)
        iconStatus.setImageDrawable(icon)

        button_ok.setOnClickListener { finish() }
    }
}
