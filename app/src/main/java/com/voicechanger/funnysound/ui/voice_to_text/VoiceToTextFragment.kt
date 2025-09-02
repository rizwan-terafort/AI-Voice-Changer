package com.voicechanger.funnysound.ui.voice_to_text

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.voicechanger.funnysound.data.Recording
import com.voicechanger.funnysound.databinding.FragmentVoiceToTextBinding
import com.voicechanger.funnysound.utils.AppUtils
import java.io.File

class VoiceToTextFragment : Fragment() {
    private var binding : FragmentVoiceToTextBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVoiceToTextBinding.inflate(inflater,container,false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity?.let { activity ->

            AppUtils.getMain(activity).hideToolbar()
            AppUtils.getMain(activity).hideBottomNavigationView()


            getAllRecordings(activity).let { theList ->
                for (record in theList) {

                }
            }

            handleBackPress(activity)
            setViewsVisibility()
            binding?.back?.setOnClickListener {
                goBack(activity)
            }
        }
    }

    fun setViewsVisibility(){
        binding?.imgTextToVoice?.visibility = View.VISIBLE
        binding?.recordButton?.visibility = View.VISIBLE
        binding?.view3?.visibility = View.VISIBLE

        binding?.playerRecordervoice?.root?.visibility = View.GONE
        binding?.view1?.visibility = View.GONE
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