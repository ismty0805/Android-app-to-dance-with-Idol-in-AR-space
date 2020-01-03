package com.example.gallerymaker.ui.hashtag;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.media.ExifInterface;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.gallerymaker.Classifier;
import com.example.gallerymaker.R;
import com.example.gallerymaker.Result;
import com.google.android.flexbox.FlexboxLayout;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class HashtagFragment extends Fragment {
    private static final String TAG = "몰입캠프";
    private Boolean isPermission = true;
    private View view;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int PICK_FROM_CAMERA = 2;
    private File tempFile;


    private SurfaceView mSurfaceView, mSurfaceView_transparent;
    private SurfaceHolder mSurfaceViewHolder, mSurfaceViewHolder_transparent;
    private Handler mHandler;
    private ImageReader mImageReader;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mSession;
    private int mDeviceRotation;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private SensorManager mSensorManager;
    //private DeviceOrientation deviceOrientation;
    int mDSI_height, mDSI_width;
    int  deviceHeight,deviceWidth;
    int previewHeight, previewWidth;
    private int RectLeft, RectTop,RectRight,RectBottom ;
    int imageWidth=400;
    int imageHeight=400;



    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        view = inflater.inflate(R.layout.activity_get_image, container, false);
        view.findViewById(R.id.btnCopyTags).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View vview) {
                // 권한 허용에 동의하지 않았을 경우 토스트를 띄웁니다.
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                answerstr = "";
                for(int i=0;i<answer.size();i++){
                    answerstr +=  "#" + answer.get(i) + " ";
                }
                ClipData clip = ClipData.newPlainText("hashtags", answerstr);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(view.getContext(),"클립보드에 복사되었습니다", Toast.LENGTH_LONG).show();
            }
        });
        final EditText edittext = (EditText) view.findViewById(R.id.taginput);
        view.findViewById(R.id.btnInputtag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View vview) {
//                BaseInputConnection mInputConnection = new BaseInputConnection(view.findViewById(R.id.taginput), true);
//                KeyEvent k = new KeyEvent(KeyEvent.ACTION_DOWN , KeyEvent.KEYCODE_ENTER);
//                Log.d("@@@@@@@@@@@@@@@@@@@", "f??????????????????????????????????????");
//                mInputConnection.sendKeyEvent(k);
                final String hashtag = edittext.getText().toString();
                if(answer.contains(hashtag) == false) {
                    FlexboxLayout tagLayout = view.findViewById(R.id.taglayout);
                    LinearLayout tagandclose = new LinearLayout(getActivity());
                    final TextView myEditText = new TextView(getActivity());
                    final ImageButton button = new ImageButton(getActivity());
                    myEditText.setText("#" + hashtag + " ");
                    myEditText.setBackground(new ColorDrawable(000000));
                    myEditText.setTypeface(null, Typeface.BOLD);
                    myEditText.setTextSize(17);
                    button.setImageResource(R.drawable.ic_close);
                    button.setMaxWidth(12);
                    button.setMinimumHeight(22);
                    button.setBackgroundColor(000000);
                    button.setPadding(0, 0, 10, 15);
                    button.setAlpha(0.6f);
                    button.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            answer.remove(hashtag);
                            myEditText.setVisibility(View.GONE);
                            button.setVisibility(View.GONE);
                        }
                    });
                    tagandclose.addView(myEditText);
                    tagandclose.addView(button);
                    tagLayout.addView(tagandclose);
                    answer.add(hashtag);
                    edittext.setText("");
                    edittext.setHint("Add Hashtags");
                }
                edittext.setText("");
                edittext.setHint("Add Hashtags");

            }
        });




        view.findViewById(R.id.getimage).setOnClickListener(new View.OnClickListener(){
            public void onClick(View vview){
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
        });


        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (Character.isWhitespace(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };
        InputFilter maxLengthFilter = new InputFilter.LengthFilter(28);
        edittext.setFilters(new InputFilter[] { filter, maxLengthFilter });
        edittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if (keyCode== EditorInfo.IME_ACTION_DONE) {
                    // Perform action on key press
                    final String hashtag = edittext.getText().toString();
                    if(answer.contains(hashtag) == false) {
                        FlexboxLayout tagLayout = view.findViewById(R.id.taglayout);
                        LinearLayout tagandclose = new LinearLayout(getActivity());
                        final TextView myEditText = new TextView(getActivity());
                        final ImageButton button = new ImageButton(getActivity());
                        myEditText.setText("#" + hashtag + " ");
                        myEditText.setBackground(new ColorDrawable(000000));
                        myEditText.setTypeface(null, Typeface.BOLD);
                        myEditText.setTextSize(17);
                        button.setImageResource(R.drawable.ic_close);
                        button.setMaxWidth(12);
                        button.setMinimumHeight(22);
                        button.setBackgroundColor(000000);
                        button.setPadding(0, 0, 10, 15);
                        button.setAlpha(0.6f);
                        button.setOnClickListener(new Button.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                answer.remove(hashtag);
                                myEditText.setVisibility(View.GONE);
                                button.setVisibility(View.GONE);
                            }
                        });
                        tagandclose.addView(myEditText);
                        tagandclose.addView(button);
                        tagLayout.addView(tagandclose);
                        answer.add(hashtag);
                        edittext.setText("");
                        edittext.setHint("Add Hashtags");
                        return true;
                    }
                    edittext.setText("");
                    edittext.setHint("Add Hashtags");
                    return true;
                }
                return true;
            }
        });

//        view.findViewById(R.id.btnCallInstagram).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View vview) {
//                // 권한 허용에 동의하지 않았을 경우 토스트를 띄웁니다.
//                if(getPackageList()) {
//                    Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage("com.instagram.android");
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                }
//                else {
//                    String url = "https://play.google.com/store/apps/details?id=com.instagram.android&hl=en_US&referrer=utm_source%3Dgoogle%26utm_medium%3Dorganic%26utm_term%3Dinstagram&pcampaignid=APPU_1_2vQJXu-kCtKbmAXR6oXwAg";
//                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                    startActivity(i);
//                }
//            }
//        });
//
//        view.findViewById(R.id.btnGallery).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View vview) {
//                // 권한 허용에 동의하지 않았을 경우 토스트를 띄웁니다.
//                if(isPermission) {
//                    goToAlbum();
//                }
//                else {
//                    Toast.makeText(view.getContext(), getResources().getString(R.string.permission_2), Toast.LENGTH_LONG).show();
//                }
//            }
//        });
//
//
//
//        view.findViewById(R.id.btnCamera).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // 권한 허용에 동의하지 않았을 경우 토스트를 띄웁니다.
//                if(isPermission)  {
//                    takePhoto();
//                }
//                else {
//                    Toast.makeText(view.getContext(), getResources().getString(R.string.permission_2), Toast.LENGTH_LONG).show();
//                }
//            }
//        });

        return view;
    }


    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.insta_bar, menu);
    }
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        switch (item.getItemId()){
            case R.id.insta:
                if(getPackageList()) {
                    Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage("com.instagram.android");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                else {
                    String url = "https://play.google.com/store/apps/details?id=com.instagram.android&hl=en_US&referrer=utm_source%3Dgoogle%26utm_medium%3Dorganic%26utm_term%3Dinstagram&pcampaignid=APPU_1_2vQJXu-kCtKbmAXR6oXwAg";
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                }
//            case R.id.getPicforinsta:
//                tedPermission();
//                final CharSequence[] galorcam = {"갤러리", "카메라"};
//                AlertDialog.Builder oDialog = new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
//                oDialog.setTitle("사진 가져오기")
//                        .setItems(galorcam, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                String selectedText = galorcam[which].toString();
//                                if(selectedText == "갤러리"){
//                                    if(isPermission) goToAlbum();
//                                    else Toast.makeText(view.getContext(), getResources().getString(R.string.permission_2), Toast.LENGTH_LONG).show();
//                                }
//                                else{
//                                    if(isPermission)  takePhoto();
//                                    else Toast.makeText(view.getContext(), getResources().getString(R.string.permission_2), Toast.LENGTH_LONG).show();
//                                }
//                            }
//                        })
//                        .show();
        }
        return true;
    }

    public boolean getPackageList() {
        boolean isExist = false;

        PackageManager pkgMgr = getActivity().getPackageManager();
        List<ResolveInfo> mApps;
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps = pkgMgr.queryIntentActivities(mainIntent, 0);

        try {
            for (int i = 0; i < mApps.size(); i++) {
                if(mApps.get(i).activityInfo.packageName.startsWith("com.instagram.android")){
                    isExist = true;
                    break;
                }
            }
        }
        catch (Exception e) {
            isExist = false;
        }
        return isExist;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
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

        int viewHeight = 1000;
        int viewWidth = 1000;
        int sendsize = 300;
        float width = bmRotated.getWidth();
        float height = bmRotated.getHeight();
        Bitmap bmResized1;
        if(height<width) {
            if (height > viewHeight) {
                float percente = (float) (height / 100);
                float scale = (float) (viewHeight / percente);
                width *= (scale / 100);
                height *= (scale / 100);
                bmResized1 = Bitmap.createScaledBitmap(bmRotated, (int) width, (int) height, true);
            } else {
                bmResized1 = Bitmap.createScaledBitmap(bmRotated, (int) (width * ((float) viewHeight / height)), viewHeight, true);
            }
        }
        else{
            if(width>viewWidth){
                float percente = (float) (width/100);
                float scale = (float) (viewWidth / percente);
                width *= (scale / 100);
                height *= (scale / 100);
                bmResized1 = Bitmap.createScaledBitmap(bmRotated, (int) width, (int) height, true);
            } else {
                bmResized1 = Bitmap.createScaledBitmap(bmRotated, viewWidth, (int)(height*((float)viewWidth/width)), true);
            }
        }
        Bitmap bmResized2 = Bitmap.createScaledBitmap(bmRotated, sendsize, sendsize, true);
//        imageView.setImageBitmap(bmResized1);
        ImageButton imageButton = view.findViewById(R.id.getimage);
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
        Bitmap bmResized3 = Bitmap.createScaledBitmap(bmResized1, 1500, 1500, true);
        imageButton.setImageBitmap(bmResized3);


        try {
            mClassifier = new Classifier(getActivity());
        } catch (IOException e) {
            Toast.makeText(getActivity(),"Failed to create Classifier", Toast.LENGTH_LONG).show();
            Log.e("@@@", "Failed to create Classifier", e);
        }

//
//        TextView textview = view.findViewById(R.id.textView);
//        result = mClassifier.classify(bmResized2);
//        answer = result.getHashtags();
//        Log.d("", ""+answer);
//        textview.setText(answer);
//
//

//        EditText textview = view.findViewById(R.id.textView);
        result = mClassifier.classify(bmResized2, getActivity());
        answer = result.getHashtags();
        Log.d("", ""+answer);
        FlexboxLayout tagLayout = view.findViewById(R.id.taglayout);
        tagLayout.removeAllViews();
        for(int i=0;i<answer.size();i++){
            LinearLayout tagandclose = new LinearLayout(getActivity());
            final TextView myEditText = new TextView(getActivity());
            final ImageButton button = new ImageButton(getActivity());
            final String hashtag = answer.get(i);
            myEditText.setText("#"+ hashtag + " ");
            myEditText.setBackground(new ColorDrawable(000000));
            myEditText.setTypeface(null, Typeface.BOLD);
            myEditText.setTextSize(17);
            button.setImageResource(R.drawable.ic_close);
            button.setMaxWidth(12);
            button.setMinimumHeight(22);
            button.setBackgroundColor(000000);
            button.setPadding(0,0,10,15);
            button.setAlpha(0.6f);
            button.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    answer.remove(hashtag);
                    myEditText.setVisibility(View.GONE);
                    button.setVisibility(View.GONE);
                }
            });
            tagandclose.addView(myEditText);
            tagandclose.addView(button);
            tagLayout.addView(tagandclose);
        }
//        textview.setText(answer);

        view.findViewById(R.id.btnCopyTags).setVisibility(View.VISIBLE);
//        view.findViewById(R.id.btnInputtag).setVisibility(View.VISIBLE);
        view.findViewById(R.id.taginput).setVisibility(View.VISIBLE);
        view.findViewById(R.id.getimage).setAlpha(1.0f);
//        view.findViewById(R.id.getimage).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.plzgetpic).setVisibility(View.INVISIBLE);
//        view.findViewById(R.id.btnCallInstagram).setVisibility(View.VISIBLE);

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

        TedPermission.with(getActivity())
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2))
                .setDeniedMessage(getResources().getString(R.string.permission_1))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();

    }

}