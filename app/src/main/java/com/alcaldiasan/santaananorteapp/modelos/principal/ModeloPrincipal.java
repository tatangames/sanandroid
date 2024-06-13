package com.alcaldiasan.santaananorteapp.modelos.principal;

import com.alcaldiasan.santaananorteapp.modelos.servicio.ModeloTipoServicio;
import com.alcaldiasan.santaananorteapp.modelos.slider.ModeloSlider;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ModeloPrincipal {

    @SerializedName("success")
    public int success;

    @SerializedName("codeandroid")
    public int codeandroid;

    public int getCodeandroid() {
        return codeandroid;
    }

    @SerializedName("slider")
    public ArrayList<ModeloSlider> modeloSliders;

    @SerializedName("tiposervicio")
    public List<ModeloTipoServicio> modeloTipoServicios;



    public int getSuccess() {
        return success;
    }

    public ArrayList<ModeloSlider> getModeloSliders() {
        return modeloSliders;
    }


    public List<ModeloTipoServicio> getModeloTipoServicios() {
        return modeloTipoServicios;
    }
}
