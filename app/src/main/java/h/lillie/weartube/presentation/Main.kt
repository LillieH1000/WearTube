package h.lillie.weartube.presentation

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import h.lillie.weartube.R
import org.json.JSONObject

class Main : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        val policy = StrictMode.ThreadPolicy.Builder().permitNetwork().build()
        StrictMode.setThreadPolicy(policy)

        val playerRequest: String = Extractor().playerRequest(this@Main, "ziGLZkisja4")
        val intent = Intent(this@Main, Player::class.java)
        intent.putExtra("url", JSONObject(playerRequest).getJSONObject("streamingData").optString("hlsManifestUrl"))
        startActivity(intent)
    }
}