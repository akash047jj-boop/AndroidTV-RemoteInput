package com.akash.androidtvremoteinput

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ConfirmationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inputId = intent.getStringExtra("input_id")
        val label = intent.getStringExtra("label") ?: "Selected input"

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(80, 60, 80, 60)
        }
        root.addView(TextView(this).apply {
            text = "Switch TV input?"
            textSize = 28f
        })
        root.addView(TextView(this).apply {
            text = label
            textSize = 22f
            setPadding(0, 24, 0, 36)
        })
        root.addView(Button(this).apply {
            text = "CONFIRM"
            setOnClickListener {
                val ok = InputSwitcher.switchTo(this@ConfirmationActivity, inputId)
                android.widget.Toast.makeText(
                    this@ConfirmationActivity,
                    if (ok) "Switch command sent" else "TV firmware did not accept the switch command",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                finish()
            }
        })
        root.addView(Button(this).apply {
            text = "CANCEL"
            setOnClickListener { finish() }
        })
        setContentView(root)
    }

    companion object {
        private const val CHANNEL_ID = "remote_input_confirm"

        fun postFallbackNotification(context: Context, inputId: String?, label: String) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= 26) {
                manager.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        "Remote Input Confirmation",
                        NotificationManager.IMPORTANCE_HIGH
                    )
                )
            }
            val confirmIntent = Intent(context, ConfirmationActivity::class.java).apply {
                putExtra("input_id", inputId)
                putExtra("label", label)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val pending = PendingIntent.getActivity(
                context, 101, confirmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Switch TV input?")
                .setContentText(label)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .setContentIntent(pending)
                .build()
            NotificationManagerCompat.from(context).notify(101, notification)
        }
    }
}