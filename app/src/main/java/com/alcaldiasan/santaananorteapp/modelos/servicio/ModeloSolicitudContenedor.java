package com.alcaldiasan.santaananorteapp.modelos.servicio;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ModeloSolicitudContenedor {

    @SerializedName("success")
    public int success;


    @SerializedName("listado")
    public List<ModeloSolicitud> modeloSolicitud;


    public int getSuccess() {
        return success;
    }

    public List<ModeloSolicitud> getModeloSolicitud() {
        return modeloSolicitud;
    }
}
