package sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

actual class Sample {
    actual fun checkMe() = 44
}

actual object Platform {
    actual val name: String = "Android"
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Sample().checkMe()
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.main_text).text = hello()
        findViewById<Button>(R.id.button).setOnClickListener {
            Collections.putSample("Hello")
        }
        viewUpdater = object : ViewUpdater {
            override fun dataUpdate(t: String) {
                findViewById<TextView>(R.id.main_text).text = t
            }
        }
    }
}