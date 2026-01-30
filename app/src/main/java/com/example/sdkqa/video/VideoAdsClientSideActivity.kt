package com.example.sdkqa.video

import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayer
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerCallback
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerConfig
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.ui.PlayerView
import com.example.sdkqa.R
import com.google.ads.interactivemedia.v3.api.AdError
import com.google.ads.interactivemedia.v3.api.AdEvent

class VideoAdsClientSideActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SDK-QA"
        private const val VIDEO_CONTENT_ID = "696bc8a832ce0ef08c6fa0ef" // Mismo ID que VOD Simple
        
        // URLs de prueba para diferentes tipos de ads
        private const val AD_URL_PRE_MID_POST = "https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/vmap_ad_samples&sz=640x480&cust_params=sample_ar%3Dpremidpost&ciu_szs=300x250&gdfp_req=1&ad_rule=1&output=vmap&unviewed_position_start=1&env=vp&cmsid=496&vid=short_onecue&correlator="
        private const val AD_URL_REDIRECT_ERROR = "https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/single_ad_samples&sz=640x480&cust_params=sample_ct%3Dredirecterror&ciu_szs=300x250%2C728x90&gdfp_req=1&output=vast&unviewed_position_start=1&env=vp&correlator="
        private const val AD_URL_REDIRECT_BROKEN = "https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/single_ad_samples&sz=640x480&cust_params=sample_ct%3Dredirecterror&ciu_szs=300x250%2C728x90&gdfp_req=1&output=vast&unviewed_position_start=1&env=vp&nofb=1&correlator="
    }

    private lateinit var container: FrameLayout
    private lateinit var spinnerAdType: Spinner
    private lateinit var tvAdStatus: TextView
    private lateinit var tvAdEvents: TextView
    private var player: MediastreamPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_ads_client_side)

        initializeViews()
        setupSpinner()
        initializePlayer()
    }

    private fun initializeViews() {
        container = findViewById(R.id.main_media_frame)
        spinnerAdType = findViewById(R.id.spinnerAdType)
        tvAdStatus = findViewById(R.id.tvAdStatus)
        tvAdEvents = findViewById(R.id.tvAdEvents)
    }

    private fun setupSpinner() {
        val adTypes = arrayOf(
            "Pre+Mid+Post Roll",
            "Redirect Error",
            "Redirect Broken (Fallback)"
        )

        val adapter = ArrayAdapter(this, R.layout.spinner_item, adTypes)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerAdType.adapter = adapter

        spinnerAdType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = adTypes[position]
                Log.d(TAG, "Ad type selected: $selectedType")
                changeAdType(selectedType)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada
            }
        }
    }

    private fun initializePlayer() {
        val config = createConfigForAdType("Pre+Mid+Post Roll")
        player = MediastreamPlayer(this, config, container, container, supportFragmentManager)
        player?.addPlayerCallback(createPlayerCallback())
        Log.d(TAG, "VideoAdsClientSideActivity initialized with Pre+Mid+Post Roll ads")
    }

    private fun changeAdType(adType: String) {
        Log.d(TAG, "Changing ad type to: $adType")
        tvAdStatus.text = "Ad Status: Loading $adType..."
        tvAdEvents.text = "Last Ad Event: None"
        
        // Crear nueva configuración con el adUrl correspondiente
        val newConfig = createConfigForAdType(adType)
        
        // Recargar player con nueva configuración
        player?.reloadPlayer(newConfig)
    }

    private fun createConfigForAdType(adType: String): MediastreamPlayerConfig {
        val adUrlValue = when (adType) {
            "Pre+Mid+Post Roll" -> AD_URL_PRE_MID_POST
            "Redirect Error" -> AD_URL_REDIRECT_ERROR
            "Redirect Broken (Fallback)" -> AD_URL_REDIRECT_BROKEN
            else -> AD_URL_PRE_MID_POST
        }

        val config = MediastreamPlayerConfig()
        config.id = VIDEO_CONTENT_ID
        config.type = MediastreamPlayerConfig.VideoTypes.VOD
        
        // 🟢 CONFIGURACIÓN DE ANUNCIOS CLIENT-SIDE (VAST/VMAP)
        config.adURL = adUrlValue
        
        Log.d(TAG, "Ad configuration set - Type: $adType, URL: $adUrlValue")
        
        return config
    }

    private fun createPlayerCallback(): MediastreamPlayerCallback {
        return object : MediastreamPlayerCallback {
            override fun playerViewReady(msplayerView: PlayerView?) {
                Log.d(TAG, "playerViewReady")
            }

            override fun onPlay() {
                Log.d(TAG, "onPlay - Content playback started")
            }

            override fun onPause() {
                Log.d(TAG, "onPause - Content playback paused")
            }

            override fun onReady() {
                Log.d(TAG, "onReady")
            }

            override fun onEnd() {
                Log.d(TAG, "onEnd - Content playback ended")
            }

            override fun onBuffering() {
                Log.d(TAG, "onBuffering")
            }

            override fun onError(error: String?) {
                Log.e(TAG, "onError: $error")
                runOnUiThread {
                    tvAdStatus.text = "Video Error: $error"
                }
            }

            override fun onDismissButton() {
                Log.d(TAG, "onDismissButton")
            }

            override fun onPlayerClosed() {
                Log.d(TAG, "onPlayerClosed")
            }

            override fun onNext() {}
            override fun onPrevious() {}

            override fun onFullscreen() {
                Log.d(TAG, "onFullscreen")
            }

            override fun offFullscreen() {
                Log.d(TAG, "offFullscreen")
            }

            override fun onNewSourceAdded(config: MediastreamPlayerConfig) {}
            override fun onLocalSourceAdded() {}

            override fun onPlayerReload() {
                Log.d(TAG, "onPlayerReload")
            }

            // Callbacks de Ad Events
            override fun onAdEvents(type: AdEvent.AdEventType) {
                Log.d(TAG, "AD EVENT: $type")
                runOnUiThread {
                    tvAdEvents.text = "Last Ad Event: $type"
                    when (type) {
                        AdEvent.AdEventType.LOADED -> {
                            tvAdStatus.text = "Ad Status: Ad Loaded ✓"
                        }
                        AdEvent.AdEventType.STARTED -> {
                            tvAdStatus.text = "Ad Status: Ad Playing ▶"
                        }
                        AdEvent.AdEventType.COMPLETED -> {
                            tvAdStatus.text = "Ad Status: Ad Completed ✓"
                        }
                        AdEvent.AdEventType.SKIPPED -> {
                            tvAdStatus.text = "Ad Status: Ad Skipped ⏭"
                        }
                        AdEvent.AdEventType.PAUSED -> {
                            tvAdStatus.text = "Ad Status: Ad Paused ⏸"
                        }
                        AdEvent.AdEventType.RESUMED -> {
                            tvAdStatus.text = "Ad Status: Ad Resumed ▶"
                        }
                        AdEvent.AdEventType.ALL_ADS_COMPLETED -> {
                            tvAdStatus.text = "Ad Status: All Ads Completed ✅"
                        }
                        else -> {
                            // Otros eventos
                        }
                    }
                }
            }

            override fun onAdErrorEvent(error: AdError) {
                Log.e(TAG, "AD ERROR: ${error.errorCode} - ${error.message}")
                runOnUiThread {
                    tvAdStatus.text = "Ad Status: Error ❌"
                    tvAdEvents.text = "Last Ad Event: ERROR ${error.errorCode} - ${error.message}"
                }
            }

            override fun onConfigChange(config: am.mediastre.mediastreamplatformsdkandroid.MediastreamMiniPlayerConfig?) {}
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
            
            override fun onPlaybackErrors(error: org.json.JSONObject?) {
                Log.e(TAG, "onPlaybackErrors: $error")
            }

            override fun onEmbedErrors(error: org.json.JSONObject?) {
                Log.e(TAG, "onEmbedErrors: $error")
            }

            override fun onLiveAudioCurrentSongChanged(data: org.json.JSONObject?) {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.releasePlayer()
        Log.d(TAG, "VideoAdsClientSideActivity destroyed")
    }
}
