package com.alcaldiasan.santaananorteapp.modelos.servicio;

import com.google.gson.annotations.SerializedName;

public class ModeloSolicitud {


    @SerializedName("success")
    public int success;

    @SerializedName("id")
    public int id;


    @SerializedName("tipo")
    public int tipo;

    @SerializedName("titulo")
    public String titulo;

    @SerializedName("mensaje")
    public String mensaje;


    @SerializedName("nombretipo")
    public String nombretipo;


    @SerializedName("estado")
    public String estado;

    @SerializedName("nota")
    public String nota;

    @SerializedName("fecha")
    public String fecha;

    @SerializedName("nombre")
    public String nombre;

    @SerializedName("telefono")
    public String telefono;

    @SerializedName("direccion")
    public String direccion;

    @SerializedName("escritura")
    public int escritura;

    @SerializedName("dui")
    public String dui;

    @SerializedName("imagen")
    public String imagen;

    public String getImagen() {
        return imagen;
    }

    public int getId() {
        return id;
    }

    public String getNombretipo() {
        return nombretipo;
    }

    public String getEstado() {
        return estado;
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

    public int getEscritura() {
        return escritura;
    }

    public String getDui() {
        return dui;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public int getSuccess() {
        return success;
    }


    public int getTipo() {
        return tipo;
    }
}
