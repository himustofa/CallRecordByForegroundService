package com.subrasystems.record.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.subrasystems.record.utils.ConstantKey;

public class CallBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "CallReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        /*final String action = intent != null ? intent.getAction() : null;
        if (action != null && intent.getExtras() != null) {
            TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephony != null) {
                telephony.listen(new MyPhoneStateListener(context), PhoneStateListener.LISTEN_CALL_STATE);
            }
        }*/

        final String action = intent != null ? intent.getAction() : null;
        if (action != null && intent.getExtras() != null) {
            try {
                String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                String mPhoneNumber = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

                if (mPhoneNumber != null && intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) { //"android.intent.action.NEW_OUTGOING_CALL"
                    mPhoneNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
                }

                if (mPhoneNumber != null) {
                    Log.d(TAG, mPhoneNumber);
                } else {
                    Log.d(TAG, "Phone number is null");
                }

                if (state != null && state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {
                    startMyService(context, ConstantKey.RINGING_CALL, mPhoneNumber);
                }
                if (state != null && state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    startMyService(context, ConstantKey.RECEIVED_CALL, mPhoneNumber);
                }
                if (state != null && state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE)) {
                    //startService(context, ConstantKey.END_CALL, mPhoneNumber);
                    stopMyService(context);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startMyService(Context context, String key, String value) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, new Intent(context, CallForegroundService.class).putExtra(key, value)); //ForegroundService
        } else {
            context.startService(new Intent(context, CallForegroundService.class).putExtra(key, value)); //BackgroundService
        }
    }

    private void stopMyService(Context context) {
        context.stopService(new Intent(context, CallForegroundService.class)); //ForegroundService
    }

    /*class MyPhoneStateListener extends PhoneStateListener {
        private Context mContext;

        MyPhoneStateListener(Context context) {
            this.mContext = context;
        }
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Intent intent = new Intent(mContext, CallRecordingService.class);
                    intent.putExtra("number", incomingNumber);
                    mContext.startService(intent);
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    break;
            }
        }

    }*/
}
