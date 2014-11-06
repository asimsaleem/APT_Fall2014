package com.ut.apt.connexusmobile.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ut.apt.connexusmobile.R;
import com.ut.apt.connexusmobile.main.CameraPreview;

/**
 * Created by asim on 10/20/14.
 */
public class CustomCameraFragment extends Fragment{

    private String TAG  = "CustomCameraFragment";

    OnTakePictureClickListener mOnTakePictureClickListenerCallback;
    OnUsePictureButtonClickListener mOnUsePictureButtonClickListenerCallback;
    OnStreamsButtonClickListener mOnStreamsButtonClickListenerCallback;

    private Camera cameraObject;
    private CameraPreview showCamera;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        Log.e(TAG, "Inside the OnCreateView method of the Custom Camera Fragment");

        View rootView = inflater.inflate(R.layout.camera_layout_fragment, container, false);

        final Button takePictureButton = (Button) rootView.findViewById(R.id.takePicBtnId);
        final Button usePictureButton = (Button) rootView.findViewById(R.id.usePicBtnId);
        final Button streamsButton = (Button) rootView.findViewById(R.id.streamsBtnId);

        Log.e(TAG, "Checking if Camera is Available");
        cameraObject = isCameraAvailable();

        Log.e(TAG, "Camera object is: " + cameraObject);

        showCamera = new CameraPreview(rootView.getContext(), cameraObject);
        Log.e(TAG, "Setting up Camera Preview");
        FrameLayout preview = (FrameLayout) rootView.findViewById(R.id.camera_preview);
        preview.addView(showCamera);

        //Take Picture Button Click Handler
        takePictureButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.e(TAG, "TAKE PICTURE BUTTON CLICKED");

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                mOnTakePictureClickListenerCallback.onTakePictureButtonClick();
            }
        });

        //Use Picture Button Handler
        usePictureButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.e(TAG, "Inside the Click Handle for Use Picture Button");
                mOnUsePictureButtonClickListenerCallback.onUsePictureButtonClick();
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

    //Add a Callback Handler
    public interface OnTakePictureClickListener{
        public void onTakePictureButtonClick();
    }

    public interface OnUsePictureButtonClickListener{
        public void onUsePictureButtonClick();
    }

    public interface OnStreamsButtonClickListener{
        public void onStreamsButtonClick();
    }

    public static Camera isCameraAvailable(){

        Camera mCamera = null;
       try {
            mCamera = Camera.open();
        }
        catch (Exception e){
        }
        return mCamera;
    }
}
