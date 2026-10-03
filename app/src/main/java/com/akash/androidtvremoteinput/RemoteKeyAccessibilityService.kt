package com.akash.androidtvremoteinput

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class RemoteKeyAccessibilityService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private val digits = StringBuilder()
    private var clearRunnable: Runnable? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = serviceInfo.apply {
            flags = flags or android.accessibilityservice.AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_UP) return false
        val digit = when (event.keyCode) {
            KeyEvent.KEYCODE_0 -> '0'
            KeyEvent.KEYCODE_1 -> '1'
            KeyEvent.KEYCODE_2 -> '2'
            KeyEvent.KEYCODE_3 -> '3'
            KeyEvent.KEYCODE_4 -> '4'
            KeyEvent.KEYCODE_5 -> '5'
            KeyEvent.KEYCODE_6 -> '6'
            KeyEvent.KEYCODE_7 -> '7'
            KeyEvent.KEYCODE_8 -> '8'
            KeyEvent.KEYCODE_9 -> '9'
            else -> return false
        }

        digits.append(digit)
        clearRunnable?.let(handler::removeCallbacks)

        if (digits.length >= 2) {
            val code = digits.takeLast(2).toString()
            digits.clear()
            val prefs = getSharedPreferences("config", MODE_PRIVATE)
            val target = (1..6).firstOrNull {
                prefs.getString("code_" + it, "") == code
            }
            if (target != null) {
                val inputId = prefs.getString("input_id_" + target, null)
                val label = prefs.getString("label_" + target, "Input " + target) ?: "Input " + target
                val confirm = Intent(this, ConfirmationActivity::class.java).apply {
                    putExtra("input_id", inputId)
                    putExtra("label", label)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                try {
                    startActivity(confirm)
                } catch (_: Exception) {
                    ConfirmationActivity.postFallbackNotification(this, inputId, label)
                }
            }
        } else {
            clearRunnable = Runnable { digits.clear() }
            handler.postDelayed(clearRunnable!!, 1800)
        }
        return false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}