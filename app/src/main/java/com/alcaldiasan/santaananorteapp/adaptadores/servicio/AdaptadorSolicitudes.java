package com.alcaldiasan.santaananorteapp.adaptadores.servicio;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alcaldiasan.santaananorteapp.R;
import com.alcaldiasan.santaananorteapp.adaptadores.principal.AdaptadorPrincipal;
import com.alcaldiasan.santaananorteapp.adaptadores.slider.AdaptadorSlider;
import com.alcaldiasan.santaananorteapp.extras.IOnRecyclerViewClickListener;
import com.alcaldiasan.santaananorteapp.fragmentos.principal.FragmentPrincipal;
import com.alcaldiasan.santaananorteapp.fragmentos.solicitudes.FragmentSolicitudes;
import com.alcaldiasan.santaananorteapp.modelos.principal.ModeloVistaPrincipal;
import com.alcaldiasan.santaananorteapp.modelos.servicio.ModeloServicio;
import com.alcaldiasan.santaananorteapp.modelos.servicio.ModeloSolicitud;
import com.alcaldiasan.santaananorteapp.modelos.slider.ModeloSlider;
import com.alcaldiasan.santaananorteapp.modelos.solicitudes.ModeloSolicitudBasico;
import com.alcaldiasan.santaananorteapp.modelos.solicitudes.ModeloSolicitudTalaArbol;
import com.alcaldiasan.santaananorteapp.modelos.solicitudes.ModeloVistaSolicitudes;
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

public class AdaptadorSolicitudes extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ModeloVistaSolicitudes> modeloVistaSolicitudes;
    private Context context;
    private FragmentSolicitudes fragmentSolicitudes;

    RequestOptions opcionesGlide = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .placeholder(R.drawable.camaradefecto)
            .priority(Priority.NORMAL);

    public AdaptadorSolicitudes(Context context, List<ModeloVistaSolicitudes> modeloVistaSolicitudes,
                                FragmentSolicitudes fragmentSolicitudes) {
        this.context = context;
        this.modeloVistaSolicitudes = modeloVistaSolicitudes;
        this.fragmentSolicitudes = fragmentSolicitudes;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView;

        switch (viewType) {
            case ModeloVistaSolicitudes.TIPO_BASICO:
                itemView = inflater.inflate(R.layout.cardview_solicitud_basica, parent, false);
                return new AdaptadorSolicitudes.SolicitudBasicoViewHolder(itemView);
            case ModeloVistaSolicitudes.TIPO_SOLI_TALARBOL:
                itemView = inflater.inflate(R.layout.cardview_solicitud_talarbol, parent, false);
                return new AdaptadorSolicitudes.SolicitudTalaArbolViewHolder(itemView);
            /*case ModeloVistaSolicitudes.TIPO_DENUN_TALAARBOL:
                itemView = inflater.inflate(R.layout.cardview_denuncia_talaarbol, parent, false);
                return new AdaptadorSolicitudes.SolicitudDenunciaTalaArbolViewHolder(itemView);
            case ModeloVistaSolicitudes.TIPO_CATASTRO:
                itemView = inflater.inflate(R.layout.cardview_solicitud_catastro, parent, false);
                return new AdaptadorSolicitudes.SolicitudCatastroViewHolder(itemView);*/


            default:
                throw new IllegalArgumentException("Tipo de vista desconocido");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ModeloVistaSolicitudes mVista = modeloVistaSolicitudes.get(position);

        switch (mVista.getTipoVista()) {
            case ModeloVistaSolicitudes.TIPO_BASICO:

                ModeloSolicitudBasico mBasico = mVista.getModeloSolicitudBasicos();

                AdaptadorSolicitudes.SolicitudBasicoViewHolder viewHolderBasico = (AdaptadorSolicitudes.SolicitudBasicoViewHolder) holder;

                viewHolderBasico.txtTipo.setText(mBasico.getTipoNombre());
                viewHolderBasico.txtFecha.setText(mBasico.getFecha());
                viewHolderBasico.txtEstado.setText(mBasico.getEstado());
                viewHolderBasico.txtNota.setText(mBasico.getNota());

                break;

            case ModeloVistaSolicitudes.TIPO_SOLI_TALARBOL:

                ModeloSolicitudTalaArbol mSoliTala = mVista.getModeloSolicitudTalaArbol();
                AdaptadorSolicitudes.SolicitudTalaArbolViewHolder holderSoliTalaArbol = (AdaptadorSolicitudes.SolicitudTalaArbolViewHolder) holder;

                holderSoliTalaArbol.txtTipo.setText(mSoliTala.getTipoNombre());
                holderSoliTalaArbol.txtFecha.setText(mSoliTala.getFecha());
                holderSoliTalaArbol.txtEstado.setText(mSoliTala.getEstado());
                holderSoliTalaArbol.txtNombre.setText(mSoliTala.getNombre());
                holderSoliTalaArbol.txtTelefono.setText(mSoliTala.getTelefono());
                holderSoliTalaArbol.txtDireccion.setText(mSoliTala.getDireccion());
                holderSoliTalaArbol.txtNota.setText(mSoliTala.getNota());

                if(mSoliTala.getImagen() != null && !TextUtils.isEmpty(mSoliTala.getImagen())){
                    Glide.with(context)
                            .load(RetrofitBuilder.urlImagenes + mSoliTala.getImagen())
                            .apply(opcionesGlide)
                            .into(holderSoliTalaArbol.imgArbol);
                }else{
                    int resourceId = R.drawable.camaradefecto;
                    Glide.with(context)
                            .load(resourceId)
                            .apply(opcionesGlide)
                            .into(holderSoliTalaArbol.imgArbol);
                }

                break;
        }
    }

    @Override
    public int getItemCount() {
        if(modeloVistaSolicitudes != null){
            return modeloVistaSolicitudes.size();
        }else{
            return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return modeloVistaSolicitudes.get(position).getTipoVista();
    }





    // SOLICITUD BASICA
    private static class SolicitudBasicoViewHolder extends RecyclerView.ViewHolder {

        private TextView txtTipo;
        private TextView txtFecha;
        private TextView txtEstado;
        private TextView txtNota;

        SolicitudBasicoViewHolder(View itemView) {
            super(itemView);
            txtTipo = itemView.findViewById(R.id.txtTipo);
            txtFecha = itemView.findViewById(R.id.txtFecha);
            txtEstado = itemView.findViewById(R.id.txtEstado);
            txtNota = itemView.findViewById(R.id.txtNota);

        }
    }


    private static class SolicitudTalaArbolViewHolder extends RecyclerView.ViewHolder {

        private TextView txtTipo, txtFecha, txtEstado, txtNombre, txtTelefono, txtDireccion, txtNota;
        private ImageView imgArbol;

        SolicitudTalaArbolViewHolder(View itemView) {
            super(itemView);
            txtTipo = itemView.findViewById(R.id.txtTipo);
            txtFecha = itemView.findViewById(R.id.txtFecha);
            txtEstado = itemView.findViewById(R.id.txtEstado);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtTelefono = itemView.findViewById(R.id.txtTelefono);
            txtDireccion = itemView.findViewById(R.id.txtDireccion);
            txtNota = itemView.findViewById(R.id.txtNota);
            imgArbol = itemView.findViewById(R.id.imgArbol);
        }
    }





}