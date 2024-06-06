package com.alcaldiasan.santaananorteapp.modelos.principal;

import com.alcaldiasan.santaananorteapp.modelos.servicio.ModeloServicio;
import com.alcaldiasan.santaananorteapp.modelos.slider.ModeloSlider;

import java.util.ArrayList;
import java.util.List;

public class ModeloVistaPrincipal {

    // CONTENEDOR PARA 2 VISTAS PARA CUANDO SE INICIA LA APLICACION

    public int tipoVista;

    public static final int TIPO_SLIDER = 0;

    public static final int TIPO_RECYCLER = 1;


    private ArrayList<ModeloSlider> modeloSliders;

    private List<ModeloServicio> modeloServicios;



    public ModeloVistaPrincipal(int tipoVista, ArrayList<ModeloSlider> modeloSliders,
                            List<ModeloServicio> modeloServicios

    ) {
        this.tipoVista = tipoVista;
        this.modeloSliders = modeloSliders;
        this.modeloServicios = modeloServicios;
    }

    public int getTipoVista() {
        return tipoVista;
    }


    public ArrayList<ModeloSlider> getModeloSliders() {
        return modeloSliders;
    }

    public List<ModeloServicio> getModeloServicios() {
        return modeloServicios;
    }
}
