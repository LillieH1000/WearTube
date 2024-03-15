package h.lillie.weartube

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
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
class PlayerService : MediaSessionService(), Player.Listener, MediaSession.Callback {
    private lateinit var videoData: Data
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var forwardingExoPlayer: ForwardingPlayer
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

        exoPlayer.addListener(this@PlayerService)

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

        forwardingExoPlayer = object : ForwardingPlayer(exoPlayer) {
            override fun isCommandAvailable(command: Int): Boolean {
                super.isCommandAvailable(command)
                if (command == Player.COMMAND_SEEK_TO_PREVIOUS || command == Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM || command == Player.COMMAND_SEEK_TO_NEXT || command == Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM) {
                    return false
                }
                return super.isCommandAvailable(command)
            }

            override fun getAvailableCommands(): Player.Commands {
                return super.getAvailableCommands()
                    .buildUpon()
                    .remove(COMMAND_SEEK_TO_PREVIOUS)
                    .remove(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                    .remove(COMMAND_SEEK_TO_NEXT)
                    .remove(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                    .build()
            }
        }

        playerSession = MediaSession.Builder(this@PlayerService, forwardingExoPlayer)
            .setCallback(this@PlayerService)
            .build()

        exoPlayer.playWhenReady = true
        exoPlayer.prepare()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return playerSession
    }

    override fun onConnect(session: MediaSession, controller: MediaSession.ControllerInfo): MediaSession.ConnectionResult {
        val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS
        val playerCommands = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS
            .buildUpon()
            .remove(Player.COMMAND_SEEK_TO_PREVIOUS)
            .remove(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
            .remove(Player.COMMAND_SEEK_TO_NEXT)
            .remove(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
            .build()

        playerSession?.setAvailableCommands(controller, sessionCommands, playerCommands)
        MediaSession.ConnectionResult.accept(sessionCommands, playerCommands)
        return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
            .setAvailableSessionCommands(sessionCommands)
            .setAvailablePlayerCommands(playerCommands)
            .build()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.pause()
        forwardingExoPlayer.pause()
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerSession?.run {
            exoPlayer.pause()
            forwardingExoPlayer.pause()
            exoPlayer.stop()
            forwardingExoPlayer.stop()
            exoPlayer.release()
            forwardingExoPlayer.release()

            release()
            playerSession = null

            Application.setVideoData("")
        }
    }
}