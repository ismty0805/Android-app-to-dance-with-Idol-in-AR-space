package com.example.gallerymaker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public ImageAdapter imageAdapter;
    public  static Context context_main;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        imageAdapter = new ImageAdapter(this);
        JSONArray result = new JSONArray();
        try {
            response(imageAdapter.gridviewimages, result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        for (int i = 1; i<22; i++){
//            String tmpSign = "pic_" + i;
//            Bitmap bitmap = BitmapFactory.decodeResource(this.getApplicationContext().getResources(), getResources().getIdentifier(tmpSign, "drawable", this.getPackageName()));
//            this.imageAdapter.gridviewimages.add(bitmap);
//        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_gallery, R.id.navigation_hashtag)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        Log.d("start", "~");
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            //System.out.println("@#@");
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }
    public void response(final ArrayList<Bitmap> arrayList, final JSONArray resultpnarr) throws JSONException {

        String url = "http://192.249.19.252:2080";
        try {
            //sign = 2인 json을 만듦(DB에 저장된 갤러리 요청)
            JSONObject request = new JSONObject();
            request.put("sign","4");
            JSONArray requestarr = new JSONArray();
            requestarr.put(request);

            //request를 전송
            final RequestQueue requestQueue = Volley.newRequestQueue(this);
            final JsonArrayRequest jsonarrRequest = new JsonArrayRequest(Request.Method.POST, url, requestarr, new Response.Listener<JSONArray>() {

                //요청에 대한 응답
                @Override
                public void onResponse(JSONArray response) {
                    try {
                        JSONArray resultarr = new JSONArray(response.toString());
                        Log.d("22222222", ""+response);
                        for (int i = 0; i < resultarr.length(); i++) {
                            JSONObject image = resultarr.getJSONObject(i);
                            String byteArray = image.getString("img");
                            Bitmap bitmap = getBitmapFromString(byteArray);
                            arrayList.add(bitmap);
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }

                //서버로 데이터 전달 및 응답 받기에 실패한 경우 아래 코드가 실행됩니다
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    //Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                }
            });
            jsonarrRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(jsonarrRequest);

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private Bitmap getBitmapFromString(String string){
        String[] bytevalues = string.substring(1, string.length() -1).split(",");
        byte[] bytes = new byte[bytevalues.length];
        for(int j=0, len=bytes.length; j<len; j++){
            bytes[j] = Byte.parseByte(bytevalues[j].trim());
        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return bitmap;
    }
}
