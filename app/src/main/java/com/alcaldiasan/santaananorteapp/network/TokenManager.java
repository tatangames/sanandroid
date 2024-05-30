package com.alcaldiasan.santaananorteapp.network;


import android.content.SharedPreferences;

import com.alcaldiasan.santaananorteapp.modelos.usuario.ModeloUsuario;

public class TokenManager {


    // GUARDAMOS DATOS DENTRO DE LA APP

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private static TokenManager INSTANCE = null;

    private TokenManager(SharedPreferences prefs) {
        this.prefs = prefs;
        this.editor = prefs.edit();
    }

    public static synchronized TokenManager getInstance(SharedPreferences prefs) {
        if (INSTANCE == null) {
            INSTANCE = new TokenManager(prefs);
        }
        return INSTANCE;
    }


    // ID DEL USUARIO
    public void guardarClienteID(ModeloUsuario token) {
        editor.putString("ID", token.getId()).commit();
    }

    // TOKEN DE SEGURIDAD DEL SERVIDOR
    public void guardarClienteTOKEN(ModeloUsuario token) {
        editor.putString("TOKEN", token.getToken()).commit();
    }


    // BORRAR UNAS REFERENCIAS
    public void deletePreferences(){
        editor.remove("ID").commit();
        editor.remove("TOKEN").commit();
    }

    public ModeloUsuario getToken(){
        ModeloUsuario token = new ModeloUsuario();
        token.setId(prefs.getString("ID", ""));
        token.setToken(prefs.getString("TOKEN", ""));

        return token;
    }
}
