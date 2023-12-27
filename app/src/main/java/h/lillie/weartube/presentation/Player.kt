package h.lillie.weartube.presentation

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.ui.PlayerView
import h.lillie.weartube.R
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

@SuppressLint("UnsafeOptInUsageError")
class Player : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.player)

        val exoPlayer: ExoPlayer = ExoPlayer.Builder(this@Player)
            .setRenderersFactory(DefaultRenderersFactory(this@Player)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
                .forceEnableMediaCodecAsynchronousQueueing())
            .setSeekParameters(SeekParameters.EXACT)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setHandleAudioBecomingNoisy(true)
            .setSeekBackIncrementMs(TimeUnit.SECONDS.toMillis(10))
            .setSeekForwardIncrementMs(TimeUnit.SECONDS.toMillis(10))
            .build()

        val hlsSource: MediaSource = HlsMediaSource.Factory(OkHttpDataSource.Factory(OkHttpClient.Builder().build()))
            .createMediaSource(MediaItem.Builder()
                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .setUri(Uri.parse(intent.getStringExtra("url")))
                .build())

        exoPlayer.setMediaSource(hlsSource)
        exoPlayer.playWhenReady = true
        exoPlayer.prepare()

        val playerView: PlayerView = findViewById(R.id.playerView)
        playerView.keepScreenOn = true
        playerView.player = exoPlayer
    }
}