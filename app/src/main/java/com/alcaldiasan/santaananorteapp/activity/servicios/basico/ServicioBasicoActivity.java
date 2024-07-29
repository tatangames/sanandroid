package com.alcaldiasan.santaananorteapp.activity.servicios.basico;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;

import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

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

import java.io.File;
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

public class ServicioBasicoActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {


    // ENVIO DE SERVICIOS BASICOS

    private int idServicio = 0;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;

    private TextView txtToolbar, tituloServicio;
    private ImageView imgFlechaAtras, imgFoto;

    private TextInputEditText edtNota;

    private KAlertDialog loadingDialog;

    private boolean bottomSheetImagen = false, hayImagen = false,
            boolSeguroEnviarDatos = true;


    // PERMISOS PARA CAMARA Y GALERIA

    // Estos son codigo cualquiera para verificar permisos
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

    // COORDENADAS GPS
    private double latitudGPS = 0;
    private double longitudGPS = 0;

    private ApiService service;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private ProgressBar progressBar;
    private RelativeLayout rootRelative;

    private TokenManager tokenManager;

    private Bitmap bitmapFoto = null;

    private RequestOptions opcionesGlide = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .placeholder(R.drawable.camaradefecto)
            .priority(Priority.NORMAL);


    private String currentPhotoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servicio_basico);

        txtToolbar = findViewById(R.id.txtToolbar);
        imgFlechaAtras = findViewById(R.id.imgFlechaAtras);
        edtNota = findViewById(R.id.edtNota);
        imgFoto = findViewById(R.id.imgFoto);
        rootRelative = findViewById(R.id.rootRelative);
        tituloServicio = findViewById(R.id.tituloServicio);

        if (getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            String titulo = bundle.getString("KEY_TITULO");
            txtToolbar.setText(titulo);
            idServicio = bundle.getInt("KEY_IDSERVICIO");

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


        // LIMITAR CARACTERES
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(1000);
        edtNota.setFilters(filterArray);

        imgFlechaAtras.setOnClickListener(v -> {
            finish();
        });

        // OBTENER LOCALIZACION
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        imgFoto.setOnClickListener(v -> {
            abrirBottomDialog();
        });

        Button btnEnviar = findViewById(R.id.btnEnviar);
        btnEnviar.setOnClickListener(v -> {
            // SE DEBE OBTENER LA LOCALIZACION

            closeKeyboard();

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            } else {

                if(!hayImagen){
                    Toasty.info(this, getString(R.string.seleccionar_imagen), Toasty.LENGTH_SHORT).show();
                    return;
                }

                getLastLocation();
            }
        });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        bitmapFoto = BitmapFactory.decodeFile(currentPhotoPath);
                        hayImagen = true;
                        imgFoto.setImageBitmap(bitmapFoto);
                    }
                }
        );
    }


    private void abrirBottomDialog(){
        if (!bottomSheetImagen) {
            bottomSheetImagen = true;

            BottomSheetDialog bottomSheetProgreso = new BottomSheetDialog(this);
            View bottomSheetViewProgreso = getLayoutInflater().inflate(R.layout.cardview_botton_sheet_camara, null);
            bottomSheetProgreso.setContentView(bottomSheetViewProgreso);

            Button btnCamara = bottomSheetProgreso.findViewById(R.id.btnCamara);
            Button btnGaleria = bottomSheetProgreso.findViewById(R.id.btnGaleria);


            btnCamara.setOnClickListener(v -> {
                bottomSheetProgreso.dismiss();
                verificarPermisoCamara();
            });


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

    // YA CON PERMISO, ABRIRA CAMARA
    private void openCamera() {
       /* Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(cameraIntent);*/


        String fileName = "photo";
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            File imageFile = File.createTempFile(fileName, ".jpg", storageDirectory);

            currentPhotoPath = imageFile.getAbsolutePath();

            Uri imageUri = FileProvider.getUriForFile(ServicioBasicoActivity.this, "com.alcaldiasan.santaananorteapp.fileprovider", imageFile);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraLauncher.launch(cameraIntent);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == CAMERA_PERMISSION_CODE) {
            openCamera();
        }
        else if (requestCode == GALERIA_PERMISSION_CODE) {
            abrirGaleria();
        }
    }


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


    // YA CON PERMISO ABRIRA GALERIA
    private void abrirGaleria(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        launcherGaleria.launch(intent);
    }

    ActivityResultLauncher<Intent> launcherGaleria = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Handle successful image selection
                    Intent data = result.getData();
                    if (data != null) {
                        Uri imageUri = data.getData();
                        try {

                            InputStream imageStream = getContentResolver().openInputStream(imageUri);
                            bitmapFoto = BitmapFactory.decodeStream(imageStream);
                            hayImagen = true;

                            Glide.with(this)
                                    .load(bitmapFoto)
                                    .apply(opcionesGlide)
                                    .into(imgFoto);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
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




    @SuppressLint("MissingPermission")
    private void getLastLocation() {

        fusedLocationClient.getLastLocation()
            .addOnSuccessListener(this, location -> {
                if (location != null) {
                    latitudGPS = location.getLatitude();
                    longitudGPS = location.getLongitude();
                    try {
                        apiEnviarDatos();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    // SIEMPRE SE ENVIARA FOTO, PERO SIN COORDENADAS
                    try {
                        apiEnviarDatos();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
    }



    // ENVIAR DATOS AL SERVIDOR
    private void apiEnviarDatos() throws IOException {

        if(boolSeguroEnviarDatos){
            boolSeguroEnviarDatos = false;

            // Crear el diálogo de carga
            loadingDialog = new KAlertDialog(this, KAlertDialog.PROGRESS_TYPE, false);
            loadingDialog.getProgressHelper().setBarColor(R.color.colorPrimary);
            loadingDialog.setTitleText(getString(R.string.cargando));
            loadingDialog.setCancelable(false);
            loadingDialog.show();

            byte[] bytes = null;

            try {
                bytes = ImageUtils.inputStreamToByteArray(bitmapFoto);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String iduser = tokenManager.getToken().getId();

            String nota = edtNota.getText().toString();
            String lati = String.valueOf(latitudGPS);
            String longi = String.valueOf(longitudGPS);

            MultipartBody multipartBody = MultipartUtil.createMultipartEnvioDatos(bytes, iduser, String.valueOf(idServicio), nota, lati, longi);

            compositeDisposable.add(
                    service.registrarServicioBasico(multipartBody)
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
                                                // HAY SOLICITUD ACTIVA, Y ESTA DENTRO DEL RANGO 20 METROS

                                                alertaSoliActiva(apiRespuesta.getTitulo(), apiRespuesta.getMensaje());

                                            }
                                            else if(apiRespuesta.getSuccess() == 2) {

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

    private void alertaSoliActiva(String titulo, String mensaje){

        KAlertDialog pDialog = new KAlertDialog(this, KAlertDialog.CUSTOM_IMAGE_TYPE, false);

        pDialog.setCustomImage(R.drawable.ic_informacion);

        pDialog.setTitleText(titulo);
        pDialog.setTitleTextGravity(Gravity.CENTER);
        pDialog.setTitleTextSize(19);

        pDialog.setContentText(mensaje);
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


    private void resetear(){
        hayImagen = false;
        edtNota.setText("");
        imgFoto.setImageResource(R.drawable.camarafoto);
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


    private void closeKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();

        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}