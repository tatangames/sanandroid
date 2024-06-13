package com.alcaldiasan.santaananorteapp.adaptadores.principal;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alcaldiasan.santaananorteapp.R;
import com.alcaldiasan.santaananorteapp.adaptadores.servicio.AdaptadorServicio;
import com.alcaldiasan.santaananorteapp.adaptadores.slider.AdaptadorSlider;
import com.alcaldiasan.santaananorteapp.fragmentos.principal.FragmentPrincipal;
import com.alcaldiasan.santaananorteapp.modelos.principal.ModeloVistaPrincipal;
import com.alcaldiasan.santaananorteapp.modelos.servicio.ModeloServicio;
import com.alcaldiasan.santaananorteapp.modelos.slider.ModeloSlider;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorPrincipal extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ModeloVistaPrincipal> modeloVistaPrincipal;
    private Context context;
    private FragmentPrincipal fragmentPrincipal;
    private AdaptadorSlider adapterSlider;




    public AdaptadorPrincipal(Context context, List<ModeloVistaPrincipal> modeloVistaPrincipal, FragmentPrincipal fragmentPrincipal) {
        this.context = context;
        this.modeloVistaPrincipal = modeloVistaPrincipal;
        this.fragmentPrincipal = fragmentPrincipal;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView;

        switch (viewType) {
            case ModeloVistaPrincipal.TIPO_SLIDER:
                itemView = inflater.inflate(R.layout.cardview_vista_slider, parent, false);
                return new AdaptadorPrincipal.SliderViewHolder(itemView);
            case ModeloVistaPrincipal.TIPO_RECYCLER:
                itemView = inflater.inflate(R.layout.cardview_vista_servicios, parent, false);
                return new AdaptadorPrincipal.ServiciosViewHolder(itemView);
            case ModeloVistaPrincipal.TIPO_TEXTVIEW:
                itemView = inflater.inflate(R.layout.cardview_vista_textview, parent, false);
                return new AdaptadorPrincipal.TituloViewHolder(itemView);

            default:
                throw new IllegalArgumentException("Tipo de vista desconocido");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ModeloVistaPrincipal mVista = modeloVistaPrincipal.get(position);

        switch (mVista.getTipoVista()) {
            case ModeloVistaPrincipal.TIPO_SLIDER:

                AdaptadorPrincipal.SliderViewHolder viewHolderSlider = (AdaptadorPrincipal.SliderViewHolder) holder;
                configurarSlider(viewHolderSlider.sliderView, mVista.getModeloSliders());

                break;
            case ModeloVistaPrincipal.TIPO_RECYCLER:

                AdaptadorPrincipal.ServiciosViewHolder viewHolderServicios = (AdaptadorPrincipal.ServiciosViewHolder) holder;
                configurarServicios(viewHolderServicios.recyclerView, mVista.getModeloServicios());
                break;

            case ModeloVistaPrincipal.TIPO_TEXTVIEW:

                AdaptadorPrincipal.TituloViewHolder viewHolderTitulo = (AdaptadorPrincipal.TituloViewHolder) holder;

                if(!TextUtils.isEmpty(mVista.getTitulo())){
                    viewHolderTitulo.txtTitulo.setText(mVista.getTitulo());
                }

                break;
        }
    }

    @Override
    public int getItemCount() {
        if(modeloVistaPrincipal != null){
            return modeloVistaPrincipal.size();
        }else{
            return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return modeloVistaPrincipal.get(position).getTipoVista();
    }

    private void configurarSlider(SliderView sliderView, ArrayList<ModeloSlider> modeloSlider){

        adapterSlider = new AdaptadorSlider(context, modeloSlider);
        sliderView.setSliderAdapter(adapterSlider);

        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
        sliderView.setIndicatorSelectedColor(Color.WHITE);
        sliderView.setIndicatorUnselectedColor(Color.GRAY);
        sliderView.setScrollTimeInSec(7);
        sliderView.setAutoCycle(true);
        sliderView.startAutoCycle();
    }

    private void configurarServicios(RecyclerView recyclerView, List<ModeloServicio> modeloServicios) {

         RecyclerView.Adapter adaptadorInterno = new AdaptadorServicio(context, modeloServicios, fragmentPrincipal);
         GridLayoutManager gridLayoutManager = new GridLayoutManager(context,2, LinearLayoutManager.VERTICAL,false);
         recyclerView.setLayoutManager(gridLayoutManager);
         recyclerView.setAdapter(adaptadorInterno);
    }


    // BLOQUE DEVOCIONAL
    private static class SliderViewHolder extends RecyclerView.ViewHolder {

        private SliderView sliderView;

        SliderViewHolder(View itemView) {
            super(itemView);
            sliderView = itemView.findViewById(R.id.imgSlider);
        }
    }

    private static class ServiciosViewHolder extends RecyclerView.ViewHolder {
        private RecyclerView recyclerView;

        ServiciosViewHolder(View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.recyclerView);
        }
    }

    private static class TituloViewHolder extends RecyclerView.ViewHolder {
        private TextView txtTitulo;

        TituloViewHolder(View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTitulo);
        }
    }

}