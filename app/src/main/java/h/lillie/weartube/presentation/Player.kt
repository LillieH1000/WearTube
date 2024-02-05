package h.lillie.weartube.presentation

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
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

@SuppressLint("PrivateResource")
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

        val loopButton: ImageButton = findViewById(R.id.loopButton)
        loopButton.x = Converter().dpToPx(this@Player, -20f)
        loopButton.y = Converter().dpToPx(this@Player, -50f)
        loopButton.setOnClickListener {
            if (playerController.repeatMode == Player.REPEAT_MODE_OFF) {
                playerController.repeatMode = Player.REPEAT_MODE_ONE
            } else {
                playerController.repeatMode = Player.REPEAT_MODE_OFF
            }
        }

        val bluetoothButton: ImageButton = findViewById(R.id.bluetoothButton)
        bluetoothButton.x = Converter().dpToPx(this@Player, 20f)
        bluetoothButton.y = Converter().dpToPx(this@Player, -50f)
        bluetoothButton.setOnClickListener {
            playerController.pause()
            val intent: Intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.putExtra("EXTRA_CONNECTION_ONLY", true)
            intent.putExtra("EXTRA_CLOSE_ON_CONNECT", true)
            intent.putExtra("android.bluetooth.devicepicker.extra.FILTER_TYPE", 1)
            startActivity(intent)
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
                loopButton.visibility = View.VISIBLE
                bluetoothButton.visibility = View.VISIBLE
            } else {
                overlayView.setBackgroundColor(0x00000000)
                playPauseButton.visibility = View.GONE
                seekBackButton.visibility = View.GONE
                seekForwardButton.visibility = View.GONE
                playerSlider.visibility = View.GONE
                loopButton.visibility = View.GONE
                bluetoothButton.visibility = View.GONE
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
            playPauseButton.setImageResource(androidx.media3.ui.R.drawable.exo_icon_play)
        } else {
            playPauseButton.setImageResource(androidx.media3.ui.R.drawable.exo_icon_pause)
        }
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        super.onRepeatModeChanged(repeatMode)
        val loopButton: ImageButton = findViewById(R.id.loopButton)
        if (repeatMode == Player.REPEAT_MODE_OFF) {
            loopButton.setImageResource(androidx.media3.ui.R.drawable.exo_icon_repeat_all)
        } else {
            loopButton.setImageResource(androidx.media3.ui.R.drawable.exo_icon_repeat_one)
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