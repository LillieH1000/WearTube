package h.lillie.weartube.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.Space
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import coil.load
import h.lillie.weartube.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class Main : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        val policy = StrictMode.ThreadPolicy.Builder().permitNetwork().build()
        StrictMode.setThreadPolicy(policy)

        val searchScrollView: ScrollView = findViewById(R.id.searchScrollView)
        val searchScrollLayout: LinearLayout = findViewById(R.id.searchScrollLayout)
        val deviceWidth: Int = windowManager.currentWindowMetrics.bounds.width()

        val searchBar: EditText = findViewById(R.id.searchBar)
        searchBar.setOnKeyListener(View.OnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(searchBar.windowToken, 0)
                searchScrollLayout.removeAllViews()
                searchScrollView.requestFocus()

                val searchRequest: String = Extractor().searchRequest(this@Main, searchBar.text.toString())
                val searchContents: JSONArray = JSONObject(searchRequest).getJSONObject("contents").getJSONObject("twoColumnSearchResultsRenderer").getJSONObject("primaryContents").getJSONObject("sectionListRenderer").getJSONArray("contents").getJSONObject(0).getJSONObject("itemSectionRenderer").getJSONArray("contents")

                for (i in 0 until searchContents.length()) {
                    try {
                        val videoID: String = searchContents.getJSONObject(i).getJSONObject("videoRenderer").optString("videoId")
                        val videoTitle: String = searchContents.getJSONObject(i).getJSONObject("videoRenderer").getJSONObject("title").getJSONArray("runs").getJSONObject(0).optString("text")
                        val videoArtworkArray: JSONArray = searchContents.getJSONObject(i).getJSONObject("videoRenderer").getJSONObject("thumbnail").getJSONArray("thumbnails")
                        val videoArtworkUrl: String = videoArtworkArray.getJSONObject((videoArtworkArray.length() - 1)).optString("url")

                        val videoRelativeView = RelativeLayout(this@Main)
                        videoRelativeView.layoutParams = RelativeLayout.LayoutParams(deviceWidth, 100)

                        val videoImageView = ImageView(this@Main)
                        videoImageView.layoutParams = LinearLayout.LayoutParams(110, 100)
                        videoImageView.scaleType = ImageView.ScaleType.FIT_XY
                        videoImageView.load(videoArtworkUrl)

                        val videoTitleTextView = TextView(this@Main)
                        videoTitleTextView.layoutParams = LinearLayout.LayoutParams(deviceWidth - 120, 100)
                        videoTitleTextView.x = 120f
                        videoTitleTextView.text = videoTitle
                        videoTitleTextView.gravity = Gravity.CENTER_VERTICAL

                        val videoButton = View(this@Main)
                        videoButton.layoutParams = LinearLayout.LayoutParams(deviceWidth, 200)
                        videoButton.setOnClickListener {
                            val playerRequest: String = Extractor().playerRequest(this@Main, videoID)
                            val intent = Intent(this@Main, Player::class.java)
                            intent.putExtra("url", JSONObject(playerRequest).getJSONObject("streamingData").optString("hlsManifestUrl"))
                            startActivity(intent)
                        }

                        videoRelativeView.addView(videoImageView)
                        videoRelativeView.addView(videoTitleTextView)
                        videoRelativeView.addView(videoButton)
                        searchScrollLayout.addView(videoRelativeView)

                        val spaceView = Space(this@Main)
                        spaceView.minimumHeight = 4
                        searchScrollLayout.addView(spaceView)
                    } catch (e: JSONException) {
                        Log.e("JSONException", e.toString())
                    }
                }
                return@OnKeyListener true
            }
            return@OnKeyListener false
        })
    }
}