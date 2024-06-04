package com.alcaldiasan.santaananorteapp.modelos.slider;

import com.google.gson.annotations.SerializedName;

public class ModeloSlider {

    @SerializedName("id")
    public int id;

    @SerializedName("imagen")
    public String imagen;

    @SerializedName("nombre")
    public String nombre;


    public ModeloSlider(int id, String imagen, String nombre) {
        this.id = id;
        this.imagen = imagen;
        this.nombre = nombre;
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
