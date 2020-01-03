package com.example.gallerymaker;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter implements Filterable {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>() ;
    private ArrayList<ListViewItem> filteredItemList = listViewItemList;
    Filter listFilter;
    ListView_ImageList profile_image_lIst;

    // ListViewAdapter의 생성자
    public ListViewAdapter() {

    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
//        return listViewItemList.size();
        return filteredItemList.size();
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
        }

        // 반복적으로 실행되어 화면에 표시되는 item
        ImageView iconImageView = (ImageView) convertView.findViewById(R.id.listview_img);
        TextView nameView = (TextView) convertView.findViewById(R.id.textView1);
        TextView phoneNumberView = (TextView) convertView.findViewById(R.id.textView2);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        ListViewItem listViewItem = filteredItemList.get(position);
        nameView.setText(listViewItem.getName());
//        phoneNumberView.setText( listViewItem.getPhoneNumber() );
        Bitmap tmp = Bitmap.createScaledBitmap(listViewItem.getImg(), 200, 200, true);
        iconImageView.setImageBitmap(listViewItem.getImg());

        Log.d("phonenumber", listViewItem.getPhoneNumber());
        StringBuffer str = new StringBuffer(listViewItem.getPhoneNumber());
        if (str.length() == 11) {
            phoneNumberView.setText(str.substring(0, 3) + "-" + str.substring(3, 7) + "-" + str.substring(7));
        }
        else
            phoneNumberView.setText(str);

        return convertView;
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position ;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        return filteredItemList.get(position);
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(Bitmap imgBitmap, String name, String phone_number, boolean isBlock, String memo) {
        ListViewItem item = new ListViewItem();
        item.setImg(imgBitmap);
        item.setName(name);
        item.setPhoneNumber(phone_number);
        item.setIsBlock(isBlock);
        item.setMemo(memo);
        listViewItemList.add(item);
        Log.d("added itme", name);
    }

    @Override
    public Filter getFilter() {
        if (listFilter == null) {
            listFilter = new ListFilter();
        }
        return listFilter;
    }

    private class ListFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults() ;

            if (constraint == null || constraint.length() == 0) {
                results.values = listViewItemList ;
                results.count = listViewItemList.size() ;
            } else {
                ArrayList<ListViewItem> itemList = new ArrayList<ListViewItem>() ;

                for (ListViewItem item : listViewItemList) {
                    if (item.getName().toUpperCase().contains(constraint.toString().toUpperCase()) ||
                            item.getPhoneNumber().toUpperCase().contains(constraint.toString().toUpperCase()))
                    {
                        itemList.add(item) ;
                    }
                }

                results.values = itemList ;
                results.count = itemList.size() ;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {// update listview by filtered data list.
            filteredItemList = (ArrayList<ListViewItem>) results.values ;
            // notify
            if (results.count > 0) {
                notifyDataSetChanged() ;
            } else {
                notifyDataSetInvalidated() ;
            }
        }
    }
}

