package com.alcaldiasan.santaananorteapp.modelos.servicio;

import com.google.gson.annotations.SerializedName;

public class ModeloServicio {

    @SerializedName("id")
    public int id;

    @SerializedName("imagen")
    public String imagen;

    @SerializedName("nombre")
    public String nombre;


    public int getId() {
        return id;
    }

    public String getImagen() {
        return imagen;
    }

    public String getNombre() {
        return nombre;
    }
}
