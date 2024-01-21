package h.lillie.weartube.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.PowerManager
import android.os.StrictMode
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import h.lillie.weartube.R
import org.json.JSONArray
import org.json.JSONObject

@SuppressLint("WakelockTimeout")
@Suppress("Deprecation")
class Main : AppCompatActivity() {
    private lateinit var wifiLock: WifiManager.WifiLock
    private lateinit var wakeLock: PowerManager.WakeLock

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        val policy = StrictMode.ThreadPolicy.Builder().permitNetwork().build()
        StrictMode.setThreadPolicy(policy)

        val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "WearTube:WifiLock")
        wifiLock.setReferenceCounted(false)
        wifiLock.acquire()

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WearTube:WakeLock")
        wakeLock.setReferenceCounted(false)
        wakeLock.acquire()

        val scrollView: ScrollView = findViewById(R.id.scrollView)
        scrollView.requestFocus()

        val trending: MaterialButton = findViewById(R.id.trending)
        trending.setOnClickListener {
            startActivity(Intent(this@Main, Trending::class.java))
        }

        val search: MaterialButton = findViewById(R.id.search)
        search.setOnClickListener {
            startActivity(Intent(this@Main, Search::class.java))
        }

        val settings: MaterialButton = findViewById(R.id.settings)
        settings.setOnClickListener {
            val playerRequest: String = Extractor().playerRequest(this@Main, "OuLZlZ18APQ")
            val playerObject = JSONObject(playerRequest)

            val nextRequest: String = Extractor().nextRequest(this@Main, "OuLZlZ18APQ")
            val nextObject = JSONObject(nextRequest)

            val artworkArray: JSONArray = playerObject.getJSONObject("videoDetails").getJSONObject("thumbnail").getJSONArray("thumbnails")
            val artworkUrl: String = artworkArray.getJSONObject((artworkArray.length() - 1)).optString("url")
            val title: String = nextObject.getJSONObject("contents").getJSONObject("singleColumnWatchNextResults").getJSONObject("results").getJSONObject("results").getJSONArray("contents").getJSONObject(1).getJSONObject("slimVideoMetadataSectionRenderer").getJSONArray("contents").getJSONObject(0).getJSONObject("elementRenderer").getJSONObject("newElement").getJSONObject("type").getJSONObject("componentType").getJSONObject("model").getJSONObject("videoMetadataModel").getJSONObject("videoMetadata").getJSONObject("title").optString("content")
            val author: String = playerObject.getJSONObject("videoDetails").optString("author")
            val videoUrl: String = playerObject.getJSONObject("streamingData").optString("hlsManifestUrl")

            Application.setVideoData(Gson().toJson(Data(
                videoUrl,
                artworkUrl,
                title,
                author
            )))
            startActivity(Intent(this@Main, Player::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (wifiLock.isHeld) {
            wifiLock.release()
        }
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }
}