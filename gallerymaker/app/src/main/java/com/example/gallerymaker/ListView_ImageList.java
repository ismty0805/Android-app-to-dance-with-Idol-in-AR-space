package com.example.gallerymaker;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;

public class ListView_ImageList {
    private static ArrayList<Bitmap> profile_image_lIst = new ArrayList<>();

    private ListView_ImageList(){
        Log.d("created","~~~~~~~~");
        Log.d("created","~~~~~~~~");
        Log.d("created","~~~~~~~~");
    }

    public static ArrayList<Bitmap> getObject(){
        return profile_image_lIst;
    }

    public static Bitmap getImg(int i) { return profile_image_lIst.get(i); }

    public static void addImg(Bitmap imgBitmap) {
        Log.d("in list", imgBitmap.toString());
        profile_image_lIst.add(imgBitmap);
    }
    public static void setImg(int i, Bitmap imgBitmap) { profile_image_lIst.set(i, imgBitmap); }
    public static int getCount (){
        return profile_image_lIst.size();
    }
}
