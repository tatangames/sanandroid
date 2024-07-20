package com.alcaldiasan.santaananorteapp.adaptadores.servicio;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alcaldiasan.santaananorteapp.R;
import com.alcaldiasan.santaananorteapp.extras.IOnRecyclerViewClickListener;
import com.alcaldiasan.santaananorteapp.fragmentos.solicitudes.FragmentSolicitudes;
import com.alcaldiasan.santaananorteapp.modelos.servicio.ModeloSolicitud;

import java.util.List;

public class AdaptadorSolicitudes extends RecyclerView.Adapter<AdaptadorSolicitudes.ViewHolder> {

    private List<ModeloSolicitud> modeloSolicitud;
    private Context context;
    private FragmentSolicitudes fragmentSolicitudes;

    public AdaptadorSolicitudes(Context context, List<ModeloSolicitud> modeloSolicitud, FragmentSolicitudes fragmentSolicitudes) {
        this.context = context;
        this.modeloSolicitud = modeloSolicitud;
        this.fragmentSolicitudes = fragmentSolicitudes;
    }

    @NonNull
    @Override
    public AdaptadorSolicitudes.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.cardview_solicitudes, parent, false);
        return new AdaptadorSolicitudes.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorSolicitudes.ViewHolder holder, int position) {
        ModeloSolicitud currentItem = modeloSolicitud.get(position);

        if(!TextUtils.isEmpty(currentItem.getNombretipo())){
            holder.txtTipo.setText(currentItem.getNombretipo());
        }

        if(!TextUtils.isEmpty(currentItem.getFecha())){
            holder.txtFecha.setText(currentItem.getFecha());
        }

        if(!TextUtils.isEmpty(currentItem.getEstado())){
            holder.txtEstado.setText(currentItem.getEstado());
        }

        if(!TextUtils.isEmpty(currentItem.getNota())){
            holder.txtNota.setText(currentItem.getNota());
        }

        holder.setListener((view, po) -> {

            int id = currentItem.getId();
            int tipo = currentItem.getTipo();

            fragmentSolicitudes.modalBorrar(id, tipo);
        });
    }

    @Override
    public int getItemCount() {

        if(modeloSolicitud != null){
            return modeloSolicitud.size();
        }else{
            return 0;
        }

    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView txtTipo;
        private TextView txtFecha;
        private TextView txtEstado;
        private TextView txtNota;

        private IOnRecyclerViewClickListener listener;

        public void setListener(IOnRecyclerViewClickListener listener) {
            this.listener = listener;
        }

        ViewHolder(View itemView) {
            super(itemView);
            txtTipo = itemView.findViewById(R.id.txtTipo);
            txtFecha = itemView.findViewById(R.id.txtFecha);
            txtEstado = itemView.findViewById(R.id.txtEstado);
            txtNota = itemView.findViewById(R.id.txtNota);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onClick(v, getAdapterPosition());
        }

    }
}