package com.voicechanger.funnysound.ui.prank_sounds

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Looper
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
import com.voicechanger.funnysound.R
import com.voicechanger.funnysound.databinding.FragmentPrankPlayerBinding
import java.io.File
import java.io.FileOutputStream

class PrankPlayerFragment : Fragment() {
    private var binding : FragmentPrankPlayerBinding? = null

    private var exoPlayer: ExoPlayer? = null

    private var updateSeekBarRunnable: Runnable? = null
    private val handler = android.os.Handler(Looper.getMainLooper())
    private var isUserSeeking = false

    private var position = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val args= PrankPlayerFragmentArgs.fromBundle(it)
            position = args.position
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

            exoPlayer = ExoPlayer.Builder(activity).build()
            playSound(position)
        }
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


    private fun playSound(position: Int) {
        try {
            val pos = position+1
            val bgPath = if (position!=0) copyAssetToCache(requireContext(), "bgmusics/$pos.mp3") else ""
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
                            binding?.seekBar?.max = duration.toInt() // ExoPlayer duration in ms


                            // Start seekbar updates after player is ready
                            startSeekBarUpdate()
                        }
                    }

                    override fun onPlaybackStateChanged(state: Int) {
                        super.onPlaybackStateChanged(state)
                        if (state == Player.STATE_ENDED) {
                            binding?.seekBar?.progress = 0
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
                   //     binding?.btnPlayPause?.setImageResource(R.drawable.ic_play_32)
                    } else {
                        player.play()
                   //     binding?.btnPlayPause?.setImageResource(R.drawable.ic_pause_32)
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
                        binding?.seekBar?.progress = currentPos.toInt()
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