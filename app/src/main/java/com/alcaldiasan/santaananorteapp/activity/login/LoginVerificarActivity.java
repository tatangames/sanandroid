package com.alcaldiasan.santaananorteapp.activity.login;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alcaldiasan.santaananorteapp.R;
import com.alcaldiasan.santaananorteapp.activity.principal.PrincipalActivity;
import com.alcaldiasan.santaananorteapp.network.ApiService;
import com.alcaldiasan.santaananorteapp.network.RetrofitBuilder;
import com.alcaldiasan.santaananorteapp.network.TokenManager;
import com.developer.kalert.KAlertDialog;

import es.dmoral.toasty.Toasty;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class LoginVerificarActivity extends AppCompatActivity {

    private EditText codeEditText;
    private TextView txtContador;
    private ProgressBar progressBar;
    private TokenManager tokenManager;
    private RelativeLayout rootRelative;
    private CountDownTimer countDownTimer;
    private ConstraintLayout vistaContraint;
    private static final long TIMER_DURATION = 5000; // 60 seconds
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ApiService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_verificar);

        codeEditText = findViewById(R.id.codeEditText);
        txtContador = findViewById(R.id.txtContador);
        vistaContraint = findViewById(R.id.vistaConstraint);
        rootRelative = findViewById(R.id.root);

        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));
        service = RetrofitBuilder.createServiceNoAuth(ApiService.class);

        int colorProgress = ContextCompat.getColor(this, R.color.barraProgreso);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        rootRelative.addView(progressBar, params);
        // Aplicar el ColorFilter al Drawable del ProgressBar
        progressBar.getIndeterminateDrawable().setColorFilter(colorProgress, PorterDuff.Mode.SRC_IN);
        progressBar.setVisibility(View.GONE);


        codeEditText.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se necesita acción aquí
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) {
                    return;
                }

                isUpdating = true;

                // Limpia los caracteres que no sean dígitos
                String cleanString = s.toString().replaceAll("[^\\d]", "");

                // Construye la nueva cadena con el espacio después del cuarto dígito
                StringBuilder formattedString = new StringBuilder();
                for (int i = 0; i < cleanString.length(); i++) {
                    if (i == 3) {
                        formattedString.append(" ");
                    }
                    formattedString.append(cleanString.charAt(i));
                }

                // Actualiza el texto en el EditText
                codeEditText.setText(formattedString.toString());
                codeEditText.setSelection(formattedString.length());

                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No se necesita acción aquí
            }
        });

        apiInformacion();
    }

    private void apiInformacion(){

        progressBar.setVisibility(View.VISIBLE);

        String telefono = codeEditText.getText().toString();

        compositeDisposable.add(
                service.verificacionTelefono(telefono)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .retry()
                        .subscribe(apiRespuesta -> {

                                    progressBar.setVisibility(View.GONE);

                                    if(apiRespuesta != null) {

                                        if(apiRespuesta.getSuccess() == 1) {
                                            usuarioBloqueado();
                                        }
                                        else if (apiRespuesta.getSuccess() == 2){
                                            vistaContraint.setVisibility(View.VISIBLE);


                                        }
                                        else{
                                            mensajeSinConexion();
                                        }
                                    }else{
                                        mensajeSinConexion();
                                    }
                                },
                                throwable -> {
                                    mensajeSinConexion();
                                })
        );
    }

    private void usuarioBloqueado(){
        KAlertDialog pDialog = new KAlertDialog(this, KAlertDialog.ERROR_TYPE, false);

        pDialog.setTitleText(getString(R.string.bloqueado));
        pDialog.setTitleTextGravity(Gravity.CENTER);
        pDialog.setTitleTextSize(19);

        pDialog.setContentText(getString(R.string.numero_bloqueado));
        pDialog.setContentTextAlignment(View.TEXT_ALIGNMENT_CENTER, Gravity.START);
        pDialog.setContentTextSize(17);

        pDialog.setCancelable(false);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.confirmButtonColor(R.drawable.codigo_kalert_dialog_corners_confirmar);
        pDialog.setConfirmClickListener(getString(R.string.salir), sDialog -> {
            sDialog.dismissWithAnimation();
            salirLoginVista();
        });
        pDialog.show();
    }

    private void salirLoginVista(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    void mensajeSinConexion(){
        progressBar.setVisibility(View.GONE);
        Toasty.error(this, getString(R.string.error_intentar_de_nuevo)).show();
    }

    @Override
    public void onStop() {
        if(compositeDisposable != null){
            compositeDisposable.clear();
        }
        super.onStop();
    }


    private void startTimer() {


        countDownTimer = new CountDownTimer(TIMER_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                txtContador.setText("Resend code in " + millisUntilFinished / 1000 + " seconds");
            }

            @Override
            public void onFinish() {
                txtContador.setText("You can now resend the code.");

            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}