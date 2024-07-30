package com.alcaldiasan.santaananorteapp.modelos.solicitudes;

import com.google.gson.annotations.SerializedName;

public class ModeloSolicitudTalaArbol {

    private int id;
    private String estado;
    private String nombreTipo;
    private String nota;
    private String fecha;
    private String nombre;
    private String telefono;
    private String direccion;
    private String imagen;
    private int tipo;


    public ModeloSolicitudTalaArbol(int id, String nombreTipo, String estado, String nota, String fecha,
                                    String nombre, String telefono, String direccion, String imagen, int tipo) {
        this.id = id;
        this.nombreTipo = nombreTipo;
        this.estado = estado;
        this.nota = nota;
        this.fecha = fecha;
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
        this.imagen = imagen;
        this.tipo = tipo;
    }

    public int getTipo() {
        return tipo;
    }

    public String getImagen() {
        return imagen;
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

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getDireccion() {
        return direccion;
    }
}
