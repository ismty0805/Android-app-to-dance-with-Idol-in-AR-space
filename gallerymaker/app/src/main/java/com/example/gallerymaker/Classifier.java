package com.example.gallerymaker;


import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Classifier {
    private static final String LOG_TAG = Classifier.class.getSimpleName();

    private static final String MODEL_NAME = "detect.tflite";

    private static final int BATCH_SIZE = 1;
    public static final int IMG_HEIGHT = 300;
    public static final int IMG_WIDTH = 300;
    private static final int NUM_CHANNEL = 1;
    private static final int NUM_CLASSES = 10;

    private final Interpreter.Options options = new Interpreter.Options();
    private final Interpreter mInterpreter;
    private final ByteBuffer mImageData;
    private final int[] mImagePixels = new int[IMG_HEIGHT * IMG_WIDTH];
    private final float[][] mResult = new float[4][NUM_CLASSES];

    public Classifier(Activity activity) throws IOException {
        mInterpreter = new Interpreter(loadModelFile(activity), options);
        mImageData = ByteBuffer.allocateDirect(
                4 * BATCH_SIZE * IMG_HEIGHT * IMG_WIDTH * NUM_CHANNEL);
        mImageData.order(ByteOrder.nativeOrder());
    }

    public Result classify(Bitmap bitmap, Activity activity) {
        long startTime = SystemClock.uptimeMillis();
        float[][][] outputLocations = new float[1][NUM_CLASSES][4];
        float[][] outputClasses = new float[1][NUM_CLASSES];
        float[][] outputScores = new float[1][NUM_CLASSES];
        float[] numDetections = new float[1];
        Map<Integer, Object> outputMap = new HashMap<>();
        outputMap.put(0, outputLocations);
        outputMap.put(1, outputClasses);
        outputMap.put(2, outputScores);
        outputMap.put(3, numDetections);
        Object[] inputArray = {convertBitmapToByteBuffer(bitmap)};
        mInterpreter.runForMultipleInputsOutputs(inputArray, outputMap);
        long endTime = SystemClock.uptimeMillis();
        long timeCost = endTime - startTime;
        Log.v(LOG_TAG, "classify(): result = " + Arrays.toString((float[][])(outputMap.get(1)))
                + ", timeCost = " + timeCost);
        Result result = new Result(outputMap, timeCost, activity);
        return result;
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BATCH_SIZE * IMG_HEIGHT * IMG_WIDTH * 3);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[IMG_HEIGHT * IMG_WIDTH];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < IMG_HEIGHT; ++i) {
            for (int j = 0; j < IMG_WIDTH; ++j) {
                final int val = intValues[pixel++];
                byteBuffer.put((byte) ((val >> 16) & 0xFF));
                byteBuffer.put((byte) ((val >> 8) & 0xFF));
                byteBuffer.put((byte) (((val) & 0xFF)));
            }
        }
        return byteBuffer;
    }


    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {

        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_NAME);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

//    private void convertBitmapToByteBuffer(Bitmap bitmap) {
//        if (mImageData == null) {
//            return;
//        }
//        mImageData.rewind();
//
//        bitmap.getPixels(mImagePixels, 0, bitmap.getWidth(), 0, 0,
//                bitmap.getWidth(), bitmap.getHeight());
//
//        int pixel = 0;
//        for (int i = 0; i < IMG_WIDTH; ++i) {
//            for (int j = 0; j < IMG_HEIGHT; ++j) {
//                int value = mImagePixels[pixel++];
//                mImageData.putFloat(convertPixel(value));
//            }
//        }
//    }

    private static float convertPixel(int color) {
//        return (255-(((color >> 16) & 0xFF) * 0.299f
//                + ((color >> 8) & 0xFF) * 0.587f
//                + (color & 0xFF) * 0.114f)) / 255.0f;
        return ((color & 0xFF)) / 255.0f;
    }
}