package com.alcaldiasan.santaananorteapp.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alcaldiasan.santaananorteapp.R;
import com.alcaldiasan.santaananorteapp.activity.principal.PrincipalActivity;
import com.alcaldiasan.santaananorteapp.fragmentos.login.FragmentLogin;
import com.alcaldiasan.santaananorteapp.network.TokenManager;

public class LoginActivity extends AppCompatActivity {


    private TokenManager tokenManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));

        if(!TextUtils.isEmpty(tokenManager.getToken().getId())){

            // vista principal
            Intent intent = new Intent(this, PrincipalActivity.class);
            startActivity(intent);
            finish();

        }else {

            // vista login
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContenedor, new FragmentLogin())
                    .commit();
        }
    }
}