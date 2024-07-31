package com.alcaldiasan.santaananorteapp.activity.servicios.talaarbol;

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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
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


public class TalaArbolActivity extends AppCompatActivity {

    // PANTALLA SOLICITUD TALA DE ARBOL Y PARA DENUNCIA

    private Bitmap photoBitmapDenuncia;
    private Bitmap photoBitmapSolicitud;


    private RequestOptions opcionesGlide = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .placeholder(R.drawable.camaradefecto)
            .priority(Priority.NORMAL);


    private TextView txtToolbar, tituloServicio;
    private ImageView imgFlechaAtras, imgFotoSolicitud, imgFotoDenuncia;
    private TextInputEditText edtNotaSolicitud, edtNotaDenuncia, edtNombre, edtTelefono, edtDireccion;;

    private boolean hayImagenSolicitud = false, hayImagenDenuncia = false,
            bottomSheetImagen = false;

    private KAlertDialog loadingDialog;

    // COORDENADAS GPS
    private double latitudGPS = 0;
    private double longitudGPS = 0;

    private boolean boolSeguroEnviarDatosSolicitud = true,
            boolSeguroEnviarDatosDenuncia = true,
            estadoSolicitud = true;

    private ApiService service;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private ProgressBar progressBar;
    private RelativeLayout rootRelative;

    private TokenManager tokenManager;

    private ConstraintLayout constraintSolicitud, constraintDenuncia;
    private RadioButton radioSolicitud, radioDenuncia;

    private CheckBox checkEscritura;


    // PARA PERMISO DE LOCALIZACION
    private FusedLocationProviderClient fusedLocationClient;

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

        // OBTENER LOCALIZACION
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


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

        // ABRIR DIALOGO EN MODO SOLICITUD TALA ARBOL
        imgFotoSolicitud.setOnClickListener(v -> {
            abrirBottomDialog();
        });

        // ABRIR DIALOGO EN MODO DENUNCIA TALA ARBOL
        imgFotoDenuncia.setOnClickListener(v -> {
            abrirBottomDialog();
        });


        // BOTON PARA SOLICITUD

        Button btnEnviarSolicitud = findViewById(R.id.btnEnviar);
        btnEnviarSolicitud.setOnClickListener(v -> {

            closeKeyboard();

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
            checkAndRequestLocationPermission(true);
        });


        // OBTENER IMAGEN DE LA CAMARA
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {

                        if(estadoSolicitud){

                            photoBitmapSolicitud = correctImageOrientation(currentPhotoPath);
                            hayImagenSolicitud = true;
                            imgFotoSolicitud.setImageBitmap(photoBitmapSolicitud);

                        }else{
                            photoBitmapDenuncia = correctImageOrientation(currentPhotoPath);
                            hayImagenDenuncia = true;
                            imgFotoDenuncia.setImageBitmap(photoBitmapDenuncia);
                        }
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


        // BOTON PARA DENUNCIAS

        Button btnEnviarDenuncias = findViewById(R.id.btnEnviarDenuncia);

        btnEnviarDenuncias.setOnClickListener(v -> {
            // SE DEBE OBTENER LA LOCALIZACION

            closeKeyboard();

            // VERIFICAR ENTRADAS DE TEXTOS
            if(TextUtils.isEmpty(edtNotaDenuncia.getText().toString())){
                Toasty.info(this, R.string.nota_es_requerida, Toasty.LENGTH_SHORT).show();
                return;
            }

            if(!hayImagenDenuncia) {
                Toasty.info(this, getString(R.string.seleccionar_imagen), Toasty.LENGTH_SHORT).show();
                return;
            }

            checkAndRequestLocationPermission(false);
        });
    }


    private void checkAndRequestLocationPermission(boolean vista) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                showPermissionExplanationDialog(Manifest.permission.ACCESS_FINE_LOCATION);
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        } else {
            // Permiso ya concedido
            getLastLocation(vista);
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
                .setPositiveButton("Aceptar", (dialog, which) -> ActivityCompat.requestPermissions(TalaArbolActivity.this,
                        new String[]{permission}, requestCode))
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    // ABRIR CAMARA YA CON PERMISO AUTORIZADO
    private void openCamera() {

        String fileName = "photo";
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            File imageFile = File.createTempFile(fileName, ".jpg", storageDirectory);

            currentPhotoPath = imageFile.getAbsolutePath();

            Uri imageUri = FileProvider.getUriForFile(TalaArbolActivity.this, "com.alcaldiasan.santaananorteapp.fileprovider", imageFile);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraLauncher.launch(cameraIntent);
            // startActivityForResult(cameraIntent, 169);

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
                .setPositiveButton("Aceptar", (dialog, which) -> ActivityCompat.requestPermissions(TalaArbolActivity.this,
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

                            if(estadoSolicitud){ // vista solicitud

                                photoBitmapSolicitud = BitmapFactory.decodeStream(imageStream);
                                photoBitmapSolicitud = rotateImageIfRequired(this, photoBitmapSolicitud, imageUri);
                                hayImagenSolicitud = true;

                                Glide.with(this)
                                        .load(photoBitmapSolicitud)
                                        .apply(opcionesGlide)
                                        .into(imgFotoSolicitud);

                            }else{ // vista denuncia
                                photoBitmapDenuncia = BitmapFactory.decodeStream(imageStream);
                                photoBitmapDenuncia = rotateImageIfRequired(this, photoBitmapDenuncia, imageUri);
                                hayImagenDenuncia = true;

                                Glide.with(this)
                                        .load(photoBitmapDenuncia)
                                        .apply(opcionesGlide)
                                        .into(imgFotoDenuncia);
                            }



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

    // OBTENER LOCALIZACION PARA DENUNCIAS
    @SuppressLint("MissingPermission")
    private void getLastLocation(boolean tipoSolicitud) {

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




    private void closeKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();

        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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


}