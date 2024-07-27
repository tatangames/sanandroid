package com.alcaldiasan.santaananorteapp.activity.servicios.catastro;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.alcaldiasan.santaananorteapp.R;
import com.alcaldiasan.santaananorteapp.network.ApiService;
import com.alcaldiasan.santaananorteapp.network.RetrofitBuilder;
import com.alcaldiasan.santaananorteapp.network.TokenManager;
import com.developer.kalert.KAlertDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

import es.dmoral.toasty.Toasty;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.http.Field;

public class CatastroActivity extends AppCompatActivity {


    // CATASTRO

    private int idServicio = 0;

    private TextInputEditText edtNombre, edtDui;

    private ApiService service;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private ProgressBar progressBar;
    private RelativeLayout rootRelative;

    private TokenManager tokenManager;

    private TextView txtToolbar;

    private ImageView imgFlechaAtras;

    private RadioButton radioInmueble, radioEmpresa;


    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;

    private double latitudGPS = 0;
    private double longitudGPS = 0;

    private boolean boolSeguroEnviarDatos = true;

    private KAlertDialog loadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catastro);

        txtToolbar = findViewById(R.id.txtToolbar);
        imgFlechaAtras = findViewById(R.id.imgFlechaAtras);
        edtNombre = findViewById(R.id.edtNombre);
        rootRelative = findViewById(R.id.rootRelative);
        edtDui = findViewById(R.id.edtDui);
        radioInmueble = findViewById(R.id.radioInmueble);
        radioEmpresa = findViewById(R.id.radioEmpresa);

        int colorProgress = ContextCompat.getColor(this, R.color.barraProgreso);
        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));
        service = RetrofitBuilder.createServiceAutentificacion(ApiService.class, tokenManager);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        rootRelative.addView(progressBar, params);
        progressBar.getIndeterminateDrawable().setColorFilter(colorProgress, PorterDuff.Mode.SRC_IN);
        progressBar.setVisibility(View.GONE);

        imgFlechaAtras.setOnClickListener(v -> {
            finish();
        });

        // OBTENER LOCALIZACION
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        if (getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            String titulo = bundle.getString("KEY_TITULO");
            txtToolbar.setText(titulo);
            idServicio = bundle.getInt("KEY_IDSERVICIO");
        }

        Button btnEnviar = findViewById(R.id.btnEnviar);
        btnEnviar.setOnClickListener(v -> {

            // VERIFICAR ENTRADAS DE TEXTOS
            if(TextUtils.isEmpty(edtNombre.getText().toString())){
                Toasty.info(this, R.string.nombre_es_requerido, Toasty.LENGTH_SHORT).show();
                return;
            }

            if(TextUtils.isEmpty(edtDui.getText().toString())){
                Toasty.info(this, R.string.dui_es_requerido, Toasty.LENGTH_SHORT).show();
                return;
            }

            getLastLocation();
        });
    }


    @SuppressLint("MissingPermission")
    private void getLastLocation() {

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        latitudGPS = location.getLatitude();
                        longitudGPS = location.getLongitude();
                        apiEnviarDatos();
                    } else {
                        // SIEMPRE SE ENVIARA FOTO, PERO SIN COORDENADAS
                        apiEnviarDatos();
                    }
                });
    }


    private void apiEnviarDatos(){

        if(boolSeguroEnviarDatos){
            boolSeguroEnviarDatos = false;

            // Crear el diálogo de carga
            loadingDialog = new KAlertDialog(this, KAlertDialog.PROGRESS_TYPE, false);
            loadingDialog.getProgressHelper().setBarColor(R.color.colorPrimary);
            loadingDialog.setTitleText(getString(R.string.cargando));
            loadingDialog.setCancelable(false);
            loadingDialog.show();

            String id = tokenManager.getToken().getId();
            String miLatitud = String.valueOf(latitudGPS);
            String miLongitud = String.valueOf(longitudGPS);
            int tipoSolicitud = 1; // solvencia inmueble
            if(radioEmpresa.isChecked()){
                tipoSolicitud = 2;
            }
            String nombre = edtNombre.getText().toString();
            String dui = edtDui.getText().toString();

            compositeDisposable.add(
                    service.registrarSolicitudCatastro(id, miLatitud, miLongitud, tipoSolicitud, nombre, dui)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .retry()
                            .subscribe(apiRespuesta -> {

                                        boolSeguroEnviarDatos = true;
                                        if (loadingDialog.isShowing()) {
                                            loadingDialog.dismissWithAnimation();
                                        }

                                        if(apiRespuesta != null) {

                                            if(apiRespuesta.getSuccess() == 1) {

                                                Toasty.success(this, getString(R.string.notificacion_enviada), Toasty.LENGTH_LONG).show();

                                                resetear();
                                            }
                                            else{
                                                mensajeSinConexion();
                                            }
                                        }else{
                                            if (loadingDialog.isShowing()) {
                                                loadingDialog.dismissWithAnimation();
                                            }
                                            mensajeSinConexion();
                                        }
                                    },
                                    throwable -> {
                                        if (loadingDialog.isShowing()) {
                                            loadingDialog.dismissWithAnimation();
                                        }
                                        mensajeSinConexion();
                                    })
            );
        }
    }



    private void resetear(){
        edtNombre.setText("");
        edtDui.setText("");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                alertDialogoPermiso();
            }
        }
    }

    private void alertDialogoPermiso(){
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.permiso_requerido))
                .setMessage(getString(R.string.permiso_lozalizacion_ajustes))
                .setPositiveButton(getString(R.string.ajustes), (dialog, which) -> openAppSettings())
                .setNegativeButton(getString(R.string.cancelar), null)
                .create()
                .show();
    }


    private void openAppSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    void mensajeSinConexion(){
        progressBar.setVisibility(View.GONE);
        Toasty.error(this, getString(R.string.error_intentar_de_nuevo)).show();
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