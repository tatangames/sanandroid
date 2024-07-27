package com.alcaldiasan.santaananorteapp.modelos.solicitudes;

import com.google.gson.annotations.SerializedName;

public class ModeloCatastro {

    @SerializedName("nombre")
    public String nombre;

    @SerializedName("dui")
    public String dui;








    public String getNombre() {
        return nombre;
    }

    public String getDui() {
        return dui;
    }
}
