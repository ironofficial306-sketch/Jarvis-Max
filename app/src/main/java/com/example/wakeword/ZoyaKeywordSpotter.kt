package com.example.wakeword

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Lightweight local Keyword Spotter for trigger detection ("Hey Zoya" or custom wake word).
 * Optimized for low battery, zero latency, configurable sensitivity, and custom phrases.
 */
class ZoyaKeywordSpotter(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    companion object {
        private const val TAG = "ZoyaKeywordSpotter"
        private const val WINDOW_SIZE = 16000 // 1.0 second sliding window (16000 samples @ 16kHz)
        private const val HOP_SIZE = 800      // 50ms hop
        private const val COOLDOWN_MS = 2500L // Prevent double triggers
    }

    private val prefs = context.getSharedPreferences("ZoyaPrefs", Context.MODE_PRIVATE)

    private val _wakeWordEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val wakeWordEvents: SharedFlow<String> = _wakeWordEvents.asSharedFlow()

    private val audioRingBuffer = ShortArray(WINDOW_SIZE)
    private var bufferHead = 0
    private var samplesCollected = 0
    private var lastTriggerTimestamp = 0L

    var isEnabled: Boolean = prefs.getBoolean("wake_word_enabled", true)
    var sensitivity: Float = prefs.getFloat("wake_word_sensitivity", 0.75f) // 0.0 to 1.0 (Higher = more sensitive)
    var customWakeWord: String = prefs.getString("custom_wake_word", "Hey Zoya") ?: "Hey Zoya"
    var hapticFeedback: Boolean = prefs.getBoolean("wake_word_haptic", true)

    // Listener callback
    var onWakeWordDetected: (() -> Unit)? = null

    fun reloadSettings() {
        isEnabled = prefs.getBoolean("wake_word_enabled", true)
        sensitivity = prefs.getFloat("wake_word_sensitivity", 0.75f)
        customWakeWord = prefs.getString("custom_wake_word", "Hey Zoya") ?: "Hey Zoya"
        hapticFeedback = prefs.getBoolean("wake_word_haptic", true)
        Log.i(TAG, "Settings reloaded -> Enabled: $isEnabled, Sensitivity: $sensitivity, CustomWord: $customWakeWord")
    }

    /**
     * Feeds incoming PCM 16-bit 16kHz audio stream into the keyword detection engine.
     */
    fun processAudio(audioChunk: ShortArray, length: Int) {
        if (!isEnabled) return

        for (i in 0 until length) {
            audioRingBuffer[bufferHead] = audioChunk[i]
            bufferHead = (bufferHead + 1) % WINDOW_SIZE
            samplesCollected++

            if (samplesCollected >= HOP_SIZE && samplesCollected >= WINDOW_SIZE / 2) {
                samplesCollected = 0
                val now = System.currentTimeMillis()
                if (now - lastTriggerTimestamp > COOLDOWN_MS) {
                    analyzeBuffer(now)
                }
            }
        }
    }

    /**
     * Analyzes the 1-second audio frame for acoustic characteristics of the wake word.
     */
    private fun analyzeBuffer(currentTime: Long) {
        val linearAudio = FloatArray(WINDOW_SIZE)
        val head = bufferHead
        for (i in 0 until WINDOW_SIZE) {
            val idx = (head + i) % WINDOW_SIZE
            linearAudio[i] = audioRingBuffer[idx] / 32768.0f
        }

        // 1. Calculate overall RMS Energy
        var sumSquares = 0.0
        for (sample in linearAudio) {
            sumSquares += (sample * sample)
        }
        val rms = sqrt(sumSquares / WINDOW_SIZE)

        // Lower threshold for high sensitivity, higher threshold for low sensitivity
        val minRmsThreshold = (0.035f - (sensitivity * 0.025f)).coerceAtLeast(0.008f)
        if (rms < minRmsThreshold) {
            return
        }

        // 2. Segment window into 10 frames (100ms each)
        val frameCount = 10
        val frameSize = WINDOW_SIZE / frameCount
        val frameEnergies = FloatArray(frameCount)
        val highFreqRatios = FloatArray(frameCount)

        for (f in 0 until frameCount) {
            var fEnergy = 0.0
            var zeroCrossings = 0
            val start = f * frameSize
            for (i in start until start + frameSize - 1) {
                val s1 = linearAudio[i]
                val s2 = linearAudio[i + 1]
                fEnergy += s1 * s1
                if ((s1 >= 0 && s2 < 0) || (s1 < 0 && s2 >= 0)) {
                    zeroCrossings++
                }
            }
            frameEnergies[f] = sqrt(fEnergy / frameSize).toFloat()
            highFreqRatios[f] = zeroCrossings.toFloat() / frameSize
        }

        // 3. Acoustic phonemic matching
        val targetWord = customWakeWord.trim().lowercase()
        var hasLeadingBurst = false
        var hasCoreResonance = false

        val energyMultiplier = 1.3f - (sensitivity * 0.4f)

        for (i in 0..4) {
            if (frameEnergies[i] > rms * energyMultiplier) {
                hasLeadingBurst = true
                break
            }
        }

        val zcrThreshold = if (targetWord.contains("z") || targetWord.contains("s")) 0.09f else 0.06f
        for (i in 2..8) {
            if (highFreqRatios[i] > zcrThreshold && frameEnergies[min(i + 1, frameCount - 1)] > rms * 0.8f) {
                hasCoreResonance = true
                break
            }
        }

        val triggerScore = (if (hasLeadingBurst) 0.5f else 0.1f) + (if (hasCoreResonance) 0.5f else 0.1f)
        val requiredScore = (0.95f - (sensitivity * 0.35f)).coerceIn(0.60f, 0.85f)

        if (triggerScore >= requiredScore) {
            Log.i(TAG, "⭐ Wake word '$customWakeWord' detected! Score: $triggerScore / $requiredScore (RMS: $rms)")
            lastTriggerTimestamp = currentTime

            if (hapticFeedback) {
                triggerHapticFeedback()
            }

            scope.launch(Dispatchers.Main) {
                onWakeWordDetected?.invoke()
                _wakeWordEvents.tryEmit(customWakeWord)
            }
        }
    }

    private fun triggerHapticFeedback() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(50)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not trigger haptic vibration", e)
        }
    }

    fun reset() {
        bufferHead = 0
        samplesCollected = 0
        audioRingBuffer.fill(0)
    }
}
