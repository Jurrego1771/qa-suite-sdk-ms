package com.example.sdkqa.video

import am.mediastre.mediastreamplatformsdkandroid.MediastreamMiniPlayerConfig
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayer
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerCallback
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerConfig
import android.graphics.Color
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.ui.PlayerView
import com.example.sdkqa.R
import com.google.ads.interactivemedia.v3.api.AdError
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VideoLiveCustomUIActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SDK-QA"
    }

    private var player: MediastreamPlayer? = null
    private lateinit var tvEventsLog: TextView
    private lateinit var tvEventStatus: TextView
    private lateinit var btnClearLog: MaterialButton
    private lateinit var fabPlay: FloatingActionButton
    private lateinit var fabPause: FloatingActionButton
    private lateinit var indicatorOnPlay: View
    private lateinit var indicatorOnPause: View
    private lateinit var indicatorOnReady: View
    private lateinit var indicatorOnError: View

    private val eventLog = StringBuilder()
    private val maxLogLines = 50

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_live_custom_ui)

        initializeViews()
        setupPlayer()
        setupControls()
    }

    private fun initializeViews() {
        tvEventsLog = findViewById(R.id.tvEventsLog)
        tvEventStatus = findViewById(R.id.tvEventStatus)
        btnClearLog = findViewById(R.id.btnClearLog)
        fabPlay = findViewById(R.id.fabPlay)
        fabPause = findViewById(R.id.fabPause)
        indicatorOnPlay = findViewById(R.id.indicatorOnPlay)
        indicatorOnPause = findViewById(R.id.indicatorOnPause)
        indicatorOnReady = findViewById(R.id.indicatorOnReady)
        indicatorOnError = findViewById(R.id.indicatorOnError)

        tvEventsLog.movementMethod = ScrollingMovementMethod()
    }

    private fun setupPlayer() {
        val mainMediaFrame = findViewById<FrameLayout>(R.id.main_media_frame)

        val config = MediastreamPlayerConfig().apply {
            id = "5fd39e065d68477eaa1ccf5a" // Live Video ID
            type = MediastreamPlayerConfig.VideoTypes.LIVE
            showControls = true
            //Uncomment to use development environment
            //environment = MediastreamPlayerConfig.Environment.DEV
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

    private fun setupControls() {
        fabPlay.setOnClickListener {
            player?.play()
            addEventToLog("USER ACTION: Play button clicked")
        }

        fabPause.setOnClickListener {
            player?.pause()
            addEventToLog("USER ACTION: Pause button clicked")
        }

        btnClearLog.setOnClickListener {
            eventLog.clear()
            tvEventsLog.text = "Log cleared..."
            addEventToLog("USER ACTION: Log cleared")
        }
    }

    private fun addEventToLog(event: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val logEntry = "[$timestamp] $event\n"

        eventLog.append(logEntry)

        // Limit log size
        val lines = eventLog.toString().split("\n")
        if (lines.size > maxLogLines) {
            val newLog = lines.takeLast(maxLogLines).joinToString("\n")
            eventLog.clear()
            eventLog.append(newLog)
        }

        runOnUiThread {
            tvEventsLog.text = eventLog.toString()
            // Auto-scroll to bottom
            val scrollView = findViewById<android.widget.ScrollView>(R.id.scrollEvents)
            scrollView.post {
                scrollView.fullScroll(View.FOCUS_DOWN)
            }
        }

        Log.d(TAG, event)
    }

    private fun updateEventStatus(status: String, color: Int) {
        runOnUiThread {
            tvEventStatus.text = status
            tvEventStatus.setTextColor(color)
        }
    }

    private fun updateIndicator(view: View, show: Boolean) {
        runOnUiThread {
            view.visibility = if (show) View.VISIBLE else View.GONE
            if (show) {
                view.setBackgroundColor(Color.GREEN)
            } else {
                view.setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }

    private fun createPlayerCallback(): MediastreamPlayerCallback {
        return object : MediastreamPlayerCallback {
            override fun playerViewReady(msplayerView: PlayerView?) {
                addEventToLog("✅ playerViewReady() - Player view is ready")
                updateEventStatus("Player Ready", Color.GREEN)
            }

            override fun onPlay() {
                addEventToLog("▶️ onPlay() - Video started playing")
                updateEventStatus("Playing", Color.GREEN)
                updateIndicator(indicatorOnPlay, true)
                updateIndicator(indicatorOnPause, false)
            }

            override fun onPause() {
                addEventToLog("⏸ onPause() - Video paused")
                updateEventStatus("Paused", Color.YELLOW)
                updateIndicator(indicatorOnPlay, false)
                updateIndicator(indicatorOnPause, true)
            }

            override fun onReady() {
                addEventToLog("✅ onReady() - Video ready to play")
                updateEventStatus("Ready", Color.GREEN)
                updateIndicator(indicatorOnReady, true)
            }

            override fun onEnd() {
                addEventToLog("🏁 onEnd() - Video playback ended")
                updateEventStatus("Ended", Color.CYAN)
            }

            override fun onBuffering() {
                addEventToLog("⏳ onBuffering() - Buffering content")
                updateEventStatus("Buffering...", Color.YELLOW)
            }

            override fun onError(error: String?) {
                val errorMsg = error ?: "Unknown error"
                addEventToLog("❌ onError() - Error: $errorMsg")
                updateEventStatus("Error: $errorMsg", Color.RED)
                updateIndicator(indicatorOnError, true)
            }

            override fun onDismissButton() {
                addEventToLog("🔘 onDismissButton() - Dismiss button clicked")
            }

            override fun onPlayerClosed() {
                addEventToLog("🚪 onPlayerClosed() - Player closed")
                updateEventStatus("Closed", Color.GRAY)
            }

            override fun onNext() {
                addEventToLog("⏭ onNext() - Next button clicked")
            }

            override fun onPrevious() {
                addEventToLog("⏮ onPrevious() - Previous button clicked")
            }

            override fun onFullscreen() {
                addEventToLog("🔲 onFullscreen() - Entered fullscreen")
            }

            override fun offFullscreen() {
                addEventToLog("🔳 offFullscreen() - Exited fullscreen")
            }

            override fun onNewSourceAdded(config: MediastreamPlayerConfig) {
                addEventToLog("📥 onNewSourceAdded() - New source added: ${config.id}")
            }

            override fun onLocalSourceAdded() {
                addEventToLog("📁 onLocalSourceAdded() - Local source added")
            }

            override fun onPlayerReload() {
                addEventToLog("🔄 onPlayerReload() - Player reloaded")
            }

            override fun onAdEvents(type: AdEvent.AdEventType) {
                addEventToLog("📢 onAdEvents() - Ad event: ${type.name}")
            }

            override fun onAdErrorEvent(error: AdError) {
                addEventToLog("❌ onAdErrorEvent() - Ad error: ${error.message}")
            }

            override fun onConfigChange(config: MediastreamMiniPlayerConfig?) {
                addEventToLog("⚙️ onConfigChange() - Configuration changed")
            }

            override fun onCastAvailable(state: Boolean?) {
                addEventToLog("📺 onCastAvailable() - Cast available: $state")
            }

            override fun onCastSessionStarting() {
                addEventToLog("📺 onCastSessionStarting() - Cast session starting")
            }

            override fun onCastSessionStarted() {
                addEventToLog("📺 onCastSessionStarted() - Cast session started")
            }

            override fun onCastSessionStartFailed() {
                addEventToLog("❌ onCastSessionStartFailed() - Cast session start failed")
            }

            override fun onCastSessionEnding() {
                addEventToLog("📺 onCastSessionEnding() - Cast session ending")
            }

            override fun onCastSessionEnded() {
                addEventToLog("📺 onCastSessionEnded() - Cast session ended")
            }

            override fun onCastSessionResuming() {
                addEventToLog("📺 onCastSessionResuming() - Cast session resuming")
            }

            override fun onCastSessionResumed() {
                addEventToLog("📺 onCastSessionResumed() - Cast session resumed")
            }

            override fun onCastSessionResumeFailed() {
                addEventToLog("❌ onCastSessionResumeFailed() - Cast session resume failed")
            }

            override fun onCastSessionSuspended() {
                addEventToLog("📺 onCastSessionSuspended() - Cast session suspended")
            }

            override fun onPlaybackErrors(error: JSONObject?) {
                val errorMsg = error?.toString() ?: "Unknown playback error"
                addEventToLog("❌ onPlaybackErrors() - Playback error: $errorMsg")
                updateEventStatus("Playback Error", Color.RED)
            }

            override fun onEmbedErrors(error: JSONObject?) {
                val errorMsg = error?.toString() ?: "Unknown embed error"
                addEventToLog("❌ onEmbedErrors() - Embed error: $errorMsg")
                updateEventStatus("Embed Error", Color.RED)
            }

            override fun onLiveAudioCurrentSongChanged(data: JSONObject?) {
                // This is for audio, but we log it anyway
                val songData = data?.toString() ?: "No data"
                addEventToLog("🎵 onLiveAudioCurrentSongChanged() - Song data: $songData")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.releasePlayer()
    }
}
