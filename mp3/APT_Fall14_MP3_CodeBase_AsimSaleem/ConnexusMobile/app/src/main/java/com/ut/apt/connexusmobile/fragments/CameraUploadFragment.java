package com.ut.apt.connexusmobile.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ut.apt.connexusmobile.R;
import com.ut.apt.connexusmobile.adapter.ViewAllStreamsImageAdapter;
import com.ut.apt.connexusmobile.model.ViewAllStreamModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by asim on 10/20/14.
 */
public class CameraUploadFragment extends Fragment{

    private String TAG  = "CameraUploadFragment";

    OnChooseFromLibraryClickListener mOnChooseFromLibraryClickListenerCallback;
    OnUseCameraButtonClickListener mOnUseCameraButtonClickListenerCallback;
    OnUploadButtonClickListener mOnUploadButtonClickListenerCallback;


    private String mStreamName;
    private String mFilePath;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mOnChooseFromLibraryClickListenerCallback = (OnChooseFromLibraryClickListener) activity;
            mOnUseCameraButtonClickListenerCallback = (OnUseCameraButtonClickListener) activity;
            mOnUploadButtonClickListenerCallback = (OnUploadButtonClickListener) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnChooseLFromLibraryClickListener/OnUseCameraButtonClickListener/OnUploadButtonClickListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.e(TAG, "Inside onCreate Method of CameraUploadFragment - START");

        //Set the Stream Name here
        Bundle bundle = getArguments();
        mStreamName  = bundle.getString("streamName");
        mFilePath = bundle.getString("filePath");
        Log.e(TAG, "Stream Name passed is: " + mStreamName + " and File Path is: " + mFilePath);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        Log.e(TAG, "Inside the onCreateView Method - START");
        View rootView = inflater.inflate(R.layout.camera_upload_layout_fragment, container, false);

        final Button chooseFromLibraryButton = (Button) rootView.findViewById(R.id.chooseFrmLibBtnId);
        final Button useCameraButton = (Button) rootView.findViewById(R.id.useCameraBtnId);
        final EditText addMessageOrTagText = (EditText) rootView.findViewById(R.id.addMsgOrTagTxtId);
        final Button uploadButton = (Button) rootView.findViewById(R.id.uploadBtnId);
        final TextView streamNameTxtView = (TextView) rootView.findViewById(R.id.streamTxtId);

        streamNameTxtView.setText("Stream Name: " + mStreamName);

        if(mFilePath != null) {
            Log.e(TAG, "Setting image into the Image View");
            final ImageView imgView = (ImageView) rootView.findViewById(R.id.imgView);
            imgView.setImageBitmap(BitmapFactory.decodeFile(mFilePath));
        }

        Log.e(TAG, "CameraUploadFragment onCreateView: Stream Name to be used is: " + mStreamName );
        Log.e(TAG, "CameraUploadFragment onCreateView: File Path available is: " + mFilePath);

        //Choose From Library Button Handler
        chooseFromLibraryButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.e(TAG, "Inside the Click Handle for Choose From Library Button");
                mOnChooseFromLibraryClickListenerCallback.onChooseFromLibraryButtonClick(mStreamName);
            }
        });

        //View Stream Button Handler
        useCameraButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.e(TAG, "Inside the Click Handle for Use Camera Button");
                mOnUseCameraButtonClickListenerCallback.onUseCameraStreamButtonClick(mStreamName);
            }
        });

        //Upload Button Handler
        uploadButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Log.e(TAG, "Inside the Click Handle for Upload Button");
                mOnUploadButtonClickListenerCallback.onUploadButtonClick(mStreamName, mFilePath);
            }
        });

        return rootView;
    }


    //Add Callback Handler
    public interface OnChooseFromLibraryClickListener{
        public void onChooseFromLibraryButtonClick(String streamName);
    }

    public interface OnUseCameraButtonClickListener{
        public void onUseCameraStreamButtonClick(String streamName);
    }

    public interface OnUploadButtonClickListener{
        public void onUploadButtonClick(String streamName, String filePath);
    }

}