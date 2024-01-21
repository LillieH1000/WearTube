package h.lillie.weartube.presentation

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import h.lillie.weartube.R

class Player : AppCompatActivity(), Player.Listener {
    private lateinit var playerControllerFuture: ListenableFuture<MediaController>
    private lateinit var playerController: MediaController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.player)

        val playPauseButton: ImageButton = findViewById(R.id.playPauseButton)
        playPauseButton.setOnClickListener {
            if (!playerController.isPlaying) {
                playerController.play()
            } else {
                playerController.pause()
            }
        }

        val seekBackButton: ImageButton = findViewById(R.id.seekBackButton)
        seekBackButton.x = Converter().dpToPx(this@Player, -40f)
        seekBackButton.setOnClickListener {
            playerController.seekBack()
        }

        val seekForwardButton: ImageButton = findViewById(R.id.seekForwardButton)
        seekForwardButton.x = Converter().dpToPx(this@Player, 40f)
        seekForwardButton.setOnClickListener {
            playerController.seekForward()
        }

        val overlayView: View = findViewById(R.id.overlayView)
        overlayView.setOnClickListener {
            if (playPauseButton.visibility == View.GONE) {
                overlayView.setBackgroundColor(0x40000000)
                playPauseButton.visibility = View.VISIBLE
                seekBackButton.visibility = View.VISIBLE
                seekForwardButton.visibility = View.VISIBLE
            } else {
                overlayView.setBackgroundColor(0x00000000)
                playPauseButton.visibility = View.GONE
                seekBackButton.visibility = View.GONE
                seekForwardButton.visibility = View.GONE
            }
        }

        val sessionToken = SessionToken(this@Player, ComponentName(this@Player, PlayerService::class.java))
        playerControllerFuture = MediaController.Builder(this@Player, sessionToken).buildAsync()
        playerControllerFuture.addListener({
            playerController = playerControllerFuture.get()
            playerController.addListener(this@Player)

            val playerView: PlayerView = findViewById(R.id.playerView)
            playerView.keepScreenOn = true
            playerView.player = playerController
        }, MoreExecutors.directExecutor())
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        val playPauseButton: ImageButton = findViewById(R.id.playPauseButton)
        if (!isPlaying) {
            playPauseButton.setImageResource(R.drawable.play)
        } else {
            playPauseButton.setImageResource(R.drawable.pause)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        val playerView: PlayerView = findViewById(R.id.playerView)
        playerView.keepScreenOn = false
        playerView.player = null

        playerController.pause()
        playerController.stop()
        playerController.release()

        MediaController.releaseFuture(playerControllerFuture)
        stopService(Intent(this@Player, PlayerService::class.java))
    }
}