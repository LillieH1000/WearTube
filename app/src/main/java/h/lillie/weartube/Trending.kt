package h.lillie.weartube

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class Trending : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.trending)

        val scrollView: ScrollView = findViewById(R.id.scrollView)
        val scrollLayout: LinearLayout = findViewById(R.id.scrollLayout)
        val deviceWidth: Int = windowManager.currentWindowMetrics.bounds.width()

        scrollLayout.removeAllViews()
        scrollView.requestFocus()

        CoroutineScope(Dispatchers.Main).launch {
            val browseRequest: String = Extractor().browseRequest(this@Trending, "FEtrending")
            val browseContents: JSONArray = JSONObject(browseRequest).getJSONObject("contents").getJSONObject("singleColumnBrowseResultsRenderer").getJSONArray("tabs").getJSONObject(0).getJSONObject("tabRenderer").getJSONObject("content").getJSONObject("sectionListRenderer").getJSONArray("contents")

            for (i in 0 until browseContents.length()) {
                try {
                    val videoID: String = browseContents.getJSONObject(i).getJSONObject("itemSectionRenderer").getJSONArray("contents").getJSONObject(0).getJSONObject("videoWithContextRenderer").getJSONObject("navigationEndpoint").getJSONObject("watchEndpoint").optString("videoId")
                    val videoTitle: String = browseContents.getJSONObject(i).getJSONObject("itemSectionRenderer").getJSONArray("contents").getJSONObject(0).getJSONObject("videoWithContextRenderer").getJSONObject("headline").getJSONArray("runs").getJSONObject(0).optString("text")
                    val videoArtworkArray: JSONArray = browseContents.getJSONObject(i).getJSONObject("itemSectionRenderer").getJSONArray("contents").getJSONObject(0).getJSONObject("videoWithContextRenderer").getJSONObject("thumbnail").getJSONArray("thumbnails")
                    val videoArtworkUrl: String = videoArtworkArray.getJSONObject((videoArtworkArray.length() - 1)).optString("url")

                    val videoRelativeView = RelativeLayout(this@Trending)
                    videoRelativeView.layoutParams = RelativeLayout.LayoutParams(deviceWidth, 100)

                    val videoImageView = ImageView(this@Trending)
                    videoImageView.layoutParams = LinearLayout.LayoutParams(110, 100)
                    videoImageView.scaleType = ImageView.ScaleType.FIT_XY
                    videoImageView.load(videoArtworkUrl)

                    val videoTitleTextView = TextView(this@Trending)
                    videoTitleTextView.layoutParams = LinearLayout.LayoutParams(deviceWidth - 120, 100)
                    videoTitleTextView.x = 120f
                    videoTitleTextView.text = videoTitle
                    videoTitleTextView.gravity = Gravity.CENTER_VERTICAL

                    val videoButton = View(this@Trending)
                    videoButton.layoutParams = LinearLayout.LayoutParams(deviceWidth, 200)
                    videoButton.setOnClickListener {
                        CoroutineScope(Dispatchers.Main).launch {
                            val playerRequest: String = Extractor().playerRequest(this@Trending, videoID)
                            val playerObject = JSONObject(playerRequest)

                            val nextRequest: String = Extractor().nextRequest(this@Trending, videoID)
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
                            startActivity(Intent(this@Trending, Player::class.java))
                        }
                    }

                    videoRelativeView.addView(videoImageView)
                    videoRelativeView.addView(videoTitleTextView)
                    videoRelativeView.addView(videoButton)
                    scrollLayout.addView(videoRelativeView)

                    val spaceView = Space(this@Trending)
                    spaceView.minimumHeight = 4
                    scrollLayout.addView(spaceView)
                } catch (e: JSONException) {
                    Log.e("JSONException", e.toString())
                }
            }
        }
    }
}