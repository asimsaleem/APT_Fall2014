package com.ut.apt.connexusmobile.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ut.apt.connexusmobile.R;
import com.ut.apt.connexusmobile.main.CameraPreview;
import android.hardware.Camera.PictureCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.spec.ECField;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by asim on 10/20/14.
 */
public class CustomCameraFragment extends Fragment{

    private String TAG  = "CustomCameraFragment";

    OnTakePictureClickListener mOnTakePictureClickListenerCallback;
    OnUsePictureButtonClickListener mOnUsePictureButtonClickListenerCallback;
    OnStreamsButtonClickListener mOnStreamsButtonClickListenerCallback;

    private Camera mCamera;
    private CameraPreview mPreview;
    private ImageView pic;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mOnTakePictureClickListenerCallback = (OnTakePictureClickListener) activity;
            mOnUsePictureButtonClickListenerCallback = (OnUsePictureButtonClickListener) activity;
            mOnStreamsButtonClickListenerCallback = (OnStreamsButtonClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnChooseLFromLibraryClickListener/OnUseCameraButtonClickListener/OnStreamsButtonClickListener");
        }
    }

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1888;

    File callbackFileGlbl;
    private String mStreamName;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.e(TAG, "Inside onCreate Method of CameraUploadFragment - START");

        //Set the Stream Name here
        Bundle bundle = getArguments();
        mStreamName  = bundle.getString("streamName");
        Log.e(TAG, "Stream Name passed is: " + mStreamName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        Log.e(TAG, "Inside the OnCreateView method of the Custom Camera Fragment");

        View rootView = inflater.inflate(R.layout.camera_layout_fragment, container, false);

        final Button takePictureButton = (Button) rootView.findViewById(R.id.takePicBtnId);
        final Button usePictureButton = (Button) rootView.findViewById(R.id.usePicBtnId);
        final Button streamsButton = (Button) rootView.findViewById(R.id.streamsBtnId);

        Log.e(TAG, "Checking if Camera is Available");
        mCamera = isCameraAvailable();

        Log.e(TAG, "Camera object is: " + mCamera);

        mPreview = new CameraPreview(rootView.getContext(), mCamera);
        Log.e(TAG, "Setting up Camera Preview");
        FrameLayout preview = (FrameLayout) rootView.findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        //Take Picture Button Click Handler
        takePictureButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.e(TAG, "TAKE PICTURE BUTTON CLICKED");
                Log.e(TAG, "MCamera is: " + mCamera);
                try {
                    mCamera.takePicture(null, null, jpegCallback);
                }catch(Exception e){
                    e.printStackTrace();
                    mCamera.startPreview();
                }
            }
        });

        //Use Picture Button Handler
        usePictureButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.e(TAG, "Inside the Click Handle for Use Picture Button");
                File callbackFile = callbackFileGlbl;
                Log.e(TAG, "CallBack file is: " + callbackFile + "with Stream Name: " + mStreamName);
                if(callbackFile != null)
                    mOnUsePictureButtonClickListenerCallback.onUsePictureButtonClick(callbackFile, mStreamName);
                else
                    return;
            }
        });

        //Streams Button Click Handler
        streamsButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Log.e(TAG, "Inside the Click Handle for Streams Button");
                mOnStreamsButtonClickListenerCallback.onStreamsButtonClick();
            }
        });

        return rootView;
    }

    PictureCallback jpegCallback = new PictureCallback() {

        public void onPictureTaken(byte[] data, Camera camera) {
            Log.e(TAG, "Inside the OnPictureTaken Method");
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions: "
                );
                return;
            }

            try {
                Log.e(TAG, "FIle is:  " + pictureFile);
                Log.e(TAG, "Setting the Callback File");
                callbackFileGlbl = pictureFile;
                FileOutputStream fos = new FileOutputStream(pictureFile);
                Log.e(TAG, "File being written");
                fos.write(data);
                fos.close();
                Log.e(TAG, "File written and Closed");
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

    public static final int MEDIA_TYPE_IMAGE = 1;
    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        }  else {
            return null;
        }

        return mediaFile;
    }

    //Add a Callback Handler
    public interface OnTakePictureClickListener{
        public void onTakePictureButtonClick();
    }

    public interface OnUsePictureButtonClickListener{
        public void onUsePictureButtonClick(File callbackFile, String streamName);
    }

    public interface OnStreamsButtonClickListener{
        public void onStreamsButtonClick();
    }

    public static Camera isCameraAvailable(){

        Camera mCamera = null;
       try {
            mCamera = Camera.open();
       }catch (Exception e){
            e.printStackTrace();
       }
        return mCamera;
    }
}
