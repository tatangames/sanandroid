package com.alcaldiasan.santaananorteapp.network;

import com.alcaldiasan.santaananorteapp.modelos.principal.ModeloPrincipal;
import com.alcaldiasan.santaananorteapp.modelos.servicio.ModeloSolicitud;
import com.alcaldiasan.santaananorteapp.modelos.servicio.ModeloSolicitudContenedor;
import com.alcaldiasan.santaananorteapp.modelos.telefono.ModeloVerificacion;
import com.alcaldiasan.santaananorteapp.modelos.usuario.ModeloUsuario;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
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


    // ENVIAR DATOS DE SERVICIOS BASICOS
    @POST("app/servicios/basicos/registrar")
    Observable<ModeloSolicitud> registrarServicioBasico(@Body RequestBody body);


    // ENVIAR DATOS DE SOLICITUD DE TALA ARBOL
    @POST("app/servicios/talaarbol-solicitud/registrar")
    Observable<ModeloSolicitud> registrarSolicitudTalaArbol(@Body RequestBody body);


    // ENVIAR DATOS DE DENUNCIA DE TALA ARBOL
    @POST("app/servicios/talaarbol-denuncia/registrar")
    Observable<ModeloSolicitud> registrarDenunciaTalaArbol(@Body RequestBody body);

    // LISTADO DE SOLICITUDES
    @POST("app/solicitudes/listado")
    @FormUrlEncoded
    Observable<ModeloSolicitudContenedor> listadoSolicitudes(@Field("iduser") String iduser);

    // OCULTAR SOLICITUD
    @POST("app/solicitudes/ocultar")
    @FormUrlEncoded
    Observable<ModeloSolicitudContenedor> ocultarSolicitudes(@Field("id") int id, @Field("tipo") int tipo);










}
