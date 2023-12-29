package h.lillie.weartube.presentation

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerView
import h.lillie.weartube.R
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

@SuppressLint("UnsafeOptInUsageError")
class Player : AppCompatActivity() {
    private lateinit var exoPlayer: ExoPlayer
    private var playerSession: MediaSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.player)

        exoPlayer = ExoPlayer.Builder(this@Player)
            .setRenderersFactory(DefaultRenderersFactory(this@Player)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
                .forceEnableMediaCodecAsynchronousQueueing())
            .setSeekParameters(SeekParameters.EXACT)
            .setHandleAudioBecomingNoisy(true)
            .setSeekBackIncrementMs(TimeUnit.SECONDS.toMillis(10))
            .setSeekForwardIncrementMs(TimeUnit.SECONDS.toMillis(10))
            .build()

        val hlsSource: MediaSource = HlsMediaSource.Factory(OkHttpDataSource.Factory(OkHttpClient.Builder().build()))
            .createMediaSource(MediaItem.Builder()
                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .setMediaMetadata(MediaMetadata.Builder()
                    .setTitle(intent.getStringExtra("title"))
                    .setArtist(intent.getStringExtra("author"))
                    .setArtworkUri(Uri.parse(intent.getStringExtra("artwork")))
                    .build())
                .setUri(Uri.parse(intent.getStringExtra("url")))
                .build())

        exoPlayer.setMediaSource(hlsSource)

        playerSession = MediaSession.Builder(this@Player, exoPlayer).build()

        val playerView: PlayerView = findViewById(R.id.playerView)
        playerView.keepScreenOn = true
        playerView.player = exoPlayer

        exoPlayer.playWhenReady = true
        exoPlayer.prepare()
    }

    override fun onStop() {
        super.onStop()
        exoPlayer.pause()
    }

    override fun onResume() {
        super.onResume()
        exoPlayer.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerSession?.run {
            val playerView: PlayerView = findViewById(R.id.playerView)
            playerView.keepScreenOn = false
            playerView.player = null

            exoPlayer.pause()
            exoPlayer.stop()
            exoPlayer.release()

            release()
            playerSession = null
        }
    }
}