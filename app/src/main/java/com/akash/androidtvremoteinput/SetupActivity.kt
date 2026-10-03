package com.akash.androidtvremoteinput
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
class SetupActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("config", MODE_PRIVATE) }
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); build() }
    private fun build() {
        val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; setPadding(64,40,64,40) }
        root.addView(TextView(this).apply { text="Remote Input Setup"; textSize=30f })
        root.addView(TextView(this).apply { text="Assign a unique 2-digit code to each input you want to use."; textSize=18f; setPadding(0,20,0,20) })
        for (i in 1..9) {
            val row=LinearLayout(this)
            row.addView(TextView(this).apply { text="Input $i   "; textSize=18f })
            row.addView(EditText(this).apply { hint="2 digits"; inputType=InputType.TYPE_CLASS_NUMBER; setText(prefs.getString("code_"+i,"")); tag=i }, LinearLayout.LayoutParams(220,-2))
            root.addView(row)
        }
        root.addView(Button(this).apply {
            text="SAVE & START"
            setOnClickListener {
                val edit=prefs.edit()
                for (i in 1..9) {
                    val field=(root.getChildAt(i+1) as LinearLayout).getChildAt(1) as EditText
                    val value=field.text.toString().filter(Char::isDigit).take(2)
                    if(value.length==2) edit.putString("code_"+i,value) else edit.remove("code_"+i)
                }
                edit.putBoolean("configured",true).apply()
                startService(Intent(this@SetupActivity,RemoteInputService::class.java))
                Toast.makeText(this@SetupActivity,"Saved. Background service started.",Toast.LENGTH_SHORT).show()
            }
        })
        setContentView(root)
    }
}