package com.alcaldiasan.santaananorteapp.activity.servicios.basico;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;

import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

import es.dmoral.toasty.Toasty;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;

public class ServicioBasicoActivity extends AppCompatActivity {


    // ENVIO DE SERVICIOS BASICOS

    private int idServicio = 0;

    // PARA PERMISO DE LOCALIZACION
    private FusedLocationProviderClient fusedLocationClient;

    private TextView txtToolbar;
    private ImageView imgFlechaAtras, imgFoto;
    private TextInputEditText edtNota;
    private KAlertDialog loadingDialog;
    private boolean bottomSheetImagen = false, hayImagen = false,
            boolSeguroEnviarDatos = true;
    // COORDENADAS GPS
    private double latitudGPS = 0;
    private double longitudGPS = 0;

    private ApiService service;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ProgressBar progressBar;
    private RelativeLayout rootRelative;
    private TokenManager tokenManager;

    // BITMAP FOTO QUE SE ENVIA
    private Bitmap bitmapFoto = null;

    private RequestOptions opcionesGlide = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .placeholder(R.drawable.camaradefecto)
            .priority(Priority.NORMAL);


    // PATH DE FOTO TEMPORAL AL TOMAR FOTOGRAFIA CON CAMARA
    private String currentPhotoPath;
    // INTENT PARA LA CAMARA
    private ActivityResultLauncher<Intent> cameraLauncher;


    // NUMERO DE PERMISOS PARA CADA UNA DE LAS FUNCIONES
    private static final int REQUEST_CODE_OPEN_GALLERY = 1;
    private static final int PERMISSION_REQUEST_READ_MEDIA_IMAGES = 2;
    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 3;
    private static final int PERMISSION_REQUEST_CAMERA = 5;

    private ActivityResultLauncher<String> requestPermissionLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servicio_basico);

        txtToolbar = findViewById(R.id.txtToolbar);
        imgFlechaAtras = findViewById(R.id.imgFlechaAtras);
        edtNota = findViewById(R.id.edtNota);
        imgFoto = findViewById(R.id.imgFoto);
        rootRelative = findViewById(R.id.rootRelative);

        // DATOS RECIBIDOS DE PANTALLA ANTERIOR
        if (getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            String titulo = bundle.getString("KEY_TITULO");
            txtToolbar.setText(titulo);
            idServicio = bundle.getInt("KEY_IDSERVICIO");
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


        // LIMITAR CARACTERES AL INPUT NOTA
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(1000);
        edtNota.setFilters(filterArray);

        // SALIR DE PANTALLA
        imgFlechaAtras.setOnClickListener(v -> {
            finish();
        });

        // OBTENER LOCALIZACION
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // ABRIR OPCIONES PARA GALERIA O CAMARA
        imgFoto.setOnClickListener(v -> {
            abrirBottomDialog();
        });

        Button btnEnviar = findViewById(R.id.btnEnviar);
        btnEnviar.setOnClickListener(v -> {
            // SE DEBE OBTENER LA LOCALIZACION

            closeKeyboard();

            if (!hayImagen) {
                Toasty.info(this, getString(R.string.seleccionar_imagen), Toasty.LENGTH_SHORT).show();
                return;
            }

            checkAndRequestLocationPermission();
        });

        // INTENT DE CAMARA PARA ABRIR LA CAMARA
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {

                        bitmapFoto = correctImageOrientation(currentPhotoPath);
                        hayImagen = true;
                        imgFoto.setImageBitmap(bitmapFoto);
                    }
                }
        );


        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                // Permiso concedido
                //getLocation();
            } else {
                // Permiso denegado
                if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showPermissionDeniedDialog();
                }
            }
        });
    }


    private void checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                showPermissionExplanationDialog(Manifest.permission.ACCESS_FINE_LOCATION);
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        } else {
            // Permiso ya concedido
            getLastLocation();
        }
    }



    private void showPermissionExplanationDialog(String permission) {
        new AlertDialog.Builder(this)
                .setTitle("Permiso requerido")
                .setMessage("Esta aplicación necesita acceso a la localización para continuar. Por favor, acepta el permiso para continuar.")
                .setPositiveButton("Aceptar", (dialog, which) -> requestPermissionLauncher.launch(permission))
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }


    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permiso requerido")
                .setMessage("Esta aplicación necesita acceso a la localización. Por favor, habilita el permiso en la configuración de la aplicación.")
                .setPositiveButton("Ir a ajustes", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getPackageName(), null));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }










    // ORIENTA CORRECTAMENTE EN VERTICAL FOTO TOMADA CON CAMARA
    private Bitmap correctImageOrientation(String photoPath) {
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
        try {
            ExifInterface exif = new ExifInterface(photoPath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    break;
            }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }



    // ABRIR DIALOGO PARA SOLICITAR ABRIR CAMARA O GALERIA
    private void abrirBottomDialog() {
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
                verificarPermisoGaleria();
                bottomSheetProgreso.dismiss();
            });

            // Configura un oyente para saber cuándo se cierra el BottomSheetDialog
            bottomSheetProgreso.setOnDismissListener(dialog -> {
                bottomSheetImagen = false;
            });

            bottomSheetProgreso.show();
        }
    }


    // VERIFICAR SI TIENE PERMISO CAMARA PARA ABRIR
    private void verificarPermisoCamara() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                showPermissionExplanationCamara(Manifest.permission.CAMERA, PERMISSION_REQUEST_CAMERA);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        PERMISSION_REQUEST_CAMERA);
            }
        } else {
            openCamera();
        }
    }

    // SINO ACEPTA PERMISO DE CAMARA, MOSTRAR DIALOGO QUE DEBE ABILITARLO
    private void showPermissionExplanationCamara(String permission, int requestCode) {
        new AlertDialog.Builder(this)
                .setTitle("Permiso requerido")
                .setMessage("Esta aplicación necesita acceso de camara. Por favor, acepta el permiso para continuar.")
                .setPositiveButton("Aceptar", (dialog, which) -> ActivityCompat.requestPermissions(ServicioBasicoActivity.this,
                        new String[]{permission}, requestCode))
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }


    // YA CON PERMISO, ABRIRA CAMARA
    private void openCamera() {

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


    // VERIFICA SI TENEMOS PERMISO DE GALERIA ACTIVO
    private void verificarPermisoGaleria() {

        // Verificar y solicitar permisos según la versión de Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)) {
                    showPermissionExplicacionGaleria(Manifest.permission.READ_MEDIA_IMAGES, PERMISSION_REQUEST_READ_MEDIA_IMAGES);
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                            PERMISSION_REQUEST_READ_MEDIA_IMAGES);
                }
            } else {
                openGallery();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showPermissionExplicacionGaleria(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
                }
            } else {
                openGallery();
            }
        }
    }

    // REDIRECCIONA A PERMISOS GALERIA PARA ACTIVARLO MANUALMENTE
    private void showPermissionExplicacionGaleria(String permission, int requestCode) {
        new AlertDialog.Builder(this)
                .setTitle("Permiso requerido")
                .setMessage("Esta aplicación necesita acceso a la galería. Por favor, acepta el permiso para continuar.")
                .setPositiveButton("Aceptar", (dialog, which) -> ActivityCompat.requestPermissions(ServicioBasicoActivity.this,
                        new String[]{permission}, requestCode))
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }



    // SOLICITA LOS PERMISOS YA SEA DE GALERIA O CAMARA, Y SI ESTA ACEPTADO LLAMAR A UN METODO
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((requestCode == PERMISSION_REQUEST_READ_MEDIA_IMAGES || requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE)) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                    showPermissionDeniedDialogGaleria();
                }
            }
        }else if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                    showPermissionDeniedDialogCamara();
                }
            }
        }
    }

    // DECIRLE AL USUARIO QUE HABILITE EL PERMISO GALERIA MANUALMENTE
    private void showPermissionDeniedDialogGaleria() {
        new AlertDialog.Builder(this)
                .setTitle("Permiso requerido")
                .setMessage("Esta aplicación necesita acceso a la galería. Por favor, habilita los permisos en la configuración de la aplicación.")
                .setPositiveButton("Ir a ajustes", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getPackageName(), null));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    // DECIRLE AL USUARIO QUE HABILITE EL PERMISO CAMARA MANUALMENTE
    private void showPermissionDeniedDialogCamara() {
        new AlertDialog.Builder(this)
                .setTitle("Permiso requerido")
                .setMessage("Esta aplicación necesita acceso a la camara. Por favor, habilita los permisos en la configuración de la aplicación.")
                .setPositiveButton("Ir a ajustes", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getPackageName(), null));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    // YA CON PERMISO ACEPTADO, ABRIR GALERIA
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
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

                            // se enviar al servidor
                            InputStream imageStream = getContentResolver().openInputStream(imageUri);
                            bitmapFoto = BitmapFactory.decodeStream(imageStream);
                            bitmapFoto = rotateImageIfRequired(this, bitmapFoto, imageUri);
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




    // ROTAR IMAGEN SI ES NECESARIO
    private Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {
        ExifInterface ei;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            InputStream input = context.getContentResolver().openInputStream(selectedImage);
            ei = new ExifInterface(input);
        } else {
            String path = getPathFromUri(context, selectedImage);
            ei = new ExifInterface(path);
        }

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    // PARA ROTAR IMAGEN
    private Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }


    // URI DE IMAGEN
    private String getPathFromUri(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(uri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }




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




    // CERRAR TECLADO
    private void closeKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();

        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}