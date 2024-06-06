package com.alcaldiasan.santaananorteapp.modelos.slider;

import com.google.gson.annotations.SerializedName;

public class ModeloSlider {

    @SerializedName("id")
    public int id;

    @SerializedName("imagen")
    public String imagen;



    public ModeloSlider(int id, String imagen) {
        this.id = id;
        this.imagen = imagen;
    }


    public int getId() {
        return id;
    }

    public String getImagen() {
        return imagen;
    }


}
