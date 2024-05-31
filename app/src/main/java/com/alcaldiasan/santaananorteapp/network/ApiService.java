package com.alcaldiasan.santaananorteapp.network;

import com.alcaldiasan.santaananorteapp.modelos.telefono.ModeloVerificacion;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {

    @POST("app/verificacion/telefono")
    @FormUrlEncoded
    Observable<ModeloVerificacion> verificacionTelefono(@Field("telefono") String telefono);






}
