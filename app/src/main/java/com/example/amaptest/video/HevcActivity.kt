package com.example.amaptest.video

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.example.amaptest.R
import java.io.IOException
import java.nio.ByteBuffer

class HevcActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private var surfaceView: SurfaceView? = null
    private var surfaceHolder: SurfaceHolder? = null
    private var extractor: MediaExtractor? = null
    private var decoder: MediaCodec? = null
    private var videoFormat: MediaFormat? = null
    private var videoTrackIndex = -1
    private var isExtractorEOS = false
    private var isDecoderEOS = false
    private var extractorThread: HandlerThread? = null
    private var extractorHandler: Handler? = null
    private var decoderCallbackThread: HandlerThread? = null
    private var decoderHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hevc)

        surfaceView = findViewById(R.id.surfaceView)
        surfaceHolder = surfaceView?.getHolder()
        surfaceHolder?.addCallback(this)

        extractorThread = HandlerThread("ExtractorThread")
        extractorThread?.start()
        extractorHandler = Handler(extractorThread!!.looper)

        decoderCallbackThread = HandlerThread("DecoderCallbackThread")
        decoderCallbackThread?.start()
        decoderHandler = Handler(decoderCallbackThread!!.looper)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "Surface created.")
        startDecoding(holder.surface, R.raw.video_h265)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(TAG, "Surface changed: " + width + "x" + height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG, "Surface destroyed.")
        stopAndRelease()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAndRelease()
        extractorThread?.quitSafely()
        decoderCallbackThread?.quitSafely()
    }

    private fun startDecoding(surface: Surface, videoResourceId1: Int) {
        extractorHandler?.post {
            if (!setupExtractor(this, videoResourceId1)) {
                Log.e(TAG, "Failed to setup extractor.")
                return@post
            }

            if (videoFormat == null) {
                Log.e(TAG, "Video format not found.")
                return@post
            }
            try {
                decoder =
                    MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC)
                decoder?.setCallback(DecoderCallback(), decoderHandler)
                decoder?.configure(videoFormat, surface, null, 0)
                decoder?.start()
                Log.d(TAG, "Decoder started.")
                isExtractorEOS = false
                isDecoderEOS = false
            } catch (e: IOException) {
                Log.e(TAG, "Error starting decoder", e)
                stopAndRelease()
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Error starting decoder", e)
                stopAndRelease()
            }
        }
    }


    private fun setupExtractor(context: Context, resourceId: Int): Boolean {
        var result = false
        extractor = MediaExtractor()
        var afd: AssetFileDescriptor? = null
        try {
            afd = context.resources.openRawResourceFd(resourceId)
            if (afd == null) {
                Log.e(TAG, "Failed to open raw resource file descriptor for ID: $resourceId")
                return result
            }
            extractor?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)

            val trackCount = extractor!!.trackCount

            for (i in 0 until trackCount) {
                val format = extractor!!.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime != null && mime == MediaFormat.MIMETYPE_VIDEO_HEVC) {
                    videoTrackIndex = i
                    videoFormat = format // Store the format from extractor
                    //videoFormat?.setInteger("rotation-degrees", 90)
                    extractor!!.selectTrack(videoTrackIndex)
                    Log.d(
                        TAG,
                        "Found HEVC track at index $i with format: $videoFormat"
                    )
                    result = true
                }
            }
            Log.e(TAG, "No HEVC video track found in resource ID: $resourceId")
        } catch (e: IOException) {
            Log.e(TAG, "Error setting up MediaExtractor for resource ID: $resourceId", e)
        } finally {
            try {
                afd?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing AssetFileDescriptor", e)
            }
            return result
        }
    }

    private fun stopAndRelease() {
        Log.d(TAG, "Stopping and releasing resources.")
        try {
            decoder?.stop()
        } catch (e: IllegalStateException) {
            Log.w(TAG, "IllegalStateException while stopping decoder: " + e.message)
        } finally {
            try {
                decoder?.release()
            } catch (e: IllegalStateException) {
                Log.w(TAG, "IllegalStateException while releasing decoder: " + e.message)
            }
            decoder = null
        }
        extractor?.release()
        extractor = null
        videoTrackIndex = -1
        isExtractorEOS = true
        isDecoderEOS = true
    }

    private inner class DecoderCallback : MediaCodec.Callback() {
        override fun onInputBufferAvailable(mc: MediaCodec, inputBufferId: Int) {
            if (isExtractorEOS || videoTrackIndex == -1 || extractor == null) {
                if (!isExtractorEOS && mc != null) {
                    try {
                        mc.queueInputBuffer(
                            inputBufferId,
                            0,
                            0,
                            0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                    } catch (e: IllegalStateException) {
                        Log.w(TAG, "Failed to queue EOS on input: " + e.message)
                    }
                }
                return
            }

            var inputBuffer: ByteBuffer? = null
            try {
                inputBuffer = mc.getInputBuffer(inputBufferId)
            } catch (e: IllegalStateException) {
                Log.e(TAG, "onInputBufferAvailable: getInputBuffer failed", e)
                return
            }

            if (inputBuffer == null) {
                Log.w(
                    TAG,
                    "onInputBufferAvailable: getInputBuffer returned null for index $inputBufferId"
                )
                return
            }

            val sampleSize = extractor!!.readSampleData(inputBuffer, 0)

            if (sampleSize < 0) {
                Log.d(TAG, "Extractor EOS. Queuing EOS to decoder.")
                try {
                    mc.queueInputBuffer(
                        inputBufferId,
                        0,
                        0,
                        0,
                        MediaCodec.BUFFER_FLAG_END_OF_STREAM
                    )
                } catch (e: IllegalStateException) {
                    Log.w(TAG, "Failed to queue EOS on input: " + e.message)
                }
                isExtractorEOS = true
            } else {

                val presentationTimeUs = extractor!!.sampleTime
                val flags = extractor!!.sampleFlags
                try {
                    mc.queueInputBuffer(inputBufferId, 0, sampleSize, presentationTimeUs, flags)
                } catch (e: IllegalStateException) {
                    Log.w(TAG, "Failed to queue input buffer: " + e.message)
                }
                extractor?.advance()
            }
        }

        override fun onOutputBufferAvailable(
            mc: MediaCodec,
            outputBufferId: Int,
            bufferInfo: MediaCodec.BufferInfo
        ) {
            if (isDecoderEOS) return

            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.d(TAG, "Decoder output EOS received.")
                isDecoderEOS = true
                runOnUiThread { this@HevcActivity.stopAndRelease() }
                return
            }

            try {
                mc.releaseOutputBuffer(outputBufferId, true)
            } catch (e: IllegalStateException) {
                Log.w(TAG, "Failed to release output buffer: " + e.message)
            }
        }

        override fun onOutputFormatChanged(mc: MediaCodec, format: MediaFormat) {
            Log.d(TAG, "Decoder output format changed: $format")
            videoFormat = format
        }

        override fun onError(mc: MediaCodec, e: MediaCodec.CodecException) {
            Log.e(TAG, "Decoder error: " + e.diagnosticInfo, e)
            isDecoderEOS = true
            runOnUiThread { this@HevcActivity.stopAndRelease() }
        }
    }

    companion object {
        private const val TAG = "H265Demo"
    }
}