package com.subrasystems.record.utils;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class AudioRecorder {

    private CallBackListener mListener;
    private MediaRecorder mRecorder = new MediaRecorder();
    private String mPath;

    public interface CallBackListener {
        void getPath(String path);
    }

    /**
     * Creates a new audio recording at the given path (relative to root of SD card).
     */
    public AudioRecorder(String path, CallBackListener listener) {
        this.mListener = listener;
        this.mPath = sanitizePath(path);
    }

    private String sanitizePath(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.contains(".")) {
            path += ".3gp";
        }
        return Environment.getExternalStorageDirectory().getAbsolutePath() + path;
    }

    /**
     * Starts a new recording.
     */
    public void start() throws IOException {
        String state = Environment.getExternalStorageState();
        if(!state.equals(Environment.MEDIA_MOUNTED))  {
            throw new IOException("SD Card is not mounted.  It is " + state + ".");
        }

        // make sure the directory we plan to store the recording in exists
        File directory = new File(mPath).getParentFile();
        if (directory != null && !directory.exists() && !directory.mkdirs()) {
            throw new IOException("Path to file could not be created.");
        }

        mRecorder.reset();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile(mPath);
        mRecorder.prepare();
        mRecorder.start();
        mListener.getPath(mPath);
    }

    /**
     * Stops a recording that has been previously started.
     */
    public void stop() throws IOException {
        //mRecorder.stop();
        //mRecorder.release();
        if (mRecorder != null) {
            mRecorder.stop(); //error happening here
            mRecorder.reset(); // You can reuse the object by going back to setAudioSource() step
            mRecorder.release();
            mRecorder = null;
        }
    }

}
