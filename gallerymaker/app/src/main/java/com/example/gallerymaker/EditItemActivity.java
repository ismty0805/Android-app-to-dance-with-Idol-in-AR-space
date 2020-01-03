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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.DrawableCompat;

import com.example.gallerymaker.ui.gallery.GalleryFragment;
import com.example.gallerymaker.ui.home.HomeFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class EditItemActivity extends AppCompatActivity {
    static final int EDIT = 1;
    private ListView_ImageList profile_image_lIst;
    private EditText name;
    private EditText phoneNumber;
    private EditText memo;
    private ImageView imgView;
    private Bitmap imgBitmap;

    private String tmpName;
    private String tmpMemo;
    private String tmpPhoneNumber;
    private Bitmap tmpImgBitmap;
    private InputMethodManager imm;

    private static final String TAG = "몰입캠프";
    private Boolean isPermission = true;
    private View view;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int PICK_FROM_CAMERA = 2;
    private File tempFile;
    private Bitmap galleryImgBitmap = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ok_bar, menu);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        imm.hideSoftInputFromWindow(findViewById(R.id.edit_name).getWindowToken(), 0);
        imm.hideSoftInputFromWindow(findViewById(R.id.edit_phoneNumber).getWindowToken(), 0);
        imm.hideSoftInputFromWindow(findViewById(R.id.edit_memo).getWindowToken(), 0);
        return super.onTouchEvent(event);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_item);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        ((AppCompatActivity) this).getSupportActionBar().setTitle("");
        this.name = (EditText) findViewById(R.id.edit_name);
        this.phoneNumber = (EditText) findViewById(R.id.edit_phoneNumber);
        this.imgView = (ImageView) findViewById(R.id.edit_img);
        this.memo = (EditText) findViewById(R.id.edit_memo);

        final Intent intent = getIntent();
        // ByteArray -> Bitmap
        byte[] arr = intent.getByteArrayExtra("img");
        this.imgBitmap = this.tmpImgBitmap = BitmapFactory.decodeByteArray(arr, 0, arr.length);
        this.imgView.setImageBitmap(BitmapFactory.decodeByteArray(arr, 0, arr.length));

        // 수정 전 정보 저장(tmp...)
        this.tmpName = intent.getStringExtra("name");
        this.tmpPhoneNumber = intent.getStringExtra("phone_number");
        this.tmpMemo = intent.getStringExtra("memo");
        name.setText(tmpName);
        phoneNumber.setText(tmpPhoneNumber);
        memo.setText(tmpMemo);

        if (name.getText().length() != 0) {
            name.setTextColor(Color.parseColor("#000000"));
        }
        if (phoneNumber.getText().length() != 0) {
            phoneNumber.setTextColor(Color.parseColor("#000000"));
        }
        if (memo.getText().length() != 0) {
            memo.setTextColor(Color.parseColor("#000000"));
        }

        final ImageButton imgButton = (ImageButton) findViewById(R.id.edit_img);
        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tedPermission();
                final CharSequence[] galorcam = {"앨범", "카메라", "기본 이미지로 변경"};
                AlertDialog.Builder oDialog = new AlertDialog.Builder(EditItemActivity.this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
                oDialog.setTitle("사진 가져오기")
                        .setItems(galorcam, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String selectedText = galorcam[which].toString();
                                if (selectedText == "앨범") {
                                    if (isPermission) goToAlbum();
                                    else
                                        Toast.makeText(view.getContext(), getResources().getString(R.string.permission_2), Toast.LENGTH_LONG).show();
                                } else if (selectedText == "카메라") {
                                    if (isPermission) takePhoto();
                                    else
                                        Toast.makeText(view.getContext(), getResources().getString(R.string.permission_2), Toast.LENGTH_LONG).show();
                                } else {
//                                    imgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_icon);

                                    imgBitmap = getBitmapFromVectorDrawable(EditItemActivity.this, R.drawable.user);
                                    imgBitmap = imgBitmap.createScaledBitmap(imgBitmap, 200, 200, true);
                                    Bitmap buttonSetImg = imgBitmap.createScaledBitmap(imgBitmap, imgButton.getWidth(), imgButton.getHeight(), true);
                                    imgButton.setImageBitmap(buttonSetImg);

                                }
                            }
                        })
                        .show();

            }
        });

        // 클릭시 json에 있는 item정보 삭제
        Button deleteItemButton = (Button) findViewById(R.id.edit_delete);
        deleteItemButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick_delete(v);
            }

        });


    }


    public void onClick_delete (View view){
        Log.d("delete", "button clicked");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder
                .setTitle("연락처")
                .setMessage("삭제하시겠습니까?")
                .setIcon(R.drawable.icons8_trash)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //확인시
                        String json = "";
                        String str = "";
                        try {
                            BufferedReader br = new BufferedReader(new FileReader(getFilesDir() + "phoneBook.txt"));
                            while (((str = br.readLine()) != null)) {
                                json += str + "\n";
                            }

                            // array -> list
                            JSONArray jsonArray = new JSONArray(json);
                            ArrayList<JSONObject> jsonList = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++)
                                jsonList.add((JSONObject) jsonArray.get(i));

                            // item의 전화번호와 json의 전화번호가 같으면 해당 object삭제
                            String itemPhoneNumber = phoneNumber.getText().toString();
                            String tmp = "";

                            for (int i = 0; i < jsonList.size(); i++) {
                                if (jsonList.get(i).has("phone_number")) {
                                    tmp = jsonList.get(i).getString("phone_number");
                                    if (tmp.equals(itemPhoneNumber)) {
                                        Log.d("phone_num", "" + itemPhoneNumber + "              " + tmp);
                                        jsonList.remove(i);
                                        break;
                                    }
                                } else {
                                    jsonList.remove(i);
                                    break;
                                }
                            }
                            // 삭제한 내용을 제외하고 json file에 저장
                            JSONArray deletedJsonArray = new JSONArray();
                            for (JSONObject o : jsonList) deletedJsonArray.put(o);

                            BufferedWriter bw = new BufferedWriter(new FileWriter(getFilesDir() + "phoneBook.txt", false));
                            bw.write(deletedJsonArray.toString());
                            Log.d("deleted json", deletedJsonArray.toString());
                            bw.close();

                            for (int i = 0; i < BaseActivity.actList.size(); i++) {
                                BaseActivity.actList.get(i).finish();
                                BaseActivity.actList.remove(i);
                            }
                            // 삭제 안될 시 문제 발생 가능
//                    Intent intent1 = new Intent(EditItemActivity.this, HomeFragment.class);
//                    startActivity(intent1);
                            setResult(3);
                            finish();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //취소시
                    }
                })
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.WHITE);
                Log.d("@@@@@@@@@@@@a", ""+dialog.getButton(AlertDialog.BUTTON_POSITIVE).toString());
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setPadding(0,0,5,0);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.WHITE);
            }
        });
        dialog.show();

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
    /**
     *  앨범에서 이미지 가져오기
     */
    public void goToAlbum() {

        Intent intent1 = new Intent(Intent.ACTION_PICK);
        intent1.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent1, PICK_FROM_ALBUM);
    }


    /**
     *  카메라에서 이미지 가져오기
     */
    public void takePhoto() {

        Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

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
                intent1.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent1, PICK_FROM_CAMERA);

            } else {

                Uri photoUri = Uri.fromFile(tempFile);
                intent1.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent1, PICK_FROM_CAMERA);

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

        ImageButton imageButton = findViewById(R.id.edit_img);

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
        Bitmap bmResized = Bitmap.createScaledBitmap(bmResized1, width, height, true);
        galleryImgBitmap = bmResized;
        imageButton.setImageBitmap(bmResized);

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

    // ok 버튼 클릭시 화면 전환
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch ( item.getItemId() ) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.ok_bar:
                Log.d("complete edit button", "at edit view");
                // item detail에 전송
                // 수정된 정보
                String newPhoneNumber = phoneNumber.getText().toString();
                String newName = name.getText().toString();
                String newMemo = memo.getText().toString();

                Intent intent = new Intent(EditItemActivity.this, itemDetail.class);
                intent.putExtra("name", newName);
                intent.putExtra("phone_number", newPhoneNumber);
                intent.putExtra("memo", newMemo);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                if( galleryImgBitmap != null ) imgBitmap = galleryImgBitmap;
                this.imgBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                intent.putExtra("img", byteArray);

                setResult(EDIT, intent);
                // json 수정
                try {
                    String json = "";
                    String str = "";
                    BufferedReader br = new BufferedReader(new FileReader(getFilesDir()+"phoneBook.txt"));
                    while(( ( str = br.readLine() ) != null )) {
                        json += str + "\n";
                    }

                    // array -> list : data delete 용이
                    JSONArray jsonArray = new JSONArray(json);
                    ArrayList<JSONObject> jsonList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) jsonList.add((JSONObject) jsonArray.get(i));

                    // 수정 전의 item정보와 json의 정보가 같으면 object수정
                    for (int i = 0; i < jsonList.size(); i++) {
                        JSONObject obj = jsonList.get(i);
                        if( tmpPhoneNumber.equals(obj.getString("phone_number")) &&
                                tmpName.equals(obj.getString("name")) && tmpMemo.equals(obj.getString("memo")) ) {
                            Log.d("same idx", i+"");
                            JSONObject tmpObj = new JSONObject();
                            tmpObj.put("img", Arrays.toString(byteArray));
                            tmpObj.put("name", newName);
                            tmpObj.put("phone_number", newPhoneNumber);
                            tmpObj.put("memo", newMemo);
                            tmpObj.put("isBlock", false);
                            jsonList.set(i, tmpObj);
                            break;
                        }
                    }
                    // 삭제한 내용을 다시 json file에 저장
                    JSONArray updateJsonArray = new JSONArray();
                    for (JSONObject o : jsonList) updateJsonArray.put(o);

                    BufferedWriter bw = new BufferedWriter(new FileWriter(getFilesDir() + "phoneBook.txt", false));
                    bw.write(updateJsonArray.toString());
                    bw.close();

                    //activity 2개 닫기
                    finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finish();
                return true;

            default: return false;
        }
    }
}