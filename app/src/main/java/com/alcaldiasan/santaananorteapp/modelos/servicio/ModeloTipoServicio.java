package com.alcaldiasan.santaananorteapp.modelos.servicio;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ModeloTipoServicio {

    @SerializedName("id")
    public int id;

    @SerializedName("nombre")
    public String nombre;

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    @SerializedName("lista")
    public List<ModeloServicio> modeloServicios = null;


    public List<ModeloServicio> getModeloServicios() {
        return modeloServicios;
    }
}
