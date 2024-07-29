package com.alcaldiasan.santaananorteapp.modelos.solicitudes;

import com.alcaldiasan.santaananorteapp.modelos.servicio.ModeloSolicitud;

import java.util.List;

public class ModeloVistaSolicitudes {

    public int tipoVista;

    public static final int TIPO_BASICO = 0;

    public static final int TIPO_SOLI_TALARBOL = 1;

    public static final int TIPO_DENUN_TALAARBOL = 2;

    public static final int TIPO_CATASTRO = 4;


    private ModeloSolicitudBasico modeloSolicitudBasicos;
    private ModeloSolicitudTalaArbol modeloSolicitudTalaArbol;

    private ModeloDenunciaTalaArbol modeloDenunciaTalaArbol;





    public ModeloVistaSolicitudes(int tipoVista, ModeloSolicitudBasico modeloSolicitudBasicos,
                                  ModeloSolicitudTalaArbol modeloSolicitudTalaArbol
    ) {
        this.tipoVista = tipoVista;
        this.modeloSolicitudBasicos = modeloSolicitudBasicos;
        this.modeloSolicitudTalaArbol = modeloSolicitudTalaArbol;
    }


    public int getTipoVista() {
        return tipoVista;
    }

    public ModeloSolicitudBasico getModeloSolicitudBasicos() {
        return modeloSolicitudBasicos;
    }

    public ModeloSolicitudTalaArbol getModeloSolicitudTalaArbol() {
        return modeloSolicitudTalaArbol;
    }
}
