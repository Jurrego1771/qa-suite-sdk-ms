package com.example.sdkqa.integration

import android.content.pm.PackageManager
import android.os.Build
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.example.sdkqa.testutils.AllureScreenshotRule
import com.example.sdkqa.testutils.LogcatCapture
import com.example.sdkqa.video.VideoLivePiPActivity
import io.qameta.allure.kotlin.Allure
import io.qameta.allure.kotlin.junit4.DisplayName
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * Test específico para Picture-in-Picture (PiP) usando UI Automator.
 * 
 * Verifica que la ventana PiP existe tras llamar a mediastreamPlayer.startPiP().
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class PictureInPictureTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(VideoLivePiPActivity::class.java)
    
    @get:Rule
    val allureScreenshotRule = AllureScreenshotRule()

    private lateinit var uiDevice: UiDevice

    @Before
    fun setUp() {
        Allure.step("Inicializar UiDevice y captura de logs") {
            uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            LogcatCapture.startCapture("PictureInPictureTest")
        }

        Allure.step("Validar soporte de PiP en el dispositivo (si no, se omite el test)") {
            val ctx = InstrumentationRegistry.getInstrumentation().targetContext
            val supportsPiP =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                    ctx.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)

            assumeTrue("El dispositivo/emulador no soporta Picture-in-Picture (PiP)", supportsPiP)
        }
    }

    @After
    fun tearDown() {
        LogcatCapture.stopCapture()
    }

    /**
     * Test que verifica que la ventana PiP se crea correctamente
     * después de llamar a startPiP().
     */
    @Test
    @DisplayName("Validar Picture-in-Picture: Verificar que la ventana PiP existe")
    fun testPictureInPictureWindow() {
        Allure.step("Esperar a que la Activity y el player estén listos") {
            // En algunos dispositivos PiP sólo funciona de forma consistente cuando hay reproducción/Surface lista
            Thread.sleep(6000)
        }

        Allure.step("Enviar la app a Home (debería disparar PiP vía onUserLeaveHint)") {
            uiDevice.pressHome()
            uiDevice.waitForIdle()
        }

        Allure.step("Validar que la Activity quedó en modo PiP (señal de sistema)") {
            // Esta es la señal más confiable. La jerarquía accesible en PiP puede variar por OEM/launcher.
            assertTrue(
                "La activity debería estar en modo Picture-in-Picture",
                waitUntilInPiP(timeoutMs = TimeUnit.SECONDS.toMillis(8))
            )
        }

        Allure.step("Best-effort: intentar detectar ventana PiP con UI Automator") {
            val pipWindow = findPiPWindow()
            if (pipWindow == null) {
                Allure.step("UI Automator no encontró un objeto PiP accesible; se adjunta screenshot para diagnóstico") {
                    com.example.sdkqa.testutils.ScreenshotCapture.capture(
                        "testPictureInPictureWindow",
                        "pip_window_not_found"
                    )
                }
                return@step
            }

            Allure.step("Verificar propiedades del objeto PiP encontrado") {
                assertTrue("La ventana PiP debe estar habilitada", pipWindow.isEnabled)

                com.example.sdkqa.testutils.ScreenshotCapture.capture(
                    "testPictureInPictureWindow",
                    "pip_window_found"
                )
            }
        }
    }

    /**
     * Busca la ventana Picture-in-Picture usando UI Automator.
     * Verifica que PiP esté activo buscando elementos del player o verificando el estado de la actividad.
     */
    private fun findPiPWindow(): UiObject2? {
        return try {
            // En PiP, muchas implementaciones no exponen el PlayerView en la jerarquía accesible.
            // Aun así intentamos por clase y validamos tamaño.
            val candidate = uiDevice.wait(
                Until.findObject(By.clazz(androidx.media3.ui.PlayerView::class.java.name)),
                TimeUnit.SECONDS.toMillis(3)
            ) ?: return null

            val bounds = candidate.visibleBounds
            val screenWidth = uiDevice.displayWidth
            val screenHeight = uiDevice.displayHeight

            if (bounds.width() < screenWidth * 0.8 && bounds.height() < screenHeight * 0.8) {
                android.util.Log.d(
                    "PictureInPictureTest",
                    "Objeto candidato encontrado con dimensiones tipo PiP: ${bounds.width()}x${bounds.height()}"
                )
                return candidate
            }

            android.util.Log.d("PictureInPictureTest", "No se encontró ventana PiP visible")
            null
            
        } catch (e: Exception) {
            android.util.Log.w("PictureInPictureTest", "Error buscando ventana PiP: ${e.message}")
            null
        }
    }

    private fun waitUntilInPiP(timeoutMs: Long): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            var inPip = false
            try {
                activityRule.scenario.onActivity { activity ->
                    inPip = activity.isInPictureInPictureMode
                }
            } catch (_: Exception) {
                // Ignorar transiciones de estado mientras se entra a PiP
            }

            if (inPip) return true
            Thread.sleep(200)
        }
        return false
    }
}
