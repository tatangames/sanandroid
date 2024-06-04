package com.alcaldiasan.santaananorteapp.activity.login;


import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Telephony;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alcaldiasan.santaananorteapp.R;
import com.alcaldiasan.santaananorteapp.activity.principal.PrincipalActivity;
import com.alcaldiasan.santaananorteapp.extras.MessageListenerInterface;
import com.alcaldiasan.santaananorteapp.extras.SmsReceiver;
import com.alcaldiasan.santaananorteapp.network.ApiService;
import com.alcaldiasan.santaananorteapp.network.RetrofitBuilder;
import com.alcaldiasan.santaananorteapp.network.TokenManager;
import com.developer.kalert.KAlertDialog;

import es.dmoral.toasty.Toasty;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class LoginVerificarActivity extends AppCompatActivity implements MessageListenerInterface {

    private EditText codeEditText;
    private ProgressBar progressBar;
    private TokenManager tokenManager;
    private RelativeLayout rootRelative;
    private CountDownTimer countDownTimer;
    private ConstraintLayout vistaContraint;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ApiService service;

    private TextView txtReintento;
    private int segundos = 0;
    private String telefono = "";
    private boolean puedeResetearCronometro = false;
    private boolean boolSeguroCheckCodigo = true;
    private OnBackPressedDispatcher onBackPressedDispatcher;
    private ImageView imgFlechaAtras;


    private static final int READ_SMS_PERMISSION_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_verificar);

        codeEditText = findViewById(R.id.codeEditText);
        txtReintento = findViewById(R.id.txtContador);
        vistaContraint = findViewById(R.id.vistaConstraint);
        rootRelative = findViewById(R.id.root);
        imgFlechaAtras = findViewById(R.id.imageView);

        if (getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            segundos = bundle.getInt("KEY_SEGUNDOS");
            telefono = bundle.getString("KEY_PHONE");
        }

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

        onBackPressedDispatcher = getOnBackPressedDispatcher();

        SmsReceiver.bindListener(this);


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
                String cleanString = s.toString().replaceAll("\\D", "");

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
            public void afterTextChanged(Editable editable) {

                String codigoIngresado = editable.toString();
                // Hacer algo con el texto ingresado
                if (!codigoIngresado.isEmpty()) {

                    if(codigoIngresado.length() >= 7 && boolSeguroCheckCodigo){
                        boolSeguroCheckCodigo = false;
                        closeKeyboard();
                        apiVerificarCodigo(codigoIngresado);
                    }
                }
            }
        });

        txtReintento.setOnClickListener(v -> {
              if(puedeResetearCronometro){
                  puedeResetearCronometro = false;
                  closeKeyboard();
                  apiReenviarCodigo();
              }
        });

        imgFlechaAtras.setOnClickListener(v -> {
            onBackPressedDispatcher.onBackPressed();
        });

        startTimer();
    }


    // SETEAR EL CODIGO RECIBIDO POR SMS
    @Override
    public void messageReceived(String message) {
        codeEditText.setText(message);
    }





    private void apiReenviarCodigo(){

        progressBar.setVisibility(View.VISIBLE);

        compositeDisposable.add(
                service.reintentoSMS(telefono)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .retry()
                        .subscribe(apiRespuesta -> {

                                    progressBar.setVisibility(View.GONE);

                                    if(apiRespuesta != null) {

                                        if(apiRespuesta.getSuccess() == 1) {
                                           // usuario bloqueado
                                            usuarioBloqueado();
                                        }
                                        else if(apiRespuesta.getSuccess() == 2){
                                            segundos = apiRespuesta.getSegundos();
                                            reiniciarContador();
                                        }else{
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


    private void reiniciarContador(){
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        startTimer();
    }


    private void startTimer() {

        String texto = getString(R.string.reintentar_en);
        countDownTimer = new CountDownTimer(segundos, 1000) {
            @Override
            public void onTick(long tiempoRestante) {
                // Actualizar el TextView con el tiempo restante
                txtReintento.setText(texto + ": " + tiempoRestante / 1000);
            }

            @Override
            public void onFinish() {
                // Actualizar el TextView cuando el contador llega a cero
                txtReintento.setText(getString(R.string.reenviar_codigo));
                puedeResetearCronometro = true;
            }
        }.start();
    }


    private void apiVerificarCodigo(String codigo){

        progressBar.setVisibility(View.VISIBLE);

        compositeDisposable.add(
                service.verificarCodigo(telefono, codigo, "")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .retry()
                        .subscribe(apiRespuesta -> {

                                    progressBar.setVisibility(View.GONE);
                                    boolSeguroCheckCodigo = true;

                                    if(apiRespuesta != null) {

                                        if(apiRespuesta.getSuccess() == 1) {
                                            // USUARIO BLOQUEADO
                                            usuarioBloqueado();
                                        }
                                        else if(apiRespuesta.getSuccess() == 2){

                                            // VERIFICO CORRECTAMENTE
                                            tokenManager.guardarClienteTOKEN(apiRespuesta);
                                            tokenManager.guardarClienteID(apiRespuesta);

                                            Toasty.success(this, getString(R.string.verificado), Toasty.LENGTH_SHORT).show();
                                            vistaPrincipal();
                                        }
                                        else if(apiRespuesta.getSuccess() == 3){
                                            Toasty.info(this, getString(R.string.codigo_incorrecto), Toasty.LENGTH_SHORT).show();
                                        }
                                        else{
                                            mensajeSinConexion();
                                        }
                                    }else{
                                        mensajeSinConexion();
                                    }
                                },
                                throwable -> {
                                    boolSeguroCheckCodigo = true;
                                    mensajeSinConexion();
                                })
        );
    }

    private void vistaPrincipal(){
        // Siguiente Actvity
        Intent intent = new Intent(this, PrincipalActivity.class);
        startActivity(intent);
        finish();
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


    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();


        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void closeKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();

        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}