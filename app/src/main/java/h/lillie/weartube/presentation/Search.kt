package h.lillie.weartube.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import com.google.gson.Gson
import h.lillie.weartube.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class Search : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search)

        val scrollView: ScrollView = findViewById(R.id.scrollView)
        val scrollLayout: LinearLayout = findViewById(R.id.scrollLayout)
        val deviceWidth: Int = windowManager.currentWindowMetrics.bounds.width()

        val searchBar: EditText = findViewById(R.id.searchBar)
        searchBar.setOnKeyListener(View.OnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(searchBar.windowToken, 0)
                scrollLayout.removeAllViews()
                scrollView.requestFocus()

                val searchRequest: String = Extractor().searchRequest(this@Search, searchBar.text.toString())
                val searchContents: JSONArray = JSONObject(searchRequest).getJSONObject("contents").getJSONObject("twoColumnSearchResultsRenderer").getJSONObject("primaryContents").getJSONObject("sectionListRenderer").getJSONArray("contents").getJSONObject(0).getJSONObject("itemSectionRenderer").getJSONArray("contents")

                for (i in 0 until searchContents.length()) {
                    try {
                        val videoID: String = searchContents.getJSONObject(i).getJSONObject("videoRenderer").optString("videoId")
                        val videoTitle: String = searchContents.getJSONObject(i).getJSONObject("videoRenderer").getJSONObject("title").getJSONArray("runs").getJSONObject(0).optString("text")
                        val videoArtworkArray: JSONArray = searchContents.getJSONObject(i).getJSONObject("videoRenderer").getJSONObject("thumbnail").getJSONArray("thumbnails")
                        val videoArtworkUrl: String = videoArtworkArray.getJSONObject((videoArtworkArray.length() - 1)).optString("url")

                        val videoRelativeView = RelativeLayout(this@Search)
                        videoRelativeView.layoutParams = RelativeLayout.LayoutParams(deviceWidth, 100)

                        val videoImageView = ImageView(this@Search)
                        videoImageView.layoutParams = LinearLayout.LayoutParams(110, 100)
                        videoImageView.scaleType = ImageView.ScaleType.FIT_XY
                        videoImageView.load(videoArtworkUrl)

                        val videoTitleTextView = TextView(this@Search)
                        videoTitleTextView.layoutParams = LinearLayout.LayoutParams(deviceWidth - 120, 100)
                        videoTitleTextView.x = 120f
                        videoTitleTextView.text = videoTitle
                        videoTitleTextView.gravity = Gravity.CENTER_VERTICAL

                        val videoButton = View(this@Search)
                        videoButton.layoutParams = LinearLayout.LayoutParams(deviceWidth, 200)
                        videoButton.setOnClickListener {
                            val playerRequest: String = Extractor().playerRequest(this@Search, videoID)
                            val playerObject = JSONObject(playerRequest)

                            val nextRequest: String = Extractor().nextRequest(this@Search, videoID)
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
                            startActivity(Intent(this@Search, Player::class.java))
                        }

                        videoRelativeView.addView(videoImageView)
                        videoRelativeView.addView(videoTitleTextView)
                        videoRelativeView.addView(videoButton)
                        scrollLayout.addView(videoRelativeView)

                        val spaceView = Space(this@Search)
                        spaceView.minimumHeight = 4
                        scrollLayout.addView(spaceView)
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