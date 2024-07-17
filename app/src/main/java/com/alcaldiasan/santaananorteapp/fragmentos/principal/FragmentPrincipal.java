package com.alcaldiasan.santaananorteapp.fragmentos.principal;

import static android.content.Context.MODE_PRIVATE;

import static androidx.browser.customtabs.CustomTabsClient.getPackageName;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.alcaldiasan.santaananorteapp.R;
import com.alcaldiasan.santaananorteapp.activity.login.LoginActivity;
import com.alcaldiasan.santaananorteapp.activity.servicios.basico.ServicioBasicoActivity;
import com.alcaldiasan.santaananorteapp.activity.servicios.talaarbol.TalaArbolActivity;
import com.alcaldiasan.santaananorteapp.adaptadores.principal.AdaptadorPrincipal;
import com.alcaldiasan.santaananorteapp.modelos.principal.ModeloVistaPrincipal;
import com.alcaldiasan.santaananorteapp.modelos.servicio.ModeloTipoServicio;
import com.alcaldiasan.santaananorteapp.network.ApiService;
import com.alcaldiasan.santaananorteapp.network.RetrofitBuilder;
import com.alcaldiasan.santaananorteapp.network.TokenManager;
import com.developer.kalert.KAlertDialog;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.dmoral.toasty.Toasty;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class FragmentPrincipal extends Fragment {

    private ApiService service;
    private ProgressBar progressBar;
    private TokenManager tokenManager;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private RelativeLayout rootRelative;
    private ShimmerFrameLayout shimmerFrameLayout;
    private LinearLayout contenedorShimmer, contenedorPrincipal;
    private RecyclerView recyclerView;
    private AdaptadorPrincipal adaptadorPrincipal;
    private ArrayList<ModeloVistaPrincipal> elementos = new ArrayList<>();;

    // VERSION DE LA APLICACION
    private int versionApp = -1;


    // FORMA PARA MOSTRAR AL USUARIO SI HAY UN NUEVO TIPO SERVICIO Y QUE ACTUALICE APLICACION

    // 1- SERVICIO BASICO (bacheo, alumbrado, desechos solidos)
    public static final List<Integer> SUPPORTED_TYPES = Arrays.asList(1,2);

    private boolean boolCartelUpdateServicio = true;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_principal, container, false);

        rootRelative = vista.findViewById(R.id.rootRelative);
        contenedorPrincipal = vista.findViewById(R.id.contenedorPrincipal);
        shimmerFrameLayout = vista.findViewById(R.id.shimmer);
        contenedorShimmer = vista.findViewById(R.id.contenedorShimmer);
        recyclerView = vista.findViewById(R.id.recyclerView);

        tokenManager = TokenManager.getInstance(getActivity().getSharedPreferences("prefs", MODE_PRIVATE));
        service = RetrofitBuilder.createServiceAutentificacion(ApiService.class, tokenManager);

        int colorProgress = ContextCompat.getColor(requireContext(), R.color.barraProgreso);
        progressBar = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleLarge);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        rootRelative.addView(progressBar, params);
        progressBar.getIndeterminateDrawable().setColorFilter(colorProgress, PorterDuff.Mode.SRC_IN);
        progressBar.setVisibility(View.GONE);

        shimmerFrameLayout.startShimmer();

        // SI DA ERROR, EL SERVIDOR TOMARA EL -1 SU VALOR POR DEFECTO, Y NO MOSTRARA ALERTA
        // DE NUEVA ACTUALIZACION
        try {
            PackageInfo pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            versionApp = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        apiSolicitarDatos();

        return vista;
    }


    // CUANDO INICIA SE SOLICITARA LOS DATOS
    private void apiSolicitarDatos(){

        compositeDisposable.add(
                service.listadoPrincipal(versionApp)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .retry()
                        .subscribe(apiRespuesta -> {

                            if(apiRespuesta != null) {

                                if(apiRespuesta.getSuccess() == 1) {
                                   // USUARIO BLOQUEADO
                                    usuarioBloqueado();
                                }
                                else if(apiRespuesta.getSuccess() == 2) {

                                    elementos.add(new ModeloVistaPrincipal(ModeloVistaPrincipal.TIPO_SLIDER, apiRespuesta.getModeloSliders(), null, null));

                                    for(ModeloTipoServicio mm : apiRespuesta.getModeloTipoServicios()){

                                        elementos.add(new ModeloVistaPrincipal(ModeloVistaPrincipal.TIPO_TEXTVIEW, null, null, mm.getNombre()));

                                        elementos.add(new ModeloVistaPrincipal(ModeloVistaPrincipal.TIPO_RECYCLER, null, mm.getModeloServicios(), null));
                                    }

                                    adaptadorPrincipal = new AdaptadorPrincipal(getContext(), elementos, this);
                                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                                    recyclerView.setAdapter(adaptadorPrincipal);

                                    int hayUpdate = apiRespuesta.getCodeandroid();

                                    esperarInicio(hayUpdate);
                                }
                                else{
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

    private void esperarInicio(int hayUpdate){
        new Handler().postDelayed(() -> {
            shimmerFrameLayout.stopShimmer();
            contenedorShimmer.setVisibility(View.GONE);
            contenedorPrincipal.setVisibility(View.VISIBLE);

            if(hayUpdate == 1){
                dialogoActualizacionServidor();
            }

        }, 500);
    }

    // PARA CUANDO EL SERVIDOR ME DICE QUE HAY UNA NUEVA ACTUALIZACION
    private void dialogoActualizacionServidor(){

        KAlertDialog pDialog = new KAlertDialog(getContext(), KAlertDialog.CUSTOM_IMAGE_TYPE, false);

        pDialog.setCustomImage(R.drawable.ic_informacion);

        pDialog.setTitleText(getString(R.string.nueva_actualizacion));
        pDialog.setTitleTextGravity(Gravity.CENTER);
        pDialog.setTitleTextSize(19);

        pDialog.setContentText(getString(R.string.actualizar_nueva_version));
        pDialog.setContentTextAlignment(View.TEXT_ALIGNMENT_CENTER, Gravity.START);
        pDialog.setContentTextSize(17);

        pDialog.setCancelable(false);
        pDialog.setCanceledOnTouchOutside(false);

        pDialog.confirmButtonColor(R.drawable.codigo_kalert_dialog_corners_confirmar);
        pDialog.setConfirmClickListener(getString(R.string.actualizar), sDialog -> {
            sDialog.dismissWithAnimation();
            redireccionarGooglePlay();
        });

        pDialog.cancelButtonColor(R.drawable.codigo_kalert_dialog_corners_cancelar);
        pDialog.setCancelClickListener(getString(R.string.no), sDialog -> {
            sDialog.dismissWithAnimation();

        });

        pDialog.show();
    }

    private void redireccionarGooglePlay(){

        try {
            String playStoreLink = "https://play.google.com/store/apps/details?id=com.alcaldiasan.santaananorteapp";

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(playStoreLink));
            startActivity(intent);
        } catch (Exception ignored) {

        }
    }


    private void usuarioBloqueado(){
        KAlertDialog pDialog = new KAlertDialog(getContext(), KAlertDialog.ERROR_TYPE, false);

        pDialog.setTitleText(getString(R.string.bloqueado));
        pDialog.setTitleTextGravity(Gravity.CENTER);
        pDialog.setTitleTextSize(19);

        pDialog.setContentText(getString(R.string.numero_bloqueado));
        pDialog.setContentTextAlignment(View.TEXT_ALIGNMENT_CENTER, Gravity.START);
        pDialog.setContentTextSize(17);

        pDialog.setCancelable(false);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.confirmButtonColor(R.drawable.codigo_kalert_dialog_corners_confirmar);
        pDialog.setConfirmClickListener(getString(R.string.aceptar), sDialog -> {
            sDialog.dismissWithAnimation();
            salir();
        });
        pDialog.show();
    }


    private void salir(){
        tokenManager.deletePreferences();
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    public void servicioSeleccionado(int tipoServicio, int id, String titulo, String descripcion){

        // Verificar si el tipo de servicio está soportado
        if (SUPPORTED_TYPES.contains(tipoServicio)) {

            if(tipoServicio == 1){
                // SERVICIO BASICO

                Intent intent = new Intent(getActivity(), ServicioBasicoActivity.class);
                intent.putExtra("KEY_IDSERVICIO", id);
                intent.putExtra("KEY_TITULO", titulo);
                intent.putExtra("KEY_NOTA", descripcion); // DESCRIPCION
                startActivity(intent);
            }
            else if(tipoServicio == 2){

                // DENUNCIA DE TALA DE ARBOL
                Intent intent = new Intent(getActivity(), TalaArbolActivity.class);
                intent.putExtra("KEY_IDSERVICIO", id);
                intent.putExtra("KEY_TITULO", titulo);
                intent.putExtra("KEY_NOTA", descripcion); // DESCRIPCION
                startActivity(intent);
            }

        }else{
            dialogoNuevoServicioDisponible();
        }
    }


    // PARA CUANDO NO ESTA EL SERVICIO QUE QUIERO ACCEDER
    private void dialogoNuevoServicioDisponible(){

        if(boolCartelUpdateServicio){
            boolCartelUpdateServicio = false;

            new Handler().postDelayed(() -> {
                boolCartelUpdateServicio = true;
            }, 2000);

            KAlertDialog pDialog = new KAlertDialog(getContext(), KAlertDialog.CUSTOM_IMAGE_TYPE, false);

            pDialog.setCustomImage(R.drawable.ic_informacion);

            pDialog.setTitleText(getString(R.string.nueva_actualizacion));
            pDialog.setTitleTextGravity(Gravity.CENTER);
            pDialog.setTitleTextSize(19);

            pDialog.setContentText(getString(R.string.por_favor_actualizar_la_app));
            pDialog.setContentTextAlignment(View.TEXT_ALIGNMENT_CENTER, Gravity.START);
            pDialog.setContentTextSize(17);

            pDialog.setCancelable(false);
            pDialog.setCanceledOnTouchOutside(false);

            pDialog.confirmButtonColor(R.drawable.codigo_kalert_dialog_corners_confirmar);
            pDialog.setConfirmClickListener(getString(R.string.actualizar), sDialog -> {
                sDialog.dismissWithAnimation();
                redireccionarGooglePlay();
            });

            pDialog.cancelButtonColor(R.drawable.codigo_kalert_dialog_corners_cancelar);
            pDialog.setCancelClickListener(getString(R.string.no), sDialog -> {
                sDialog.dismissWithAnimation();

            });

            pDialog.show();
        }
    }



    private void mensajeSinConexion(){
        progressBar.setVisibility(View.GONE);
        Toasty.error(getActivity(), getString(R.string.error_intentar_de_nuevo)).show();
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
}
