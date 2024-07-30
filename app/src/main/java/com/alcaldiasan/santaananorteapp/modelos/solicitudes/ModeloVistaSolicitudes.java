package com.alcaldiasan.santaananorteapp.modelos.solicitudes;

import com.alcaldiasan.santaananorteapp.modelos.servicio.ModeloSolicitud;

import java.util.List;

public class ModeloVistaSolicitudes {

    public int tipoVista;

    public static final int TIPO_BASICO = 0;

    public static final int TIPO_SOLI_TALARBOL = 1;

    public static final int TIPO_DENUN_TALAARBOL = 2;

    public static final int TIPO_CATASTRO = 4;

    public static final int TIPO_SIN_SOLICITUDES = 5;


    private ModeloSolicitudBasico modeloSolicitudBasicos;
    private ModeloSolicitudTalaArbol modeloSolicitudTalaArbol;
    private ModeloDenunciaTalaArbol modeloDenunciaTalaArbol;

    private ModeloSolicitudCatastro modeloSolicitudCatastro;




    public ModeloVistaSolicitudes(int tipoVista, ModeloSolicitudBasico modeloSolicitudBasicos,
                                  ModeloSolicitudTalaArbol modeloSolicitudTalaArbol,
                                  ModeloDenunciaTalaArbol modeloDenunciaTalaArbol,
                                  ModeloSolicitudCatastro modeloSolicitudCatastro
    ) {
        this.tipoVista = tipoVista;
        this.modeloSolicitudBasicos = modeloSolicitudBasicos;
        this.modeloSolicitudTalaArbol = modeloSolicitudTalaArbol;
        this.modeloDenunciaTalaArbol = modeloDenunciaTalaArbol;
        this.modeloSolicitudCatastro = modeloSolicitudCatastro;
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

    public ModeloDenunciaTalaArbol getModeloDenunciaTalaArbol() {
        return modeloDenunciaTalaArbol;
    }

    public ModeloSolicitudCatastro getModeloSolicitudCatastro() {
        return modeloSolicitudCatastro;
    }
}
