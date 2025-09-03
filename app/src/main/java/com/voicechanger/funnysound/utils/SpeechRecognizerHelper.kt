package com.voicechanger.funnysound.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

object SpeechRecognizerHelper {

    private var speechRecognizer: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null

    // Store last recognized text
    private var lastResult: String? = null

    fun init(context: Context) {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US") // ya "ur-PK" if Urdu chahiye
            }

            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    lastResult = "Error code: $error"
                }

                override fun onResults(results: Bundle?) {
                    val matches =
                        results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    lastResult = matches?.joinToString(" ") ?: ""
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    fun startListening() {
        speechRecognizer?.startListening(recognizerIntent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
    }

    fun getResult(): String? {
        return lastResult
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        recognizerIntent = null
        lastResult = null
    }
}

