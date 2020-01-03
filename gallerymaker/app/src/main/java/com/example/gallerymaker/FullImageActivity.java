package com.example.gallerymaker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.gallerymaker.ui.gallery.GalleryFragment;

import uk.co.senab.photoview.PhotoViewAttacher;
public class FullImageActivity extends AppCompatActivity {
    public static int position;
    PhotoViewAttacher mAttacher;
    ViewPager pager;
    private CustomAdapter adapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_image);

        // get intent data
        Intent i = getIntent();


        // 상태바를 안보이도록 합니다.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ((AppCompatActivity)this).getSupportActionBar().hide();
        position = i.getExtras().getInt("id");
        pager = (ViewPager) findViewById(R.id.pager) ;
        adapter = new CustomAdapter(getLayoutInflater());
        pager.setAdapter(adapter);
        pager.setCurrentItem(position);
////
//        // Selected image id
//        int position = i.getExtras().getInt("id");
//        ImageView imageView = (ImageView) findViewById(R.id.full_image_view);
//        mAttacher = new PhotoViewAttacher(imageView);
////        Log.d( "", ""+ ((MainActivity) MainActivity.context_main).imageAdapter.getCount());
//        imageView.setImageBitmap(GalleryFragment.imageAdapterinfrag.gridviewimages.get(position));
//        imageView.bringToFront();

        findViewById(R.id.deletebutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick_delete(view);
            }});
        }


    public void onClick_delete(View view){
        position = pager.getCurrentItem();
        new AlertDialog.Builder(this)
                .setTitle("갤러리")
                .setMessage("삭제하시겠습니까?")
                .setIcon(R.drawable.icons8_trash)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                      //확인시
                        GalleryFragment.imageAdapterinfrag.gridviewimages.remove(GalleryFragment.imageAdapterinfrag.gridviewimages.get(position));
                        pager.setAdapter(adapter);
                        if(position==GalleryFragment.imageAdapterinfrag.getCount()){
                            position-=1;
                        }
                        if(GalleryFragment.imageAdapterinfrag.getCount() != 0) {
                            pager.setCurrentItem(position);
                        }
                        else{
                            finish();
                        }
                    }
                 })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //취소시
                  }
                })
                .show();
    }

}