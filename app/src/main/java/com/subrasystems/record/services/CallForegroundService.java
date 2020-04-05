package com.subrasystems.record.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.subrasystems.record.utils.ConstantKey;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CallForegroundService extends Service {

    private static final String TAG = "ForegroundService";
    private boolean isStarted;
    private MediaRecorder mRecorder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getExtras() != null) {
            if (intent.getExtras().containsKey(ConstantKey.RINGING_CALL)) {
                Toast.makeText(this, "RingingCall", Toast.LENGTH_SHORT).show();
            } else if (intent.getExtras().containsKey(ConstantKey.RECEIVED_CALL)) {
                final String number = intent.getStringExtra(ConstantKey.RECEIVED_CALL);
                if (number != null) {
                    startRecording(number);
                }
                Toast.makeText(this, "ReceivedCall", Toast.LENGTH_SHORT).show();
            } else if (intent.getExtras().containsKey(ConstantKey.END_CALL)) {
                String number = intent.getStringExtra(ConstantKey.END_CALL);
                Toast.makeText(this, "End/MissedCall " + number, Toast.LENGTH_SHORT).show();
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecording();
    }

    private void startRecording(String number) {
        try {
            String path = getApplicationContext().getFilesDir().getPath();
            //String selectedPath = Environment.getExternalStorageDirectory() + "/Testing";
            //String selectedPath = Environment.getExternalStorageDirectory().getAbsolutePath() +"/Android/data/" + packageName + "/system_sound";

            File file = new File(path);
            if (!file.exists()){
                file.mkdirs();
            }

            mRecorder = new MediaRecorder();
            mRecorder.reset();

            //android.permission.MODIFY_AUDIO_SETTINGS
            AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE); //turn on speaker
            if (mAudioManager != null) {
                mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION); // MODE_IN_COMMUNICATION OR MODE_IN_CALL
                mAudioManager.setSpeakerphoneOn(true);
                mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0); // increase Volume
                hasWiredHeadset(mAudioManager);
            }

            //android.permission.RECORD_AUDIO
            String manufacturer = Build.MANUFACTURER;
            Log.d(TAG, manufacturer);
            /*if (manufacturer.toLowerCase().contains("samsung")) {
                mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
            } else {
                mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
            }*/
            mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION); //MIC | VOICE_COMMUNICATION | VOICE_RECOGNITION | VOICE_UPLINK | VOICE_DOWNLINK
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); //THREE_GPP | MPEG_4
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); //AMR_NB | AAC
            String mFilePath = file + "/" + "REC_" + number + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".3gp"; //.3gp | .mp3
            mRecorder.setOutputFile(mFilePath);
            mRecorder.prepare();
            mRecorder.start();
            isStarted = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if (isStarted && mRecorder != null) {
            mRecorder.stop();
            mRecorder.reset(); // You can reuse the object by going back to setAudioSource() step
            mRecorder.release();
            mRecorder = null;
            isStarted = false;
        }
    }

    // To detect the connected other device like headphone, wifi headphone, usb headphone etc
    private boolean hasWiredHeadset(AudioManager mAudioManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return mAudioManager.isWiredHeadsetOn();
        } else {
            final AudioDeviceInfo[] devices = mAudioManager.getDevices(AudioManager.GET_DEVICES_ALL);
            for (AudioDeviceInfo device : devices) {
                final int type = device.getType();
                if (type == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
                    Log.d(TAG, "hasWiredHeadset: found wired headset");
                    return true;
                } else if (type == AudioDeviceInfo.TYPE_USB_DEVICE) {
                    Log.d(TAG, "hasWiredHeadset: found USB audio device");
                    return true;
                } else if (type == AudioDeviceInfo.TYPE_TELEPHONY) {
                    Log.d(TAG, "hasWiredHeadset: found audio signals over the telephony network");
                    return true;
                }
            }
            return false;
        }
    }
}
