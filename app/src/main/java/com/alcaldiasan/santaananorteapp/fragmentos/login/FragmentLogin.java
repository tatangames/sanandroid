package com.alcaldiasan.santaananorteapp.fragmentos.login;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.alcaldiasan.santaananorteapp.R;
import com.alcaldiasan.santaananorteapp.activity.login.LoginActivity;
import com.alcaldiasan.santaananorteapp.activity.login.LoginVerificarActivity;
import com.alcaldiasan.santaananorteapp.activity.principal.PrincipalActivity;
import com.alcaldiasan.santaananorteapp.network.ApiService;
import com.alcaldiasan.santaananorteapp.network.RetrofitBuilder;
import com.alcaldiasan.santaananorteapp.network.TokenManager;
import com.developer.kalert.KAlertDialog;

import es.dmoral.toasty.Toasty;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class FragmentLogin extends Fragment {


    private EditText edtTelefono;
    private Button btnRegistro;
    private Boolean boolSeguroKAlert = true;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ApiService service;
    private ProgressBar progressBar;
    private RelativeLayout rootRelative;
    private KAlertDialog progressVerificando;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_login, container, false);

        edtTelefono = vista.findViewById(R.id.edtTelefono);
        btnRegistro = vista.findViewById(R.id.btnRegistro);
        rootRelative = vista.findViewById(R.id.root);


        service = RetrofitBuilder.createServiceNoAuth(ApiService.class);

        int colorProgress = ContextCompat.getColor(getContext(), R.color.barraProgreso);
        progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleLarge);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        rootRelative.addView(progressBar, params);
        // Aplicar el ColorFilter al Drawable del ProgressBar
        progressBar.getIndeterminateDrawable().setColorFilter(colorProgress, PorterDuff.Mode.SRC_IN);
        progressBar.setVisibility(View.GONE);


        btnRegistro.setOnClickListener(v -> {
            verificar();
        });

        edtTelefono.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    edtTelefono.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[^\\d]", "");
                    String formattedString = "";

                    if (cleanString.length() > 4) {
                        formattedString = cleanString.substring(0, 4) + " " + cleanString.substring(4);
                    } else {
                        formattedString = cleanString;
                    }

                    if (formattedString.length() > 9) {
                        formattedString = formattedString.substring(0, 9);
                    }

                    current = formattedString;
                    edtTelefono.setText(formattedString);
                    edtTelefono.setSelection(formattedString.length());

                    edtTelefono.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed here
            }
        });

        return vista;
    }


    private void verificar(){

        if(TextUtils.isEmpty(edtTelefono.getText().toString())){
            Toasty.info(getContext(), getString(R.string.telefono_es_requerido)).show();
            return;
        }

        closeKeyboard();

        String telefono = edtTelefono.getText().toString();
        if(telefono.length() < 9){

            if(boolSeguroKAlert){
                boolSeguroKAlert = false;
                new Handler().postDelayed(() -> {
                    boolSeguroKAlert = true;
                }, 2000);

                KAlertDialog pDialog = new KAlertDialog(getContext(), KAlertDialog.WARNING_TYPE, false);

                pDialog.setTitleText(getString(R.string.nota));
                pDialog.setTitleTextGravity(Gravity.CENTER);
                pDialog.setTitleTextSize(19);

                pDialog.setContentText(getString(R.string.el_numero_telefono_introducido));
                pDialog.setContentTextAlignment(View.TEXT_ALIGNMENT_CENTER, Gravity.START);
                pDialog.setContentTextSize(17);

                pDialog.setCancelable(false);
                pDialog.setCanceledOnTouchOutside(false);
                pDialog.confirmButtonColor(R.drawable.codigo_kalert_dialog_corners_confirmar);
                pDialog.setConfirmClickListener(getString(R.string.editar), sDialog -> {
                    sDialog.dismissWithAnimation();

                });
                pDialog.show();

                return;
            }
        }

        if(boolSeguroKAlert){
            boolSeguroKAlert = false;
            new Handler().postDelayed(() -> {
                boolSeguroKAlert = true;
            }, 1000);


            String mensaje = getString(R.string.verificar_el_numero_de_telefono) + "\n +503 " + telefono;

            KAlertDialog pDialog = new KAlertDialog(getContext(), KAlertDialog.WARNING_TYPE, false);

            pDialog.setTitleText("");
            pDialog.setTitleTextGravity(Gravity.CENTER);
            pDialog.setTitleTextSize(19);

            pDialog.setContentText(mensaje);
            pDialog.setContentTextAlignment(View.TEXT_ALIGNMENT_CENTER, Gravity.START);
            pDialog.setContentTextSize(17);

            pDialog.setCancelable(false);
            pDialog.setCanceledOnTouchOutside(false);

            pDialog.confirmButtonColor(R.drawable.codigo_kalert_dialog_corners_confirmar);
            pDialog.setConfirmClickListener(getString(R.string.verificar), sDialog -> {
                sDialog.dismissWithAnimation();
                apiVerificar();
            });

            pDialog.cancelButtonColor(R.drawable.codigo_kalert_dialog_corners_cancelar);
            pDialog.setCancelClickListener(getString(R.string.editar), sDialog -> {
                sDialog.dismissWithAnimation();

            });

            pDialog.show();
        }
    }


    private void closeKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
        View view = getActivity().getCurrentFocus();

        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    private void apiVerificar(){
        progressBar.setVisibility(View.VISIBLE);

        String telefono = edtTelefono.getText().toString();

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

                                            // SOLO CUANDO ES PERMITIDO Y ENVIO EL SMS, MOSTRAR TOAST
                                            if(apiRespuesta.getCanRetry() == 1){
                                                Toasty.success(getContext(), getString(R.string.codigo_enviado), Toasty.LENGTH_SHORT).show();
                                            }

                                            // LOS SEGUNDOS PARA EL CRONOMETRO
                                            int segundos = apiRespuesta.getSegundos();

                                            siguienteVista(segundos, telefono);
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
        KAlertDialog pDialog = new KAlertDialog(getContext(), KAlertDialog.ERROR_TYPE, false);

        pDialog.setTitleText(getString(R.string.bloqueado));
        pDialog.setTitleTextGravity(Gravity.CENTER);
        pDialog.setTitleTextSize(19);

        pDialog.setContentText(getString(R.string.numero_bloqueado));
        pDialog.setContentTextAlignment(View.TEXT_ALIGNMENT_CENTER, Gravity.START);
        pDialog.setContentTextSize(17);

        pDialog.setCancelable(false);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.confirmButtonColor(R.drawable.codigo_kalert_dialog_corners_confirmar);
        pDialog.setConfirmClickListener(getString(R.string.aceptar), sDialog -> {
            sDialog.dismissWithAnimation();

        });
        pDialog.show();
    }

    private void siguienteVista(int segundos, String telefono){

        Intent intent = new Intent(getContext(), LoginVerificarActivity.class);
        intent.putExtra("KEY_SEGUNDOS", segundos);
        intent.putExtra("KEY_PHONE", telefono);

        startActivity(intent);
    }

    private void mensajeSinConexion(){
        progressBar.setVisibility(View.GONE);
        Toasty.error(getContext(), getString(R.string.error_intentar_de_nuevo)).show();
    }






    private void showProgressDialog() {
        progressVerificando = new KAlertDialog(getContext(), KAlertDialog.PROGRESS_TYPE, false);
        progressVerificando.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        progressVerificando.setTitleText(getString(R.string.verificando));
        progressVerificando.setCancelable(false);
        progressVerificando.show();
    }

    private void hideProgressVerificando() {
        if (progressVerificando != null && progressVerificando.isShowing()) {
            progressVerificando.dismiss();
        }
    }




    @Override
    public void onDestroy(){
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        if(compositeDisposable != null){
            compositeDisposable.clear();
        }
        super.onStop();
    }
}
