package h.lillie.weartube

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.PowerManager
import android.os.StrictMode
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import h.lillie.weartube.R

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
            Toast.makeText(this@Main, "In Development", Toast.LENGTH_SHORT).show()
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