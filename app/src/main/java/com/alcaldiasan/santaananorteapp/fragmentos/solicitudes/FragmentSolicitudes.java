package com.alcaldiasan.santaananorteapp.fragmentos.solicitudes;

import static android.content.Context.MODE_PRIVATE;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alcaldiasan.santaananorteapp.R;
import com.alcaldiasan.santaananorteapp.activity.principal.PrincipalActivity;
import com.alcaldiasan.santaananorteapp.adaptadores.servicio.AdaptadorSolicitudes;
import com.alcaldiasan.santaananorteapp.modelos.principal.ModeloVistaPrincipal;
import com.alcaldiasan.santaananorteapp.modelos.servicio.ModeloSolicitud;
import com.alcaldiasan.santaananorteapp.modelos.solicitudes.ModeloDenunciaTalaArbol;
import com.alcaldiasan.santaananorteapp.modelos.solicitudes.ModeloSolicitudBasico;
import com.alcaldiasan.santaananorteapp.modelos.solicitudes.ModeloSolicitudCatastro;
import com.alcaldiasan.santaananorteapp.modelos.solicitudes.ModeloSolicitudTalaArbol;
import com.alcaldiasan.santaananorteapp.modelos.solicitudes.ModeloVistaSolicitudes;
import com.alcaldiasan.santaananorteapp.network.ApiService;
import com.alcaldiasan.santaananorteapp.network.RetrofitBuilder;
import com.alcaldiasan.santaananorteapp.network.TokenManager;
import com.developer.kalert.KAlertDialog;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class FragmentSolicitudes extends Fragment {

    private boolean seguroModal = true;
    private RelativeLayout rootRelative;

    private ApiService service;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private TokenManager tokenManager;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private AdaptadorSolicitudes adaptadorSolicitudes;

    private ArrayList<ModeloVistaSolicitudes> elementos = new ArrayList<ModeloVistaSolicitudes>();;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_solicitudes, container, false);

        ((PrincipalActivity)getActivity()).setActionBarTitle(getString(R.string.solicitudes));

        rootRelative = vista.findViewById(R.id.rootRelative);
        recyclerView = vista.findViewById(R.id.recyclerView);

        int colorProgress = ContextCompat.getColor(getContext(), R.color.barraProgreso);

        tokenManager = TokenManager.getInstance(getActivity().getSharedPreferences("prefs", MODE_PRIVATE));
        service = RetrofitBuilder.createServiceAutentificacion(ApiService.class, tokenManager);

        progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleLarge);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        rootRelative.addView(progressBar, params);
        progressBar.getIndeterminateDrawable().setColorFilter(colorProgress, PorterDuff.Mode.SRC_IN);

        apiBuscarSolicitudes();
        return vista;
    }


    private void apiBuscarSolicitudes(){

        String iduser = tokenManager.getToken().getId();

        compositeDisposable.add(
                service.listadoSolicitudes(iduser)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .retry()
                        .subscribe(apiRespuesta -> {

                                    progressBar.setVisibility(View.GONE);
                                    if(apiRespuesta != null) {

                                        if(apiRespuesta.getSuccess() == 1) {

                                            if(apiRespuesta.getHaydatos() == 1){
                                                for(ModeloSolicitud mm : apiRespuesta.getModeloSolicitud()){

                                                    if(mm.getTipo() == 1){ // denuncias basicas

                                                        ModeloSolicitudBasico basico = new ModeloSolicitudBasico(mm.getId(), mm.getNombretipo(), mm.getEstado(), mm.getNota(), mm.getFecha(), mm.getTipo());

                                                        elementos.add(new ModeloVistaSolicitudes(ModeloVistaSolicitudes.TIPO_BASICO, basico, null, null, null));
                                                    }
                                                    else if(mm.getTipo() == 2){// solicitud tala de arbol

                                                        ModeloSolicitudTalaArbol mSoliTalaArbol = new ModeloSolicitudTalaArbol(mm.getId(), mm.getNombretipo(), mm.getEstado(), mm.getNota(), mm.getFecha(),
                                                                mm.getNombre(), mm.getTelefono(), mm.getDireccion(), mm.getImagen(), mm.getTipo());

                                                        elementos.add(new ModeloVistaSolicitudes(ModeloVistaSolicitudes.TIPO_SOLI_TALARBOL, null, mSoliTalaArbol, null, null));
                                                    }
                                                    else if(mm.getTipo() == 3){// denuncia tala de arbol

                                                        ModeloDenunciaTalaArbol mDenunTalaArbol = new ModeloDenunciaTalaArbol(mm.getId(), mm.getNombretipo(), mm.getEstado(), mm.getNota(), mm.getFecha(), mm.getTipo());


                                                        elementos.add(new ModeloVistaSolicitudes(ModeloVistaSolicitudes.TIPO_DENUN_TALAARBOL, null, null, mDenunTalaArbol, null));
                                                    }
                                                    else if(mm.getTipo() == 4){// catastro

                                                        ModeloSolicitudCatastro mCatastro = new ModeloSolicitudCatastro(mm.getId(), mm.getNombretipo(), mm.getEstado(), mm.getFecha(), mm.getNombre(), mm.getDui(), mm.getTipo());

                                                        elementos.add(new ModeloVistaSolicitudes(ModeloVistaSolicitudes.TIPO_CATASTRO, null, null, null, mCatastro));
                                                    }
                                                }
                                            }else{

                                            // VISTA SIN SOLICITUDES

                                                elementos.add(new ModeloVistaSolicitudes(ModeloVistaSolicitudes.TIPO_SIN_SOLICITUDES, null, null, null, null));
                                            }


                                            adaptadorSolicitudes = new AdaptadorSolicitudes(getContext(), elementos, this);
                                            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                                            recyclerView.setAdapter(adaptadorSolicitudes);

                                            recyclerView.setVisibility(View.VISIBLE);


                                        }else{
                                            mensajeSinConexion();
                                        }
                                    }else{
                                        mensajeSinConexion();
                                    }
                                },
                                throwable -> {

                                    mensajeSinConexion();
                                })
        );
    }


    void mensajeSinConexion(){
        progressBar.setVisibility(View.GONE);
        Toasty.error(getContext(), getString(R.string.error_intentar_de_nuevo)).show();
    }


    @Override
    public void onDestroy(){
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        if(compositeDisposable != null){
            compositeDisposable.clear();
        }
        super.onStop();
    }




    public void modalBorrar(int id, int tipo){

        // Tipo para apuntar a que tabla setear

        if(seguroModal) {
            seguroModal = false;

            KAlertDialog pDialog = new KAlertDialog(getContext(), KAlertDialog.WARNING_TYPE, false);

            pDialog.setTitleText(getString(R.string.ocultar_solicitud));
            pDialog.setTitleTextGravity(Gravity.CENTER);
            pDialog.setTitleTextSize(19);

            pDialog.setContentText("");
            pDialog.setContentTextAlignment(View.TEXT_ALIGNMENT_VIEW_START, Gravity.START);
            pDialog.setContentTextSize(17);

            pDialog.setCancelable(false);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.confirmButtonColor(R.drawable.codigo_kalert_dialog_corners_confirmar);
            pDialog.setConfirmClickListener(getString(R.string.si), sDialog -> {
                sDialog.dismissWithAnimation();

                solicitarOcultar(id, tipo);
            });

            pDialog.cancelButtonColor(R.drawable.codigo_kalert_dialog_corners_cancelar);
            pDialog.setCancelClickListener(getString(R.string.no), sDialog -> {
                sDialog.dismissWithAnimation();
                seguroModal = true;
            });

            pDialog.show();
        }
    }

    private void solicitarOcultar(int id, int tipo){

        progressBar.setVisibility(View.VISIBLE);

        compositeDisposable.add(
                service.ocultarSolicitudes(id, tipo)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .retry()
                        .subscribe(apiRespuesta -> {

                                    progressBar.setVisibility(View.GONE);
                                    seguroModal = true;

                                    if(apiRespuesta != null) {

                                        if(apiRespuesta.getSuccess() == 1) {

                                            Toasty.success(getContext(), getString(R.string.ocultado)).show();
                                            recyclerView.setVisibility(View.GONE);
                                            elementos.clear();
                                            apiBuscarSolicitudes();
                                        }else{
                                            mensajeSinConexion();
                                        }
                                    }else{
                                        mensajeSinConexion();
                                    }
                                },
                                throwable -> {

                                    mensajeSinConexion();
                                })
        );
    }
}
