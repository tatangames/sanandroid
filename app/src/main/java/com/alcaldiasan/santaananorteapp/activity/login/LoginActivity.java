package com.alcaldiasan.santaananorteapp.activity.login;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alcaldiasan.santaananorteapp.R;
import com.alcaldiasan.santaananorteapp.activity.principal.PrincipalActivity;
import com.alcaldiasan.santaananorteapp.fragmentos.login.FragmentLogin;
import com.alcaldiasan.santaananorteapp.network.TokenManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class LoginActivity extends AppCompatActivity {

    private TokenManager tokenManager;
    private static final int SMS_PERMISSION_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));

        // Verificar y solicitar permisos si la versión de Android es inferior a 13



        if (!TextUtils.isEmpty(tokenManager.getToken().getId())) {

            // vista principal
            Intent intent = new Intent(this, PrincipalActivity.class);
            startActivity(intent);
            finish();

        } else {

            // SOLICITUD DE PERMISOS PARA RECIBIR SMS
            if (!hasSmsPermissions()) {
                requestSmsPermissions();
            }

            // vista login
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContenedor, new FragmentLogin())
                    .commit();
        }
    }


    // Método para verificar si los permisos están otorgados
    private boolean hasSmsPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
    }


    // SOLICITUD DE PERMISO
    private void requestSmsPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS)) {
            // MOSTRAR AL USUARIO PORQUE ES NECESARIO EL PERMISO

        }

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS}, SMS_PERMISSION_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
               // PERMISO SMS AUTORIZADO
            }
        }
    }

}