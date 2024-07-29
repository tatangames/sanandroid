package com.alcaldiasan.santaananorteapp.modelos.solicitudes;

import com.google.gson.annotations.SerializedName;

public class ModeloSolicitudBasico {

    private int id;
    private String estado;
    private String tipoNombre;
    private String nota;
    private String fecha;


    public ModeloSolicitudBasico(int id, String tipoNombre, String estado, String nota, String fecha) {
        this.id = id;
        this.tipoNombre = tipoNombre;
        this.estado = estado;
        this.nota = nota;
        this.fecha = fecha;
    }


    public int getId() {
        return id;
    }

    public String getEstado() {
        return estado;
    }

    public String getTipoNombre() {
        return tipoNombre;
    }

    public String getNota() {
        return nota;
    }

    public String getFecha() {
        return fecha;
    }
}
