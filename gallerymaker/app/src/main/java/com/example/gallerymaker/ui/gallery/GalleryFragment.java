package com.example.gallerymaker.ui.gallery;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.gallerymaker.FullImageActivity;
import com.example.gallerymaker.ImageAdapter;
import com.example.gallerymaker.MainActivity;
import com.example.gallerymaker.R;
import com.google.gson.JsonObject;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
public class GalleryFragment extends Fragment {
    private static final String TAG = "몰입캠프";
    private Boolean isPermission = true;
    private View view;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int PICK_FROM_CAMERA = 2;
    private static final int FULL_SCREEN = 3;
    private File tempFile;
    private GridView gridView;
    private View gridviewitem;
    final String url = "http://192.249.19.252:2080";
    public static ImageAdapter imageAdapterinfrag;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.grid_layout, container, false);
        gridviewitem = inflater.inflate(R.layout.gridview_item, container, false);
        gridView = (GridView) view.findViewById(R.id.grid_view);
        imageAdapterinfrag = ((MainActivity)getActivity()).imageAdapter;
        Log.d("length", ""+imageAdapterinfrag.getCount());
        // Instance of ImageAdapter Class
        gridView.setAdapter(imageAdapterinfrag);

////        jsonarray에 기본 이미지 추가
//        JSONArray jsonArray = new JSONArray();
//        JSONArray resultarray = null;
//        for(int i= 1; i<22; i++){
//            String tmpSign = "pic_" + i;
//            Bitmap bitmap = BitmapFactory.decodeResource(getActivity().getApplicationContext().getResources(), getResources().getIdentifier(tmpSign, "drawable", getActivity().getPackageName()));
////            bitmap = Bitmap.createScaledBitmap(bitmap, 340, 250, true);
//            byte[] bytes = getByteArrayFromBitmap(bitmap);
//            JSONObject obj =  new JSONObject();
//            try {
//                obj.put("img", Arrays.toString(bytes));
//                obj.put("sign", 3);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            jsonArray.put(obj);
//        }
//        //db에 추가
//        request(jsonArray);
//        String tmpSign = "pic_" + 1;
//      Bitmap bitmap = BitmapFactory.decodeResource(getActivity().getApplicationContext().getResources(), getResources().getIdentifier(tmpSign, "drawable", getActivity().getPackageName()));
//        postData(bitmap);

        /**
         * On Click event for Single Gridview Item
         * */
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                // Sending image id to FullScreenActivity
                Intent i = new Intent(getActivity().getApplicationContext(), FullImageActivity.class);
                // passing array index
                i.putExtra("id", position);
                startActivityForResult(i, FULL_SCREEN);
            }
        });
        return view;
    }

    public static void postData(Bitmap imageToSend) {
        try
        {
            URL url = new URL("http://192.249.19.252:2080/upload-image");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Cache-Control", "no-cache");

            conn.setReadTimeout(35000);
            conn.setConnectTimeout(35000);

            // directly let .compress write binary image data
            // to the output-stream
            OutputStream os = conn.getOutputStream();
            imageToSend.compress(Bitmap.CompressFormat.JPEG, 100, os);
            conn.setRequestProperty("Content-Type", "multipart/form-data");
            conn.setFixedLengthStreamingMode(1024);
            os.flush();
            os.close();

            System.out.println("Response Code: " + conn.getResponseCode());

            InputStream in = new BufferedInputStream(conn.getInputStream());
            Log.d("sdfs", "sfsd");
            BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = responseStreamReader.readLine()) != null)
                stringBuilder.append(line).append("\n");
            responseStreamReader.close();

            String response = stringBuilder.toString();
            System.out.println(response);

            conn.disconnect();
        }
        catch(MalformedURLException e) {
            e.printStackTrace();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
    public void request(JSONArray pnarr){
        //JSON형식으로 데이터 통신을 진행합니다!
        //이제 전송해볼까요?
        final RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        final JsonArrayRequest jsonarrRequest = new JsonArrayRequest(Request.Method.POST, url, pnarr, new Response.Listener<JSONArray>() {
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

//    public void response(final ArrayList<Bitmap> arrayList, final JSONArray resultpnarr) throws JSONException {
//
//        try {
//            //sign = 2인 json을 만듦(DB에 저장된 갤러리 요청)
//            JSONObject request = new JSONObject();
//            request.put("sign","4");
//            JSONArray requestarr = new JSONArray();
//            requestarr.put(request);
//
//            //request를 전송
//            final RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
//            final JsonArrayRequest jsonarrRequest = new JsonArrayRequest(Request.Method.POST, url, requestarr, new Response.Listener<JSONArray>() {
//
//                //요청에 대한 응답
//                @Override
//                public void onResponse(JSONArray response) {
//                    try {
//                        JSONArray resultarr = new JSONArray(response.toString());
//                        Log.d("22222222", ""+response);
//                        for (int i = 0; i < resultarr.length(); i++) {
//                            JSONObject image = resultarr.getJSONObject(i);
//                            String byteArray = image.getString("img");
//                            Bitmap bitmap = getBitmapFromString(byteArray);
//                            arrayList.add(bitmap);
//                        }
//                    } catch (JSONException ex) {
//                        ex.printStackTrace();
//                    }
//                }
//
//                //서버로 데이터 전달 및 응답 받기에 실패한 경우 아래 코드가 실행됩니다
//            }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    error.printStackTrace();
//                    //Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
//                }
//            });
//            jsonarrRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//            requestQueue.add(jsonarrRequest);
//
//        }
//        catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
    private Bitmap getBitmapFromString(String string){
        String[] bytevalues = string.substring(1, string.length() -1).split(",");
        byte[] bytes = new byte[bytevalues.length];
        for(int j=0, len=bytes.length; j<len; j++){
            bytes[j] = Byte.parseByte(bytevalues[j].trim());
        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return bitmap;
    }
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.gallerymenubar, menu);
    }

    private byte[] getByteArrayFromBitmap(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        switch (item.getItemId()){
            case R.id.addpic:
                tedPermission();
                final CharSequence[] galorcam = {"갤러리", "카메라"};
                AlertDialog.Builder oDialog = new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
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
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FULL_SCREEN){
            gridView.setAdapter(imageAdapterinfrag);
        }
        else if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(getView().getContext(), "취소 되었습니다.", Toast.LENGTH_SHORT).show();

            if(tempFile != null) {
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
                String[] proj = { MediaStore.Images.Media.DATA };

                assert photoUri != null;
                cursor = getActivity().getContentResolver().query(photoUri, proj, null, null, null);

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
            Toast.makeText(getActivity(), "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            getActivity().finish();
            e.printStackTrace();
        }
        if (tempFile != null) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

                Uri photoUri = FileProvider.getUriForFile(getActivity(),
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
        File storageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "/몰입캠프/");
        if (!storageDir.exists()) storageDir.mkdirs();

        // 파일 생성
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        Log.d(TAG, "createImageFile : " + image.getAbsolutePath());

        return image;
    }

    /**
     *  tempFile 을 bitmap 으로 변환 후 ImageView 에 설정한다.
     */
    public void setImage() {

        ImageView imageView = gridviewitem.findViewById(R.id.gridview_item);


        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap originalBm = BitmapFactory.decodeFile(tempFile.getAbsolutePath(), options);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(tempFile.getAbsolutePath());
        }catch (IOException e){
            e.printStackTrace();
        }
        assert exif != null;
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap bmRotated = rotateBitmap(originalBm, orientation);
        Log.d(TAG, "setImage : " + tempFile.getAbsolutePath());
        imageView.setImageBitmap(bmRotated);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new GridView.LayoutParams(320, 230));

        ((MainActivity)getActivity()).imageAdapter.gridviewimages.add(bmRotated);
        Bitmap bitmap = Bitmap.createScaledBitmap(bmRotated, 340, 250, true);
        JSONArray jsonArray = new JSONArray();
        byte[] bytes = getByteArrayFromBitmap(bitmap);
        JSONObject obj =  new JSONObject();
        try {
                obj.put("img", Arrays.toString(bytes));
                obj.put("sign", 3);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(obj);
        request(jsonArray);
        gridView.setAdapter(((MainActivity)getActivity()).imageAdapter);
        /**
         *  tempFile 사용 후 null 처리를 해줘야 합니다.
         *  (resultCode != RESULT_OK) 일 때 tempFile 을 삭제하기 때문에
         *  기존에 데이터가 남아 있게 되면 원치 않은 삭제가 이뤄집니다.
         */
        if (tempFile == null) {
            Toast.makeText(getActivity(), "실패", Toast.LENGTH_SHORT).show();
        }
        tempFile = null;

    }

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
//    public void request(JSONArray imagebytearray){
//
//
//        //url 요청주소 넣는 editText를 받아 url만들기
//        String url = "http://192.249.19.252:2080";
//
//        //JSON형식으로 데이터 통신을 진행합니다!
//
//        try {
//            //주소록 전체가 담긴 pnarr에서 전송할 각 jsonObject 생성
//            for (int i = 0; i < imagebytearray.length(); i++) {
//
//                final RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
//                final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, sendjson, new Response.Listener<JSONObject>() {
//                    //데이터 전달을 끝내고 이제 그 응답을 받을 차례입니다.
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        try {
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    //서버로 데이터 전달 및 응답 받기에 실패한 경우 아래 코드가 실행됩니다.
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        error.printStackTrace();
//                        //Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//                jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//                requestQueue.add(jsonObjectRequest);
//                //
//            }
//        }
//        catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
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

        TedPermission.with(getActivity())
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2))
                .setDeniedMessage(getResources().getString(R.string.permission_1))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();

    }
}