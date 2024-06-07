package com.alcaldiasan.santaananorteapp.adaptadores.slider;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alcaldiasan.santaananorteapp.R;
import com.alcaldiasan.santaananorteapp.modelos.slider.ModeloSlider;
import com.alcaldiasan.santaananorteapp.network.RetrofitBuilder;
import com.bumptech.glide.Glide;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorSlider extends SliderViewAdapter<AdaptadorSlider.SliderAdapterVH> {

    // ADAPTADOR PARA EL SLIDER PRINCIPAL

    private Context context;
    private ArrayList<ModeloSlider> mSliderItems;

    public AdaptadorSlider(Context context, ArrayList<ModeloSlider> mSliderItems) {
        this.context = context;
        this.mSliderItems = mSliderItems;
    }

    @Override
    public AdaptadorSlider.SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_slider, null);
        return new AdaptadorSlider.SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(AdaptadorSlider.SliderAdapterVH viewHolder, final int position) {

        ModeloSlider sliderItem = mSliderItems.get(position);

        Glide.with(viewHolder.itemView)
                .load(RetrofitBuilder.urlImagenes + sliderItem.getImagen())
                .fitCenter()
                .into(viewHolder.imageViewBackground);


    }

    @Override
    public int getCount() {
        //slider view count could be dynamic size
        return mSliderItems.size();
    }

    static class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        private View itemView;
        private ImageView imageViewBackground;
        private ImageView imageGifContainer;

        public SliderAdapterVH(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.iv_auto_image_slider);
            imageGifContainer = itemView.findViewById(R.id.iv_gif_container);

            this.itemView = itemView;
        }
    }

}