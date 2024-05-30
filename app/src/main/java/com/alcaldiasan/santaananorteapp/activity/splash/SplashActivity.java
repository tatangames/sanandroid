package com.alcaldiasan.santaananorteapp.activity.splash;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import com.alcaldiasan.santaananorteapp.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class SplashActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {



    // Estos son codigo cualquiera para verificar permisos
    private static final int CAMERA_PERMISSION_CODE = 101;
    private static final int GALERIA_PERMISSION_CODE = 102;

    private final int sdkVersion = Build.VERSION.SDK_INT;

    private ActivityResultLauncher<Intent> cameraLauncher;

    private Button btnCamara, btnGaleria;
    private ImageView img;



    // ******** PERMISOS (ABRIR GALERIA) ANDROID 13 O SUPERIOR *********

    private final String[] requiredPermission = new String[]{
            Manifest.permission.READ_MEDIA_IMAGES,
    };


    private boolean is_storage_image_permitted = false;

    private boolean allPermissionResultCheck(){
        return is_storage_image_permitted;
    }




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















    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        img = findViewById(R.id.imagen);
        btnCamara = findViewById(R.id.btnCamara);
        btnGaleria = findViewById(R.id.btnGaleria);

        btnCamara.setOnClickListener(v -> {
            verificarPermisoCamara();
        });

        btnGaleria.setOnClickListener(v -> {
            verificarPermisoGaleria();
        });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getExtras() != null) {
                            Bitmap photo = (Bitmap) data.getExtras().get("data");
                            img.setImageBitmap(photo);
                        }
                    }
                }
        );



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


    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(cameraIntent);
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

    private void openAppSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
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

                        cargar(imageUri);
                    }
                }
            }
    );


    private void cargar(Uri uri){
        Glide.with(this)
                .load(uri)
                .apply(opcionesGlide)
                //.circleCrop()
                .into(img);
    }


    RequestOptions opcionesGlide = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .placeholder(R.drawable.camaradefecto)
            .priority(Priority.NORMAL);








}