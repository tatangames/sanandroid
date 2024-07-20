package com.alcaldiasan.santaananorteapp.extras;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class MultipartUtil {

    public static MultipartBody createMultipartEnvioDatos(byte[] imageData, String iduser, String tiposervicio, String nota, String latitud, String longitud) {


        RequestBody imagenBody = RequestBody.create(MediaType.parse("image/*"), imageData);

        // Crear el MultipartBody que contiene la imagen, el nombre y el apellido
        return new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("imagen", "image.jpg", imagenBody)
                .addFormDataPart("iduser", iduser)
                .addFormDataPart("idservicio", tiposervicio)
                .addFormDataPart("nota", nota)
                .addFormDataPart("latitud", latitud)
                .addFormDataPart("longitud", longitud)
                .build();
    }


    public static MultipartBody createMultipartSolicitudTalaArbol(byte[] imageData, String iduser, String nota, String latitud, String longitud,
                                                                  String nombre, String telefono, String direccion, int checkEscri) {


        RequestBody imagenBody = RequestBody.create(MediaType.parse("image/*"), imageData);

        // Crear el MultipartBody que contiene la imagen, el nombre y el apellido
        return new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("imagen", "image.jpg", imagenBody)
                .addFormDataPart("iduser", iduser) //*
                .addFormDataPart("nombre", nombre) //*
                .addFormDataPart("telefono", telefono) //*
                .addFormDataPart("direccion", direccion) //*
                .addFormDataPart("escritura", String.valueOf(checkEscri)) //*

                .addFormDataPart("nota", nota)
                .addFormDataPart("latitud", latitud)
                .addFormDataPart("longitud", longitud)

                .build();
    }


    public static MultipartBody createMultipartDenunciaTalaArbol(byte[] imageData, String iduser, String nota, String latitud, String longitud) {


        RequestBody imagenBody = RequestBody.create(MediaType.parse("image/*"), imageData);

        // Crear el MultipartBody que contiene la imagen, el nombre y el apellido
        return new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("imagen", "image.png", imagenBody)
                .addFormDataPart("iduser", iduser)
                .addFormDataPart("nota", nota)
                .addFormDataPart("latitud", latitud)
                .addFormDataPart("longitud", longitud)

                .build();
    }





}
