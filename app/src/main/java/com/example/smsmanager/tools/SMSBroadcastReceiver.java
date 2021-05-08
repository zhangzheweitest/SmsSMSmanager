package com.example.smsmanager.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SMSBroadcastReceiver extends BroadcastReceiver {


    private String smsSender = "";
    private String smsBody = "";
    private static SmsListener smsListener;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Telephony.Sms.Intents.SMS_DELIVER_ACTION)) {


            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {

                smsSender = smsMessage.getDisplayOriginatingAddress();
                smsBody += smsMessage.getDisplayMessageBody();
            }


            smsListener.onTextReceived(smsSender, smsBody);

        }
    }

    public static void setListener(SmsListener listener) {
        smsListener = listener;
    }

    public interface SmsListener {
        void onTextReceived(String address, String msg);
    }
}