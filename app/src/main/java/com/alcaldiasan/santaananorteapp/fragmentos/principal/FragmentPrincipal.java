package com.alcaldiasan.santaananorteapp.fragmentos.principal;

import static android.content.Context.MODE_PRIVATE;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
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
import com.alcaldiasan.santaananorteapp.adaptadores.principal.AdaptadorPrincipal;
import com.alcaldiasan.santaananorteapp.adaptadores.servicio.AdaptadorServicio;
import com.alcaldiasan.santaananorteapp.modelos.principal.ModeloVistaPrincipal;
import com.alcaldiasan.santaananorteapp.network.ApiService;
import com.alcaldiasan.santaananorteapp.network.RetrofitBuilder;
import com.alcaldiasan.santaananorteapp.network.TokenManager;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;

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
        apiSolicitarDatos();

        return vista;
    }


    // CUANDO INICIA SE SOLICITARA LOS DATOS
    private void apiSolicitarDatos(){

        compositeDisposable.add(
                service.listadoPrincipal()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .retry()
                        .subscribe(apiRespuesta -> {

                            if(apiRespuesta != null) {

                                if(apiRespuesta.getSuccess() == 1) {

                                    elementos.add(new ModeloVistaPrincipal(ModeloVistaPrincipal.TIPO_SLIDER, apiRespuesta.getModeloSliders(), null));
                                    elementos.add(new ModeloVistaPrincipal(ModeloVistaPrincipal.TIPO_RECYCLER, null, apiRespuesta.getModeloServicio()));

                                    adaptadorPrincipal = new AdaptadorPrincipal(getContext(), elementos, this);
                                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                                    recyclerView.setAdapter(adaptadorPrincipal);

                                    esperarInicio();
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

    private void esperarInicio(){
        new Handler().postDelayed(() -> {
            shimmerFrameLayout.stopShimmer();
            contenedorShimmer.setVisibility(View.GONE);
            contenedorPrincipal.setVisibility(View.VISIBLE);
        }, 500);
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
