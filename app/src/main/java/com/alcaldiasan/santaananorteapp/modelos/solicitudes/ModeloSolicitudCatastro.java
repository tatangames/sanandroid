package com.alcaldiasan.santaananorteapp.modelos.solicitudes;

public class ModeloSolicitudCatastro {

    private int id;
    private String estado;
    private String nombreTipo;
    private String nombre;
    private String fecha;
    private String dui;

    private int tipo;

    public ModeloSolicitudCatastro(int id, String nombreTipo, String estado, String fecha, String nombre, String dui, int tipo) {
        this.id = id;
        this.nombreTipo = nombreTipo;
        this.estado = estado;
        this.fecha = fecha;
        this.nombre = nombre;
        this.dui = dui;
        this.tipo = tipo;
    }

    public int getTipo() {
        return tipo;
    }

    public int getId() {
        return id;
    }

    public String getEstado() {
        return estado;
    }

    public String getFecha() {
        return fecha;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDui() {
        return dui;
    }

    public String getNombreTipo() {
        return nombreTipo;
    }
}
