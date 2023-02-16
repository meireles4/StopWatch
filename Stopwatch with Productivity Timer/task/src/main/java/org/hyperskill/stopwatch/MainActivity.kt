package org.hyperskill.stopwatch

import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat


const val CHANNEL_ID = "org.hyperskill"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var seconds = 0
        var started = false
        var upperLimitSeconds = -1
        var upperLimitReached = false
        var timerText = findViewById<TextView>(R.id.textView)
        val startButton = findViewById<Button>(R.id.startButton)
        val resetButton = findViewById<Button>(R.id.resetButton)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val setingsButton = findViewById<Button>(R.id.settingsButton)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Upper time reached"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle("Notification")
            .setContentText("Time exceeded")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .build()

        notificationBuilder.flags = Notification.FLAG_INSISTENT or Notification.FLAG_ONLY_ALERT_ONCE

        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager



        progressBar.visibility = View.INVISIBLE
        var progressBarDefaultColor = Color.CYAN

        val handler = Handler(Looper.getMainLooper())

        resetButton.setOnClickListener {
            started = false
            seconds = 0
            timerText.text = secondsToFormattedString(seconds)
            progressBar.visibility = View.INVISIBLE
            setingsButton.isEnabled = true
            timerText.setTextColor(Color.GRAY)
            upperLimitReached = false
        }

        val timer: Runnable = object: Runnable {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun run() {
                val newColor = getRandomColor(progressBarDefaultColor)
                progressBar.indeterminateTintList = ColorStateList.valueOf(newColor)
                progressBarDefaultColor = newColor
                timerText.text = secondsToFormattedString(seconds)
                if (seconds >= upperLimitSeconds && upperLimitSeconds > 0){
                    timerText.setTextColor(Color.RED)
                    notificationManager.notify(393939, notificationBuilder)
                    upperLimitReached = true
                }
                seconds++
                if (started) handler.postDelayed(this, 1000)
            }
        }

        startButton.setOnClickListener {
            if (!started) {
                progressBar.visibility = View.VISIBLE
                started = true
                handler.post(timer)
                setingsButton.isEnabled = false
            }
        }

        setingsButton.setOnClickListener{

            val contentView  = LayoutInflater.from(this).inflate(R.layout.alert_dialog, null, false)

            AlertDialog.Builder(this)
                .setTitle("Set upper limit in seconds")
                .setView(contentView)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val editText = contentView.findViewById<EditText>(R.id.upperLimitEditText)
                    if (editText.text.isNotBlank()) {
                        upperLimitSeconds = editText.text.toString().toInt()
                    }
                }
                .show()
        }

    }

    private fun secondsToFormattedString(seconds: Int): String {
        val min = seconds / 60
        val sec = seconds - min * 60
        return "${String.format("%02d", min)}:${String.format("%02d", sec)}"
    }

    private fun getRandomColor(currentColor: Int): Int {
        val progressBarColors = arrayOf(Color.RED, Color.BLUE, Color.CYAN, Color.GREEN)
        return progressBarColors.filter { it != currentColor }.random()
    }
}