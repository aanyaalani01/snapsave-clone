package com.snapsave

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.*

class MainActivity : AppCompatActivity() {

    val client = OkHttpClient()

    // CHANGE THIS to your backend IP
    val server = "http://YOUR_SERVER_IP:5000"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(30,30,30,30)

        val urlInput = EditText(this)
        urlInput.hint = "Paste URL"

        val btn = Button(this)
        btn.text = "Download"

        val progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)

        layout.addView(urlInput)
        layout.addView(btn)
        layout.addView(progress)

        setContentView(layout)

        btn.setOnClickListener {
            download(urlInput.text.toString(), progress)
        }
    }

    fun download(url: String, progress: ProgressBar) {

        val json = JSONObject()
        json.put("url", url)
        json.put("mode", "mp4")

        val body = RequestBody.create(
            MediaType.parse("application/json"),
            json.toString()
        )

        val req = Request.Builder()
            .url("$server/download")
            .post(body)
            .build()

        client.newCall(req).enqueue(object: Callback {

            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {

                val input = response.body!!.byteStream()
                val file = File(getExternalFilesDir(null), "video.mp4")
                val output = FileOutputStream(file)

                val buffer = ByteArray(1024)
                var len: Int
                var total = 0

                while (input.read(buffer).also { len = it } > 0) {
                    total += len
                    output.write(buffer, 0, len)

                    runOnUiThread {
                        progress.progress = (total / 1000)
                    }
                }

                output.close()
            }
        })
    }
}
