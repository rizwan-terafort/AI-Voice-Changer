package com.voicechanger.funnysound.ui.recorder.preview

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.voicechanger.funnysound.R
import com.voicechanger.funnysound.data.Recording
import com.voicechanger.funnysound.databinding.FragmentPreviewBinding
import java.io.File

class VoicePreviewFragment : Fragment() {

    private var binding : FragmentPreviewBinding? = null

    private var recording: Recording? = null

    private var exoPlayer: ExoPlayer? = null

    private var updateSeekBarRunnable: Runnable? = null
    private val handler = android.os.Handler(Looper.getMainLooper())
    private var isUserSeeking = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPreviewBinding.inflate(inflater,container,false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity?.let { activity->
            getAllRecordings(activity).let { theList ->
                if (theList.isNotEmpty()){
                    val latestRec = theList[0]
                    recording = latestRec
                    playRecording(latestRec.uri)
                }
            }

            clickListeners()
        }
    }

    private fun clickListeners(){
        binding?.apply {
            btnAddMoreEffect.setOnClickListener {
                findNavController().navigate(VoicePreviewFragmentDirections.actionPreviewFragmentToVoiceEffect(recording?.uri.toString(), isFromRecordings = true))
            }
            back.setOnClickListener {
                findNavController().navigate(VoicePreviewFragmentDirections.actionPreviewFragmentHome())
            }
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
                            binding?.seekBar?.max = duration.toInt() // ExoPlayer duration in ms
                            binding?.tvTotal?.text = formatTime(duration.toInt())

                            // Start seekbar updates after player is ready
                            startSeekBarUpdate()
                        }
                    }

                    override fun onPlaybackStateChanged(state: Int) {
                        super.onPlaybackStateChanged(state)
                        if (state == Player.STATE_ENDED) {
                            binding?.seekBar?.progress = 0
                            binding?.tvCurrent?.text = "00:00"
                            binding?.btnPlayPause?.setImageResource(R.drawable.ic_play_32)
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
            binding?.btnPlayPause?.setOnClickListener {
                exoPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.pause()
                        binding?.btnPlayPause?.setImageResource(R.drawable.ic_play_32)
                    } else {
                        player.play()
                        binding?.btnPlayPause?.setImageResource(R.drawable.ic_pause_32)
                    }
                }
            }

            // Seekbar change listener
            binding?.seekBar?.setOnSeekBarChangeListener(object :
                android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: android.widget.SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser && exoPlayer != null) {
                        binding?.tvCurrent?.text = formatTime(progress)
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

    private fun formatTime(ms: Int): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun startSeekBarUpdate() {
        updateSeekBarRunnable = object : Runnable {
            override fun run() {
                exoPlayer?.let { player ->
                    if (!isUserSeeking) {
                        val currentPos = player.currentPosition
                        binding?.seekBar?.progress = currentPos.toInt()
                        binding?.tvCurrent?.text = formatTime(currentPos.toInt())
                    }
                }
                handler.postDelayed(this, 500) // update every 0.5 sec
            }
        }
        handler.post(updateSeekBarRunnable!!)
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

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateSeekBarRunnable ?: return)
        exoPlayer?.release()
        exoPlayer = null
    }
}