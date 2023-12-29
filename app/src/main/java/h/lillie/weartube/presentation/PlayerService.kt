package h.lillie.weartube.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
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
import androidx.media3.session.MediaSessionService
import com.google.gson.Gson
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

@SuppressLint("UnsafeOptInUsageError")
class PlayerService : MediaSessionService() {
    private lateinit var videoData: Data
    private lateinit var exoPlayer: ExoPlayer
    private var playerSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        if (Application.getVideoData() != "") {
            videoData = Gson().fromJson(Application.getVideoData(), Data::class.java)
        }

        exoPlayer = ExoPlayer.Builder(this@PlayerService)
            .setRenderersFactory(DefaultRenderersFactory(this@PlayerService)
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
                    .setTitle(videoData.title)
                    .setArtist(videoData.author)
                    .setArtworkUri(Uri.parse(videoData.artworkURL))
                    .build())
                .setUri(Uri.parse(videoData.videoURL))
                .build())

        exoPlayer.setMediaSource(hlsSource)

        playerSession = MediaSession.Builder(this@PlayerService, exoPlayer).build()

        exoPlayer.playWhenReady = true
        exoPlayer.prepare()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return playerSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.pause()
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerSession?.run {
            exoPlayer.pause()
            exoPlayer.stop()
            exoPlayer.release()

            release()
            playerSession = null

            Application.setVideoData("")
        }
    }
}