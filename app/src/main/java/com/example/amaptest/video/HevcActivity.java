package com.example.amaptest.video;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.amaptest.R;

import java.io.IOException;
import java.nio.ByteBuffer;


public class HevcActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final String TAG = "H265Demo";

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private MediaExtractor extractor;
    private MediaCodec decoder;
    private MediaFormat videoFormat; // Store format from extractor/decoder
    private int videoTrackIndex = -1;
    private boolean isExtractorEOS = false;
    private boolean isDecoderEOS = false; // To track decoder output EOS

    // For running extractor and decoder operations off the main thread
    private HandlerThread extractorThread;
    private Handler extractorHandler;
    private HandlerThread decoderCallbackThread; // For MediaCodec callbacks
    private Handler decoderHandler; // For MediaCodec callbacks


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hevc); // Assuming activity_main.xml has a SurfaceView

        surfaceView = findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        // Initialize HandlerThreads
        extractorThread = new HandlerThread("ExtractorThread");
        extractorThread.start();
        extractorHandler = new Handler(extractorThread.getLooper());

        decoderCallbackThread = new HandlerThread("DecoderCallbackThread");
        decoderCallbackThread.start();
        decoderHandler = new Handler(decoderCallbackThread.getLooper());

        boolean s = CodecChecker.isHevcDecoderSupported();
        System.out.println(s);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Log.d(TAG, "Surface created.");
        // It's better to start decoding when surface is ready and we have a video file.
        // For simplicity, assuming a video file is ready to be loaded.
        // Example: R.raw.sample_hevc_video
        startDecoding(holder.getSurface(), R.raw.video_h265);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "Surface changed: " + width + "x" + height);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Log.d(TAG, "Surface destroyed.");
        stopAndRelease();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAndRelease(); // Ensure cleanup
        if (extractorThread!= null) {
            extractorThread.quitSafely();
        }
        if (decoderCallbackThread!= null) {
            decoderCallbackThread.quitSafely();
        }
    }

    private void startDecoding(Surface surface, int videoResourceId1) {
        extractorHandler.post(() -> { // Run extractor setup on its own thread
            if (!setupExtractor(this, videoResourceId1)) {
                Log.e(TAG, "Failed to setup extractor.");
                return;
            }

            if (videoFormat == null) {
                Log.e(TAG, "Video format not found.");
                return;
            }

            try {
                decoder = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC);
                decoder.setCallback(new DecoderCallback(), decoderHandler); // Use dedicated handler for callbacks
                decoder.configure(videoFormat, surface, null, 0);
                decoder.start();
                Log.d(TAG, "Decoder started.");
                isExtractorEOS = false;
                isDecoderEOS = false;
            } catch (IOException | IllegalStateException e) {
                Log.e(TAG, "Error starting decoder", e);
                stopAndRelease();
            }
        });
    }


    private boolean setupExtractor(Context context, int resourceId) {
        extractor = new MediaExtractor();
        AssetFileDescriptor afd = null;
        try {
            afd = context.getResources().openRawResourceFd(resourceId);
            if (afd == null) {
                Log.e(TAG, "Failed to open raw resource file descriptor for ID: " + resourceId);
                return false;
            }
            extractor.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime != null && mime.equals(MediaFormat.MIMETYPE_VIDEO_HEVC)) {
                    videoTrackIndex = i;
                    videoFormat = format; // Store the format from extractor
                    extractor.selectTrack(videoTrackIndex);
                    Log.d(TAG, "Found HEVC track at index " + i + " with format: " + videoFormat);
                    return true;
                }
            }
            Log.e(TAG, "No HEVC video track found in resource ID: " + resourceId);
            return false;
        } catch (IOException e) {
            Log.e(TAG, "Error setting up MediaExtractor for resource ID: " + resourceId, e);
            return false;
        } finally {
            try {
                if (afd != null) {
                    afd.close(); // Close the file descriptor
                }
            } catch (IOException e) {
                Log.e(TAG, "Error closing AssetFileDescriptor", e);
            }
        }
    }
    private void stopAndRelease() {
        Log.d(TAG, "Stopping and releasing resources.");
        if (decoder!= null) {
            try {
                // It's important to stop() before release()
                // and handle potential IllegalStateExceptions if already released or in wrong state
                decoder.stop();
            } catch (IllegalStateException e) {
                Log.w(TAG, "IllegalStateException while stopping decoder: " + e.getMessage());
            } finally {
                try {
                    decoder.release();
                } catch (IllegalStateException e) {
                    Log.w(TAG, "IllegalStateException while releasing decoder: " + e.getMessage());
                }
                decoder = null;
            }
        }
        if (extractor!= null) {
            extractor.release();
            extractor = null;
        }
        videoTrackIndex = -1;
        isExtractorEOS = true; // Mark as EOS to prevent further processing
        isDecoderEOS = true;
    }

    private class DecoderCallback extends MediaCodec.Callback {
        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec mc, int inputBufferId) {
            if (isExtractorEOS || videoTrackIndex == -1 || extractor == null) {
                // If extractor already sent EOS, or not properly initialized, do nothing or queue EOS again if needed.
                // This check prevents issues if callbacks arrive after cleanup starts.
                if (!isExtractorEOS && mc!= null) { // Check mc to prevent NPE if already released
                    try {
                        mc.queueInputBuffer(inputBufferId, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    } catch (IllegalStateException e) {
                        Log.w(TAG, "Failed to queue EOS on input: " + e.getMessage());
                    }
                }
                return;
            }

            ByteBuffer inputBuffer = null;
            try {
                inputBuffer = mc.getInputBuffer(inputBufferId);
            } catch (IllegalStateException e) {
                Log.e(TAG, "onInputBufferAvailable: getInputBuffer failed", e);
                return;
            }

            if (inputBuffer == null) {
                Log.w(TAG, "onInputBufferAvailable: getInputBuffer returned null for index " + inputBufferId);
                return;
            }

            // Run extractor operations on the extractor thread to avoid blocking callback thread
            // and to ensure thread safety for extractor.
            // However, for simplicity in this example, direct call is shown.
            // A more robust solution would use a message queue or post to extractorHandler.
            int sampleSize = extractor.readSampleData(inputBuffer, 0);

            if (sampleSize < 0) {
                Log.d(TAG, "Extractor EOS. Queuing EOS to decoder.");
                try {
                    mc.queueInputBuffer(inputBufferId, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                } catch (IllegalStateException e) {
                    Log.w(TAG, "Failed to queue EOS on input: " + e.getMessage());
                }
                isExtractorEOS = true;
            } else {
                long presentationTimeUs = extractor.getSampleTime();
                int flags = extractor.getSampleFlags();
                try {
                    mc.queueInputBuffer(inputBufferId, 0, sampleSize, presentationTimeUs, flags);
                } catch (IllegalStateException e) {
                    Log.w(TAG, "Failed to queue input buffer: " + e.getMessage());
                }
                extractor.advance();
            }
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec mc, int outputBufferId, @NonNull MediaCodec.BufferInfo bufferInfo) {
            if (isDecoderEOS) return; // Already handled EOS or cleanup started

            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM)!= 0) {
                Log.d(TAG, "Decoder output EOS received.");
                isDecoderEOS = true;
                // Post to main thread or appropriate handler if UI updates or further cleanup logic needed
                runOnUiThread(HevcActivity.this::stopAndRelease);
                return;
            }

            try {
                // Render the buffer to the surface
                mc.releaseOutputBuffer(outputBufferId, true); // true to render
            } catch (IllegalStateException e) {
                Log.w(TAG, "Failed to release output buffer: " + e.getMessage());
                // Potentially stopAndRelease() here too if error is critical
            }
        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec mc, @NonNull MediaFormat format) {
            Log.d(TAG, "Decoder output format changed: " + format);
            videoFormat = format; // Update with the actual output format from decoder
        }

        @Override
        public void onError(@NonNull MediaCodec mc, @NonNull MediaCodec.CodecException e) {
            Log.e(TAG, "Decoder error: " + e.getDiagnosticInfo(), e);
            isDecoderEOS = true; // Mark as error state
            runOnUiThread(HevcActivity.this::stopAndRelease);
        }
    }
}