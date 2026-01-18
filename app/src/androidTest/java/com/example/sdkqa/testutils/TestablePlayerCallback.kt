package com.example.sdkqa.testutils

import am.mediastre.mediastreamplatformsdkandroid.MediastreamMiniPlayerConfig
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerCallback
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerConfig
import androidx.media3.ui.PlayerView
import com.google.ads.interactivemedia.v3.api.AdError
import com.google.ads.interactivemedia.v3.api.AdEvent
import org.json.JSONObject

/**
 * Wrapper de MediastreamPlayerCallback que rastrea eventos para pruebas.
 * Este callback puede ser usado en las Activities para permitir verificación en tests.
 */
class TestablePlayerCallback(
    private val originalCallback: MediastreamPlayerCallback? = null
) : MediastreamPlayerCallback {
    
    override fun playerViewReady(msplayerView: PlayerView?) {
        PlayerCallbackTracker.onPlayerViewReady()
        originalCallback?.playerViewReady(msplayerView)
    }
    
    override fun onPlay() {
        PlayerCallbackTracker.onPlay()
        originalCallback?.onPlay()
    }
    
    override fun onPause() {
        PlayerCallbackTracker.onPause()
        originalCallback?.onPause()
    }
    
    override fun onReady() {
        PlayerCallbackTracker.onReady()
        originalCallback?.onReady()
    }
    
    override fun onEnd() {
        originalCallback?.onEnd()
    }
    
    override fun onBuffering() {
        originalCallback?.onBuffering()
    }
    
    override fun onError(error: String?) {
        PlayerCallbackTracker.onError()
        originalCallback?.onError(error)
    }
    
    override fun onDismissButton() {
        originalCallback?.onDismissButton()
    }
    
    override fun onPlayerClosed() {
        originalCallback?.onPlayerClosed()
    }
    
    override fun onNext() {
        originalCallback?.onNext()
    }
    
    override fun onPrevious() {
        originalCallback?.onPrevious()
    }
    
    override fun onFullscreen() {
        originalCallback?.onFullscreen()
    }
    
    override fun offFullscreen() {
        originalCallback?.offFullscreen()
    }
    
    override fun onNewSourceAdded(config: MediastreamPlayerConfig) {
        originalCallback?.onNewSourceAdded(config)
    }
    
    override fun onLocalSourceAdded() {
        originalCallback?.onLocalSourceAdded()
    }
    
    override fun onPlayerReload() {
        originalCallback?.onPlayerReload()
    }
    
    override fun onAdEvents(type: AdEvent.AdEventType) {
        originalCallback?.onAdEvents(type)
    }
    
    override fun onAdErrorEvent(error: AdError) {
        originalCallback?.onAdErrorEvent(error)
    }
    
    override fun onConfigChange(config: MediastreamMiniPlayerConfig?) {
        originalCallback?.onConfigChange(config)
    }
    
    override fun onCastAvailable(state: Boolean?) {
        originalCallback?.onCastAvailable(state)
    }
    
    override fun onCastSessionStarting() {
        originalCallback?.onCastSessionStarting()
    }
    
    override fun onCastSessionStarted() {
        originalCallback?.onCastSessionStarted()
    }
    
    override fun onCastSessionStartFailed() {
        originalCallback?.onCastSessionStartFailed()
    }
    
    override fun onCastSessionEnding() {
        originalCallback?.onCastSessionEnding()
    }
    
    override fun onCastSessionEnded() {
        originalCallback?.onCastSessionEnded()
    }
    
    override fun onCastSessionResuming() {
        originalCallback?.onCastSessionResuming()
    }
    
    override fun onCastSessionResumed() {
        originalCallback?.onCastSessionResumed()
    }
    
    override fun onCastSessionResumeFailed() {
        originalCallback?.onCastSessionResumeFailed()
    }
    
    override fun onCastSessionSuspended() {
        originalCallback?.onCastSessionSuspended()
    }
    
    override fun onPlaybackErrors(error: JSONObject?) {
        originalCallback?.onPlaybackErrors(error)
    }
    
    override fun onEmbedErrors(error: JSONObject?) {
        originalCallback?.onEmbedErrors(error)
    }
    
    override fun onLiveAudioCurrentSongChanged(data: JSONObject?) {
        originalCallback?.onLiveAudioCurrentSongChanged(data)
    }
}
