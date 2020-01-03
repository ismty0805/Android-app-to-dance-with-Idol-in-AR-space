package com.example.gallerymaker.ui.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.ListFragment;

import com.example.gallerymaker.ListViewAdapter;
import com.example.gallerymaker.ListViewItem;
import com.example.gallerymaker.MainActivity;
import com.example.gallerymaker.R;
import com.example.gallerymaker.add_item;
import com.example.gallerymaker.itemDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilePermission;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private String memo;
    private boolean isBlock;
    public ListViewAdapter adapter;
    public ListView listview;
    private View view;
    private EditText searchEditText;
    private ArrayAdapter filterAdapter;
    private ListViewAdapter listViewAdapter;
    private ActionBar actionBar;
    public static final int ADD_ITEM = 2;
    public static final int UPDATE_ITEM = 1;
    InputMethodManager imm;

    public void onCreate(Bundle savedInstanceState) {
//        listViewAdapter.addItem();
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // show listView from json
        Log.d("on CreateView","start");
        view = inflater.inflate(R.layout.fragment_home, container, false);
        listview = view.findViewById(R.id.list);
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle("연락처");

//        ((MainActivity) getActivity()).getSupportActionBar().
//        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"red\">" + getString(R.string.app_name) + "</font>"));

        jsonParsing(getJson());

        listview = (ListView) view.findViewById(R.id.list);
        view.findViewById(R.id.searchIcon).bringToFront();
        if (listview == null) {
            Log.d("listview","is null");
        } else {
            Log.d("listview", "is not null");
        }
//        view.setOnTouchListener(new View.OnTouchListener(){
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                Log.d("asdfasfasdf","clickced");
//                imm.hideSoftInputFromWindow(view.findViewById(R.id.editTextFilter).getWindowToken(), 0);
//                return false;
//            }
//        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListViewItem item = (ListViewItem) adapter.getItem(position);
                String name = item.getName() ;
                String phone_number = item.getPhoneNumber() ;
                String memo = item.getMemo();
                Bitmap imgBitmap = item.getImg();
                // image transfer setting

                Intent intent = new Intent(getActivity().getApplicationContext(), itemDetail.class);
                intent.putExtra("name", name);
                intent.putExtra("phone_number", phone_number);
                intent.putExtra("memo", memo);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                imgBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                intent.putExtra("img", byteArray);

                startActivityForResult(intent, UPDATE_ITEM);
            }
        });

        EditText editTextFilter = (EditText) view.findViewById(R.id.editTextFilter);
//        Log.d("editTextFilter", editTextFilter.getText().toString() + "//////////////");
        editTextFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String filterText = s.toString() ;
                Log.d("after filtering", s.toString());
//                if (filterText.length() > 0) {
//                    listview.setFilterText(filterText) ;
//                } else {
//                    listview.clearTextFilter() ;
//                }
                ((ListViewAdapter)listview.getAdapter()).getFilter().filter(filterText);
            }
        });
        return view;
    }

    // bar 추가
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.add_bar, menu);
    }

    // 전화번호 아이템 추가 버튼
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Fragment add_fragment = new Fragment();
        switch ( item.getItemId() ) {
            case R.id.add_bar:
                Log.d("add_bar", "clicked");
                Intent intent = new Intent(getActivity(), add_item.class);
                startActivityForResult(intent, ADD_ITEM);
                return true;
            default: return false;
        }
    }

    // 전화번호 아이템이 추가된 이후 실행하는 call back
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        jsonParsing(getJson());
        listview.setAdapter(adapter);
//        switch ( resultCode ) {
//            case ADD_ITEM:
//                // json 다시 읽어오기
//                listview.setAdapter(adapter);
////                adapter.notifyDataSetChanged();
//                break;
//            case UPDATE_ITEM:
//                listview.setAdapter(adapter);
//                adapter.notifyDataSetChanged();
//                break;
//            default: break;
//        }
    }

    // asset폴더의 json파일을 읽어 string 타입으로 return
    public String getJson() {
        String json = "";
        int data = -1;
        try {
            // read assets file(initialize) -> 주석처리
//            InputStream is = getResources().getAssets().open("phone_Book.txt");
//            int fileSize = is.available();
//
//            byte[] buffer = new byte[fileSize];
//            is.read(buffer);
//            is.close();
//            json = new String(buffer, "UTF-8");
//            Log.d("asset json", json);
//
//            BufferedWriter bw = new BufferedWriter(new FileWriter(getActivity().getFilesDir() + "phoneBook.txt", false));
//            bw.write(json);
//            bw.close();
////
            BufferedReader br = new BufferedReader(new FileReader(getActivity().getFilesDir()+"phoneBook.txt"));
            String str = null;
            while(( ( str = br.readLine() ) != null )) {
                json += str +"\n";
            }

            Log.d("text file json", json);
            br.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return json;
    }

    // json파일로 listView의 item 각각 추가
    private void jsonParsing(String json) {
        adapter = new ListViewAdapter() ;
        try{
            JSONArray phoneBook_list = new JSONArray(json);

            for(int i = 0; i < phoneBook_list.length(); i++) {
                JSONObject item = phoneBook_list.getJSONObject(i);

                Bitmap imgBitmap;
                //getBytes: string to byteArray
                if( item.getString("img").equals("0") ) {
                    Log.d("hey", "no img");
                    imgBitmap = getBitmapFromVectorDrawable(getActivity(), R.drawable.ic_icon);
//                   imgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_icon);
//                    imgBitmap = imgBitmap.createScaledBitmap(imgBitmap, 36, 36, true);
                } else {
//                    byte[] arr = item.getString("img").getBytes();
//                    imgBitmap = BitmapFactory.decodeByteArray(arr, 0, arr.length);
//                    Log.d("extist2222", item.getString("img"));
//                    Log.d("exists22222222", imgBitmap.toString());

                    String response = item.getString("img");
                    String[] bytevalues = response.substring(1, response.length() -1).split(",");
                    byte[] bytes = new byte[bytevalues.length];
                    for(int j=0, len=bytes.length; j<len; j++){
                        bytes[j] = Byte.parseByte(bytevalues[j].trim());
                    }
                    imgBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                }

                String phone_number = item.getString("phone_number");
                String name = item.getString("name");
                String memo = item.getString("memo");
                isBlock = Boolean.valueOf(item.getString("isBlock"));
                adapter.addItem(imgBitmap, name, phone_number, isBlock, memo);
            }
            listview.setAdapter(adapter);
        }catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}