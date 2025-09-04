package com.voicechanger.funnysound.utils

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object ReverseHelper {

    fun createReversedTempFile(context: Context, inputPath: String): File {
        val inputFile = File(inputPath)

        // Temp file in cacheDir
        val tempFile = File.createTempFile("reverse_", ".wav", context.cacheDir)

        FileInputStream(inputFile).use { fis ->
            FileOutputStream(tempFile).use { fos ->

                // WAV header copy
                val header = ByteArray(44)
                fis.read(header)
                fos.write(header)

                // Audio data read
                val data = fis.readBytes()
                val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
                val shortArray = ShortArray(data.size / 2)
                buffer.asShortBuffer().get(shortArray)

                // Reverse samples
                shortArray.reverse()

                // Back to bytes
                val outBuffer = ByteBuffer.allocate(data.size).order(ByteOrder.LITTLE_ENDIAN)
                outBuffer.asShortBuffer().put(shortArray)

                fos.write(outBuffer.array())
            }
        }

        return tempFile
    }
}
