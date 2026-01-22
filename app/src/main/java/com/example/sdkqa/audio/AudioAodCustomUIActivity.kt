package com.example.sdkqa.audio

import am.mediastre.mediastreamplatformsdkandroid.MediastreamMiniPlayerConfig
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayer
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerCallback
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerConfig
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.C
import androidx.media3.ui.PlayerView
import com.example.sdkqa.R
import com.google.ads.interactivemedia.v3.api.AdError
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.android.material.button.MaterialButton
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AudioAodCustomUIActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SDK-QA"
        private const val SEEK_STEP_MS = 15_000L

        // Ajusta estos IDs luego (dejé uno real y uno placeholder)
        private const val CONTENT_ID_A = "696c625d32ce0ef08ca5ef9d"
        private const val CONTENT_ID_B = "67ae0ec86dcc4a0dca2e9b00"
    }

    private var player: MediastreamPlayer? = null
    private var msPlayerView: PlayerView? = null

    private lateinit var tvContentId: TextView
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvDuration: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var btnBackward: MaterialButton
    private lateinit var btnPlayPause: MaterialButton
    private lateinit var btnForward: MaterialButton
    private lateinit var btnSpeed: MaterialButton
    private lateinit var btnRelease: MaterialButton
    private lateinit var rgReloadStations: RadioGroup

    private var isPlaying = false
    private var isUserSeeking = false

    private val speeds = floatArrayOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
    private var speedIndex = 1 // 1.0x

    private val uiHandler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateProgressFromPlayer()
            uiHandler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_aod_custom_ui)

        bindViews()
        updateContentIdLabel(CONTENT_ID_A, "A")
        rgReloadStations.check(R.id.rbStationA)
        setupControls()
        setupPlayer(CONTENT_ID_A)
    }

    private fun bindViews() {
        tvContentId = findViewById(R.id.tvContentId)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvDuration = findViewById(R.id.tvDuration)
        seekBar = findViewById(R.id.seekBar)
        btnBackward = findViewById(R.id.btnBackward)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnForward = findViewById(R.id.btnForward)
        btnSpeed = findViewById(R.id.btnSpeed)
        btnRelease = findViewById(R.id.btnRelease)
        rgReloadStations = findViewById(R.id.rgReloadStations)
    }

    private fun setupControls() {
        btnBackward.setOnClickListener {
            Log.d(TAG, "USER ACTION: backward(${SEEK_STEP_MS}ms)")
            player?.backward(SEEK_STEP_MS)
        }

        btnForward.setOnClickListener {
            Log.d(TAG, "USER ACTION: forward(${SEEK_STEP_MS}ms)")
            player?.forward(SEEK_STEP_MS)
        }

        btnPlayPause.setOnClickListener {
            if (isPlaying) {
                Log.d(TAG, "USER ACTION: pause()")
                player?.pause()
            } else {
                Log.d(TAG, "USER ACTION: play()")
                player?.play()
            }
        }

        btnSpeed.text = "${speeds[speedIndex]}x"
        btnSpeed.setOnClickListener {
            speedIndex = (speedIndex + 1) % speeds.size
            val speed = speeds[speedIndex]
            btnSpeed.text = "${speed}x"
            Log.d(TAG, "USER ACTION: changeSpeed($speed)")
            player?.changeSpeed(speed)
        }

        btnRelease.setOnClickListener {
            Log.d(TAG, "USER ACTION: releasePlayer() + finish()")
            player?.releasePlayer()
            finish()
        }

        seekBar.isEnabled = false
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    tvCurrentTime.text = formatTime(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
                Log.d(TAG, "USER ACTION: start seeking")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val target = seekBar?.progress?.toLong() ?: return
                Log.d(TAG, "USER ACTION: seekTo($target)")
                player?.seekTo(target)
                isUserSeeking = false
            }
        })

        // Reload “como radio”: al cambiar la opción, recarga con otro contentId
        rgReloadStations.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbStationA -> {
                    updateContentIdLabel(CONTENT_ID_A, "A")
                    Log.d(TAG, "USER ACTION: select station A")
                    reloadPlayerWithContentId(CONTENT_ID_A)
                }
                R.id.rbStationB -> {
                    updateContentIdLabel(CONTENT_ID_B, "B")
                    Log.d(TAG, "USER ACTION: select station B")
                    reloadPlayerWithContentId(CONTENT_ID_B)
                }
            }
        }
    }

    private fun setupPlayer(contentId: String) {
        val mainMediaFrame = findViewById<android.widget.FrameLayout>(R.id.main_media_frame)

        val config = createAodConfig(contentId)
        player = MediastreamPlayer(
            this,
            config,
            mainMediaFrame,
            mainMediaFrame,
            supportFragmentManager
        )
        player?.addPlayerCallback(createPlayerCallback())
    }

    private fun reloadPlayerWithContentId(contentId: String) {
        val config = createAodConfig(contentId)
        Log.d(TAG, "Reload player with contentId=$contentId")
        player?.reloadPlayer(config)
    }

    private fun createAodConfig(contentId: String): MediastreamPlayerConfig {
        return MediastreamPlayerConfig().apply {
            id = contentId
            type = MediastreamPlayerConfig.VideoTypes.VOD
            playerType = MediastreamPlayerConfig.PlayerType.AUDIO
            videoFormat = MediastreamPlayerConfig.AudioVideoFormat.MP3
            showControls = false // UI tipo Spotify (custom)
            // environment = MediastreamPlayerConfig.Environment.DEV
        }
    }

    private fun createPlayerCallback(): MediastreamPlayerCallback {
        return object : MediastreamPlayerCallback {
            override fun playerViewReady(msplayerView: PlayerView?) {
                Log.d(TAG, "playerViewReady")
                msPlayerView = msplayerView
                startProgressUpdates()
            }

            override fun onPlay() {
                Log.d(TAG, "onPlay")
                isPlaying = true
                btnPlayPause.setIconResource(R.drawable.ic_pause)
            }

            override fun onPause() {
                Log.d(TAG, "onPause")
                isPlaying = false
                btnPlayPause.setIconResource(R.drawable.ic_play_arrow)
            }

            override fun onReady() {
                Log.d(TAG, "onReady")
                startProgressUpdates()
            }

            override fun onEnd() {
                Log.d(TAG, "onEnd")
                isPlaying = false
                btnPlayPause.setIconResource(R.drawable.ic_play_arrow)
            }

            override fun onBuffering() {
                Log.d(TAG, "onBuffering")
            }

            override fun onError(error: String?) {
                Log.e(TAG, "onError: $error")
                isPlaying = false
                btnPlayPause.setIconResource(R.drawable.ic_play_arrow)
            }

            override fun onDismissButton() {
                Log.d(TAG, "onDismissButton")
            }

            override fun onPlayerClosed() {
                Log.d(TAG, "onPlayerClosed")
            }
            override fun onNext() {}
            override fun onPrevious() {}
            override fun onFullscreen() {}
            override fun offFullscreen() {}
            override fun onNewSourceAdded(config: MediastreamPlayerConfig) {}
            override fun onLocalSourceAdded() {}

            override fun onPlayerReload() {
                Log.d(TAG, "onPlayerReload")
                // Reiniciamos UI de progreso y estado
                isPlaying = false
                btnPlayPause.setIconResource(R.drawable.ic_play_arrow)
                seekBar.progress = 0
                tvCurrentTime.text = "0:00"
                tvDuration.text = "--:--"
            }

            override fun onAdEvents(type: AdEvent.AdEventType) {}
            override fun onAdErrorEvent(error: AdError) {}
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

    private fun startProgressUpdates() {
        uiHandler.removeCallbacks(updateRunnable)
        uiHandler.post(updateRunnable)
    }

    private fun updateProgressFromPlayer() {
        val p = msPlayerView?.player ?: return
        val durationMs = p.duration
        val positionMs = p.currentPosition

        if (durationMs == C.TIME_UNSET || durationMs <= 0) {
            tvDuration.text = "--:--"
            seekBar.isEnabled = false
            return
        }

        if (!seekBar.isEnabled) {
            seekBar.isEnabled = true
            seekBar.max = durationMs.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
            tvDuration.text = formatTime(durationMs)
        }

        if (!isUserSeeking) {
            seekBar.progress = positionMs.coerceAtMost(seekBar.max.toLong()).toInt()
            tvCurrentTime.text = formatTime(positionMs)
        }
    }

    private fun updateContentIdLabel(contentId: String, stationLabel: String) {
        tvContentId.text = "contentId ($stationLabel): $contentId"
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(ms.coerceAtLeast(0))
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }

    override fun onDestroy() {
        uiHandler.removeCallbacks(updateRunnable)
        super.onDestroy()
        player?.releasePlayer()
    }
}

