package com.alcaldiasan.santaananorteapp.modelos.principal;

import com.alcaldiasan.santaananorteapp.modelos.servicio.ModeloServicio;
import com.alcaldiasan.santaananorteapp.modelos.slider.ModeloSlider;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ModeloPrincipal {

    @SerializedName("success")
    public int success;

    @SerializedName("slider")
    public ArrayList<ModeloSlider> modeloSliders;

    @SerializedName("servicio")
    public List<ModeloServicio> modeloServicio;


    public List<ModeloServicio> getModeloServicio() {
        return modeloServicio;
    }

    public int getSuccess() {
        return success;
    }

    public ArrayList<ModeloSlider> getModeloSliders() {
        return modeloSliders;
    }
}
