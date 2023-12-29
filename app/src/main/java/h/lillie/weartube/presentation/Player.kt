package h.lillie.weartube.presentation

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import h.lillie.weartube.R

class Player : AppCompatActivity() {
    private lateinit var playerControllerFuture: ListenableFuture<MediaController>
    private lateinit var playerController: MediaController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.player)

        val sessionToken = SessionToken(this@Player, ComponentName(this@Player, PlayerService::class.java))
        playerControllerFuture = MediaController.Builder(this@Player, sessionToken).buildAsync()
        playerControllerFuture.addListener({
            playerController = playerControllerFuture.get()

            val playerView: PlayerView = findViewById(R.id.playerView)
            playerView.keepScreenOn = true
            playerView.player = playerController
        }, MoreExecutors.directExecutor())
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