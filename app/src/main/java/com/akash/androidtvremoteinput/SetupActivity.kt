package com.akash.androidtvremoteinput

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SetupActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("config", MODE_PRIVATE) }
    private val targetNames = listOf(
        "Google TV Home", "Antenna", "ATV", "AV",
        "HDMI 1 / MiBOX", "HDMI 2 / PlayStation 5"
    )
    private val spinners = mutableListOf<Spinner>()
    private val codeFields = mutableListOf<EditText>()
    private var inputs: List<TvInputItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        build()
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 10)
        }
    }

    override fun onResume() {
        super.onResume()
        if (spinners.isNotEmpty()) refreshInputs()
    }

    private fun build() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(56, 32, 56, 32)
        }
        root.addView(TextView(this).apply {
            text = "Remote Input — BPL TV"
            textSize = 28f
        })
        root.addView(TextView(this).apply {
            text = "Assign a 2-digit code to each input. Refresh to read the actual TV input IDs from Android."
            textSize = 16f
            setPadding(0, 12, 0, 20)
        })

        for (i in targetNames.indices) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 8, 0, 8)
            }
            row.addView(TextView(this).apply {
                text = targetNames[i]
                textSize = 17f
            }, LinearLayout.LayoutParams(250, -2))

            val spinner = Spinner(this)
            spinners += spinner
            row.addView(spinner, LinearLayout.LayoutParams(420, -2))

            val code = EditText(this).apply {
                hint = "2 digits"
                inputType = InputType.TYPE_CLASS_NUMBER
                setText(prefs.getString("code_" + (i + 1), ""))
                setSelectAllOnFocus(true)
            }
            codeFields += code
            row.addView(code, LinearLayout.LayoutParams(150, -2))
            root.addView(row)
        }

        root.addView(Button(this).apply {
            text = "REFRESH TV INPUTS"
            setOnClickListener { refreshInputs() }
        })
        root.addView(Button(this).apply {
            text = "SAVE CONFIGURATION"
            setOnClickListener { saveConfiguration() }
        })
        root.addView(Button(this).apply {
            text = "ENABLE REMOTE KEY CAPTURE"
            setOnClickListener {
                try {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                } catch (_: Exception) {
                    startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            }
        })
        root.addView(Button(this).apply {
            text = "OPEN DIAGNOSTICS"
            setOnClickListener { showDiagnostics() }
        })
        root.addView(TextView(this).apply {
            text = "Enter two digits within 1.8 seconds. The app will ask for confirmation before switching."
            textSize = 14f
            setPadding(0, 18, 0, 0)
        })
        setContentView(root)
        refreshInputs()
    }

    private fun refreshInputs() {
        inputs = InputCatalog.get(this)
        val labels = mutableListOf("Google TV Home")
        labels += if (inputs.isEmpty()) listOf("No TV inputs reported — refresh after TV startup")
        else inputs.map { it.label + if (it.passthrough) "  • passthrough" else "" }

        spinners.forEachIndexed { index, spinner ->
            spinner.adapter = ArrayAdapter(
                this, android.R.layout.simple_spinner_dropdown_item, labels
            )
            val savedId = prefs.getString("input_id_" + (index + 1), null)
            val selectedIndex = when {
                savedId == InputCatalog.HOME_ID -> 0
                savedId != null -> inputs.indexOfFirst { it.id == savedId }
                    .let { if (it >= 0) it + 1 else 0 }
                else -> autoMatch(index)
            }
            spinner.setSelection(selectedIndex)
        }
    }

    private fun autoMatch(index: Int): Int {
        if (index == 0) return 0
        val found = inputs.indexOfFirst {
            val label = it.label.lowercase().replace(" ", "")
            when (index) {
                1 -> label.contains("antenna") || label.contains("dvb") || label.contains("dtv")
                2 -> label == "atv" || label.contains("analog")
                3 -> label == "av" || label.contains("composite")
                4 -> label.contains("hdmi1")
                5 -> label.contains("hdmi2")
                else -> false
            }
        }
        return if (found >= 0) found + 1 else 0
    }

    private fun saveConfiguration() {
        val edit = prefs.edit()
        for (i in targetNames.indices) {
            val code = codeFields[i].text.toString().filter(Char::isDigit).take(2)
            if (code.length == 2) edit.putString("code_" + (i + 1), code)
            else edit.remove("code_" + (i + 1))

            val selected = spinners[i].selectedItemPosition
            if (selected == 0) {
                edit.putString("input_id_" + (i + 1), InputCatalog.HOME_ID)
                edit.putString("label_" + (i + 1), targetNames[i])
            } else if (selected - 1 in inputs.indices) {
                val item = inputs[selected - 1]
                edit.putString("input_id_" + (i + 1), item.id)
                edit.putString("label_" + (i + 1), targetNames[i])
            }
        }
        edit.putBoolean("configured", true).apply()
        try {
            startForegroundService(Intent(this, RemoteInputService::class.java))
        } catch (_: Exception) {
            startService(Intent(this, RemoteInputService::class.java))
        }
        Toast.makeText(this, "Saved. Enable Remote Input in Accessibility.", Toast.LENGTH_LONG).show()
    }

    private fun showDiagnostics() {
        val message = buildString {
            append("TV inputs reported by Android:\n\n")
            if (inputs.isEmpty()) append("None reported.\n")
            inputs.forEach {
                append("Label: " + it.label + "\n")
                append("ID: " + it.id + "\n")
                append("Passthrough: " + it.passthrough + "\n")
                append("Type: " + it.type + "\n\n")
            }
        }
        AlertDialog.Builder(this)
            .setTitle("TV Input Diagnostics")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}