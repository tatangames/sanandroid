package com.alcaldiasan.santaananorteapp.modelos.solicitudes;

import com.alcaldiasan.santaananorteapp.modelos.servicio.ModeloSolicitud;

import java.util.List;

public class ModeloVistaSolicitudes {

    public int tipoVista;

    public static final int TIPO_BASICO = 0;

    public static final int TIPO_SOLI_TALARBOL = 1;

    public static final int TIPO_DENUN_TALAARBOL = 2;

    public static final int TIPO_CATASTRO = 4;



    private List<ModeloSolicitud> modeloSolicitudes;


    public ModeloVistaSolicitudes(int tipoVista, List<ModeloSolicitud> modeloSolicitudes
    ) {
        this.tipoVista = tipoVista;
        this.modeloSolicitudes = modeloSolicitudes;
    }


    public int getTipoVista() {
        return tipoVista;
    }

    public List<ModeloSolicitud> getModeloSolicitudes() {
        return modeloSolicitudes;
    }
}
