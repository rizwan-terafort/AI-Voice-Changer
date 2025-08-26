package com.voicechanger.funnysound.ui.recorder

import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.voicechanger.funnysound.data.Recording
import com.voicechanger.funnysound.databinding.FragmentRecordingsBinding
import android.os.Environment
import android.media.MediaMetadataRetriever
import java.io.File

class RecordingsFragment : Fragment() {
    private var mActivity: FragmentActivity? = null

    private var binding: FragmentRecordingsBinding? = null

    private val REQUEST_READ_STORAGE = 100


    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (requireContext().checkSelfPermission(android.Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO),
                    REQUEST_READ_STORAGE
                )
            } else {
                loadRecordings()
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requireContext().checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_STORAGE
                )
            } else {
                loadRecordings()
            }
        } else {
            loadRecordings()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadRecordings()
            } else {
                // Permission denied, show message
                Toast.makeText(requireContext(), "Storage permission required to load recordings", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecordingsBinding.inflate(inflater, container, false)

        return binding?.root
    }

    private fun loadRecordings(){
        val recordings = getAllRecordings(requireContext())
        val adapter = RecordingAdapter(recordings) { recording ->
            // play audio
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(requireContext(), recording.uri)
                prepare()
                start()
            }
        }

        binding?.rvRecordings?.layoutManager = LinearLayoutManager(requireContext())
        binding?.rvRecordings?.adapter = adapter

        if (recordings.isEmpty()){
            binding?.clNoRecordings?.visibility = View.VISIBLE
            binding?.rvRecordings?.visibility = View.GONE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity.let { activity->

            checkPermission()

            binding?.clNoRecordings?.setOnClickListener {
                findNavController().popBackStack()
            }
            binding?.back?.setOnClickListener {
                findNavController().popBackStack()
            }
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

                    val contentUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString())

                    recordings.add(Recording(name, size, duration, contentUri))
                }
            }
        } else {
            // Android 9 and below: list files directly under Music/Recordings
            val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            val recordingsDir = File(musicDir, "Recordings")
            if (recordingsDir.exists() && recordingsDir.isDirectory) {
                recordingsDir.listFiles()?.sortedByDescending { it.lastModified() }?.forEach { file ->
                    if (file.isFile) {
                        val uri = Uri.fromFile(file)
                        val size = file.length()
                        val name = file.name
                        val duration = try {
                            val retriever = MediaMetadataRetriever()
                            retriever.setDataSource(file.absolutePath)
                            val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                            retriever.release()
                            durStr?.toLongOrNull() ?: 0L
                        } catch (_: Exception) { 0L }
                        recordings.add(Recording(name, size, duration, uri))
                    }
                }
            }
        }

        return recordings
    }



    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = requireActivity()
    }

    override fun onDetach() {
        super.onDetach()
        mActivity = null
    }
}