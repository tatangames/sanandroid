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
import com.bumptech.glide.Glide;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorSlider extends SliderViewAdapter<AdaptadorSlider.SliderAdapterVH> {

    private Context context;
    private ArrayList<ModeloSlider> mSliderItems;

    public AdaptadorSlider(Context context, ArrayList<ModeloSlider> mSliderItems) {
        this.context = context;
        this.mSliderItems = mSliderItems;
    }


    @Override
    public AdaptadorSlider.SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_layout_item, null);
        return new AdaptadorSlider.SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(AdaptadorSlider.SliderAdapterVH viewHolder, final int position) {

        ModeloSlider sliderItem = mSliderItems.get(position);

        viewHolder.textViewDescription.setText(sliderItem.getNombre());
        viewHolder.textViewDescription.setTextSize(16);
        viewHolder.textViewDescription.setTextColor(Color.WHITE);

        Glide.with(viewHolder.itemView)
                .load(R.drawable.flag_elsalvador)
                .fitCenter()
                .into(viewHolder.imageViewBackground);

        viewHolder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "This is item in position " + position, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getCount() {
        //slider view count could be dynamic size
        return mSliderItems.size();
    }

    static class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        View itemView;
        ImageView imageViewBackground;
        ImageView imageGifContainer;
        TextView textViewDescription;

        public SliderAdapterVH(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.iv_auto_image_slider);
            imageGifContainer = itemView.findViewById(R.id.iv_gif_container);
            textViewDescription = itemView.findViewById(R.id.tv_auto_image_slider);
            this.itemView = itemView;
        }
    }

}