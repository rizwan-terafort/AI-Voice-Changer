package com.voicechanger.funnysound.ui.voice_to_text

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.voicechanger.funnysound.data.Recording
import com.voicechanger.funnysound.databinding.FragmentVoiceToTextConverterBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File


import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.coroutines.resumeWithException
import com.voicechanger.funnysound.R
import com.voicechanger.funnysound.utils.SpeechRecognizerHelper


//import org.vosk.Model
//import org.vosk.Recognizer
//import org.vosk.android.StorageService


class VoiceToTextConverter : Fragment() {

    private var binding : FragmentVoiceToTextConverterBinding? = null


    companion object{
        var theText = "No Result"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVoiceToTextConverterBinding.inflate(inflater,container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity?.let { activity ->

            clickListeners(activity)

            binding?.tvText?.text = theText

//            getAllRecordings(activity).let { theList ->
//                val record = theList[0]
//                lifecycleScope.launch {
//                    val path = getFilePathFromContentUri(record.uri, activity)
//                    val text = convertSpeechToText(activity, path.toString())
//                    Log.d("VOSK", text.toString())
//                   // Toast.makeText(activity, text, Toast.LENGTH_LONG).show()
//                    binding?.tvText?.text = text
//                }
//            }

        }
    }


    private fun clickListeners(activity: FragmentActivity){
        binding?.back?.setOnClickListener {
            findNavController().navigate(R.id.action_global_to_home_fragment)
        }

        binding?.home?.setOnClickListener {
            findNavController().navigate(R.id.action_global_to_home_fragment)
        }

        binding?.btnCopy?.setOnClickListener {
            val textToCopy = binding?.tvText?.text.toString()
            val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", textToCopy)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(activity, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
        }
    }

    fun getFilePathFromContentUri(contentUri: Uri, context: Context): String? {
        try {
            var filePath: String? = null
            // Query the media store for the file path associated with the content URI
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val cursor: Cursor? =
                context.contentResolver.query(contentUri, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                filePath = cursor.getString(columnIndex)
                cursor.close()
            }
            return filePath
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    fun copyAssetFolder(context: Context, assetFolderName: String, outDir: File) {
        val assetManager = context.assets
        val files = assetManager.list(assetFolderName) ?: return
        if (!outDir.exists()) outDir.mkdirs()

        for (file in files) {
            val path = "$assetFolderName/$file"
            val outFile = File(outDir, file)

            val subFiles = assetManager.list(path)
            if (subFiles?.isNotEmpty() == true) {
                copyAssetFolder(context, path, outFile) // recursion for folders
            } else {
                assetManager.open(path).use { inStream ->
                    FileOutputStream(outFile).use { outStream ->
                        val buffer = ByteArray(1024)
                        var read: Int
                        while (inStream.read(buffer).also { read = it } != -1) {
                            outStream.write(buffer, 0, read)
                        }
                    }
                }
            }
        }
    }

 /*   suspend fun initModel(context: Context): Model =
        suspendCancellableCoroutine { cont ->
            val modelPath = File(context.filesDir, "model").absolutePath
            StorageService.unpack(context, "model", modelPath,
                { model ->
                    cont.resume(model) {}
                },
                { exception ->
                    cont.resumeWithException(exception)
                }
            )
        }

    suspend fun convertSpeechToText(
        context: Context,
        audioFilePath: String
    ): String = withContext(Dispatchers.IO) {
        var transcription = ""
        try {
            // Model init with StorageService
            val model = initModel(context)
            val recognizer = Recognizer(model, 16000.0f)

            FileInputStream(audioFilePath).use { input ->
                val buffer = ByteArray(4096)
                var read: Int
                while (input.read(buffer).also { read = it } >= 0) {
                    if (recognizer.acceptWaveForm(buffer, read)) {
                        val resultJson = recognizer.result
                        transcription += extractText(resultJson) + " "
                    }
                }
            }

            transcription += extractText(recognizer.finalResult)

            recognizer.close()
            model.close()
        } catch (e: Exception) {
            transcription = "Error: ${e.message}"
        }
        transcription.trim()
    }*/


    fun extractText(resultJson: String): String {
        return try {
            val json = JSONObject(resultJson)
            json.optString("text", "")
        } catch (e: Exception) {
            ""
        }
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