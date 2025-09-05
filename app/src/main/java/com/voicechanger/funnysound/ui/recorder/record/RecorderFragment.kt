package com.voicechanger.funnysound.ui.recorder.record

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.voicechanger.funnysound.R
import com.voicechanger.funnysound.databinding.FragmentRecorderBinding
import com.voicechanger.funnysound.ui.recorder.voice_effect.VoicesFragment.VoiceFragmentCallback
import com.voicechanger.funnysound.ui.voice_to_text.VoiceToTextConverter
import com.voicechanger.funnysound.utils.AppUtils
import com.voicechanger.funnysound.utils.PcmToWavConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Locale

class RecorderFragment : Fragment() {

    private var binding: FragmentRecorderBinding? = null
    private var mActivity: FragmentActivity? = null

    private lateinit var audioFile: File
    private var outputFile: String = ""

    private var mediaPlayer: MediaPlayer? = null

    private var isRecording = false
    private var isPaused = false

    private var startRecord = true


    private lateinit var audioRecord: AudioRecord


    private var recordingStartTime: Long = 0L
    private var pausedTime: Long = 0L
    private var isTimerRunning = false

    private var isFromVoiceToText = false

    private var speechRecognizer: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val args = RecorderFragmentArgs.fromBundle(it)
            isFromVoiceToText = args.isFromVoiceToText
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecorderBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity?.let { activity ->
            AppUtils.getMain(activity).hideToolbar()
            AppUtils.getMain(activity).hideBottomNavigationView()

            clickListeners(activity)
            handleBackPress(activity)

            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO), 1001
                )
                return
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1002
                    )
                    return
                }
            }


        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun clickListeners(activity: FragmentActivity) {
        binding?.close?.setOnClickListener {
            goBack(activity)
        }

        binding?.btnRecord?.setOnClickListener {

            if (startRecord) {
                startRecording()
                startRecord = false
            } else if (isPaused) {
                resumeRecording()
            } else if (!isPaused) {
                pauseRecording()
            }
            updateViews()
        }

        binding?.btnRepeat?.setOnClickListener {
            repeatRecording()
        }

        binding?.btnDone?.setOnClickListener {
            stopRecording()
        }

        binding?.tvImport?.setOnClickListener {
            findNavController().navigate(RecorderFragmentDirections.actionRecorderFragmentToRecordingsFragment())
        }
    }

    private fun handleBackPress(activity: FragmentActivity) {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!activity.isFinishing && !activity.isDestroyed) {
                    goBack(activity)
                }

            }
        }
        activity.onBackPressedDispatcher.addCallback(activity, onBackPressedCallback)
    }

    private fun goBack(activity: FragmentActivity){
        findNavController().popBackStack()
        AppUtils.getMain(activity).showToolbar()
        AppUtils.getMain(activity).showBottomNavigationView()
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun startRecording() {

        if (isFromVoiceToText){
            setupSpeechRecognizer()
            startListening()
            startTimer()
        }else{
            isRecording = true
            isPaused = false
            startTimer()
            val sampleRate = 44100
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize
            )

            val fileName = "${System.currentTimeMillis()}.pcm"
            audioFile = File(requireContext().cacheDir, fileName) // Temporary raw PCM file


            audioRecord.startRecording()

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val buffer = ShortArray(bufferSize)
                    val outputStream = FileOutputStream(audioFile)

                    while (isRecording) {
                        if (isPaused) {
                            // Just wait without reading/writing audio
                            Thread.sleep(100)
                            continue
                        }

                        val read = audioRecord.read(buffer, 0, buffer.size)
                        if (read > 0) {
                            val byteBuffer = ByteArray(read * 2)
                            var i = 0
                            for (s in buffer.take(read)) {
                                byteBuffer[i++] = (s.toInt() and 0x00FF).toByte()
                                byteBuffer[i++] = ((s.toInt() shr 8) and 0xFF).toByte()
                            }
                            outputStream.write(byteBuffer)

                            val amplitude = buffer.maxOrNull()?.toInt() ?: 0
                            withContext(Dispatchers.Main) {
                                binding?.barVisualizer?.addAmplitude(amplitude)
                            }
                        }
                    }
                    outputStream.close()
                }
            }
            binding?.btnRecord?.let {
                mActivity?.let { it1 -> Glide.with(it1) }?.load(R.drawable.ic_recording)?.into(it)
            }
        }


    }

    private fun pauseRecording() {
        if (isRecording) {
            isPaused = true
            pauseTimer()
        }
        binding?.btnRecord?.let {
            activity?.let { it1 -> Glide.with(it1) }?.load(R.drawable.ic_paused)?.into(it)
        }
    }

    private fun resumeRecording() {
        if (isPaused) {
            isPaused = false
            startTimer()
        }
    }

    // Stop Recording
    private fun stopRecording() {
        isRecording = false
        audioRecord.stop()
        audioRecord.release()
        resetTimer()

        // Convert PCM → WAV
        val wavFile = File(requireContext().cacheDir, "${System.currentTimeMillis()}.wav")
        PcmToWavConverter.convert(audioFile, wavFile, 44100, 1, 16)

        // Save in your method
        val savedPath = saveAudioToStorage(mActivity!!, wavFile)

        outputFile = savedPath ?: wavFile.absolutePath

        if (isFromVoiceToText){
            resetTimer()
            setFragmentResult("audioRecorded", bundleOf())
            findNavController().popBackStack()
        }else{
            // findNavController().navigate(RecorderFragmentDirections.actionRecorderFragmentToVoiceEffect(savedPath.toString()))
            val bundle = Bundle()
            bundle.putString("audioPath", savedPath.toString())
            findNavController().navigate(R.id.action_global_to_voice_effect_fragment, bundle)
        }


    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun repeatRecording() {
        // Stop current recording if any
        if (isRecording || isPaused) {
            isRecording = false
            isPaused = false
            audioRecord.stop()
            audioRecord.release()
        }

        // Reset timer
        resetTimer()

        // Delete previous temporary audio file
        if (this::audioFile.isInitialized && audioFile.exists()) {
            audioFile.delete()
        }

        // Reset flags
        startRecord = true
        outputFile = ""

        // Update UI
        binding?.btnRecord?.setImageResource(R.drawable.ic_recording)
        binding?.btnDone?.visibility = View.GONE
        binding?.btnRepeat?.visibility = View.GONE

        // Start fresh recording
        //  startRecord = false
        startRecording()
        updateViews()
    }


    // Play Recording
    private fun playRecording() {
        if (outputFile.isNotEmpty()) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(outputFile)
                prepare()
                start()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = requireActivity()
    }

    override fun onDetach() {
        super.onDetach()
        mActivity = null
    }


    fun saveAudioToStorage(context: Context, sourceFile: File): String? {
        val filename = "${System.currentTimeMillis()}.wav"
        var audioUri: Uri? = null
        var audioPath: String? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // ✅ Recordings folder path
                val folderPath = Environment.DIRECTORY_MUSIC + File.separator + "Recordings"

                context.contentResolver?.also { resolver ->
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "audio/wav")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, folderPath)
                        put(MediaStore.Audio.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                    }

                    // ✅ Insert into Audio collection
                    audioUri =
                        resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)

                    audioUri?.let { uri ->
                        resolver.openOutputStream(uri)?.use { outputStream ->
                            FileInputStream(sourceFile).use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }

                        // Approx path (not guaranteed on Q+)
                        audioPath =
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath + "/Recordings/" + filename
                    }
                }
            } else {
                // ✅ For Android 9 and below
                val musicDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                val recordingsDir = File(musicDir, "Recordings")
                if (!recordingsDir.exists()) recordingsDir.mkdirs()

                val audioFile = File(recordingsDir, filename)
                FileInputStream(sourceFile).use { input ->
                    FileOutputStream(audioFile).use { output ->
                        input.copyTo(output)
                    }
                }

                audioUri = Uri.fromFile(audioFile)
                audioPath = audioFile.absolutePath

                // notify gallery/media apps
                MediaScannerConnection.scanFile(
                    context, arrayOf(audioFile.absolutePath), null, null
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return audioPath
    }


    private fun updateViews() {
        if (isPaused) {
            mActivity?.let { activity ->
                binding?.btnRecord?.let {
                    // Glide.with(activity).load(R.drawable.ic_paused).into(it)
                    it.setImageResource(R.drawable.ic_paused)
                }
            }
        } else {
            mActivity?.let { activity ->
                binding?.btnRecord?.let {
                    //   Glide.with(activity).load(R.drawable.ic_recording).into(it)
                    it.setImageResource(R.drawable.ic_recording)
                    binding?.btnDone?.visibility = View.VISIBLE
                    binding?.btnRepeat?.visibility = View.VISIBLE
                }
            }
        }
        if (isFromVoiceToText){
            binding?.btnDone?.visibility = View.GONE
            binding?.btnRepeat?.visibility = View.GONE
        }
    }


    private fun startTimer() {
        recordingStartTime = System.currentTimeMillis() - pausedTime
        isTimerRunning = true

        lifecycleScope.launch {
            while (isTimerRunning) {
                val elapsed = System.currentTimeMillis() - recordingStartTime

                val totalSeconds = elapsed / 1000
                val hours = totalSeconds / 3600
                val minutes = (totalSeconds % 3600) / 60
                val seconds = totalSeconds % 60

                val formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                binding?.tvTime?.text = formattedTime

                delay(500)
            }
        }
    }

    private fun pauseTimer() {
        isTimerRunning = false
        pausedTime = System.currentTimeMillis() - recordingStartTime
    }

    private fun resetTimer() {
        isTimerRunning = false
        pausedTime = 0L
        recordingStartTime = 0L
        binding?.tvTime?.text = "00:00:00"
    }



    private fun setupSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())

            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Toast.makeText(context, "Listening...", Toast.LENGTH_SHORT).show()
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.joinToString(" ") ?: ""
                    // binding?.textView?.text = text
                   // Toast.makeText(mActivity,text, Toast.LENGTH_SHORT).show()
                    VoiceToTextConverter.theText = text
                    findNavController().navigate(RecorderFragmentDirections.actionRecorderToVoiceConverter())
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
        } else {
            Toast.makeText(context, "Speech recognition not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startListening() {
        speechRecognizer?.startListening(recognizerIntent)
    }

}