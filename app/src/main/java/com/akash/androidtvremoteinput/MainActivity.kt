package com.akash.androidtvremoteinput

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var resultText: TextView
    private lateinit var sequenceText: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val buffer = StringBuilder()
    private var clearRunnable: Runnable? = null

    private val mappings = mapOf(
        "11" to "INPUT 1", "12" to "INPUT 2", "13" to "INPUT 3",
        "21" to "INPUT 4", "22" to "INPUT 5", "23" to "INPUT 6",
        "31" to "INPUT 7", "32" to "INPUT 8", "33" to "INPUT 9"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.rgb(12, 12, 12))
            setPadding(48, 32, 48, 32)
            isFocusableInTouchMode = true
        }
        val title = TextView(this).apply {
            text = "REMOTE INPUT"
            textSize = 26f
            setTextColor(Color.rgb(102, 187, 106))
            gravity = Gravity.CENTER
        }
        resultText = TextView(this).apply {
            text = "READY"
            textSize = 58f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 36, 0, 20)
        }
        sequenceText = TextView(this).apply {
            text = "Enter a 2-digit code"
            textSize = 20f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }
        val clearButton = Button(this).apply {
            text = "CLEAR"
            setOnClickListener { resetSequence() }
        }
        root.addView(title)
        root.addView(resultText, LinearLayout.LayoutParams(-1, -2))
        root.addView(sequenceText, LinearLayout.LayoutParams(-1, -2))
        root.addView(clearButton)
        setContentView(root)
        root.requestFocus()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val digit = when (keyCode) {
            KeyEvent.KEYCODE_0 -> '0'; KeyEvent.KEYCODE_1 -> '1'
            KeyEvent.KEYCODE_2 -> '2'; KeyEvent.KEYCODE_3 -> '3'
            KeyEvent.KEYCODE_4 -> '4'; KeyEvent.KEYCODE_5 -> '5'
            KeyEvent.KEYCODE_6 -> '6'; KeyEvent.KEYCODE_7 -> '7'
            KeyEvent.KEYCODE_8 -> '8'; KeyEvent.KEYCODE_9 -> '9'
            else -> null
        }
        if (digit != null) {
            buffer.append(digit)
            if (buffer.length > 2) buffer.delete(0, buffer.length - 2)
            sequenceText.text = "Code: $buffer"
            clearPendingReset()
            if (buffer.length == 2) {
                resultText.text = mappings[buffer.toString()] ?: "NO INPUT"
                scheduleReset()
            } else {
                handler.postDelayed({ if (buffer.length == 1) resetSequence() }, 1800)
            }
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            resetSequence()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun scheduleReset() {
        clearPendingReset()
        clearRunnable = Runnable { resetSequence() }
        handler.postDelayed(clearRunnable!!, 2500)
    }

    private fun clearPendingReset() {
        clearRunnable?.let(handler::removeCallbacks)
        clearRunnable = null
    }

    private fun resetSequence() {
        clearPendingReset()
        buffer.clear()
        sequenceText.text = "Enter a 2-digit code"
    }

    override fun onDestroy() {
        clearPendingReset()
        super.onDestroy()
    }
}
