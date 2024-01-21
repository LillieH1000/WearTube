package h.lillie.weartube.presentation

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import com.google.android.material.slider.Slider
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import h.lillie.weartube.R

class Player : AppCompatActivity(), Player.Listener {
    private var playerSliderHandler: Handler = Handler(Looper.getMainLooper())

    private lateinit var playerControllerFuture: ListenableFuture<MediaController>
    private lateinit var playerController: MediaController
    private lateinit var playerSlider: Slider

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

        playerSlider = findViewById(R.id.playerSlider)
        playerSlider.y = Converter().dpToPx(this@Player, 40f)
        playerSlider.addOnChangeListener { _, value, fromUser ->
            val duration: Float = playerController.duration.toFloat()
            val position: Float = playerController.currentPosition.toFloat()
            if (fromUser && duration >= 0 && position >= 0) {
                playerController.seekTo(value.toLong())
            }
        }

        val overlayView: View = findViewById(R.id.overlayView)
        overlayView.setOnClickListener {
            if (playPauseButton.visibility == View.GONE) {
                overlayView.setBackgroundColor(0x40000000)
                playPauseButton.visibility = View.VISIBLE
                seekBackButton.visibility = View.VISIBLE
                seekForwardButton.visibility = View.VISIBLE
                playerSlider.visibility = View.VISIBLE
            } else {
                overlayView.setBackgroundColor(0x00000000)
                playPauseButton.visibility = View.GONE
                seekBackButton.visibility = View.GONE
                seekForwardButton.visibility = View.GONE
                playerSlider.visibility = View.GONE
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

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        playerSliderHandler.post(playerSliderTask)
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

        playerSliderHandler.removeCallbacks(playerSliderTask)
        playerSliderHandler.removeCallbacksAndMessages(null)

        playerController.pause()
        playerController.stop()
        playerController.release()

        MediaController.releaseFuture(playerControllerFuture)
        stopService(Intent(this@Player, PlayerService::class.java))
    }

    private val playerSliderTask = object : Runnable {
        override fun run() {
            val duration: Float = playerController.duration.toFloat()
            val position: Float = playerController.currentPosition.toFloat()
            if (duration >= 0 && position >= 0 && position <= duration) {
                playerSlider.valueTo = duration
                playerSlider.value = position
            }
            playerSliderHandler.postDelayed(this, 1000)
        }
    }
}