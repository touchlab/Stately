package sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

actual class Sample {
    actual fun checkMe() = 44
}

actual object Platform {
    actual val name: String = "Android"
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hello()
        Sample().checkMe()
        setContentView(R.layout.activity_main)

        val sampleText = findViewById<EditText>(R.id.sampleText)
        findViewById<Button>(R.id.addSample).setOnClickListener {
            State.putSample(sampleText.text.toString())
        }
        findViewById<Button>(R.id.printValues).setOnClickListener {
            State.printAll()
        }
    }

}