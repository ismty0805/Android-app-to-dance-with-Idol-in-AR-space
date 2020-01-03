package com.example.gallerymaker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.gallerymaker.ui.home.HomeFragment;
import com.google.android.flexbox.FlexboxLayout;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

public class add_item extends AppCompatActivity {
    private String name;
    private String phoneNumber;
    private String memo;
    private InputMethodManager imm;
    private static final String TAG = "몰입캠프";
    private Boolean isPermission = true;
    private View view;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int PICK_FROM_CAMERA = 2;
    private File tempFile;
    private Bitmap galleryImgBitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        galleryImgBitmap = BitmapFactory.decodeResource(this.getApplicationContext().getResources(), getResources().getIdentifier("user", "drawable", this.getPackageName()));

        Log.d("init bitmap", galleryImgBitmap.toString());
        Log.d("init bitmap", galleryImgBitmap.toString());
        Log.d("init bitmap", galleryImgBitmap.toString());

        ((AppCompatActivity)this).getSupportActionBar().setTitle("");
        ImageButton imgButton = (ImageButton) findViewById(R.id.add_img);
        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tedPermission();
                final CharSequence[] galorcam = {"갤러리", "카메라"};
                AlertDialog.Builder oDialog = new AlertDialog.Builder(add_item.this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
                oDialog.setTitle("사진 가져오기")
                        .setItems(galorcam, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String selectedText = galorcam[which].toString();
                                if(selectedText == "갤러리"){
                                    if(isPermission) goToAlbum();
                                    else Toast.makeText(view.getContext(), getResources().getString(R.string.permission_2), Toast.LENGTH_LONG).show();
                                }
                                else{
                                    if(isPermission)  takePhoto();
                                    else Toast.makeText(view.getContext(), getResources().getString(R.string.permission_2), Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .show();

            }
        });
    }


    /**
     *  앨범에서 이미지 가져오기
     */
    public void goToAlbum() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }


    /**
     *  카메라에서 이미지 가져오기
     */
    public void takePhoto() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            tempFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            this.finish();
            e.printStackTrace();
        }
        if (tempFile != null) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

                Uri photoUri = FileProvider.getUriForFile(this,
                        "gallerymaker.provider", tempFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, PICK_FROM_CAMERA);

            } else {

                Uri photoUri = Uri.fromFile(tempFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, PICK_FROM_CAMERA);

            }
        }
    }

    /**
     *  폴더 및 파일 만들기
     */
    public File createImageFile() throws IOException {

        // 이미지 파일 이름 ( blackJin_{시간}_ )
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "몰입캠프" + timeStamp + "_";

        // 이미지가 저장될 폴더 이름 ( blackJin )
        File storageDir = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "/몰입캠프/");
        if (!storageDir.exists()) storageDir.mkdirs();

        // 파일 생성
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        Log.d(TAG, "createImageFile : " + image.getAbsolutePath());

        return image;
    }

    private Classifier mClassifier;
    private Result result;
    //    private String answer;
    private String answerstr;
    private ArrayList<String> answer;
    /**
     *  tempFile 을 bitmap 으로 변환 후 ImageView 에 설정한다.
     */
    public void setImage() {
//        ImageView imageView = view.findViewById(R.id.imageView123);


        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap originalBm = BitmapFactory.decodeFile(tempFile.getAbsolutePath(), options);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(tempFile.getAbsolutePath());
        }catch (IOException e){
            e.printStackTrace();
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap bmRotated = rotateBitmap(originalBm, orientation);
        Log.d(TAG, "setImage : " + tempFile.getAbsolutePath());

        ImageButton imageButton = findViewById(R.id.add_img);

        int height = imageButton.getHeight();
        int width = imageButton.getWidth();
        Bitmap bmResized1;
        if (bmRotated.getWidth() >= bmRotated.getHeight()){

            bmResized1 = Bitmap.createBitmap(
                    bmRotated,
                    bmRotated.getWidth()/2 - bmRotated.getHeight()/2,
                    0,
                    bmRotated.getHeight(),
                    bmRotated.getHeight()
            );

        }else{

            bmResized1 = Bitmap.createBitmap(
                    bmRotated,
                    0,
                    bmRotated.getHeight()/2 -bmRotated.getWidth()/2,
                    bmRotated.getWidth(),
                    bmRotated.getWidth()
            );
        }
        Bitmap bmResized2 = Bitmap.createScaledBitmap(bmResized1, 400, 400, true);
        galleryImgBitmap = bmResized2;

        imageButton.setImageBitmap(bmResized1);

        /**
         *  tempFile 사용 후 null 처리를 해줘야 합니다.
         *  (resultCode != RESULT_OK) 일 때 tempFile 을 삭제하기 때문에
         *  기존에 데이터가 남아 있게 되면 원치 않은 삭제가 이뤄집니다.
         */
        if (tempFile == null) {
            Toast.makeText(this, "실패", Toast.LENGTH_SHORT).show();
        }
        tempFile = null;

    }

    public Bitmap getGalleyImgBitmap () {
        return this.galleryImgBitmap;
    }
    //회전
    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation){
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1,1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1,1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1,1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1,1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try{
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e){
            e.printStackTrace();
            return null;
        }
    }
    /**
     *  권한 설정
     */
    public void tedPermission() {

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 권한 요청 성공
                isPermission = true;

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // 권한 요청 실패
                isPermission = false;

            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2))
                .setDeniedMessage(getResources().getString(R.string.permission_1))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();

    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("widnow","clicked");
        imm.hideSoftInputFromWindow(findViewById(R.id.add_name).getWindowToken(), 0);
        imm.hideSoftInputFromWindow(findViewById(R.id.add_phoneNumber).getWindowToken(), 0);
        imm.hideSoftInputFromWindow(findViewById(R.id.add_memo).getWindowToken(), 0);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ok_bar, menu);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();

            if (tempFile != null) {
                if (tempFile.exists()) {
                    if (tempFile.delete()) {
                        Log.e(TAG, tempFile.getAbsolutePath() + " 삭제 성공");
                        tempFile = null;
                    }
                }
            }

            return;
        }

        if (requestCode == PICK_FROM_ALBUM) {

            Uri photoUri = data.getData();
            Log.d(TAG, "PICK_FROM_ALBUM photoUri : " + photoUri);

            Cursor cursor = null;

            try {

                /*
                 *  Uri 스키마를
                 *  content:/// 에서 file:/// 로  변경한다.
                 */
                String[] proj = {MediaStore.Images.Media.DATA};

                assert photoUri != null;
                cursor = this.getContentResolver().query(photoUri, proj, null, null, null);

                assert cursor != null;
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                cursor.moveToFirst();

                tempFile = new File(cursor.getString(column_index));

                Log.d(TAG, "tempFile Uri : " + Uri.fromFile(tempFile));

            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            setImage();

        } else if (requestCode == PICK_FROM_CAMERA) {

            setImage();

        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch ( item.getItemId() ) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.ok_bar:
                Log.d("count", ListView_ImageList.getCount()+"");
                ListView_ImageList.addImg(galleryImgBitmap);
                Log.d("complete_add_bar", "clicked");

                // 추가 할 연락처 정보 listView에 넘기기
                EditText nameView = (EditText)findViewById(R.id.add_name);
                EditText phoneNumberView = (EditText)findViewById(R.id.add_phoneNumber);
                EditText memoView = (EditText)findViewById(R.id.add_memo);

                this.name = nameView.getText().toString();
                this.phoneNumber = phoneNumberView.getText().toString();
                this.memo = memoView.getText().toString();

                //json에 집어넣기
                try {
                    addJsonObject(galleryImgBitmap , name, phoneNumber, false, memo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(add_item.this, MainActivity.class);
                setResult(HomeFragment.ADD_ITEM, intent);
                finish();
                return true;
            default: return false;
        }
    }

    private void addJsonObject(Bitmap imgBitmap, String name, String phoneNumber, boolean isBlock, String memo) throws JSONException {
        JSONObject obj = new JSONObject();
        JSONArray jsonArray;
        try {
            // 기존 json 읽어서 배열 만들기
            String json = "";
            String str = "";
            BufferedReader br = new BufferedReader(new FileReader(getFilesDir()+"phoneBook.txt"));
            while(( ( str = br.readLine() ) != null )) {
                json += str + "\n";
            }
            jsonArray = new JSONArray(json);

            // 입력 값을 jsonobject로 만들고 json 배열에 넣기
             ByteArrayOutputStream stream = new ByteArrayOutputStream() ;
             imgBitmap.compress( Bitmap.CompressFormat.PNG, 100, stream ) ;
             byte[] byteArray = stream.toByteArray() ;
            //
            //
            obj.put("img", Arrays.toString(byteArray) );
            obj.put("name", name);
            obj.put("phone_number", phoneNumber);
            obj.put("isBlock", isBlock);
            obj.put("memo", memo);
            jsonArray.put(obj);

            // 정렬
            sortJsonArray(jsonArray);


            // 수정된 josn 배열을 josn파일에 넣기
            BufferedWriter bw = new BufferedWriter(new FileWriter(getFilesDir() + "phoneBook.txt", false));
            bw.write(jsonArray.toString());
            bw.close();

            br.close();
//            Log.d("resresres", jsonArray.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        Log.d("adpater","changed");
//        HomeFragment.adapter.notifyDataSetChanged();
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                HomeFragment.adapter.notifyDataSetChanged();
//            }
//        });
    }

    public void sortJsonArray(JSONArray jsonArray) {
        List<JSONObject> jsonList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                jsonList.add(jsonArray.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Collections.sort(jsonList, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                String str1 = "";
                String str2 = "";
                try {
                    str1 = o1.getString("name");
                    str2 = o2.getString("name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return str1.compareTo(str2);
            }
        });

        for (int i = 0; i < jsonList.size(); i++) {
            try {
                jsonArray.put(i, jsonList.get(i));
                Log.d("after sort", jsonList.get(i).getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
