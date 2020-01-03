
package com.example.gallerymaker.ui.home;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
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
import androidx.fragment.app.ListFragment;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.example.gallerymaker.MainActivity;
import com.example.gallerymaker.R;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A placeholder fragment containing a simple view.
 */
public class HomeFragment extends ListFragment{

    private static final String ARG_SECTION_NUMBER = "section_number";
    ListView list1;
    LinearLayout ll;
    Button addBtn;

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

    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phonenumber,container,false);
        LinearLayoutManager llm = new LinearLayoutManager(view.getContext());
        ll = (LinearLayout) view.findViewById(R.id.LinearLayout1);
        list1 = (ListView) view.findViewById(android.R.id.list);
        addBtn = (Button) view.findViewById(R.id.addperson);

        //전화번호부에서 번호 가져오기
        final ArrayList<String> contacts = new ArrayList<String>();

        call(contacts);

        //추가 버튼
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                addpersonshow(contacts);
            }
        });
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

    public void call(ArrayList<String> contacts){
        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " asc";
        Context applicationContext = getActivity().getApplicationContext();
        Cursor c = applicationContext.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                null, null, sortOrder);
        while (c.moveToNext()) {
            String contactName = c
                    .getString(c
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phNumber = c
                    .getString(c
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String id =  c
                    .getString(c
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));

            contacts.add("이       름 : "+ contactName + "\n"+ "전화번호 : " + phNumber);
        }
        c.close();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getActivity().getApplicationContext(), R.layout.text, contacts);
        adapter.notifyDataSetChanged();
        list1.setAdapter(adapter);
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