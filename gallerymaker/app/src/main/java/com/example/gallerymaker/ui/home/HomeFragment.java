
package com.example.gallerymaker.ui.home;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.gallerymaker.MainActivity;
import com.example.gallerymaker.R;
import com.google.gson.JsonObject;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

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


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phonenumber,container,false);
        LinearLayoutManager llm = new LinearLayoutManager(view.getContext());
        ll = (LinearLayout) view.findViewById(R.id.LinearLayout1);
        list1 = (ListView) view.findViewById(R.id.list);
        loadbtn = (Button) view.findViewById(R.id.load);

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
        list1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int pos, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("전화번호 삭제");
                builder.setMessage("정말 삭제하시겠습니까?");

                builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " asc";
                        Context applicationContext = getActivity().getApplicationContext();
                        Cursor c = applicationContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, sortOrder);
                        for(int i =0; i<pos+1;i++){
                            c.moveToNext();
                        }
                        String deleteID = c.getString(c.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));
                        Log.v("long clicked", deleteID);
                        getContext().getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI, ContactsContract.RawContacts.CONTACT_ID + " = " + deleteID, null);
                        contacts.remove(pos);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                getActivity().getApplicationContext(), R.layout.text, contacts);
                        adapter.notifyDataSetChanged();
                        list1.setAdapter(adapter);
                        Toast.makeText(getActivity().getApplicationContext(),"삭제완료",Toast.LENGTH_LONG).show();
                    }
                });

                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity().getApplicationContext(),"취소됨",Toast.LENGTH_LONG).show();
                    }
                });
                builder.show();
                return true;

            }
        });

        return view;
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
            } catch (JSONException e) {
                e.printStackTrace();
            }

            pnarr.put(phonenumjson);


        }
        c.close();


    }

    public void request(JSONArray pnarr, final JSONArray resultpnarr){


        //url 요청주소 넣는 editText를 받아 url만들기
        String url = "http://192.249.19.252:2080";

        //JSON형식으로 데이터 통신을 진행합니다!

            try {
                //주소록 전체가 담긴 pnarr에서 전송할 각 jsonObject 생성
                for (int i = 0; i < pnarr.length(); i++) {
                    JSONObject phonenumjson = pnarr.getJSONObject(i);
                    Log.d("name : ", phonenumjson.getString("name"));
                    //데이터를 json형식으로 바꿔 넣어줌

                    final String jsonString = phonenumjson.toString(); //완성된 json 포맷

                    phonenumjson.put("sign", "1");
                    //이제 전송해볼까요?
                                final RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
                                final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, phonenumjson, new Response.Listener<JSONObject>() {
                                    //데이터 전달을 끝내고 이제 그 응답을 받을 차례입니다.
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            Log.d("@@@@", "2");

                                            //받은 json형식의 응답을 받아
                                            JSONObject jsonObject = new JSONObject(response.toString());

                                            //key값에 따라 value값을 쪼개 받아옵니다.
                                            String resultId = jsonObject.getString("name");
                                            String resultPassword = jsonObject.getString("phonenum");
                                            resultpnarr.put(jsonObject);
                                            Log.d("@@@@",jsonObject.toString());

                                //만약 그 값이 같다면 로그인에 성공한 것입니다.
                                if (resultId.equals("OK") & resultPassword.equals("OK")) {
                                    Log.d("######", "3");
                                    //이 곳에 성공 시 화면이동을 하는 등의 코드를 입력하시면 됩니다.
                                } else {
                                    Log.d("$$$$$$$$", "4");
                                    //로그인에 실패했을 경우 실행할 코드를 입력하시면 됩니다.
                                }

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
                    jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    requestQueue.add(jsonObjectRequest);
                    //
                }
            }
                catch (JSONException e) {
                    e.printStackTrace();
                }
    }

    public void response(ListView list1, JSONArray resultpnarr) throws JSONException {

        final ArrayList<String> resultcontacts = new ArrayList<String>();
        for(int i=0;i<resultpnarr.length();i++){
            JSONObject resultjson = resultpnarr.getJSONObject(i);
            String resultname = resultjson.getString("name");
            String resultph = resultjson.getString("phonenum");
            resultcontacts.add("이       름 : "+ resultname + "\n"+ "전화번호 : " + resultph);
        }
        //리스트뷰로 보여주기
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getActivity().getApplicationContext(), R.layout.text, resultcontacts);
        adapter.notifyDataSetChanged();
        list1.setAdapter(adapter);
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