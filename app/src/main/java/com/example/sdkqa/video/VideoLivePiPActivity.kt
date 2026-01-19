package com.example.sdkqa.video

import am.mediastre.mediastreamplatformsdkandroid.MediastreamMiniPlayerConfig
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayer
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerCallback
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerConfig
import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.ui.PlayerView
import com.google.ads.interactivemedia.v3.api.AdError
import com.google.ads.interactivemedia.v3.api.AdEvent
import org.json.JSONObject

/**
 * Activity dedicada a validar Picture-in-Picture (PiP) en instrumentación.
 *
 * - Declárala en el manifest con android:supportsPictureInPicture="true"
 * - Entra a PiP usando el método del SDK (si existe) y/o el API nativo como fallback
 */
class VideoLivePiPActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SDK-QA-PiP"
    }

    // Mantener el nombre "player" para facilitar tests por reflection si se requiere
    private var player: MediastreamPlayer? = null
    private var lastPlayerView: PlayerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainMediaFrame = FrameLayout(this).apply {
            id = View.generateViewId()
            setBackgroundColor(Color.BLACK)
            keepScreenOn = true
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val playerContainer = FrameLayout(this).apply {
            id = View.generateViewId()
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        mainMediaFrame.addView(playerContainer)
        setContentView(mainMediaFrame)

        setupPlayer(mainMediaFrame)
    }

    /**
     * Método callable desde tests para forzar entrada a PiP de forma determinística.
     */
    fun enterPiPForTest() {
        triggerPiP()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        triggerPiP()
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        notifySdkPiPChanged(isInPictureInPictureMode)
    }

    private fun setupPlayer(mainMediaFrame: FrameLayout) {
        val config = MediastreamPlayerConfig().apply {
            id = "5fd39e065d68477eaa1ccf5a"
            type = MediastreamPlayerConfig.VideoTypes.LIVE
            // environment = MediastreamPlayerConfig.Environment.DEV
        }

        player = MediastreamPlayer(
            this,
            config,
            mainMediaFrame,
            mainMediaFrame,
            supportFragmentManager
        )

        player?.addPlayerCallback(createPlayerCallback())
    }

    private fun createPlayerCallback(): MediastreamPlayerCallback {
        return object : MediastreamPlayerCallback {
            override fun playerViewReady(msplayerView: PlayerView?) {
                Log.d(TAG, "playerViewReady")
                lastPlayerView = msplayerView
                try {
                    msplayerView?.player?.let { media3Player ->
                        if (!media3Player.isPlaying) {
                            media3Player.play()
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "No se pudo iniciar reproducción: ${e.message}")
                }
            }

            override fun onPlay() {
                Log.d(TAG, "onPlay")
            }

            override fun onPause() {
                Log.d(TAG, "onPause")
            }

            override fun onReady() {
                Log.d(TAG, "onReady")
            }

            override fun onEnd() {
                Log.d(TAG, "onEnd")
            }

            override fun onBuffering() {
                Log.d(TAG, "onBuffering")
            }

            override fun onError(error: String?) {
                Log.e(TAG, "onError: $error")
            }

            override fun onDismissButton() {}
            override fun onPlayerClosed() {}
            override fun onNext() {}
            override fun onPrevious() {}
            override fun onFullscreen() {}
            override fun offFullscreen() {}
            override fun onNewSourceAdded(config: MediastreamPlayerConfig) {}
            override fun onLocalSourceAdded() {}
            override fun onPlayerReload() {}

            override fun onAdEvents(type: AdEvent.AdEventType) {
                Log.d(TAG, "onAdEvents: ${type.name}")
            }

            override fun onAdErrorEvent(error: AdError) {
                Log.e(TAG, "onAdErrorEvent: ${error.message}")
            }

            override fun onConfigChange(config: MediastreamMiniPlayerConfig?) {}
            override fun onCastAvailable(state: Boolean?) {}
            override fun onCastSessionStarting() {}
            override fun onCastSessionStarted() {}
            override fun onCastSessionStartFailed() {}
            override fun onCastSessionEnding() {}
            override fun onCastSessionEnded() {}
            override fun onCastSessionResuming() {}
            override fun onCastSessionResumed() {}
            override fun onCastSessionResumeFailed() {}
            override fun onCastSessionSuspended() {}
            override fun onPlaybackErrors(error: JSONObject?) {}
            override fun onEmbedErrors(error: JSONObject?) {}
            override fun onLiveAudioCurrentSongChanged(data: JSONObject?) {}
        }
    }

    private fun triggerPiP() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val hasPiPFeature = packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        Log.d(TAG, "PiP feature soportado por el sistema: $hasPiPFeature")

        // 1) Preferir método del SDK si está disponible: startPiP()
        val sdkStartAttempted = try {
            val msPlayer = player ?: return
            val m = msPlayer.javaClass.getMethod("startPiP")
            m.invoke(msPlayer)
            Log.d(TAG, "startPiP() ejecutado vía SDK")
            true
        } catch (e: Exception) {
            Log.d(TAG, "startPiP() no disponible o falló: ${e.message}")
            false
        }

        // Importante: aunque el SDK tenga startPiP(), puede que no ejecute el API nativo en algunos builds.
        // Por eso intentamos también el fallback nativo y logueamos el resultado.
        try {
            val params = PictureInPictureParams.Builder().build()
            val nativeResult = enterPictureInPictureMode(params)
            Log.d(
                TAG,
                "enterPictureInPictureMode() vía API nativa -> result=$nativeResult, " +
                    "isInPiPAhora=$isInPictureInPictureMode, sdkStartAttempted=$sdkStartAttempted"
            )
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo entrar a PiP: ${e.message}")
        }
    }

    private fun notifySdkPiPChanged(isInPiP: Boolean) {
        // Llamar a mediastreamPlayer.onPictureInPictureModeChanged(boolean) si existe
        try {
            val msPlayer = player ?: return
            val m = msPlayer.javaClass.getMethod("onPictureInPictureModeChanged", Boolean::class.javaPrimitiveType)
            m.invoke(msPlayer, isInPiP)
            Log.d(TAG, "onPictureInPictureModeChanged($isInPiP) notificado al SDK")
        } catch (e: Exception) {
            Log.d(TAG, "SDK no expone onPictureInPictureModeChanged(boolean): ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            lastPlayerView = null
            player?.releasePlayer()
        } finally {
            player = null
        }
    }
}
