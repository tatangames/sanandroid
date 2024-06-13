package com.alcaldiasan.santaananorteapp.extras;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.onesignal.Continue;
import com.onesignal.OneSignal;

public class SistemaController extends Application {


    // PRIMEROS AJUSTES DE APLICACION CUANDO SE INICIA


    // ID DE API
    private static final String ONESIGNAL_APP_ID = "68e96d5e-4852-405c-bf5c-e81168b05174";


    @Override
    public void onCreate() {
        super.onCreate();

        // OneSignal Initialization
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);

        // requestPermission will show the native Android notification permission prompt.
        // NOTE: It's recommended to use a OneSignal In-App Message to prompt instead.
        OneSignal.getNotifications().requestPermission(true, Continue.with(r -> {
            if (r.isSuccess()) {
                if (r.getData()) {
                    // `requestPermission` completed successfully and the user has accepted permission
                }
                else {
                    // `requestPermission` completed successfully but the user has rejected permission
                }
            }
            else {
                // `requestPermission` completed unsuccessfully, check `r.getThrowable()` for more info on the failure reason
            }
        }));
    }
}
