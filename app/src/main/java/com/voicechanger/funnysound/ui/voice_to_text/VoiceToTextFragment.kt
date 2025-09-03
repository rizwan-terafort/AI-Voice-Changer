package com.voicechanger.funnysound.ui.voice_to_text

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.voicechanger.funnysound.R
import com.voicechanger.funnysound.data.Recording
import com.voicechanger.funnysound.databinding.FragmentVoiceToTextBinding
import com.voicechanger.funnysound.ui.recorder.record.RecorderFragment
import com.voicechanger.funnysound.utils.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.StorageService
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Locale
import kotlin.coroutines.resumeWithException

class VoiceToTextFragment : Fragment() {
    private var binding : FragmentVoiceToTextBinding? = null

    private var exoPlayer: ExoPlayer? = null
    private var updateSeekBarRunnable: Runnable? = null
    private val handler = android.os.Handler(Looper.getMainLooper())
    private var isUserSeeking = false

    private var speechRecognizer: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVoiceToTextBinding.inflate(inflater,container,false)
        return binding?.root
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
                    Toast.makeText(mActivity,text, Toast.LENGTH_SHORT).show()
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity?.let { activity ->

            AppUtils.getMain(activity).hideToolbar()
            AppUtils.getMain(activity).hideBottomNavigationView()

//            setupSpeechRecognizer()
//            startListening()

            exoPlayer = ExoPlayer.Builder(activity).build()


            clickListeners(activity)
            handleBackPress(activity)
            setViewsVisibility(false)


            parentFragmentManager.setFragmentResultListener("audioRecorded", this) { _, bundle ->
               setViewsVisibility(true)
                getAllRecordings(activity).let { theList ->
                val record = theList[0]

                    playRecording(record.uri)

//                lifecycleScope.launch {
//                    val path = getFilePathFromContentUri(record.uri, activity)
//                     val text = convertSpeechToText(activity, path.toString())
//                    Log.d("VOSK", text.toString())
//                     Toast.makeText(activity, text, Toast.LENGTH_LONG).show()
//                }
            }
            }
        }
    }

    private fun clickListeners(activity: FragmentActivity){
        binding?.recordButton?.setOnClickListener {
            findNavController().navigate(VoiceToTextFragmentDirections.actionVoiceToTextToRecorderFragment(true))
        }


        binding?.playerRecordervoice?.btnCross?.setOnClickListener {
            exoPlayer?.pause()
            setViewsVisibility(false)
        }

        binding?.back?.setOnClickListener {
            goBack(activity)
        }

        binding?.btnGenerate?.setOnClickListener {
            findNavController().navigate(VoiceToTextFragmentDirections.actionVoiceToTextToVoiceConverter())
        }
    }

    private fun playRecording(uri: Uri) {
        try {
            // Release the current player if it exists
            exoPlayer?.release()
            exoPlayer = ExoPlayer.Builder(requireContext()).build().apply {
                val mediaItem = MediaItem.fromUri(uri)
                setMediaItem(mediaItem)
                prepare()
                repeatMode = Player.REPEAT_MODE_ONE

                // Listen for when the player is ready
                addListener(object : Player.Listener {
                    @OptIn(UnstableApi::class)
                    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                        super.onPlayerStateChanged(playWhenReady, playbackState)
                        if (playbackState == Player.STATE_READY) {
                            // Once the player is ready, set the seekbar max value and start syncing it
                            binding?.playerRecordervoice?.seekBar?.max = duration.toInt() // ExoPlayer duration in ms
                            binding?.playerRecordervoice?.tvTotal?.text = formatTime(duration.toInt())

                            // Start seekbar updates after player is ready
                            startSeekBarUpdate()
                        }
                    }

                    override fun onPlaybackStateChanged(state: Int) {
                        super.onPlaybackStateChanged(state)
                        if (state == Player.STATE_ENDED) {
                            binding?.playerRecordervoice?.seekBar?.progress = 0
                            binding?.playerRecordervoice?.tvCurrent?.text = "00:00"
                            binding?.playerRecordervoice?.btnPlayPause?.setImageResource(R.drawable.ic_play_32)
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        Toast.makeText(requireContext(), "Error playing audio", Toast.LENGTH_SHORT)
                            .show()
                    }
                })

                playWhenReady = true // Start playback immediately when ready
            }

            // Play/Pause button
            binding?.playerRecordervoice?.btnPlayPause?.setOnClickListener {
                exoPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.pause()
                        binding?.playerRecordervoice?.btnPlayPause?.setImageResource(R.drawable.ic_play_32)
                    } else {
                        player.play()
                        binding?.playerRecordervoice?.btnPlayPause?.setImageResource(R.drawable.ic_pause_32)
                    }
                }
            }

            // Seekbar change listener
            binding?.playerRecordervoice?.seekBar?.setOnSeekBarChangeListener(object :
                android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: android.widget.SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser && exoPlayer != null) {
                        binding?.playerRecordervoice?.tvCurrent?.text = formatTime(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {
                    isUserSeeking = true
                }

                override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                    isUserSeeking = false
                    exoPlayer?.seekTo(seekBar?.progress?.toLong() ?: 0L)
                }
            })

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error playing audio", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startSeekBarUpdate() {
        updateSeekBarRunnable = object : Runnable {
            override fun run() {
                exoPlayer?.let { player ->
                    if (!isUserSeeking) {
                        val currentPos = player.currentPosition
                        binding?.playerRecordervoice?.seekBar?.progress = currentPos.toInt()
                        binding?.playerRecordervoice?.tvCurrent?.text = formatTime(currentPos.toInt())
                    }
                }
                handler.postDelayed(this, 500) // update every 0.5 sec
            }
        }
        handler.post(updateSeekBarRunnable!!)
    }

    private fun formatTime(ms: Int): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }


    fun setViewsVisibility(isRecorded : Boolean){
        if (!isRecorded){
            binding?.imgTextToVoice?.visibility = View.VISIBLE
            binding?.recordButton?.visibility = View.VISIBLE
            binding?.view3?.visibility = View.VISIBLE

            binding?.playerRecordervoice?.root?.visibility = View.GONE
            binding?.view1?.visibility = View.GONE
        }else{
            binding?.imgTextToVoice?.visibility = View.GONE
            binding?.recordButton?.visibility = View.GONE
            binding?.view3?.visibility = View.GONE

            binding?.playerRecordervoice?.root?.visibility = View.VISIBLE
            binding?.view1?.visibility = View.VISIBLE
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
        AppUtils.getMain(activity).showToolbar()
        AppUtils.getMain(activity).showBottomNavigationView()
        findNavController().popBackStack()
    }


    fun getAllRecordings(context: Context): List<Recording> {
        val recordings = mutableListOf<Recording>()

        val targetRelativePath = Environment.DIRECTORY_MUSIC + File.separator + "Recordings"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.RELATIVE_PATH
            )

            val selection = MediaStore.Audio.Media.RELATIVE_PATH + " LIKE ?"
            val selectionArgs = arrayOf("$targetRelativePath%")
            val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

            val query = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            query?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val size = cursor.getLong(sizeColumn)
                    val duration = cursor.getLong(durationColumn)

                    val contentUri = Uri.withAppendedPath(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id.toString()
                    )

                    recordings.add(Recording(name, size, duration, contentUri))
                }
            }
        } else {
            // Android 9 and below: list files directly under Music/Recordings
            val musicDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            val recordingsDir = File(musicDir, "Recordings")
            if (recordingsDir.exists() && recordingsDir.isDirectory) {
                recordingsDir.listFiles()?.sortedByDescending { it.lastModified() }
                    ?.forEach { file ->
                        if (file.isFile) {
                            val uri = Uri.fromFile(file)
                            val size = file.length()
                            val name = file.name
                            val duration = try {
                                val retriever = MediaMetadataRetriever()
                                retriever.setDataSource(file.absolutePath)
                                val durStr =
                                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                retriever.release()
                                durStr?.toLongOrNull() ?: 0L
                            } catch (_: Exception) {
                                0L
                            }
                            recordings.add(Recording(name, size, duration, uri))
                        }
                    }
            }
        }

        return recordings
    }


    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateSeekBarRunnable ?: return)
        exoPlayer?.release()
        exoPlayer = null
    }

     private var mActivity: FragmentActivity? = null
          override fun onAttach(context: Context) {
             super.onAttach(context)
             mActivity = requireActivity()

         }

         override fun onDetach() {
             super.onDetach()
             mActivity = null
         }

}