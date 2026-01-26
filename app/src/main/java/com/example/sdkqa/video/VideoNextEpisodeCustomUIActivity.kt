package com.example.sdkqa.video

import am.mediastre.mediastreamplatformsdkandroid.MediastreamMiniPlayerConfig
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayer
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerCallback
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerConfig
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.ui.PlayerView
import com.example.sdkqa.R
import com.google.ads.interactivemedia.v3.api.AdError
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.android.material.button.MaterialButton
import org.json.JSONObject

/**
 * Next Episode (Manual/Custom):
 *
 * - Modo manual: setea msConfig.nextEpisodeId (y opcional nextEpisodeTime).
 * - El SDK emite nextEpisodeIncoming(...) en callbackTime, pero NO muestra overlay
 *   hasta que la app confirme llamando updateNextEpisode(config).
 *
 * Esta Activity además permite alternar entre:
 * - DEFAULT (API/auto overlay)
 * - CUSTOM (manual/confirmado por la app)
 */
class VideoNextEpisodeCustomUIActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SDK-QA"

        private const val MODE_DEFAULT = "NEXT_EPISODE"
        private const val MODE_CUSTOM = "NEXT_EPISODE_CUSTOM"

        // IDs tomados del ejemplo de integración. Ajusta si necesitas otros.
        private const val DEFAULT_EPISODE_ID = "6839b2d6a4149963bfe295e0"
        private const val CUSTOM_START_ID = "69400673158f35714666be04"

        private val NEXT_EPISODE_IDS = listOf(
            "689e339960f9be00168c3c16",
            "68fa58deb2254649c8f393bf"
        )
    }

    private var player: MediastreamPlayer? = null
    private lateinit var mainMediaFrame: FrameLayout

    private lateinit var btnDefault: MaterialButton
    private lateinit var btnCustom: MaterialButton
    private lateinit var tvStatus: TextView

    private var currentMode: String = MODE_DEFAULT
    private var currentEpisodeIndex = 0

    // Single callback instance to avoid duplicate registrations
    private val playerCallback by lazy { createPlayerCallback() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_next_episode_custom_ui)

        bindViews()
        setupButtons()
        initializePlayer()
    }

    private fun bindViews() {
        mainMediaFrame = findViewById(R.id.main_media_frame)
        btnDefault = findViewById(R.id.btnNextEpisodeDefault)
        btnCustom = findViewById(R.id.btnNextEpisodeCustom)
        tvStatus = findViewById(R.id.tvNextEpisodeStatus)
    }

    private fun setupButtons() {
        btnDefault.setOnClickListener { switchToMode(MODE_DEFAULT) }
        btnCustom.setOnClickListener { switchToMode(MODE_CUSTOM) }
        updateUiForMode(MODE_DEFAULT)
    }

    private fun initializePlayer() {
        val config = createConfigForMode(MODE_DEFAULT)
        player = MediastreamPlayer(this, config, mainMediaFrame, mainMediaFrame, supportFragmentManager)
        player?.addPlayerCallback(playerCallback)
        tvStatus.text = "Modo: DEFAULT (API)"
    }

    private fun switchToMode(mode: String) {
        if (currentMode == mode) return

        currentMode = mode
        updateUiForMode(mode)

        if (mode == MODE_CUSTOM) {
            currentEpisodeIndex = 0
        }

        val newConfig = createConfigForMode(mode)
        player?.reloadPlayer(newConfig)
        tvStatus.text = if (mode == MODE_DEFAULT) "Modo: DEFAULT (API)" else "Modo: CUSTOM (Manual)"
    }

    private fun updateUiForMode(mode: String) {
        val selectedColor = getColor(R.color.accent_video)
        val unselectedColor = getColor(R.color.card_background)
        val selectedText = getColor(R.color.black)
        val unselectedText = getColor(R.color.text_primary)

        val isDefault = mode == MODE_DEFAULT

        btnDefault.setBackgroundColor(if (isDefault) selectedColor else unselectedColor)
        btnDefault.setTextColor(if (isDefault) selectedText else unselectedText)

        btnCustom.setBackgroundColor(if (!isDefault) selectedColor else unselectedColor)
        btnCustom.setTextColor(if (!isDefault) selectedText else unselectedText)
    }

    private fun createConfigForMode(mode: String): MediastreamPlayerConfig {
        return when (mode) {
            MODE_DEFAULT -> MediastreamPlayerConfig().apply {
                id = DEFAULT_EPISODE_ID
                environment = MediastreamPlayerConfig.Environment.DEV
                type = MediastreamPlayerConfig.VideoTypes.EPISODE
                loadNextAutomatically = true
                showControls = true
                isDebug = true
            }

            MODE_CUSTOM -> MediastreamPlayerConfig().apply {
                // “Contenido actual” (VOD) con siguiente episodio manual
                id = CUSTOM_START_ID
                environment = MediastreamPlayerConfig.Environment.DEV
                type = MediastreamPlayerConfig.VideoTypes.VOD
                showControls = true
                isDebug = true

                // Activar modo manual en SDK: requiere confirmación vía updateNextEpisode(...)
                nextEpisodeId = NEXT_EPISODE_IDS.firstOrNull()
                // nextEpisodeTime = 15 // opcional (segundos)
            }

            else -> MediastreamPlayerConfig()
        }
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

            override fun onPlayerClosed() {
                Log.d(TAG, "onPlayerClosed")
            }

            override fun onBuffering() {}

            override fun onError(error: String?) {
                Log.e(TAG, "onError: $error")
            }

            override fun onNext() {
                Log.d(TAG, "onNext")
            }

            override fun onPrevious() {}

            override fun nextEpisodeIncoming(nextEpisodeId: String) {
                Log.d(TAG, "nextEpisodeIncoming: $nextEpisodeId (mode=$currentMode)")
                runOnUiThread {
                    tvStatus.text = "nextEpisodeIncoming: $nextEpisodeId"
                }

                if (currentMode != MODE_CUSTOM) return

                // Confirmación manual: construir nextConfig y llamar updateNextEpisode(...)
                if (currentEpisodeIndex >= NEXT_EPISODE_IDS.size) return

                val nextConfig = MediastreamPlayerConfig().apply {
                    id = NEXT_EPISODE_IDS[currentEpisodeIndex]
                    type = MediastreamPlayerConfig.VideoTypes.VOD
                    environment = MediastreamPlayerConfig.Environment.DEV
                    showControls = true
                    isDebug = true

                    val nextIndex = currentEpisodeIndex + 1
                    if (nextIndex < NEXT_EPISODE_IDS.size) {
                        this.nextEpisodeId = NEXT_EPISODE_IDS[nextIndex]
                    }
                }

                currentEpisodeIndex++
                player?.updateNextEpisode(nextConfig)
            }

            override fun onFullscreen() {}
            override fun offFullscreen() {}
            override fun onNewSourceAdded(config: MediastreamPlayerConfig) {}
            override fun onLocalSourceAdded() {}

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
            override fun onDismissButton() {}

            override fun onPlayerReload() {
                Log.d(TAG, "onPlayerReload")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.releasePlayer()
    }
}

