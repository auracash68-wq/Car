package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class SoundEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    var soundEnabled: Boolean = true

    @Volatile
    var musicEnabled: Boolean = true

    @Volatile
    private var appInForeground: Boolean = true

    private var musicJob: Job? = null

    fun setForegroundState(inForeground: Boolean) {
        appInForeground = inForeground
        if (!inForeground) {
            stopMusicLoop()
        } else if (musicEnabled) {
            startMusicLoop()
        }
    }

    fun updateSettings(sound: Boolean, music: Boolean) {
        soundEnabled = sound
        val musicChanged = musicEnabled != music
        musicEnabled = music
        if (musicChanged) {
            if (musicEnabled && appInForeground) {
                startMusicLoop()
            } else {
                stopMusicLoop()
            }
        }
    }

    fun startMusicLoop() {
        if (musicJob?.isActive == true) return
        musicJob = scope.launch {
            val bassNotes = doubleArrayOf(110.0, 110.0, 130.81, 146.83, 110.0, 164.81, 146.83, 130.81)
            var idx = 0
            while (isActive) {
                if (musicEnabled && appInForeground) {
                    val freq = bassNotes[idx % bassNotes.size]
                    playSynthTone(freq = freq, durationMs = 170, volume = 0.08f, harmonic = true)
                    idx++
                }
                delay(240L)
            }
        }
    }

    fun stopMusicLoop() {
        musicJob?.cancel()
        musicJob = null
    }

    fun playButtonClick() {
        if (!soundEnabled || !appInForeground) return
        scope.launch {
            playSynthTone(freq = 740.0, durationMs = 45, volume = 0.18f)
        }
    }

    fun playCoinCollect() {
        if (!soundEnabled || !appInForeground) return
        scope.launch {
            playSynthTone(freq = 987.77, durationMs = 55, volume = 0.22f)
            delay(45)
            playSynthTone(freq = 1318.51, durationMs = 90, volume = 0.24f)
        }
    }

    fun playAcceleratePulse(speedRatio: Float) {
        if (!soundEnabled || !appInForeground) return
        scope.launch {
            val baseFreq = 95.0 + (speedRatio.coerceIn(0f, 1.4f) * 140.0)
            playSynthTone(freq = baseFreq, durationMs = 70, volume = 0.12f, harmonic = true)
        }
    }

    fun playBrakeScreech() {
        if (!soundEnabled || !appInForeground) return
        scope.launch {
            playSynthTone(freq = 1480.0, durationMs = 80, volume = 0.14f, slideFreqEnd = 920.0)
        }
    }

    fun playNitroBoost() {
        if (!soundEnabled || !appInForeground) return
        scope.launch {
            playSynthTone(freq = 220.0, durationMs = 240, volume = 0.25f, slideFreqEnd = 660.0, harmonic = true)
        }
    }

    fun playCollision() {
        if (!soundEnabled || !appInForeground) return
        scope.launch {
            playSynthTone(freq = 125.0, durationMs = 200, volume = 0.32f, slideFreqEnd = 55.0, noiseMix = true)
        }
    }

    fun playVictory() {
        if (!soundEnabled || !appInForeground) return
        scope.launch {
            val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
            for (n in notes) {
                playSynthTone(freq = n, durationMs = 110, volume = 0.25f, harmonic = true)
                delay(95)
            }
        }
    }

    fun playGameOver() {
        if (!soundEnabled || !appInForeground) return
        scope.launch {
            val notes = doubleArrayOf(392.0, 349.23, 311.13, 261.63)
            for (n in notes) {
                playSynthTone(freq = n, durationMs = 140, volume = 0.25f)
                delay(120)
            }
        }
    }

    private fun playSynthTone(
        freq: Double,
        durationMs: Int,
        volume: Float,
        slideFreqEnd: Double = freq,
        harmonic: Boolean = false,
        noiseMix: Boolean = false
    ) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * durationMs / 1000).coerceAtLeast(128)
            val samples = ShortArray(numSamples)
            var phase = 0.0
            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val currentFreq = freq + (slideFreqEnd - freq) * progress
                val envelope = when {
                    progress < 0.1 -> progress / 0.1
                    progress > 0.75 -> (1.0 - progress) / 0.25
                    else -> 1.0
                }
                phase += 2.0 * PI * currentFreq / sampleRate
                var wave = sin(phase)
                if (harmonic) {
                    wave = 0.7 * wave + 0.3 * sin(phase * 2.0)
                }
                if (noiseMix) {
                    val pseudoNoise = sin(i * 19.37) * sin(i * 73.13)
                    wave = 0.5 * wave + 0.5 * pseudoNoise
                }
                val sampleVal = (wave * envelope * volume * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                samples[i] = sampleVal.toShort()
            }

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(samples.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(samples, 0, samples.size)
            track.play()
            Thread.sleep(durationMs.toLong() + 15L)
            track.stop()
            track.release()
        } catch (_: Throwable) {
            // Fail gracefully on environments or unit tests without hardware audio tracks
        }
    }
}
