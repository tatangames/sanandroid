package com.alcaldiasan.santaananorteapp.network;

import com.alcaldiasan.santaananorteapp.modelos.principal.ModeloPrincipal;
import com.alcaldiasan.santaananorteapp.modelos.telefono.ModeloVerificacion;
import com.alcaldiasan.santaananorteapp.modelos.usuario.ModeloUsuario;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    // VERIFICACION DE NUMERO
    @POST("app/verificacion/telefono")
    @FormUrlEncoded
    Observable<ModeloVerificacion> verificacionTelefono(@Field("telefono") String telefono);


    // REINTENTO DE ENVIAR SMS CODIGO
    @POST("app/reintento/telefono")
    @FormUrlEncoded
    Observable<ModeloVerificacion> reintentoSMS(@Field("telefono") String telefono);


    // VERIFICACION DEL CODIGO Y TELEFONO
    @POST("app/verificarcodigo/telefono")
    @FormUrlEncoded
    Observable<ModeloUsuario> verificarCodigo(@Field("telefono") String telefono,
                                              @Field("codigo") String codigo,
                                              @Field("idonesignal") String onesignal);


    // LISTADO PRINCIPAL DE LA APLICACION
    @POST("app/principal/listado")
    @FormUrlEncoded
    Observable<ModeloPrincipal> listadoPrincipal(@Field("codeapp") int versionCode);





















}
