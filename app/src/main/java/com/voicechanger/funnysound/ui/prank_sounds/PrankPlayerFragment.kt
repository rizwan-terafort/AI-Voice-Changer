package com.voicechanger.funnysound.ui.prank_sounds

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.voicechanger.funnysound.R
import com.voicechanger.funnysound.common.getAllVoiceEffects
import com.voicechanger.funnysound.data.VoiceEffect
import com.voicechanger.funnysound.databinding.FragmentPrankPlayerBinding
import com.voicechanger.funnysound.utils.AppUtils
import com.voicechanger.funnysound.utils.PrankSoundClickListener
import java.io.File
import java.io.FileOutputStream

class PrankPlayerFragment : Fragment(), PrankSoundClickListener {
    private var binding : FragmentPrankPlayerBinding? = null

    private var adapter : PrankPlayerAdapter ? = null

    private var exoPlayer: ExoPlayer? = null

    private var updateSeekBarRunnable: Runnable? = null
    private val handler = android.os.Handler(Looper.getMainLooper())
    private var isUserSeeking = false

    private var position = 0
    private var isFromHome = false

    private var isPlaying = false

    private var currentTime = 5
    private var sessionStartTime = 0L // to measure how much played in looping case
    private var elapsedPlayTime = 0L // total played time in ms

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val args= PrankPlayerFragmentArgs.fromBundle(it)
            position = args.position
            isFromHome = args.isFromHome
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPrankPlayerBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity?.let { activity ->


            if (isFromHome){
                AppUtils.getMain(activity).hideToolbar()
                AppUtils.getMain(activity).hideBottomNavigationView()
            }

            exoPlayer = ExoPlayer.Builder(activity).build()
            playSound(position)
            handleBackPress(activity)

            binding?.back?.setOnClickListener {
                goBack(activity)
            }
            clickListeners(activity)
            adjustVolume()
            setUpRecyclerview(activity)
        }
    }

    private fun setUpRecyclerview(activity: FragmentActivity){
         adapter = PrankPlayerAdapter(
            activity,
            getAllVoiceEffects(),
            this@PrankPlayerFragment
        )


        binding?.rvPrankSound?.adapter = adapter
        adapter?.setTheSelectedPosition(position)
    }

    private fun adjustVolume(){
        binding?.seekBar?.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                val volume = progress / 100f
                exoPlayer?.volume = volume
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
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

    override fun onPrankSoundClick(
        position: Int,
        item: VoiceEffect
    ) {

        adapter?.setTheSelectedPosition(position)
        playSound(position)
        isPlaying = true
        binding?.btnplay?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_20, 0, 0, 0)
        binding?.btnplay?.text = getString(R.string.pause)
    }


    private fun playSound(position: Int) {
        try {
            binding?.seekBar?.progress = 50
            isPlaying = true
            val pos = position+1
            val bgPath =  copyAssetToCache(requireContext(), "bgmusics/$pos.mp3")
            val bgItem = MediaItem.fromUri(Uri.fromFile(File(bgPath)))

            // Release the current player if it exists
            exoPlayer?.release()

            exoPlayer = ExoPlayer.Builder(requireContext()).build().apply {
                setMediaItem(bgItem)
                prepare()
                repeatMode = Player.REPEAT_MODE_ONE

                // Listen for when the player is ready
                addListener(object : Player.Listener {
                    @OptIn(UnstableApi::class)
                    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                        super.onPlayerStateChanged(playWhenReady, playbackState)
                        if (playbackState == Player.STATE_READY) {
                            // Once the player is ready, set the seekbar max value and start syncing it
                           // binding?.seekBar?.max = duration.toInt() // ExoPlayer duration in ms


                            // Start seekbar updates after player is ready
                            startSeekBarUpdate()
                        }
                    }

                    override fun onPlaybackStateChanged(state: Int) {
                        super.onPlaybackStateChanged(state)
                        if (state == Player.STATE_ENDED) {
                          //  binding?.seekBar?.progress = 0
                          //  binding?.btnPlayPause?.setImageResource(R.drawable.ic_play_32)
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
            binding?.btnplay?.setOnClickListener {
                exoPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.pause()
                        isPlaying = false
                        binding?.btnplay?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_20, 0, 0, 0)
                        binding?.btnplay?.text = getString(R.string.Play)

                    } else {
                        player.play()
                        isPlaying = true
                        binding?.btnplay?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_20, 0, 0, 0)
                        binding?.btnplay?.text = getString(R.string.pause)
                    }
                }

            }



        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error playing audio", Toast.LENGTH_SHORT).show()
        }
    }


    private fun startSeekBarUpdate() {
        elapsedPlayTime = 0
        sessionStartTime = System.currentTimeMillis()

        updateSeekBarRunnable = object : Runnable {
            override fun run() {
                exoPlayer?.let { player ->
                    if (!isUserSeeking) {
                        val currentPos = player.currentPosition
                      //  binding?.seekBar?.progress = currentPos.toInt()

                        if (currentTime > 0) {
                            val limitMs = currentTime * 1000L
                            elapsedPlayTime = System.currentTimeMillis() - sessionStartTime

                            if (elapsedPlayTime >= limitMs) {
                                stopPlayer()
                                return
                            }
                        } else if (currentTime == -1) {
                            // Infinite loop
                            if (currentPos >= player.duration) {
                                player.seekTo(0)
                                player.playWhenReady = true
                            }
                        }
                    }
                }
                handler.postDelayed(this, 200)
            }
        }
        handler.post(updateSeekBarRunnable!!)
    }

    private fun stopPlayer() {
        exoPlayer?.pause()
        isPlaying = false
        elapsedPlayTime = 0
        sessionStartTime = 0
     //   binding?.seekBar?.progress = 0
        binding?.btnplay?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_20, 0, 0, 0)
        binding?.btnplay?.text = getString(R.string.Play)
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

    private fun goBack(activity: FragmentActivity){
        if (isFromHome){
            AppUtils.getMain(activity).showToolbar()
            AppUtils.getMain(activity).showBottomNavigationView()
        }
        findNavController().popBackStack()

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

    private fun clickListeners(activity: FragmentActivity){
        binding?.txt5s?.setOnClickListener {

            currentTime = 5
            binding?.txt5s?.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.blue1)

            binding?.txt15s?.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.gray02)
            binding?.txt30s?.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.gray02)
            binding?.txt1m?.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.gray02)
            binding?.txtloop?.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.gray02)
        }

        binding?.txt15s?.setOnClickListener {

            currentTime = 15
            binding?.txt15s?.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.blue1)

            binding?.txt5s?.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.gray02)
            binding?.txt30s?.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.gray02)
            binding?.txt1m?.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.gray02)
            binding?.txtloop?.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.gray02)
        }

        binding?.txt30s?.setOnClickListener {

            currentTime = 30
            binding?.txt30s?.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.blue1)

            binding?.txt5s?.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.gray02)
            binding?.txt15s?.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.gray02)
            binding?.txt1m?.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.gray02)
            binding?.txtloop?.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.gray02)
        }

        binding?.txt1m?.setOnClickListener {

            currentTime = 60
            binding?.txt1m?.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.blue1)

            binding?.txt5s?.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.gray02)
            binding?.txt15s?.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.gray02)
            binding?.txt30s?.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.gray02)
            binding?.txtloop?.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.gray02)
        }

        binding?.txtloop?.setOnClickListener {

            currentTime = -1
            binding?.txtloop?.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.blue1)

            binding?.txt5s?.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.gray02)
            binding?.txt15s?.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.gray02)
            binding?.txt30s?.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.gray02)
            binding?.txt1m?.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.gray02)
        }
    }


}