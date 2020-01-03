package com.example.gallerymaker;

import android.app.Activity;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Random;

public class Result {
    private final String[] mallLabels;
    private final String[] mHashtags;
    private final String[] mgoodLabels;
    private final String mjson;
    private final float[] mgoodPercent;
    private final long mTimeCost;
    private final Map<Integer, Object> mProbs;
    private final Map<String, Object> mretMap;

    public Result(Map<Integer, Object> probs, long timeCost, Activity activity) {
        mTimeCost = timeCost;
        mProbs = probs;
        mallLabels = getAllLabels();
        mgoodLabels =  getGoodLabels();
        mgoodPercent = getGoodPercent();
        mjson = readJsonFile(activity);
        mretMap = getHashtagMap();
        mHashtags = new String[] {"instagood", "picoftheday", "ftf", "선팔맞팔", "daily", "HappyNewYear", "2020"};
    }

    //    public String getHashtags() {
//        String strhashtag = "";
    public ArrayList<String> getHashtags() {
        ArrayList<String> objectList = new ArrayList<>();
        ArrayList<Integer> maskList = new ArrayList<>();
        ArrayList<String> finaltagList = new ArrayList<>();
        for (int i=0;i<10;i++){
            if(mgoodPercent[i]<0.5){
                continue;
            }
            else{
                ArrayList hashtaglist = (ArrayList) mretMap.get(mgoodLabels[i]);
                LinkedTreeMap hashtagMap = (LinkedTreeMap) hashtaglist.get(0);
                Set set = hashtagMap.keySet();
                Iterator iterator = set.iterator();
                while(iterator.hasNext()){
                    String hashtag = (String)iterator.next();
                    if (objectList.contains(hashtag)){
                        int index = objectList.indexOf(hashtag);
                        maskList.set(index, maskList.get(index) + Integer.parseInt(((String)hashtagMap.get(hashtag))));
                    }
                    else {
                        objectList.add(hashtag);
                        maskList.add(Integer.parseInt(((String)hashtagMap.get(hashtag))));
                    }
                }


            }
        }
        for (int i=0;i<objectList.size();i++){
            if((maskList.get(i) >20) && (maskList.get(i)<300) ){
                finaltagList.add(objectList.get(i));
            }
        }
        Random random = new Random();
        int newadded = random.nextInt(mHashtags.length);
        while(finaltagList.size() <5) {
            finaltagList.add(mHashtags[newadded]);
            newadded = (newadded + 1)%mHashtags.length;
        }
//        for (int i=0;i<finaltagList.size();i++) {
//            strhashtag += "#" + finaltagList.get(i) +" ";
//        }
//        return strhashtag;
        return finaltagList;
    }


    public String[] getAllLabels() {
        return new String[] {"???", "person", "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat", "traffic light", "fire hydrant", "???", "stop sign", "parking meter", "bench", "bird", "cat", "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe", "???", "backpack", "umbrella", "???", "???", "handbag", "tie", "suitcase", "frisbee", "skis", "snowboard", "sports ball", "kite", "baseball bat", "baseball glove", "skateboard", "surfboard", "tennis racket", "bottle", "???", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple", "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", "chair", "couch", "potted plant", "bed", "???", "dining table", "???", "???", "toilet", "???", "tv", "laptop", "mouse", "remote", "keyboard", "cell phone", "microwave", "oven", "toaster", "sink", "refrigerator", "???", "book", "clock", "vase", "scissors", "teddy bear", "hair drier", "toothbrush"};
    }

    public String[] getGoodLabels() {
        String[] goodLabels = new String[10];
        int[] goodLabelIndexes = new int[10];
        for( int i=0;i<10;i++){
            goodLabelIndexes[i] = (int)((float[][]) (mProbs.get(1)))[0][i];
            goodLabels[i] = mallLabels[goodLabelIndexes[i]+1];
        }
        return goodLabels;
    }

    public String readJsonFile(Activity activity) {
        try {
            InputStream is =  activity.getResources().getAssets().open("labelTagMap.json");
            int fileSize = is.available();
            byte[] buffer = new byte[fileSize];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            Log.d("asset json", json);
            return json;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "failed";
    }

    public Map getHashtagMap() {
        Map<String, Object> retMap = new Gson().fromJson(
                mjson, new TypeToken<HashMap<String, Object>>() {}.getType()
        );
        return retMap;
    }


    public float[] getGoodPercent() {
        float[] goodPercent = new float[10];
        for( int i=0;i<10;i++) {
            goodPercent[i] = ((float[][]) (mProbs.get(2)))[0][i];
        }
        return goodPercent;
    }



    public long getTimeCost() {
        return mTimeCost;
    }


}