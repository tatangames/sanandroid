package com.alcaldiasan.santaananorteapp.modelos.servicio;

import com.google.gson.annotations.SerializedName;

public class ModeloServicio {

    @SerializedName("id")
    public int id;

    @SerializedName("id_tiposervicio")
    public int idTipoServicio;

    @SerializedName("imagen")
    public String imagen;

    @SerializedName("nombre")
    public String nombre;

    @SerializedName("descripcion")
    public String descripcion;


    public ModeloServicio(int id, int idTipoServicio, String imagen, String nombre, String descripcion) {
        this.id = id;
        this.idTipoServicio = idTipoServicio;
        this.imagen = imagen;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public ModeloServicio(int idTipoServicio) {
        this.idTipoServicio = idTipoServicio;
    }

    public int getIdTipoServicio() {
        return idTipoServicio;
    }

    public int getId() {
        return id;
    }

    public String getImagen() {
        return imagen;
    }

    public String getNombre() {
        return nombre;
    }
}
