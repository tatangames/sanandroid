package com.alcaldiasan.santaananorteapp.extras;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.alcaldiasan.santaananorteapp.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsReceiver extends BroadcastReceiver {

    // creating a variable for a message listener interface on below line.
    private static MessageListenerInterface mListener;
    @Override
    public void onReceive(Context context, Intent intent) {
        // getting bundle data on below line from intent.
        Bundle data = intent.getExtras();
        // creating an object on below line.
        Object[] pdus = (Object[]) data.get("pdus");

        // running for loop to read the sms on below line.
        for (int i = 0; i < pdus.length; i++) {
            // getting sms message on below line.
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
            // Use the correct createFromPdu method depending on the Android version

            String texto = context.getString(R.string.codigo_para_nortego);

            // Expresión regular para extraer el código de 6 dígitos
            Pattern pattern = Pattern.compile(texto + " " + "(\\d{6})");
            Matcher matcher = pattern.matcher(smsMessage.getMessageBody());
            if (matcher.find()) {
                String code = matcher.group(1);

                mListener.messageReceived(code);
            }
        }
    }
    // on below line we are binding the listener.
    public static void bindListener(MessageListenerInterface listener) {
        mListener = listener;
    }
}