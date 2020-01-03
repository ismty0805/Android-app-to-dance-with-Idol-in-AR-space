package com.example.gallerymaker;


import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    public ArrayList<Integer> pictures = new ArrayList<>();
    public ArrayList<Bitmap> gridviewimages = new ArrayList<>();
    // Keep all Images in array
//    public Integer[] mThumbIds = {
//            R.drawable.pic_1, R.drawable.pic_2,
//            R.drawable.pic_3, R.drawable.pic_4,
//            R.drawable.pic_5, R.drawable.pic_6,
//            R.drawable.pic_7, R.drawable.pic_8,
//            R.drawable.pic_9, R.drawable.pic_10,
//            R.drawable.pic_11, R.drawable.pic_12,
//            R.drawable.pic_13, R.drawable.pic_14,
//            R.drawable.pic_15, R.drawable.pic_16,
//            R.drawable.pic_17, R.drawable.pic_18,
//            R.drawable.pic_19, R.drawable.pic_20,
//            R.drawable.pic_21
//    };
    // Constructor
    public ImageAdapter(Context c){
        mContext = c;
    }

    @Override
    public int getCount() {
        return gridviewimages.size();
    }

    @Override
    public Object getItem(int position) {
        return gridviewimages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView imageView = new ImageView(mContext);
//
//        imageView.setImageResource(pictures.get(position));
        Bitmap bitmap;
        bitmap = gridviewimages.get(position);
//        ImageView imageView = gridviewimages.get(position);
        imageView.setImageBitmap(bitmap);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        imageView.setLayoutParams(new GridView.LayoutParams(340, 250));
        return imageView;
    }
}