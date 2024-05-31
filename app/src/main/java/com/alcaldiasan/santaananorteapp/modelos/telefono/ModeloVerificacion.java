package com.alcaldiasan.santaananorteapp.modelos.telefono;

import com.google.gson.annotations.SerializedName;

public class ModeloVerificacion {

    @SerializedName("success")
    public int success;
    @SerializedName("canretry")
    public boolean canRetry;

    @SerializedName("segundos")
    public int segundos;

    public int getSuccess() {
        return success;
    }

    public boolean isCanRetry() {
        return canRetry;
    }

    public int getSegundos() {
        return segundos;
    }
}
