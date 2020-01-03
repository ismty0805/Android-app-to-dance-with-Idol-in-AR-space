package com.example.gallerymaker;
import android.app.AppComponentFactory;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gallerymaker.ui.home.HomeFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

public class itemDetail extends AppCompatActivity{
    private ListView_ImageList profile_image_lIst;
    public static final int EDIT = 1;
    public static final int DELETE = 3;
    private TextView name;
    private TextView phone_number;
    private Bitmap imgBitmap;
    private ImageView imgView;
    private TextView memo;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_detailview);
        BaseActivity.actList.add(itemDetail.this);
        ((AppCompatActivity)this).getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // item의 정보를 가져와 detail 화면으로 보여주기
        Intent intent = getIntent();
        this.name = (TextView)findViewById(R.id.detail_name);
        this.phone_number = (TextView)findViewById(R.id.detail_phoneNumber);
        this.imgView = (ImageView) findViewById(R.id.detail_img);
        this.memo = (TextView) findViewById(R.id.detail_memo);

        phone_number.setText( intent.getStringExtra ("phone_number") );
        name.setText( intent.getStringExtra ("name") );
        memo.setText( getMemo ( getJson(), intent.getStringExtra ("phone_number") ) );

        byte[] arr = intent.getByteArrayExtra("img");
        this.imgBitmap = BitmapFactory.decodeByteArray(arr, 0, arr.length);
        imgView.setImageBitmap(imgBitmap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return true;
    }

    // edit 버튼 클릭시 화면 전환 or 뒤로가기
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch ( item.getItemId() ) {
            case android.R.id.home:
                Intent intent1 = new Intent(itemDetail.this, MainActivity.class);
                setResult(HomeFragment.UPDATE_ITEM, intent1);
                finish();
                return true;
            case R.id.edit_bar:
                Log.d("edit_bar at itemDetail", "clicked");
                Intent intent = new Intent(itemDetail.this, EditItemActivity.class);

                intent.putExtra("name", this.name.getText().toString());
                intent.putExtra("phone_number", this.phone_number.getText().toString());
                intent.putExtra("memo", this.memo.getText().toString());
                intent.putExtra("isBlock", intent.getStringExtra("isBlock"));

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                imgBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                intent.putExtra("img", byteArray);

                startActivityForResult(intent, EDIT);
                return true;
            default: return false;
        }
    }

    // editView에서 수정한 값을 반영
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("itemdetail", "callback" + resultCode);
        switch ( resultCode ) {
            case EDIT:
                byte[] arr = data.getByteArrayExtra("img");
                imgView.setImageBitmap(BitmapFactory.decodeByteArray(arr, 0, arr.length));
                name.setText(data.getStringExtra("name"));
                phone_number.setText(data.getStringExtra("phone_number"));
                memo.setText(data.getStringExtra("memo"));
                break;
            case DELETE: {
                finish();
                break;
            }
                default: break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(itemDetail.this, MainActivity.class);
        setResult(HomeFragment.UPDATE_ITEM, intent);
        finish();
    }

    public String getJson() {
        String json = "";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(getFilesDir()+"phoneBook.txt"));
            String str = null;
            while(( ( str = br.readLine() ) != null )) {
                json += str +"\n";
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return json;
    }

    // json 파일 속 phone_number에 해당하는 memo 반환
    public String getMemo(String json, String phone_number)
    {
        try{
            JSONArray phoneBook_list = new JSONArray(json);
            for(int i = 0; i < phoneBook_list.length(); i++) {
                JSONObject item = phoneBook_list.getJSONObject(i);
//                Log.d("json phonNumber", item.getString("phone_number"));
//                Log.d("original phoneNumber", phone_number);
                if ( item.getString("phone_number").equals(phone_number) ) {
                    Log.d("memo", item.getString("memo"));
                    return item.getString("memo");
                }
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return "blank";
    }
}
