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

/**
 * Next Episode (SDK/UI): modo API/default.
 *
 * - Reproduce un contenido EPISODE.
 * - Deja que el SDK configure "mediaInfo.next" y muestre el overlay automáticamente.
 */
class VideoNextEpisodeActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SDK-QA"

        // IDs tomados del ejemplo de integración. Ajusta si necesitas otros.
        private const val EPISODE_ID = "6839b2d6a4149963bfe295e0"
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

        setContentView(mainMediaFrame)
        setupPlayer(mainMediaFrame)
    }

    private fun setupPlayer(container: FrameLayout) {
        val config = MediastreamPlayerConfig().apply {
            id = EPISODE_ID
            type = MediastreamPlayerConfig.VideoTypes.EPISODE
            loadNextAutomatically = true
            showControls = true
            isDebug = true
            environment = MediastreamPlayerConfig.Environment.DEV
        }

        player = MediastreamPlayer(
            this,
            config,
            container,
            container,
            supportFragmentManager
        )

        player?.addPlayerCallback(createPlayerCallback())
    }

    private fun createPlayerCallback(): MediastreamPlayerCallback {
        return object : MediastreamPlayerCallback {
            override fun playerViewReady(msplayerView: PlayerView?) {
                Log.d(TAG, "playerViewReady")
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

            override fun onDismissButton() {
                Log.d(TAG, "onDismissButton")
            }

            override fun onPlayerClosed() {
                Log.d(TAG, "onPlayerClosed")
            }

            override fun onNext() {
                Log.d(TAG, "onNext")
            }

            override fun onPrevious() {}

            override fun nextEpisodeIncoming(nextEpisodeId: String) {
                // En modo API/default, el SDK mostrará el overlay automáticamente al llegar a appearTime.
                Log.d(TAG, "nextEpisodeIncoming: $nextEpisodeId")
            }

            override fun onFullscreen() {}
            override fun offFullscreen() {}
            override fun onNewSourceAdded(config: MediastreamPlayerConfig) {}
            override fun onLocalSourceAdded() {}

            override fun onPlayerReload() {
                Log.d(TAG, "onPlayerReload")
            }

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

            override fun onPlaybackErrors(error: JSONObject?) {
                Log.e(TAG, "onPlaybackErrors: $error")
            }

            override fun onEmbedErrors(error: JSONObject?) {
                Log.e(TAG, "onEmbedErrors: $error")
            }

            override fun onLiveAudioCurrentSongChanged(data: JSONObject?) {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.releasePlayer()
    }
}

