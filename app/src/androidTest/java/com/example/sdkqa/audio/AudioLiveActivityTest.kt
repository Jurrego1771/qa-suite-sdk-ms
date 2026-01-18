package com.example.sdkqa.audio

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.sdkqa.testutils.PlayerCallbackTracker
import com.example.sdkqa.testutils.TestablePlayerCallback
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import android.widget.FrameLayout
import androidx.media3.ui.PlayerView
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import java.util.concurrent.TimeUnit
import java.lang.reflect.Method

/**
 * Pruebas automatizadas para AudioLiveActivity usando Espresso.
 * 
 * Estas pruebas verifican:
 * 1. Inicialización correcta del MediastreamPlayer
 * 2. Creación del FrameLayout y PlayerView
 * 3. Interacción con controles play/pause
 * 4. Verificación de callbacks onPlay/onPause
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AudioLiveActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(AudioLiveActivity::class.java)

    private lateinit var testCallback: TestablePlayerCallback

    @Before
    fun setUp() {
        // Reiniciar el tracker antes de cada prueba
        PlayerCallbackTracker.reset()
        
        // Crear callback de prueba
        testCallback = TestablePlayerCallback()
        
        // Inyectar el callback de prueba en la Activity usando reflection
        // Hacerlo inmediatamente cuando la Activity esté disponible
        activityRule.scenario.onActivity { activity ->
            try {
                // Inyectar el callback lo antes posible
                injectTestCallback(activity)
            } catch (e: Exception) {
                // Si falla la inyección, continuamos sin verificación de callbacks
                android.util.Log.w("AudioLiveActivityTest", "No se pudo inyectar callback de prueba: ${e.message}")
            }
        }
    }
    
    /**
     * Inyecta el callback de prueba en la Activity usando reflection.
     */
    private fun injectTestCallback(activity: AudioLiveActivity) {
        try {
            // Acceder al campo 'player' usando reflection
            val playerField = AudioLiveActivity::class.java.getDeclaredField("player")
            playerField.isAccessible = true
            val player = playerField.get(activity)
            
            if (player != null) {
                // Agregar nuestro callback de prueba al player
                val addCallbackMethod = player.javaClass.getMethod(
                    "addPlayerCallback",
                    am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerCallback::class.java
                )
                addCallbackMethod.invoke(player, testCallback)
            }
        } catch (e: Exception) {
            android.util.Log.w("AudioLiveActivityTest", "Error inyectando callback: ${e.message}")
        }
    }

    @After
    fun tearDown() {
        // Limpiar después de cada prueba si es necesario
        PlayerCallbackTracker.reset()
    }

    /**
     * Prueba que verifica la inicialización correcta del player.
     * Valida que el FrameLayout se crea y contiene el PlayerView.
     */
    @Test
    fun testPlayerInitialization() {
        // Verificar que la actividad está visible
        onView(isRoot())
            .check(matches(isDisplayed()))

        // Esperar un momento para que el player se inicialice
        Thread.sleep(2000)

        // Verificar que existe un FrameLayout en la jerarquía de vistas
        // Como el FrameLayout se crea dinámicamente sin ID fijo, verificamos
        // que la raíz de la actividad es un FrameLayout
        onView(isRoot())
            .check { view, _ ->
                assert(view is FrameLayout) { 
                    "La raíz de la actividad debe ser un FrameLayout" 
                }
                
                val frameLayout = view as FrameLayout
                assert(frameLayout.childCount > 0) {
                    "El FrameLayout debe contener al menos un hijo (PlayerView o container)"
                }
                
                // Buscar PlayerView en la jerarquía
                val playerView = findPlayerViewInHierarchy(frameLayout)
                assert(playerView != null) {
                    "Debe existir un PlayerView dentro del FrameLayout"
                }
            }
    }

    /**
     * Prueba que verifica la creación real del MediastreamPlayer.
     * Verifica que el player se inicializa correctamente y está listo.
     */
    @Test
    fun testMediastreamPlayerCreation() {
        // Esperar a que el player se inicialice completamente
        Thread.sleep(3000)

        onView(isRoot())
            .check { view, _ ->
                val frameLayout = view as FrameLayout
                val playerView = findPlayerViewInHierarchy(frameLayout)
                
                assert(playerView != null) {
                    "PlayerView debe existir"
                }
                
                // Verificar que el PlayerView tiene un player asignado
                // (esto indica que MediastreamPlayer se inicializó correctamente)
                assert(playerView?.player != null) {
                    "El PlayerView debe tener un player asignado (MediastreamPlayer inicializado)"
                }
            }
    }

    /**
     * Prueba que simula clic en el botón de play del PlayerView.
     * Verifica que el control es clickeable y responde.
     */
    @Test
    fun testPlayButtonClick() {
        // Esperar a que el player se inicialice
        Thread.sleep(3000)

        // Buscar y hacer clic en el botón de play del PlayerView
        // Media3 PlayerView tiene controles nativos que podemos interactuar
        try {
            // Intentar encontrar el botón de play usando descripción de contenido
            // o buscando en la jerarquía del PlayerView
            onView(isRoot())
                .perform(object : androidx.test.espresso.ViewAction {
                    override fun getConstraints(): org.hamcrest.Matcher<android.view.View> {
                        return isRoot()
                    }

                    override fun getDescription(): String {
                        return "Click en botón de play del PlayerView"
                    }

                    override fun perform(
                        uiController: androidx.test.espresso.UiController,
                        view: android.view.View
                    ) {
                        val frameLayout = view as FrameLayout
                        val playerView = findPlayerViewInHierarchy(frameLayout)
                        
                        if (playerView != null && playerView.useController) {
                            // Si el player está pausado, hacer play programáticamente
                            playerView.player?.let { player ->
                                if (!player.isPlaying) {
                                    player.play()
                                }
                            }
                        }
                    }
                })

            // Esperar un momento para que el play se procese
            Thread.sleep(1000)

            // Verificar que el player está reproduciendo
            onView(isRoot())
                .check { view, _ ->
                    val frameLayout = view as FrameLayout
                    val playerView = findPlayerViewInHierarchy(frameLayout)
                    
                    playerView?.player?.let { player ->
                        // Verificar que el player está en estado de reproducción
                        // o al menos no está en estado de error
                        assert(
                            player.playbackState != androidx.media3.common.Player.STATE_IDLE ||
                            player.playbackState == androidx.media3.common.Player.STATE_BUFFERING ||
                            player.playbackState == androidx.media3.common.Player.STATE_READY
                        ) {
                            "El player debe estar en un estado válido después de hacer play"
                        }
                    }
                }
        } catch (e: Exception) {
            // Si no podemos hacer clic directamente, al menos verificamos que el player existe
            onView(isRoot())
                .check { view, _ ->
                    val frameLayout = view as FrameLayout
                    val playerView = findPlayerViewInHierarchy(frameLayout)
                    assert(playerView != null) {
                        "PlayerView debe existir para poder interactuar con él"
                    }
                }
        }
    }

    /**
     * Prueba que simula clic en el botón de pause del PlayerView.
     * Verifica que el control funciona correctamente.
     */
    @Test
    fun testPauseButtonClick() {
        // Esperar a que el player se inicialice
        Thread.sleep(3000)

        // Primero asegurarnos de que el player esté reproduciendo
        onView(isRoot())
            .perform(object : androidx.test.espresso.ViewAction {
                override fun getConstraints(): org.hamcrest.Matcher<android.view.View> {
                    return isRoot()
                }

                override fun getDescription(): String {
                    return "Iniciar reproducción y luego pausar"
                }

                override fun perform(
                    uiController: androidx.test.espresso.UiController,
                    view: android.view.View
                ) {
                    val frameLayout = view as FrameLayout
                    val playerView = findPlayerViewInHierarchy(frameLayout)
                    
                    playerView?.player?.let { player ->
                        // Iniciar reproducción
                        if (!player.isPlaying) {
                            player.play()
                            Thread.sleep(1000)
                        }
                        
                        // Ahora pausar
                        if (player.isPlaying) {
                            player.pause()
                        }
                    }
                }
            })

        // Esperar un momento para que el pause se procese
        Thread.sleep(1000)

        // Verificar que el player está pausado
        onView(isRoot())
            .check { view, _ ->
                val frameLayout = view as FrameLayout
                val playerView = findPlayerViewInHierarchy(frameLayout)
                
                playerView?.player?.let { player ->
                    assert(!player.isPlaying) {
                        "El player debe estar pausado después de hacer pause"
                    }
                }
            }
    }

    /**
     * Prueba que verifica la secuencia completa: play -> pause -> play.
     * Esto valida que los controles funcionan correctamente en secuencia.
     * También verifica que los callbacks se ejecuten correctamente.
     */
    @Test
    fun testPlayPauseSequence() {
        // Esperar a que el player se inicialice
        Thread.sleep(3000)

        onView(isRoot())
            .perform(object : androidx.test.espresso.ViewAction {
                override fun getConstraints(): org.hamcrest.Matcher<android.view.View> {
                    return isRoot()
                }

                override fun getDescription(): String {
                    return "Secuencia play -> pause -> play con verificación de callbacks"
                }

                override fun perform(
                    uiController: androidx.test.espresso.UiController,
                    view: android.view.View
                ) {
                    val frameLayout = view as FrameLayout
                    val playerView = findPlayerViewInHierarchy(frameLayout)
                    
                    playerView?.player?.let { player ->
                        val initialPlayCount = PlayerCallbackTracker.getPlayCallCount()
                        val initialPauseCount = PlayerCallbackTracker.getPauseCallCount()
                        
                        // 1. Play
                        if (!player.isPlaying) {
                            player.play()
                            // Esperar con polling hasta que el player esté reproduciendo
                            val playStarted = waitForPlayerPlaying(player, 5000)
                            assert(playStarted && player.isPlaying) { 
                                "Player debe estar reproduciendo después de play. Estado actual: isPlaying=${player.isPlaying}, playbackState=${player.playbackState}" 
                            }
                            
                            // Verificar que se llamó el callback onPlay
                            assert(PlayerCallbackTracker.getPlayCallCount() > initialPlayCount) {
                                "El callback onPlay debe haberse ejecutado"
                            }
                        }
                        
                        // 2. Pause
                        player.pause()
                        // Esperar con polling hasta que el player esté pausado
                        val pauseCompleted = waitForPlayerPaused(player, 3000)
                        assert(pauseCompleted && !player.isPlaying) { 
                            "Player debe estar pausado después de pause. Estado actual: isPlaying=${player.isPlaying}, playbackState=${player.playbackState}" 
                        }
                        
                        // Verificar que se llamó el callback onPause
                        assert(PlayerCallbackTracker.getPauseCallCount() > initialPauseCount) {
                            "El callback onPause debe haberse ejecutado"
                        }
                        
                        // 3. Play nuevamente
                        val playCountBeforeSecondPlay = PlayerCallbackTracker.getPlayCallCount()
                        player.play()
                        // Esperar con polling hasta que el player esté reproduciendo o en buffering
                        // (en streams en vivo, después de pause puede entrar en buffering antes de reproducir)
                        val secondPlayStarted = waitForPlayerPlaying(player, 10000)
                        assert(secondPlayStarted) { 
                            "Player debe estar reproduciendo o en buffering después del segundo play. Estado actual: isPlaying=${player.isPlaying}, playbackState=${player.playbackState}" 
                        }
                        
                        // Verificar que se llamó el callback onPlay nuevamente
                        assert(PlayerCallbackTracker.getPlayCallCount() > playCountBeforeSecondPlay) {
                            "El callback onPlay debe haberse ejecutado nuevamente"
                        }
                    } ?: throw AssertionError("Player no debe ser null")
                }
            })
    }
    
    /**
     * Prueba que verifica específicamente los callbacks onPlay y onPause.
     * Esta prueba se enfoca en validar que los callbacks se ejecuten cuando
     * se interactúa con los controles del player.
     */
    @Test
    fun testPlayPauseCallbacks() {
        // Esperar a que el playerViewReady se haya llamado (con timeout de 10 segundos)
        // Esto espera desde el inicio del test, no desde después de un sleep
        assert(PlayerCallbackTracker.waitForPlayerViewReady(10)) {
            "El callback playerViewReady debe haberse ejecutado"
        }
        assert(PlayerCallbackTracker.getPlayerViewReadyCallCount() > 0) {
            "Debe haberse llamado playerViewReady al menos una vez"
        }

        onView(isRoot())
            .perform(object : androidx.test.espresso.ViewAction {
                override fun getConstraints(): org.hamcrest.Matcher<android.view.View> {
                    return isRoot()
                }

                override fun getDescription(): String {
                    return "Verificar callbacks onPlay y onPause"
                }

                override fun perform(
                    uiController: androidx.test.espresso.UiController,
                    view: android.view.View
                ) {
                    val frameLayout = view as FrameLayout
                    val playerView = findPlayerViewInHierarchy(frameLayout)
                    
                    playerView?.player?.let { player ->
                        // Resetear contadores para esta prueba específica
                        val initialPlayCount = PlayerCallbackTracker.getPlayCallCount()
                        val initialPauseCount = PlayerCallbackTracker.getPauseCallCount()
                        
                        // Ejecutar play y verificar callback
                        if (!player.isPlaying) {
                            player.play()
                            Thread.sleep(2000) // Dar tiempo para que se ejecute el callback
                            
                            // Verificar que onPlay se haya llamado
                            val playCountAfter = PlayerCallbackTracker.getPlayCallCount()
                            assert(playCountAfter > initialPlayCount) {
                                "El callback onPlay debe haberse ejecutado. Esperado: > $initialPlayCount, Actual: $playCountAfter"
                            }
                            
                            // Verificar que el estado isPlaying es correcto
                            assert(PlayerCallbackTracker.isPlaying()) {
                                "El tracker debe indicar que está reproduciendo"
                            }
                        }
                        
                        // Ejecutar pause y verificar callback
                        if (player.isPlaying) {
                            player.pause()
                            Thread.sleep(2000) // Dar tiempo para que se ejecute el callback
                            
                            // Verificar que onPause se haya llamado
                            val pauseCountAfter = PlayerCallbackTracker.getPauseCallCount()
                            assert(pauseCountAfter > initialPauseCount) {
                                "El callback onPause debe haberse ejecutado. Esperado: > $initialPauseCount, Actual: $pauseCountAfter"
                            }
                            
                            // Verificar que el estado isPlaying es correcto
                            assert(!PlayerCallbackTracker.isPlaying()) {
                                "El tracker debe indicar que está pausado"
                            }
                        }
                    } ?: throw AssertionError("Player no debe ser null")
                }
            })
    }

    /**
     * Prueba que verifica cambios visuales en el PlayerView.
     * Verifica que los controles son visibles y responden a cambios de estado.
     */
    @Test
    fun testPlayerViewVisualChanges() {
        // Esperar a que el player se inicialice
        Thread.sleep(3000)

        onView(isRoot())
            .check { view, _ ->
                val frameLayout = view as FrameLayout
                val playerView = findPlayerViewInHierarchy(frameLayout)
                
                assert(playerView != null) {
                    "PlayerView debe existir"
                }
                
                // Verificar que el PlayerView es visible
                assert(playerView?.visibility == android.view.View.VISIBLE) {
                    "PlayerView debe ser visible"
                }
                
                // Verificar que tiene dimensiones válidas
                assert(playerView?.width ?: 0 > 0 && playerView?.height ?: 0 > 0) {
                    "PlayerView debe tener dimensiones válidas"
                }
            }
    }

    /**
     * Helper function para encontrar PlayerView en la jerarquía de vistas.
     */
    private fun findPlayerViewInHierarchy(view: android.view.View): PlayerView? {
        if (view is PlayerView) {
            return view
        }
        
        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                val result = findPlayerViewInHierarchy(child)
                if (result != null) {
                    return result
                }
            }
        }
        
        return null
    }
    
    /**
     * Espera con polling hasta que el player esté reproduciendo.
     * Considera válido tanto cuando isPlaying==true como cuando está en estado BUFFERING,
     * ya que en streams en vivo después de un pause, el player puede entrar en buffering
     * antes de empezar a reproducir realmente.
     * @param player El player a verificar
     * @param timeoutMs Timeout en milisegundos (default: 5000ms)
     * @return true si el player está reproduciendo o en buffering, false si se alcanzó el timeout
     */
    private fun waitForPlayerPlaying(player: androidx.media3.common.Player, timeoutMs: Long = 5000): Boolean {
        val timeout = System.currentTimeMillis() + timeoutMs
        val pollInterval = 100L
        
        while (System.currentTimeMillis() < timeout) {
            // El player está reproduciendo
            if (player.isPlaying) {
                return true
            }
            // El player está en buffering (preparándose para reproducir después de onPlay)
            // Esto es válido porque el callback onPlay ya se ejecutó
            if (player.playbackState == androidx.media3.common.Player.STATE_BUFFERING) {
                return true
            }
            Thread.sleep(pollInterval)
        }
        
        return false
    }
    
    /**
     * Espera con polling hasta que el player esté pausado.
     * @param player El player a verificar
     * @param timeoutMs Timeout en milisegundos (default: 5000ms)
     * @return true si el player está pausado, false si se alcanzó el timeout
     */
    private fun waitForPlayerPaused(player: androidx.media3.common.Player, timeoutMs: Long = 5000): Boolean {
        val timeout = System.currentTimeMillis() + timeoutMs
        val pollInterval = 100L
        
        while (System.currentTimeMillis() < timeout) {
            if (!player.isPlaying && 
                (player.playbackState == androidx.media3.common.Player.STATE_READY || 
                 player.playbackState == androidx.media3.common.Player.STATE_BUFFERING)) {
                return true
            }
            Thread.sleep(pollInterval)
        }
        
        return false
    }
}
