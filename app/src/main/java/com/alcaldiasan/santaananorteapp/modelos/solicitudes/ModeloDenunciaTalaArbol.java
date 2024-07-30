package com.alcaldiasan.santaananorteapp.modelos.solicitudes;

public class ModeloDenunciaTalaArbol {

    private int id;
    private String estado;
    private String nombreTipo;
    private String nota;
    private String fecha;

    private int tipo;


    public ModeloDenunciaTalaArbol(int id, String nombreTipo, String estado, String nota, String fecha, int tipo) {
        this.id = id;
        this.nombreTipo = nombreTipo;
        this.estado = estado;
        this.nota = nota;
        this.fecha = fecha;
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

    public String getNombreTipo() {
        return nombreTipo;
    }

    public String getNota() {
        return nota;
    }

    public String getFecha() {
        return fecha;
    }

}
