package com.example.gallerymaker;
import android.graphics.Bitmap;
import android.widget.ImageView;

public class ListViewItem {
    private String name ;
    private String phone_number ;
    private Bitmap imgBitmap;
    private ImageView imageView;
    private int img;
    private boolean isBlock;
    private String memo;

    public void setImg(Bitmap bitmap) {
        this.imgBitmap = bitmap;
//        Log.d("imgBitmap", bitmap.toString());
//        Log.d("imgBitmap", bitmap.toString());
    }
    public void setName(String name) { this.name = name; }
    public void setPhoneNumber(String phone_number) { this.phone_number = phone_number; }
    public void setMemo(String memo) { this.memo = memo; }
    public void setIsBlock(boolean isBlock) { this.isBlock = isBlock; }
//    public void setImageView(ImageView imageView) { this.imageView = imageView; }

    public Bitmap getImg() { return this.imgBitmap; }
    public String getName() {
        return this.name;
    }
    public String getPhoneNumber() {
        return this.phone_number;
    }
    public String getMemo() { return this.memo; }
    public boolean getIsBlock() { return this.isBlock; }
//    public ImageView getImageView() { return this.imageView; }
}