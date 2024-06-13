package com.alcaldiasan.santaananorteapp.modelos.servicio;

import com.google.gson.annotations.SerializedName;

public class ModeloSolicitud {

    @SerializedName("success")
    public int success;

    @SerializedName("titulo")
    public String titulo;

    @SerializedName("mensaje")
    public String mensaje;


    public int getSuccess() {
        return success;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getMensaje() {
        return mensaje;
    }
}
