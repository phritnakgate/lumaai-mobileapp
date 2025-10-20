package org.bkkz.lumaapp.presentation.main.chat.voice_chat

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import org.bkkz.lumaapp.R

class VoiceChatActivity : AppCompatActivity() {

    //UI
    private lateinit var animationView: LottieAnimationView
    private lateinit var cancelVoice : ImageButton

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechRecognizerIntent: Intent

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startListening()
        } else {
            Toast.makeText(this, "Permission denied. Cannot use voice feature.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_voice_chat)

        findView()
        setupView()
        setupEvents()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun findView(){
        animationView = findViewById(R.id.animationView_voice_chat)
        cancelVoice = findViewById(R.id.imgbtn_voice_chat_cancel)
    }
    private fun setupView(){
        setupSpeechRecognizer()
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
    private fun setupEvents(){
        cancelVoice.setOnClickListener { finish() }
    }

    private fun setupSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech recognition is not available on this device.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this@VoiceChatActivity)
        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "th-TH")
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                animationView.playAnimation()
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                returnResult(null)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    returnResult(matches[0])
                } else {
                    returnResult(null)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startListening() {
        speechRecognizer.startListening(speechRecognizerIntent)
    }

    private fun returnResult(text: String?) {
        val resultIntent = Intent()
        if (text != null) {
            resultIntent.putExtra("VOICE_RESULT", text)
            setResult(RESULT_OK, resultIntent)
        } else {
            setResult(RESULT_CANCELED, resultIntent)
        }
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }

    companion object {
        const val VOICE_RESULT = "VOICE_RESULT"
    }
}