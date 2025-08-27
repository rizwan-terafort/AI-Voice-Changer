package com.voicechanger.funnysound.ui.recorder.voice_effect

import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.voicechanger.funnysound.data.Recording
import com.voicechanger.funnysound.databinding.FragmentVoiceEffectBinding
import java.io.File
import com.voicechanger.funnysound.R
import com.voicechanger.funnysound.ui.MainPagerAdapter
import com.voicechanger.funnysound.ui.home.HomeFragment

class VoiceEffectFragment : Fragment() {
    private var binding : FragmentVoiceEffectBinding? = null
    private var mActivity: FragmentActivity? = null

    private var audioPath : String? = null
    private var recording : Recording? = null

    private var mediaPlayer: MediaPlayer? = null
    private var updateSeekBarRunnable: Runnable? = null
    private val handler = android.os.Handler()
    private var isUserSeeking = false

    private var isFromRecordings = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val args = VoiceEffectFragmentArgs.fromBundle(it)
            audioPath = args.audioPath
            isFromRecordings = args.isFromRecordings
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVoiceEffectBinding.inflate(inflater,container,false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity?.let { activity->
           getAllRecordings(activity).let { theList->
               for (record in theList){
                   if (isFromRecordings){
                       if (record.uri.toString()== audioPath){
                           recording = record
                           break
                       }
                   }else{
                       if (getFilePathFromContentUri(record.uri , activity)== audioPath){
                           recording = record
                           break
                       }
                   }

               }
               playRecording(recording?.uri!!)
           }

            clickListeners()

            val adapter = VoicePagerAdapter(childFragmentManager, lifecycle)
            adapter.addFragment(VoicesFragment())
            adapter.addFragment(VoicesFragment())
            binding?.viewPager?.adapter = adapter
            binding?.viewPager?.isUserInputEnabled = false


        }
    }


    private fun playRecording(uri: Uri) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(requireContext(), uri)
                prepare()
                start()

                // Seekbar setup
                binding?.seekBar?.max = duration
                binding?.tvTotal?.text = formatTime(duration)

                // SeekBar update runnable
                updateSeekBarRunnable = object : Runnable {
                    override fun run() {
                        if (mediaPlayer != null && !isUserSeeking) {
                            val currentPos = mediaPlayer!!.currentPosition
                            binding?.seekBar?.progress = currentPos
                            binding?.tvCurrent?.text = formatTime(currentPos)
                        }
                        handler.postDelayed(this, 500) // update every 0.5 sec
                    }
                }
                handler.post(updateSeekBarRunnable!!)
            }

            // Seekbar change listener
            binding?.seekBar?.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser && mediaPlayer != null) {
                        binding?.tvCurrent?.text = formatTime(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {
                    isUserSeeking = true
                }

                override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                    isUserSeeking = false
                    mediaPlayer?.seekTo(seekBar?.progress ?: 0)
                }
            })

            // Play/Pause button
            binding?.btnPlayPause?.setOnClickListener {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        mp.pause()
                        binding?.btnPlayPause?.setImageResource(R.drawable.ic_play_32)
                    } else {
                        mp.start()
                        binding?.btnPlayPause?.setImageResource(R.drawable.ic_pause_32)
                    }
                }
            }

            // Reset UI when audio completes
            mediaPlayer?.setOnCompletionListener {
                binding?.seekBar?.progress = 0
                binding?.tvCurrent?.text = "00:00"
                binding?.btnPlayPause?.setImageResource(R.drawable.ic_play_32)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error playing audio", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clickListeners(){
        binding.apply {
            this?.back?.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    private fun formatTime(ms: Int): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
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
        }catch (e:Exception){
            e.printStackTrace()
            return ""
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

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateSeekBarRunnable ?: return)
        mediaPlayer?.release()
        mediaPlayer = null
    }


    inner class VoicePagerAdapter(
        fragmentManager: FragmentManager, lifecycle: Lifecycle
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {

        private val fragmentList = arrayListOf<Fragment>()


        override fun getItemCount(): Int {
            return fragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> VoicesFragment()
                1 -> VoicesFragment()

                else -> VoicesFragment()
            }
        }
        fun addFragment(fragment: Fragment) {
            fragmentList.add(fragment)
        }

    }
}