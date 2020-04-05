package com.subrasystems.record.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.widget.Toast;

import com.aykuttasil.callrecord.CallRecord;
import com.subrasystems.record.R;
import com.subrasystems.record.utils.ConstantKey;

public class MyService extends Service {

    private NotificationManager mNotificationManager;
    private boolean isRecording;
    private CallRecord mCallRecord;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
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
                stopRecording();
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Record Service Destroyed", Toast.LENGTH_LONG).show();
        super.onDestroy();
    }

    public void startRecording(String number) {
        createNotification();
        // start call recorder
        mCallRecord = new CallRecord.Builder(this)
                .setLogEnable(true)
                .setRecordFileName("RecordFileName")
                .setRecordDirName("RecordDirName")
                .setRecordDirPath(getApplicationContext().getFilesDir().getPath())
                //.setRecordDirPath(Environment.getExternalStorageDirectory().getPath()) // optional & default value
                .setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB) // optional & default value
                .setOutputFormat(MediaRecorder.OutputFormat.AMR_NB) // optional & default value
                .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION) // optional & default value
                .setShowSeed(true) // optional & default value ->Ex: RecordFileName_incoming.amr || RecordFileName_outgoing.amr
                .build();
        mCallRecord.startCallReceiver();
        isRecording = true;
    }

    private void stopRecording() {
        if (isRecording && mCallRecord != null) {
            mCallRecord.stopCallReceiver();
            isRecording = false;
            mNotificationManager.cancel(0);
        }
    }

    public void createNotification() {
        Notification mNotification = new Notification.Builder(this)
                .setContentTitle("Recording started...")
                .setContentText("Recording started...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.notify(0, mNotification);
        }
    }

}