package com.voicechanger.funnysound.ui.recorder.voice_effect

import ai.instavision.ffmpegkit.FFmpegKit
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.voicechanger.funnysound.R
import com.voicechanger.funnysound.data.Recording
import com.voicechanger.funnysound.data.VoiceEffect
import com.voicechanger.funnysound.databinding.FragmentVoiceEffectBinding
import com.voicechanger.funnysound.ui.recorder.voice_effect.bg_music.BgMusicFragment
import com.voicechanger.funnysound.utils.toFloatValue
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class VoiceEffectFragment : Fragment(), VoicesFragment.VoiceFragmentCallback,
    BgMusicFragment.BgMusicVolumeCallback {
    private var binding: FragmentVoiceEffectBinding? = null
    private var mActivity: FragmentActivity? = null

    private var audioPath: String? = null
    private var recording: Recording? = null

    private var isFromRecordings = false

    private var bgPlayer: ExoPlayer? = null

    private var exoPlayer: ExoPlayer? = null
    private var updateSeekBarRunnable: Runnable? = null
    private val handler = android.os.Handler(Looper.getMainLooper())
    private var isUserSeeking = false

    private var selectedPitch = 1.0f
    private var selectedSpeed = 1.0f

    private var isBgMusicAdded = false

    private var isEffectApplied = false

    val savingDialog by lazy {
        Dialog(mActivity!!)
    }

    val saveWithoutEffectDialog by lazy {
        Dialog(mActivity!!)
    }

    val discardDialog by lazy {
        Dialog(mActivity!!)
    }

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
        binding = FragmentVoiceEffectBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity?.let { activity ->
            exoPlayer = ExoPlayer.Builder(activity).build()
            getAllRecordings(activity).let { theList ->
                for (record in theList) {
                    if (isFromRecordings) {
                        if (record.uri.toString() == audioPath) {
                            recording = record
                            break
                        }
                    } else {
                        if (getFilePathFromContentUri(record.uri, activity) == audioPath) {
                            recording = record
                            break
                        }
                    }

                }
                playRecording(recording?.uri!!)
            }

            clickListeners(activity)

            val adapter = VoicePagerAdapter(childFragmentManager, lifecycle)
            adapter.addFragment(VoicesFragment())
            adapter.addFragment(BgMusicFragment())
            binding?.viewPager?.adapter = adapter
            binding?.viewPager?.isUserInputEnabled = false

            binding?.btnSave?.setOnClickListener {
                if (isEffectApplied) {
                    pausePlayer()
                    showSavingDialog(activity)
                    val voicePath = if (isFromRecordings) getFilePathFromContentUri(
                        audioPath?.toUri()!!,
                        requireContext()
                    ) else audioPath
                    //   val voicePath = getFilePathFromContentUri(audioPath?.toUri()!!,requireContext())
                    mixAndSaveWithPitchSpeed(
                        requireContext(),
                        voicePath.toString(),
                        "bgmusics/bgmusic.mp3"
                    )
                } else {
                    showSaveWithoutEffectDialog(activity)
                }

            }

        }
    }

    private fun pausePlayer() {
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                binding?.btnPlayPause?.setImageResource(R.drawable.ic_play_32)
            } else {
                player.play()
                binding?.btnPlayPause?.setImageResource(R.drawable.ic_pause_32)
            }
        }
        bgPlayer?.let { bgPlayer ->
            if (bgPlayer.isPlaying) {
                bgPlayer.pause()
            } else {
                if (isBgMusicAdded) {
                    bgPlayer.play()
                }
            }
        }
    }

    fun copyAssetToFile(context: Context, assetPath: String, outFile: File): String {
        context.assets.open(assetPath).use { input ->
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
        }
        return outFile.absolutePath
    }

    fun mixAndSaveWithPitchSpeed(
        context: Context,
        voicePath: String,
        assetBgPath: String, // "bgmusics/rain.wav"
    ) {
        // 1️⃣ Copy BG music from assets
        val bgMusicFile = File(context.cacheDir, "bg_temp.wav")
        copyAssetToFile(context, assetBgPath, bgMusicFile)

        // 2️⃣ Temp output file
        val currentName = "${System.currentTimeMillis()}"
        val tempOutput = File(context.cacheDir, "$currentName.wav")


        val command = arrayOf(
            "-i", voicePath,
            "-i", bgMusicFile.absolutePath,
            "-filter_complex",
            "[0:a]asetrate=44100*${selectedPitch},atempo=${selectedSpeed}[voice];" +
                    "[voice][1:a]amix=inputs=2:duration=first:dropout_transition=0",
            "-c:a", "pcm_s16le",
            tempOutput.absolutePath
        )

        // 4️⃣ Run
        FFmpegKit.executeAsync(command.joinToString(" ")) { session ->
            val returnCode = session.returnCode
            if (returnCode.isValueSuccess) {
                val finalPath = saveAudioToStorage(context, tempOutput)
                (context as? Activity)?.runOnUiThread {
                    savingDialog.dismiss()
                    //  Toast.makeText(context, "Saved at: $finalPath", Toast.LENGTH_LONG).show()
                    Toast.makeText(
                        context,
                        mActivity?.getString(R.string.audio_saved),
                        Toast.LENGTH_LONG
                    ).show()
                    findNavController().navigate(VoiceEffectFragmentDirections.actionVoiceEffectToPreviewFragment())
                }
            } else {
                (context as? Activity)?.runOnUiThread {
                    savingDialog.dismiss()
                    Toast.makeText(context, "Mixing failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun saveAudioToStorage(context: Context, sourceFile: File): String? {
        val filename = "mixed-${System.currentTimeMillis()}.wav"
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
                        audioPath = Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                            .absolutePath + "/Recordings/" + filename
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
                    context,
                    arrayOf(audioFile.absolutePath),
                    null,
                    null
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return audioPath
    }

    private fun changePitchOfSound(speed: Float, pitch: Float) {
        mActivity?.let { activity ->
            try {

                selectedPitch = pitch
                selectedSpeed = speed

                // Get the current URI of the audio
                val uri = if (isFromRecordings) {
                    getFilePathFromContentUri(audioPath?.toUri()!!, activity)
                } else {
                    audioPath
                }
                if (uri != null) {
                    // Set the media item and prepare only if it's the first time
                    if (exoPlayer?.isPlaying != true) {
                        val mediaItem = MediaItem.fromUri(uri)
                        exoPlayer?.setMediaItem(mediaItem)
                        exoPlayer?.prepare()
                    }
                    // Set new playback parameters (change pitch and speed)
                    //   val playbackParameters = PlaybackParameters(1.2f, 1.5f)  // Speed = 1.2x, Pitch = 1.5x
                    val playbackParameters =
                        PlaybackParameters(speed, pitch)  // Speed = 1.2x, Pitch = 1.5x
                    exoPlayer?.playbackParameters = playbackParameters
                    // Continue playing from the current position without resetting
                    exoPlayer?.play()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun startBgMusic(position: Int) {
        isBgMusicAdded = true
        if (bgPlayer == null) {
            bgPlayer = ExoPlayer.Builder(requireContext()).build()

        }
            // Copy bg music from assets → cache (ExoPlayer can’t play assets directly)

            val bgPath = if (position!=0) copyAssetToCache(requireContext(), "bgmusics/$position.mp3") else ""
            val bgItem = MediaItem.fromUri(Uri.fromFile(File(bgPath)))

            bgPlayer?.setMediaItem(bgItem)
            //  bgPlayer?.volume = 0.3f // keep background soft
            bgPlayer?.repeatMode = Player.REPEAT_MODE_ONE
            bgPlayer?.prepare()
       // }
        bgPlayer?.playWhenReady = true
    }

    private fun stopBgMusic() {
        bgPlayer?.pause()
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
                bgPlayer?.let { bgPlayer ->
                    if (bgPlayer.isPlaying) {
                        bgPlayer.pause()
                    } else {
                        if (isBgMusicAdded) {
                            bgPlayer.play()
                        }
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

    // Function to start updating the SeekBar
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


    override fun onValuesAdjusted(
        position: Int, speedProgress: Int, pitchProgress: Int
    ) {
        val actualSpeed = speedProgress.toFloatValue()
        val actualPitch = pitchProgress.toFloatValue()
        changePitchOfSound(actualSpeed, actualPitch)
        isEffectApplied = true
    }

    override fun onItemClick(
        position: Int,
        item: VoiceEffect
    ) {
        //here change the pitch
        changePitchOfSound(item.speed, item.pitch)
        isEffectApplied = true
        binding?.btnPlayPause?.setImageResource(R.drawable.ic_pause_32)
    }


    private fun clickListeners(activity: FragmentActivity) {
        binding.apply {
            this?.back?.setOnClickListener {
                if (isEffectApplied) {
                    showDiscardDialog(activity)
                } else {
                    findNavController().popBackStack()
                }

            }

            this?.tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    when (tab.position) {
                        0 -> {
                            // First tab clicked (Change Voice)
                            binding?.viewPager?.currentItem = 0
                        }

                        1 -> {
                            // Second tab clicked (Background Effects)
                            binding?.viewPager?.currentItem = 1
                        }
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    // optional
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                    // fires again if user clicks the same tab twice
                }
            })
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


    override fun onMusicVolumeChanged(
        progress: Int,
        item: VoiceEffect
    ) {
        //change bg music.
     //   bgPlayer?.volume = progress.toFloat()
        val volume = progress / 100f
        bgPlayer?.volume = volume
    }

    override fun onMusicItemClick(position: Int) {
        isEffectApplied = true
        startBgMusic(position)
        exoPlayer?.play()
        binding?.btnPlayPause?.setImageResource(R.drawable.ic_pause_32)
    }


    fun copyAssetToCache(context: Context, assetFileName: String): String {
        val cacheFile = File(context.cacheDir, assetFileName.substringAfterLast("/"))
        if (!cacheFile.exists()) {
            context.assets.open(assetFileName).use { input ->
                FileOutputStream(cacheFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
        return cacheFile.absolutePath
    }


    fun showSavingDialog(context: FragmentActivity) {

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_saving, null)
        savingDialog.setContentView(view)
        savingDialog.setCancelable(false)
        //  Set rounded background to dialog window
        savingDialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.bg_rounded_constraintlayout
            )
        )
        //  Set the dialog width to match_parent after setting content
        val marginInDp = 16
        val displayMetrics = context.resources.displayMetrics
        val marginInPx = (marginInDp * displayMetrics.density).toInt()
        val screenWidth = displayMetrics.widthPixels
        val dialogWidth = screenWidth - (marginInPx * 2) // Subtract margin from both sides
        savingDialog.window?.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)


        savingDialog.show()
    }

    fun showDiscardDialog(context: FragmentActivity) {

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_discard, null)
        discardDialog.setContentView(view)
        discardDialog.setCancelable(false)
        //  Set rounded background to dialog window
        discardDialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.bg_rounded_constraintlayout
            )
        )
        //  Set the dialog width to match_parent after setting content
        val marginInDp = 16
        val displayMetrics = context.resources.displayMetrics
        val marginInPx = (marginInDp * displayMetrics.density).toInt()
        val screenWidth = displayMetrics.widthPixels
        val dialogWidth = screenWidth - (marginInPx * 2) // Subtract margin from both sides
        discardDialog.window?.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)

        val btnExit = discardDialog.findViewById<TextView>(R.id.btn_exit)
        val btnContinueEditing = discardDialog.findViewById<TextView>(R.id.btn_continue_editing)

        btnContinueEditing.setOnClickListener {
            discardDialog.dismiss()
        }

        btnExit.setOnClickListener {
            discardDialog.dismiss()
            findNavController().popBackStack()
        }

        discardDialog.show()
    }


    fun showSaveWithoutEffectDialog(context: FragmentActivity) {

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_warning, null)
        saveWithoutEffectDialog.setContentView(view)
        saveWithoutEffectDialog.setCancelable(false)
        //  Set rounded background to dialog window
        saveWithoutEffectDialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.bg_rounded_constraintlayout
            )
        )
        //  Set the dialog width to match_parent after setting content
        val marginInDp = 16
        val displayMetrics = context.resources.displayMetrics
        val marginInPx = (marginInDp * displayMetrics.density).toInt()
        val screenWidth = displayMetrics.widthPixels
        val dialogWidth = screenWidth - (marginInPx * 2) // Subtract margin from both sides
        saveWithoutEffectDialog.window?.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)

        val btnSave = saveWithoutEffectDialog.findViewById<TextView>(R.id.btn_save)
        val btnContinueEditing =
            saveWithoutEffectDialog.findViewById<TextView>(R.id.btn_continue_editing)

        btnContinueEditing.setOnClickListener {
            saveWithoutEffectDialog.dismiss()
        }

        btnSave.setOnClickListener {
            saveWithoutEffectDialog.dismiss()

            pausePlayer()
            showSavingDialog(context)
            val voicePath = if (isFromRecordings) getFilePathFromContentUri(
                audioPath?.toUri()!!,
                requireContext()
            ) else audioPath
            //   val voicePath = getFilePathFromContentUri(audioPath?.toUri()!!,requireContext())
            mixAndSaveWithPitchSpeed(requireContext(), voicePath.toString(), "bgmusics/bgmusic.mp3")
        }

        saveWithoutEffectDialog.show()
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
        exoPlayer?.release()
        exoPlayer = null
        bgPlayer?.release()
        bgPlayer = null
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
                1 -> BgMusicFragment()

                else -> VoicesFragment()
            }
        }

        fun addFragment(fragment: Fragment) {
            fragmentList.add(fragment)
        }

    }


}