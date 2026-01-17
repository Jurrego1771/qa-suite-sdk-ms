package com.example.sdkqa.video

import am.mediastre.mediastreamplatformsdkandroid.MediastreamMiniPlayerConfig
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayer
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerCallback
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerConfig
import android.graphics.Color
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
import java.util.HashMap

class VideoLiveDrmActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SDK-QA"
    }

    private var player: MediastreamPlayer? = null

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

    private fun setupPlayer(mainMediaFrame: FrameLayout) {
        // Token de acceso DRM (JWT)
        val DRM_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ2ZXJzaW9uIjoxLCJjb21fa2V5X2lkIjoiMjBmM2FhZTctNjA4Yy00YTQyLWI4MzAtYjMzMjAxNWI1ZTY1IiwibWVzc2FnZSI6eyJ0eXBlIjoiZW50aXRsZW1lbnRfbWVzc2FnZSIsInZlcnNpb24iOjEsImV4cGlyYXRpb25fZGF0ZSI6IjIwMjYtMDEtMTdUMTI6MjI6MTMuODEzWiIsImtleXMiOlt7ImlkIjoiNkQ5RTE2MTEtMkUzQi00RDM5LTkzQUMtN0JCQjg3MUQyMjRDIn1dfX0.X9i33ExZLtDjiyy3yvBVceGpNUKiC92Ut9fNtVinBPA"
        
        // Headers DRM como Map<String, String>
        val drmHeaders = HashMap<String, String>().apply {
            put("X-AxDRM-Message", DRM_ACCESS_TOKEN)
        }
        
        val config = MediastreamPlayerConfig().apply {
            id = "5eed2426b08cec61b6f3ab86" // Reemplazar con tu ID de contenido Live con DRM
            type = MediastreamPlayerConfig.VideoTypes.LIVE
            
            // Configuración DRM
            drmData = MediastreamPlayerConfig.DrmData(
                "https://d231f6fd.drm-widevine-licensing.axprod.net/AcquireLicense",
                drmHeaders
            )
            
            // Formato DASH (requerido para Widevine DRM)
            videoFormat = MediastreamPlayerConfig.AudioVideoFormat.DASH
            
            showControls = true
            
            //Uncomment to use development environment
            environment = MediastreamPlayerConfig.Environment.DEV
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
                Log.d(TAG, "playerViewReady - DRM Live")
            }

            override fun onPlay() {
                Log.d(TAG, "onPlay - DRM Live")
            }

            override fun onPause() {
                Log.d(TAG, "onPause - DRM Live")
            }

            override fun onReady() {
                Log.d(TAG, "onReady - DRM Live")
            }

            override fun onEnd() {
                Log.d(TAG, "onEnd - DRM Live")
            }

            override fun onBuffering() {
                Log.d(TAG, "onBuffering - DRM Live")
            }

            override fun onError(error: String?) {
                Log.e(TAG, "onError - DRM Live: $error")
            }

            override fun onDismissButton() {
                Log.d(TAG, "onDismissButton - DRM Live")
            }

            override fun onPlayerClosed() {
                Log.d(TAG, "onPlayerClosed - DRM Live")
            }

            override fun onNext() {}
            override fun onPrevious() {}
            override fun onFullscreen() {
                Log.d(TAG, "onFullscreen - DRM Live")
            }

            override fun offFullscreen() {
                Log.d(TAG, "offFullscreen - DRM Live")
            }

            override fun onNewSourceAdded(config: MediastreamPlayerConfig) {
                Log.d(TAG, "onNewSourceAdded - DRM Live")
            }
            override fun onLocalSourceAdded() {}
            
            override fun onPlayerReload() {
                Log.d(TAG, "onPlayerReload - DRM Live")
            }

            override fun onAdEvents(type: AdEvent.AdEventType) {
                Log.d(TAG, "onAdEvents - DRM Live: ${type.name}")
            }

            override fun onAdErrorEvent(error: AdError) {
                Log.e(TAG, "onAdErrorEvent - DRM Live: ${error.message}")
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
            override fun onPlaybackErrors(error: JSONObject?) {
                Log.e(TAG, "onPlaybackErrors - DRM Live: $error")
            }

            override fun onEmbedErrors(error: JSONObject?) {
                Log.e(TAG, "onEmbedErrors - DRM Live: $error")
            }

            override fun onLiveAudioCurrentSongChanged(data: JSONObject?) {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.releasePlayer()
    }
}
