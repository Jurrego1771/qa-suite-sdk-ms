package com.example.sdkqa.audio

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sdkqa.R
import com.example.sdkqa.testutils.LogcatCapture
import com.example.sdkqa.testutils.TestFailureWatcher
import org.hamcrest.CoreMatchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioAodCustomUIActivityTest {

    @get:Rule
    val failureWatcher = TestFailureWatcher()

    @Test
    fun launches_and_emits_ready_callbacks() {
        ActivityScenario.launch(AudioAodCustomUIActivity::class.java).use {
            // Validar que el SDK llega a estados básicos
            waitForLogIncrement("onReady", timeoutMs = 30_000)
            waitForLogIncrement("playerViewReady", timeoutMs = 30_000)
        }
    }

    @Test
    fun play_pause_button_triggers_callbacks() {
        ActivityScenario.launch(AudioAodCustomUIActivity::class.java).use {
            waitForLogIncrement("playerViewReady", timeoutMs = 30_000)

            val playActionBefore = countLogsContaining("USER ACTION: play()")
            val pauseActionBefore = countLogsContaining("USER ACTION: pause()")
            val onPlayBefore = countLogsContaining("onPlay")
            val onPauseBefore = countLogsContaining("onPause")

            onView(withId(R.id.btnPlayPause)).perform(click())

            // El botón puede ejecutar play() o pause() dependiendo del estado inicial.
            val played = waitForCountIncrease("USER ACTION: play()", playActionBefore, timeoutMs = 10_000)
            val paused = if (!played) waitForCountIncrease("USER ACTION: pause()", pauseActionBefore, timeoutMs = 10_000) else false

            if (played) {
                waitForCountIncrease("onPlay", onPlayBefore, timeoutMs = 20_000)
            } else if (paused) {
                waitForCountIncrease("onPause", onPauseBefore, timeoutMs = 20_000)
            } else {
                throw AssertionError("No se detectó acción de play() ni pause() en Logcat tras el click.")
            }
        }
    }

    @Test
    fun speed_forward_backward_and_reload_radio_generate_events() {
        ActivityScenario.launch(AudioAodCustomUIActivity::class.java).use {
            waitForLogIncrement("playerViewReady", timeoutMs = 30_000)

            // Speed
            val speedBefore = countLogsContaining("USER ACTION: changeSpeed(")
            onView(withId(R.id.btnSpeed)).perform(click())
            onView(withId(R.id.btnSpeed)).check(matches(withText("1.25x")))
            waitForCountIncrease("USER ACTION: changeSpeed(", speedBefore, timeoutMs = 10_000)

            // Forward / Backward
            val forwardBefore = countLogsContaining("USER ACTION: forward(")
            val backwardBefore = countLogsContaining("USER ACTION: backward(")
            onView(withId(R.id.btnForward)).perform(click())
            waitForCountIncrease("USER ACTION: forward(", forwardBefore, timeoutMs = 10_000)
            onView(withId(R.id.btnBackward)).perform(click())
            waitForCountIncrease("USER ACTION: backward(", backwardBefore, timeoutMs = 10_000)

            // Reload radio B
            val reloadBefore = countLogsContaining("Reload player with contentId=")
            val onReloadBefore = countLogsContaining("onPlayerReload")

            onView(withId(R.id.rbStationB)).perform(click())
            onView(withId(R.id.tvContentId)).check(matches(withText(containsString("contentId (B):"))))

            waitForCountIncrease("Reload player with contentId=", reloadBefore, timeoutMs = 30_000)
            waitForCountIncrease("onPlayerReload", onReloadBefore, timeoutMs = 30_000)
        }
    }

    private fun countLogsContaining(needle: String): Int {
        return LogcatCapture.getRecentLogs(1000).count { it.contains(needle) }
    }

    private fun waitForLogIncrement(needle: String, timeoutMs: Long): Boolean {
        val before = countLogsContaining(needle)
        return waitForCountIncrease(needle, before, timeoutMs)
    }

    private fun waitForCountIncrease(needle: String, before: Int, timeoutMs: Long): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (countLogsContaining(needle) > before) return true
            Thread.sleep(100)
        }
        throw AssertionError("Timeout esperando log '$needle' (before=$before).")
    }
}

