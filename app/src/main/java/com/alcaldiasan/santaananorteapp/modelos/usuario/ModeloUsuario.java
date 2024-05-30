package com.alcaldiasan.santaananorteapp.modelos.usuario;

import com.google.gson.annotations.SerializedName;

public class ModeloUsuario {

    @SerializedName("success")
    public int success;

    @SerializedName("id")
    public String id;

    @SerializedName("token")
    public String token;

    @SerializedName("nombre")
    public String nombre;

    @SerializedName("imagen")
    private String imagen;


    public int getSuccess() {
        return success;
    }

    public String getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public String getNombre() {
        return nombre;
    }

    public String getImagen() {
        return imagen;
    }


    public void setSuccess(int success) {
        this.success = success;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
}
