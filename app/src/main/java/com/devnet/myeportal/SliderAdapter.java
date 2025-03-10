package com.devnet.myeportal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;

import java.util.List;

public class SliderAdapter extends PagerAdapter {

    private Context context;
    private List<SlideItem> slideItems;  // List of SlideItem objects

    public SliderAdapter(Context context, List<SlideItem> slideItems) {
        this.context = context;
        this.slideItems = slideItems;
    }

    @Override
    public int getCount() {
        return slideItems.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.slide_holder, container, false);

        ImageView imageView = view.findViewById(R.id.imageView);
        TextView headlineTextView = view.findViewById(R.id.textview1);
        TextView storyTextView = view.findViewById(R.id.textview2);

        // Get the current slide item
        SlideItem slideItem = slideItems.get(position);

        // Load the image using Glide
        Glide.with(context)
                .load(slideItem.getImageUrl())
                .into(imageView);

        // Set the headline and story
        headlineTextView.setText(slideItem.getHeadline());
        storyTextView.setText(slideItem.getStory());

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}

