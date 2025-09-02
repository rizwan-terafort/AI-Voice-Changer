package com.voicechanger.funnysound.ui.text_to_voice

import ai.instavision.ffmpegkit.FFmpegKit
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.voicechanger.funnysound.R
import com.voicechanger.funnysound.databinding.FragmentInputTextBinding
import com.voicechanger.funnysound.ui.home.PrankSoundAdapter
import com.voicechanger.funnysound.utils.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Locale

class InputTextFragment : Fragment() {

    private var tts: TextToSpeech? = null
    private var generatedFile: File? = null

    private var binding : FragmentInputTextBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInputTextBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity?.let { activity->
            AppUtils.getMain(activity).hideToolbar()
            AppUtils.getMain(activity).hideBottomNavigationView()


            val adapter = PrankSoundAdapter(activity)
            binding?.rvVoiceEffects?.adapter = adapter

            initializeTextToSpeech()
            handleBackPress(activity)
            binding?.back?.setOnClickListener {
               goBack(activity)
            }

            binding?.btnGenerate?.setOnClickListener {
                generateVoiceAndSave(mActivity!!, binding?.editext?.text?.toString()?.trim() ?: "This is a dummy text")
            }
        }
    }


    private fun generateVoiceAndSave(context: Context, text: String) {
        val tempFile = File(context.cacheDir, "tts_temp.wav")

        // 🔹 Save TTS to temp file
        tts?.synthesizeToFile(
            text,
            null,
            tempFile,
            "ttsGenerated"
        )

        // 🔹 On completion listener
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}

            override fun onDone(utteranceId: String?) {
                // ✅ Save to Music/Recordings
                val finalPath = saveAudioToStorage(context, tempFile)

                finalPath?.let {
                    // Ab fragment me bhejo

                    val bundle = Bundle()
                    bundle.putString("audioPath", it.toString())
                  //  bundle.putBoolean("isFromRecordings", true)
                    findNavController().navigate(R.id.action_global_to_voice_effect_fragment, bundle)
                }
            }

            override fun onError(utteranceId: String?) {
                Toast.makeText(mActivity,"errro", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initializeTextToSpeech(){
        tts = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
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

    private var mActivity: FragmentActivity? = null
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
}