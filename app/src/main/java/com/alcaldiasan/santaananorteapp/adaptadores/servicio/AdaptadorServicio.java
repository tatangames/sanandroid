package com.alcaldiasan.santaananorteapp.adaptadores.servicio;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.alcaldiasan.santaananorteapp.R;
import com.alcaldiasan.santaananorteapp.adaptadores.slider.AdaptadorSlider;
import com.alcaldiasan.santaananorteapp.extras.IOnRecyclerViewClickListener;
import com.alcaldiasan.santaananorteapp.fragmentos.principal.FragmentPrincipal;
import com.alcaldiasan.santaananorteapp.modelos.principal.ModeloVistaPrincipal;
import com.alcaldiasan.santaananorteapp.modelos.servicio.ModeloServicio;
import com.alcaldiasan.santaananorteapp.modelos.slider.ModeloSlider;
import com.alcaldiasan.santaananorteapp.network.RetrofitBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorServicio extends RecyclerView.Adapter<AdaptadorServicio.MyViewHolder> {

    // UTILIZADO CON PAGINACION
    private Context context;
    private List<ModeloServicio> modeloServicios;
    private FragmentPrincipal fragmentPrincipal;

    private RequestOptions opcionesGlide = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .error(R.drawable.ic_error)
            .priority(Priority.HIGH);


    public AdaptadorServicio(Context context, List<ModeloServicio> modeloServicios, FragmentPrincipal fragmentPrincipal) {
        this.context = context;
        this.modeloServicios = modeloServicios;
        this.fragmentPrincipal = fragmentPrincipal;
    }

    @NonNull
    @Override
    public AdaptadorServicio.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.cardview_servicios, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorServicio.MyViewHolder holder, int position) {

        ModeloServicio miModelo = modeloServicios.get(position);

        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(30f);
        circularProgressDrawable.setColorSchemeColors(Color.BLUE);
        circularProgressDrawable.start();

        Log.d("ETIQUETA", miModelo.getImagen());

        if(miModelo.getImagen() != null && !TextUtils.isEmpty(miModelo.getImagen())){
            Glide.with(context)
                    .load(RetrofitBuilder.urlImagenes + miModelo.getImagen())
                    .apply(opcionesGlide)
                    .placeholder(circularProgressDrawable)
                    .into(holder.imgServicio);
        }else{
            int resourceId = R.drawable.camaradefecto;
            Glide.with(context)
                    .load(resourceId)
                    .apply(opcionesGlide)
                    .placeholder(circularProgressDrawable)
                    .into(holder.imgServicio);
        }

        holder.txtServicio.setText(miModelo.getNombre());

        holder.setListener((view, po) -> {
            // ID TIPO DE SERVICIO
            // TITULO DE SERVICIO
            // ID DEL SERVICIO

            fragmentPrincipal.servicioSeleccionado(miModelo.getIdTipoServicio(),
                    miModelo.getNombre(), miModelo.getId());
        });
    }

    @Override
    public int getItemCount() {
        if(modeloServicios != null){
            return modeloServicios.size();
        }else{
            return 0;
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView txtServicio;
        private ImageView imgServicio;

        private IOnRecyclerViewClickListener listener;

        public void setListener(IOnRecyclerViewClickListener listener) {
            this.listener = listener;
        }

        public MyViewHolder(View itemView){
            super(itemView);

            imgServicio = itemView.findViewById(R.id.imgServicio);
            txtServicio = itemView.findViewById(R.id.txtServicio);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onClick(v, getAdapterPosition());
        }
    }


}