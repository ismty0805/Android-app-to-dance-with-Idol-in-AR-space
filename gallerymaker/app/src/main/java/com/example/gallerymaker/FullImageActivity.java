package com.example.gallerymaker;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.gallerymaker.ui.gallery.GalleryFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

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
                        Bitmap eraseBitmap = GalleryFragment.imageAdapterinfrag.gridviewimages.get(position);
                        GalleryFragment.imageAdapterinfrag.gridviewimages.remove(eraseBitmap);
                        byte[] bytes = getByteArrayFromBitmap(eraseBitmap);
                        JSONObject obj =  new JSONObject();
                        try {
                            obj.put("img", Arrays.toString(bytes));
                            obj.put("sign", 5);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        JSONArray erasearray = new JSONArray();
                        erasearray.put(obj);
                        erase(erasearray);
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
    private byte[] getByteArrayFromBitmap(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }
    public void erase(JSONArray array){
        //JSON형식으로 데이터 통신을 진행합니다!
        String url = "http://192.249.19.252:2080";
        //이제 전송해볼까요?
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final JsonArrayRequest jsonarrRequest = new JsonArrayRequest(Request.Method.POST, url, array, new Response.Listener<JSONArray>() {
            //데이터 전달을 끝내고 이제 그 응답을 받을 차례입니다.
            @Override
            public void onResponse(JSONArray response) {
                try {
                    //받은 jsonArray형식의 응답을 받아
                    Log.d("@@@@", "2");
                    JSONArray result = new JSONArray(response.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //서버로 데이터 전달 및 응답 받기에 실패한 경우 아래 코드가 실행됩니다.
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.d("!!!!!", "1");
                //Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        jsonarrRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonarrRequest);
        //
    }

}