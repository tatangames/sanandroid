package com.alcaldiasan.santaananorteapp.modelos.servicio;

import com.google.gson.annotations.SerializedName;

public class ModeloServicio {

    @SerializedName("id")
    public int id;

    @SerializedName("tiposervicio")
    public int tiposervicio;

    @SerializedName("imagen")
    public String imagen;

    @SerializedName("nombre")
    public String nombre;

    @SerializedName("descripcion")
    public String descripcion;




    public String getDescripcion() {
        return descripcion;
    }

    public int getTiposervicio() {
        return tiposervicio;
    }

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
