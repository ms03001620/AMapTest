package com.example.amaptest.video;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;

public class CodecChecker {
    private static final String TAG = "CodecChecker";

    public static boolean isHevcDecoderSupported() {
        MediaCodecList codecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        for (MediaCodecInfo codecInfo : codecList.getCodecInfos()) {
            if (!codecInfo.isEncoder()) { // We are looking for a decoder
                for (String type : codecInfo.getSupportedTypes()) {
                    if (type.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_HEVC)) {
                        Log.d(TAG, "HEVC decoder found: " + codecInfo.getName());
                        return true;
                    }
                }
            }
        }
        Log.d(TAG, "No HEVC decoder found on this device.");
        return false;
    }
}
