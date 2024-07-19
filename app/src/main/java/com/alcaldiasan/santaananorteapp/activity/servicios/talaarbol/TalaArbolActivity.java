package com.alcaldiasan.santaananorteapp.activity.servicios.talaarbol;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.alcaldiasan.santaananorteapp.R;
import com.alcaldiasan.santaananorteapp.extras.ImageUtils;
import com.alcaldiasan.santaananorteapp.extras.MultipartUtil;
import com.alcaldiasan.santaananorteapp.network.ApiService;
import com.alcaldiasan.santaananorteapp.network.RetrofitBuilder;
import com.alcaldiasan.santaananorteapp.network.TokenManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.developer.kalert.KAlertDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import es.dmoral.toasty.Toasty;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class TalaArbolActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {


    // PANTALLA SOLICITUD TALA DE ARBOL Y PARA DENUNCIA

    private Bitmap photoBitmapDenuncia;
    private Bitmap photoBitmapSolicitud;

    // PERMISO DE LOCALIZACION
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;


    private RequestOptions opcionesGlide = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .placeholder(R.drawable.camaradefecto)
            .priority(Priority.NORMAL);



    private TextView txtToolbar, tituloServicio;
    private ImageView imgFlechaAtras, imgFotoSolicitud, imgFotoDenuncia;
    private TextInputEditText edtNotaSolicitud, edtNotaDenuncia, edtNombre, edtTelefono, edtDireccion;;

    private boolean bottomSheetImagen = false;

    private boolean hayImagenSolicitud = false, hayImagenDenuncia = false;


    // PERMISOS PARA CAMARA Y GALERIA

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_PERMISSION_CODE = 101;
    private static final int GALERIA_PERMISSION_CODE = 102;

    private final int sdkVersion = Build.VERSION.SDK_INT;

    private ActivityResultLauncher<Intent> cameraLauncher;


    // ******** PERMISOS (ABRIR GALERIA) ANDROID 13 O SUPERIOR *********

    private final String[] requiredPermission = new String[]{
            Manifest.permission.READ_MEDIA_IMAGES,
    };

    private boolean is_storage_image_permitted = false;

    private boolean allPermissionResultCheck(){
        return is_storage_image_permitted;
    }

    private KAlertDialog loadingDialog;




    // COORDENADAS GPS
    private double latitudGPS = 0;
    private double longitudGPS = 0;

    private boolean boolSeguroEnviarDatosSolicitud = true, boolSeguroEnviarDatosDenuncia = true;

    private ApiService service;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private ProgressBar progressBar;
    private RelativeLayout rootRelative;

    private TokenManager tokenManager;

    private ConstraintLayout constraintSolicitud, constraintDenuncia;
    private RadioButton radioSolicitud, radioDenuncia;

    private CheckBox checkEscritura;

    private boolean estadoSolicitud = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tala_arbol);

        txtToolbar = findViewById(R.id.txtToolbar);
        imgFlechaAtras = findViewById(R.id.imgFlechaAtras);
        rootRelative = findViewById(R.id.rootRelative);
        tituloServicio = findViewById(R.id.tituloServicio);
        edtNotaSolicitud = findViewById(R.id.edtNota);
        imgFotoSolicitud = findViewById(R.id.imgFoto);
        constraintSolicitud = findViewById(R.id.constraintSolicitud);
        constraintDenuncia = findViewById(R.id.constraintDenuncia);
        radioSolicitud = findViewById(R.id.radioSolicitud);
        radioDenuncia = findViewById(R.id.radioDenuncia);
        edtNombre = findViewById(R.id.edtNombre);
        edtTelefono = findViewById(R.id.edtTelefono);
        edtDireccion = findViewById(R.id.edtDireccion);
        checkEscritura = findViewById(R.id.checkEscritura);
        imgFotoDenuncia = findViewById(R.id.imgFotoDenuncia);
        edtNotaDenuncia = findViewById(R.id.edtNotaDenuncia);


        radioSolicitud.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                estadoSolicitud = true;
                mostrarVistaSolicitud();
            }
        });

        radioDenuncia.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                estadoSolicitud = false;
                mostrarVistaDenuncia();
            }
        });

        // RECUPERAR DATOS DE VISTA ANTERIOR
        if (getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            String titulo = bundle.getString("KEY_TITULO");
            txtToolbar.setText(titulo);

            String textoServicio = bundle.getString("KEY_NOTA");

            // DESCRIPCION DE ESTA PANTALLA
            if(!TextUtils.isEmpty(textoServicio)){
                tituloServicio.setText(textoServicio);
            }
        }

        int colorProgress = ContextCompat.getColor(this, R.color.barraProgreso);
        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));
        service = RetrofitBuilder.createServiceAutentificacion(ApiService.class, tokenManager);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        rootRelative.addView(progressBar, params);
        progressBar.getIndeterminateDrawable().setColorFilter(colorProgress, PorterDuff.Mode.SRC_IN);
        progressBar.setVisibility(View.GONE);

        // FILTRO DE 1000 CARACTERES PARA CUADRO DE TEXTO PARA SOLICITUD
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(1000);
        edtNotaSolicitud.setFilters(filterArray);


        // FILTRO DE 1000 CARACTERES PARA CUADRO DE TEXTO PARA DENUNCIA
        InputFilter[] filterArray2 = new InputFilter[1];
        filterArray2[0] = new InputFilter.LengthFilter(1000);
        edtNotaDenuncia.setFilters(filterArray);


        imgFlechaAtras.setOnClickListener(v -> {
            finish();
        });

        // LOCALIZACION
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        imgFotoSolicitud.setOnClickListener(v -> {
            abrirBottomDialog();
        });

        imgFotoDenuncia.setOnClickListener(v -> {
            abrirBottomDialog();
        });


        // BOTON PARA SOLICITUD

        Button btnEnviarSolicitud = findViewById(R.id.btnEnviar);
        btnEnviarSolicitud.setOnClickListener(v -> {

            closeKeyboard();

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            } else {


                if(TextUtils.isEmpty(edtNombre.getText().toString())){
                    Toasty.info(this, R.string.nombre_es_requerido, Toasty.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(edtTelefono.getText().toString())){
                    Toasty.info(this, R.string.telefono_es_requerido, Toasty.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(edtDireccion.getText().toString())){
                    Toasty.info(this, R.string.direccion_es_requerido, Toasty.LENGTH_SHORT).show();
                    return;
                }

                if(!hayImagenSolicitud){
                    Toasty.info(this, getString(R.string.seleccionar_imagen), Toasty.LENGTH_SHORT).show();
                    return;
                }

                obtenerLocalizacion(true);
            }
        });


        // OBTENER IMAGEN DE LA CAMARA
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getExtras() != null) {

                            if(estadoSolicitud){
                                photoBitmapSolicitud = (Bitmap) data.getExtras().get("data");
                                hayImagenSolicitud = true;
                                imgFotoSolicitud.setImageBitmap(photoBitmapSolicitud);

                            }else{
                                photoBitmapDenuncia = (Bitmap) data.getExtras().get("data");
                                hayImagenDenuncia = true;
                                imgFotoDenuncia.setImageBitmap(photoBitmapDenuncia);
                            }
                        }
                    }
                }
        );


        // BOTON PARA DENUNCIAS

        Button btnEnviarDenuncias = findViewById(R.id.btnEnviarDenuncia);

        btnEnviarDenuncias.setOnClickListener(v -> {
            // SE DEBE OBTENER LA LOCALIZACION

            closeKeyboard();

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            } else {

                // VERIFICAR ENTRADAS DE TEXTOS
                if(TextUtils.isEmpty(edtNotaDenuncia.getText().toString())){
                    Toasty.info(this, R.string.nota_es_requerida, Toasty.LENGTH_SHORT).show();
                    return;
                }

                if(!hayImagenDenuncia) {
                    Toasty.info(this, getString(R.string.seleccionar_imagen), Toasty.LENGTH_SHORT).show();
                    return;
                }

                obtenerLocalizacion(false);
            }
        });
    }

    // MOSTRAR VISTA PARA SOLICITUD
    private void mostrarVistaSolicitud(){
        constraintDenuncia.setVisibility(View.GONE);
        constraintSolicitud.setVisibility(View.VISIBLE);
    }


    // MOSTRAR VISTA PARA DENUNCIA
    private void mostrarVistaDenuncia(){
        constraintSolicitud.setVisibility(View.GONE);
        constraintDenuncia.setVisibility(View.VISIBLE);
    }

    // ABRE DIALOGO PARA MOSTRAR SI QUIERE ABRIR CAMARA O GALERIA DE FOTOS
    private void abrirBottomDialog(){
        if (!bottomSheetImagen) {
            bottomSheetImagen = true;

            BottomSheetDialog bottomSheetProgreso = new BottomSheetDialog(this);
            View bottomSheetViewProgreso = getLayoutInflater().inflate(R.layout.cardview_botton_sheet_camara, null);
            bottomSheetProgreso.setContentView(bottomSheetViewProgreso);

            Button btnCamara = bottomSheetProgreso.findViewById(R.id.btnCamara);
            Button btnGaleria = bottomSheetProgreso.findViewById(R.id.btnGaleria);

            // VERIFICA PERMISO CAMARA
            btnCamara.setOnClickListener(v -> {
                bottomSheetProgreso.dismiss();
                verificarPermisoCamara();
            });


            // VERIFICA PERMISO GALERIA
            btnGaleria.setOnClickListener(v -> {
                bottomSheetProgreso.dismiss();
                verificarPermisoGaleria();
            });

            // Configura un oyente para saber cuándo se cierra el BottomSheetDialog
            bottomSheetProgreso.setOnDismissListener(dialog -> {
                bottomSheetImagen = false;
            });

            bottomSheetProgreso.show();
        }
    }


    // VERIFICA PERMISO Y ABRE LA CAMARA
    @AfterPermissionGranted(CAMERA_PERMISSION_CODE)
    private void verificarPermisoCamara() {
        String[] perms = {Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {

            openCamera();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.esta_aplicacion_camara),
                    CAMERA_PERMISSION_CODE, perms);
        }
    }


    // ABRIR CAMARA YA CON PERMISO AUTORIZADO
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(cameraIntent);
    }



    // SEGUN CUANDO AUTORICE EL PERMISO, ABRIRA CAMARA O GALERIA
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == CAMERA_PERMISSION_CODE) {
            openCamera();
        }
        else if (requestCode == GALERIA_PERMISSION_CODE) {
            abrirGaleria();
        }
    }

    // SI LOS PERMISOS FUERON DENEGADOS, SE MOSTRARA EL CARTEL DE PERMISO DENEGADO
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode == CAMERA_PERMISSION_CODE ) {
            if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.permiso_requerido))
                        .setMessage(getString(R.string.permiso_camara_ajustes))
                        .setPositiveButton(getString(R.string.ajustes), (dialog, which) -> openAppSettings())
                        .setNegativeButton(getString(R.string.cancelar), null)
                        .create()
                        .show();
            } else {
                Toast.makeText(this, getString(R.string.permiso_denegado), Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == GALERIA_PERMISSION_CODE ) {
            if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.permiso_requerido))
                        .setMessage(getString(R.string.permiso_galeria_ajustes))
                        .setPositiveButton(getString(R.string.ajustes), (dialog, which) -> openAppSettings())
                        .setNegativeButton(getString(R.string.cancelar), null)
                        .create()
                        .show();
            } else {
                Toast.makeText(this, getString(R.string.permiso_denegado), Toast.LENGTH_SHORT).show();
            }
        }
    }



    // VERIFICADOR DE PERMISOS PARA ABRIR GALERIA
    private void verificarPermisoGaleria(){
        if (sdkVersion >= Build.VERSION_CODES.TIRAMISU) {
            // El dispositivo ejecuta Android 13 o superior.

            if(!allPermissionResultCheck()){

                if(ContextCompat.checkSelfPermission(this, requiredPermission[0]) == PackageManager.PERMISSION_GRANTED){
                    is_storage_image_permitted = true;
                } else{
                    request_permission_launcher_storage_image.launch(requiredPermission[0]);
                }

            }else{
                // aqui ya puede abrir galeria

                abrirGaleria();
            }

        } else {
            // El dispositivo no ejecuta Android 13.
            String[] perms = {android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

            if(EasyPermissions.hasPermissions(this,
                    perms)){
                // permiso autorizado

                // aqui ya puede abrir galeria

                abrirGaleria();

            }else{
                // permiso denegado
                EasyPermissions.requestPermissions(this,
                        getString(R.string.esta_aplicacion_galeria),
                        GALERIA_PERMISSION_CODE,
                        perms);
            }
        }
    }



    // ABRIR GALERIA YA CON PERMISOS
    private void abrirGaleria(){

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        launcherGaleria.launch(intent);
    }

    // OBTENER YA LA IMAGEN DE GALERIA
    ActivityResultLauncher<Intent> launcherGaleria = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Handle successful image selection
                    Intent data = result.getData();
                    if (data != null) {
                        Uri imageUri = data.getData();

                        try {
                            // Obtener el Bitmap de la imagen seleccionada
                            InputStream imageStream = getContentResolver().openInputStream(imageUri);

                          if(estadoSolicitud){ // vista solicitud
                                photoBitmapSolicitud = BitmapFactory.decodeStream(imageStream);
                                hayImagenSolicitud = true;

                                Glide.with(this)
                                      .load(photoBitmapSolicitud)
                                      .apply(opcionesGlide)
                                      .into(imgFotoSolicitud);

                            }else{ // vista denuncia
                              photoBitmapDenuncia = BitmapFactory.decodeStream(imageStream);
                              hayImagenDenuncia = true;

                              Glide.with(this)
                                      .load(photoBitmapDenuncia)
                                      .apply(opcionesGlide)
                                      .into(imgFotoDenuncia);
                            }

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
    );

    private final ActivityResultLauncher<String> request_permission_launcher_storage_image =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted-> {
                        if(isGranted){
                            is_storage_image_permitted = true;
                        }else{
                            is_storage_image_permitted = false;

                            new AlertDialog.Builder(this)
                                    .setTitle(getString(R.string.permiso_requerido))
                                    .setMessage(getString(R.string.permiso_galeria_ajustes))
                                    .setPositiveButton(getString(R.string.ajustes), (dialog, which) -> openAppSettings())
                                    .setNegativeButton(getString(R.string.cancelar), (dialog, which) -> {
                                        Toast.makeText(this, getString(R.string.permiso_denegado), Toast.LENGTH_SHORT).show();
                                    })
                                    .create()
                                    .show();
                        }
                    });

    // ******** END - PERMISOS (ABRIR GALERIA) ANDROID 13 O SUPERIOR *********




    // OBTENER LOCALIZACION PARA DENUNCIAS
    @SuppressLint("MissingPermission")
    private void obtenerLocalizacion(boolean tipoSolicitud) {

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        latitudGPS = location.getLatitude();
                        longitudGPS = location.getLongitude();

                        if(tipoSolicitud){
                            apiEnviarDatosSolicitud();
                        }else{
                            apiEnviarDatosDenuncia();
                        }

                    } else {

                        if(tipoSolicitud){
                            apiEnviarDatosSolicitud();
                        }else{
                            apiEnviarDatosDenuncia();
                        }
                    }
                });
    }







    // ENVIAR DATOS AL SERVIDOR
    private void apiEnviarDatosSolicitud() {

        if(boolSeguroEnviarDatosSolicitud){
            boolSeguroEnviarDatosSolicitud = false;

            // Crear el diálogo de carga
            loadingDialog = new KAlertDialog(this, KAlertDialog.PROGRESS_TYPE, false);
            loadingDialog.getProgressHelper().setBarColor(R.color.colorPrimary);
            loadingDialog.setTitleText(getString(R.string.cargando));
            loadingDialog.setCancelable(false);
            loadingDialog.show();

            byte[] bytes = null;

            try {
                bytes = ImageUtils.inputStreamToByteArray(photoBitmapSolicitud);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String iduser = tokenManager.getToken().getId();

            String nombre = edtNombre.getText().toString();
            String telefono = edtTelefono.getText().toString();
            String direccion = edtDireccion.getText().toString();

            String nota = edtNotaSolicitud.getText().toString();
            String lati = String.valueOf(latitudGPS);
            String longi = String.valueOf(longitudGPS);

            int checkEscri = 0;
            if(checkEscritura.isChecked()){
                checkEscri = 1;
            }

            MultipartBody multipartBody = MultipartUtil.createMultipartSolicitudTalaArbol(bytes, iduser, nota, lati, longi,
                    nombre, telefono, direccion, checkEscri);

            compositeDisposable.add(
                    service.registrarSolicitudTalaArbol(multipartBody)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .retry()
                            .subscribe(apiRespuesta -> {

                                        boolSeguroEnviarDatosSolicitud = true;
                                        if (loadingDialog.isShowing()) {
                                            loadingDialog.dismissWithAnimation();
                                        }

                                        if(apiRespuesta != null) {

                                            if(apiRespuesta.getSuccess() == 1){
                                                Toasty.success(this, getString(R.string.notificacion_enviada), Toasty.LENGTH_LONG).show();
                                                limpiarCamposSolicitud();
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

    private void limpiarCamposSolicitud(){
        edtNombre.setText("");
        edtTelefono.setText("");
        edtDireccion.setText("");
        hayImagenSolicitud = false;
        photoBitmapSolicitud = null;
        edtNotaSolicitud.setText("");
        imgFotoSolicitud.setImageResource(R.drawable.camarafoto);
        checkEscritura.setChecked(false);
    }





    // ENVIAR DATOS AL SERVIDOR
    private void apiEnviarDatosDenuncia() {

        if(boolSeguroEnviarDatosDenuncia){
            boolSeguroEnviarDatosDenuncia = false;

            // Crear el diálogo de carga
            loadingDialog = new KAlertDialog(this, KAlertDialog.PROGRESS_TYPE, false);
            loadingDialog.getProgressHelper().setBarColor(R.color.colorPrimary);
            loadingDialog.setTitleText(getString(R.string.cargando));
            loadingDialog.setCancelable(false);
            loadingDialog.show();

            byte[] bytes = null;

            try {
                bytes = ImageUtils.inputStreamToByteArray(photoBitmapDenuncia);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String iduser = tokenManager.getToken().getId();
            String nota = edtNotaDenuncia.getText().toString();
            String lati = String.valueOf(latitudGPS);
            String longi = String.valueOf(longitudGPS);

            MultipartBody multipartBody = MultipartUtil.createMultipartDenunciaTalaArbol(bytes, iduser, nota, lati, longi);

            compositeDisposable.add(
                    service.registrarDenunciaTalaArbol(multipartBody)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .retry()
                            .subscribe(apiRespuesta -> {

                                        boolSeguroEnviarDatosDenuncia = true;

                                        if (loadingDialog.isShowing()) {
                                            loadingDialog.dismissWithAnimation();
                                        }

                                        if(apiRespuesta != null) {

                                            if(apiRespuesta.getSuccess() == 1){
                                                Toasty.success(this, getString(R.string.notificacion_enviada), Toasty.LENGTH_LONG).show();
                                                limpiarCamposDenuncia();
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

    private void limpiarCamposDenuncia(){

        hayImagenDenuncia = false;
        photoBitmapDenuncia = null;
        edtNotaDenuncia.setText("");
        imgFotoDenuncia.setImageResource(R.drawable.camarafoto);
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

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


    private void closeKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();

        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}