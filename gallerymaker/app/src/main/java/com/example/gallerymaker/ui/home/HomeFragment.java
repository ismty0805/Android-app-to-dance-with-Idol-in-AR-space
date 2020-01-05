
package com.example.gallerymaker.ui.home;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.gallerymaker.MainActivity;
import com.example.gallerymaker.R;
import com.facebook.AccessToken;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;



/**
 * A placeholder fragment containing a simple view.
 */
public class HomeFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    final ArrayList<String> contacts = new ArrayList<String>();
    ListView list1;
    LinearLayout ll;
    Button loadbtn;
    Boolean isPermission = true;
    final String url = "http://192.249.19.252:2080";


    public static HomeFragment newInstance(int index) {
        HomeFragment fragment = new HomeFragment();
        Bundle bundle = new Bundle();
        //bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

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
                .setPermissions(Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS)
                .check();

    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.add_bar, menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        switch (item.getItemId()){
            case R.id.add_bar:
                addpersonshow(contacts);
        }
        return true;
    }


    private  CallbackManager callbackManager;
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        View view = inflater.inflate(R.layout.fragment_phonenumber,container,false);
        LinearLayoutManager llm = new LinearLayoutManager(view.getContext());
        callbackManager = CallbackManager.Factory.create();

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        final boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

        ll = (LinearLayout) view.findViewById(R.id.LinearLayout1);
        list1 = (ListView) view.findViewById(R.id.list);
        loadbtn = (Button) view.findViewById(R.id.load);

        final LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        if(isLoggedIn){
            loadbtn.setVisibility(View.VISIBLE);
        }
        else{
            loadbtn.setVisibility(View.INVISIBLE);
        }
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));
        loginButton.setFragment(this);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {


            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        loadbtn.setVisibility(View.VISIBLE);
                        Log.v("result",object.toString());
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday");
                graphRequest.setParameters(parameters);
                graphRequest.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.d("logout?", "aa");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("LoginErr",error.toString());
            }
        });


        //전화번호부에서 번호 가져와서 서버로 전송

        final JSONArray pnarr = new JSONArray();
        final JSONArray resultpnarr = new JSONArray();

        call(pnarr); //주소록에서 전화번호들 json array로 만들어줌.
        request(pnarr,resultpnarr); //만든 json array를 하나씩 서버로 전송.

        //버튼을 누르면 (DB)서버로부터 전화번호 모두 받아옴
        Button.OnClickListener mClickListener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ll.removeView(loadbtn);
                try {
                    response(list1,resultpnarr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        loadbtn.setOnClickListener(mClickListener);



        //길게 클릭 -> 삭제알림
//        list1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int pos, long id) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                builder.setTitle("전화번호 삭제");
//                builder.setMessage("정말 삭제하시겠습니까?");
//
//                builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " asc";
//                        Context applicationContext = getActivity().getApplicationContext();
//                        Cursor c = applicationContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, sortOrder);
//                        for(int i =0; i<pos+1;i++){
//                            c.moveToNext();
//                        }
//                        String deleteID = c.getString(c.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));
//                        Log.v("long clicked", deleteID);
//                        getContext().getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI, ContactsContract.RawContacts.CONTACT_ID + " = " + deleteID, null);
//                        contacts.remove(pos);
//                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
//                                getActivity().getApplicationContext(), R.layout.text, contacts);
//                        adapter.notifyDataSetChanged();
//                        list1.setAdapter(adapter);
//                        Toast.makeText(getActivity().getApplicationContext(),"삭제완료",Toast.LENGTH_LONG).show();
//                    }
//                });
//
//                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        Toast.makeText(getActivity().getApplicationContext(),"취소됨",Toast.LENGTH_LONG).show();
//                    }
//                });
//                builder.show();
//                return true;
//
//            }
//        });

        return view;
    }


    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void call(JSONArray pnarr){
        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " asc";
        Context applicationContext = getActivity().getApplicationContext();
        Cursor c = applicationContext.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                null, null, sortOrder);

        while (c.moveToNext()) {

            JSONObject phonenumjson = new JSONObject();
            String contactName = c
                    .getString(c
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

            String phNumber = c
                    .getString(c
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String id =  c
                    .getString(c
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));

            try {
                phonenumjson.put("name",contactName);
                phonenumjson.put("phonenumber",phNumber);
                phonenumjson.put("sign","1");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            pnarr.put(phonenumjson);


        }
        c.close();


    }

    public void request(JSONArray pnarr, final JSONArray resultpnarr){

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

    public void response(final ListView list1, final JSONArray resultpnarr) throws JSONException {

        try {
            //sign = 2인 json을 만듦(DB에 저장된 전화번호부 요청)
            JSONObject request = new JSONObject();
            request.put("sign","2");
            JSONArray requestarr = new JSONArray();
            requestarr.put(request);

            //request를 전송
            final RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
            final JsonArrayRequest jsonarrRequest = new JsonArrayRequest(Request.Method.POST, url, requestarr, new Response.Listener<JSONArray>() {

                //요청에 대한 응답
                @Override
                public void onResponse(JSONArray response) {
                    try {
                        JSONArray resultarr = new JSONArray(response.toString());
                        Log.d("22222222", ""+response);
                        for (int i = 0; i < resultarr.length(); i++) {
                            JSONObject contact = resultarr.getJSONObject(i);
                            String resultname = contact.getString("name");
                            String resultph = contact.getString("phonenumber");
                            resultpnarr.put(contact);
                        }
                        final ArrayList<String> resultcontacts = new ArrayList<String>();
                        for(int i=0;i<resultpnarr.length();i++){
                            JSONObject resultjson = resultpnarr.getJSONObject(i);
                            String resultname = resultjson.getString("name");
                            String resultph = resultjson.getString("phonenumber");
                            resultcontacts.add("이       름 : "+ resultname + "\n"+ "전화번호 : " + resultph);
                        }
                        //리스트뷰로 보여주기
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                getActivity().getApplicationContext(), R.layout.text, resultcontacts);
                        list1.setAdapter(adapter);
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

    void addpersonshow(final ArrayList<String> contacts){

        final Context mContext = getActivity().getApplicationContext();
        final LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.addperson_dialog, (ViewGroup)getActivity().findViewById( R.id.addpersondialog));
        //final bookEntity bookEntity = new bookEntity();

        //실행코드
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show();

        //각 view 별 정의
        final EditText inputname = (EditText)view.findViewById(R.id.inputName);
        final EditText inputphonenum = (EditText)view.findViewById(R.id.inputPhonenumber);
        inputphonenum.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);

        Button cancel = (Button) view.findViewById(R.id.cancelbutton);
        Button save = (Button) view.findViewById(R.id.savebutton);


        //클릭했을 때 실행되는 매서드

        cancel.setOnClickListener(
                new Button.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        dialog.dismiss();
                        Toast.makeText(getActivity().getApplicationContext(),"취소",Toast.LENGTH_LONG).show();
                    }
                });

        save.setOnClickListener(
                new Button.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        // 정보 저장
                        String name = inputname.getText().toString();
                        String phonenumber = inputphonenum.getText().toString();
                        ContactAdd(name, phonenumber);
                        contacts.add("이       름 : "+ name + "\n"+ "전화번호 : " + phonenumber);
                        //call(contacts);
                        Collections.sort(contacts);

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                getActivity().getApplicationContext(), R.layout.text, contacts);
                        adapter.notifyDataSetChanged();
                        list1.setAdapter(adapter);
                        Toast.makeText(getActivity().getApplicationContext(),"저장완료",Toast.LENGTH_LONG).show();
                        dialog.cancel();
                    }
                });

    }

    public void ContactAdd(String name, String phonenum){
        ArrayList<ContentProviderOperation> list = new ArrayList<>();
        try{
            list.add(
                    ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                            .build()
            );

            list.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)

                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)   //이름
                            .build()

            );

            list.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)

                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phonenum)           //전화번호
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE  , ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)   //번호타입(Type_Mobile : 모바일)

                            .build()
            );
            getActivity().getApplicationContext().getContentResolver().applyBatch(ContactsContract.AUTHORITY, list);  //주소록추가
            list.clear();   //리스트 초기화
        }catch(RemoteException e){
            e.printStackTrace();
        }catch(OperationApplicationException e){
            e.printStackTrace();
        }
    }

}