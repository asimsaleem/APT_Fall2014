package com.ut.apt.connexusmobile.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ut.apt.connexusmobile.R;
import com.ut.apt.connexusmobile.adapter.ViewAllStreamsImageAdapter;
import com.ut.apt.connexusmobile.adapter.ViewSingleStreamImageAdapter;
import com.ut.apt.connexusmobile.model.ViewAllStreamModel;
import com.ut.apt.connexusmobile.model.ViewSingleStreamModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asim on 10/20/14.
 */
public class ViewSingleStreamFragment extends Fragment implements AdapterView.OnItemClickListener{

    private String TAG = "ViewSingleStreamFragment";

    //Initializing the Callbacks
    OnMorePicturesButtonClickListener mOnMorePicturesButtonClickListenerCallback;
    OnUploadImageButtonClickListener mOnUploadImageButtonClickListenerCallback;
    OnStreamsButtonClickListener mOnStreamsButtonClickListenerCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mOnMorePicturesButtonClickListenerCallback = (OnMorePicturesButtonClickListener) activity;
            mOnUploadImageButtonClickListenerCallback = (OnUploadImageButtonClickListener) activity;
            mOnStreamsButtonClickListenerCallback = (OnStreamsButtonClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMorePicturesButtonClickListener/OnUploadImageButtonClickListener/OnStreamsButtonClickListener");
        }
    }

    private List<ViewSingleStreamModel> mItems;
    private ViewSingleStreamImageAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.e(TAG, "Inside onCreate Method of ViewSingleStreamFragment - START");

        // initialize the items list
        mItems = new ArrayList<ViewSingleStreamModel>();
        Log.e(TAG, "Setting up the Input for the ViewSingleStreamModel Object by Parsing the Json first...");
        List<ViewSingleStreamModel> viewSingleStreamModelList = parseInputJson();


        for(ViewSingleStreamModel viewSingleStreamModel : viewSingleStreamModelList) {
            mItems.add(new ViewSingleStreamModel(viewSingleStreamModel.getStreamId(),
                    viewSingleStreamModel.getStreamName(),
                    viewSingleStreamModel.getUploadedImgsUrl(),viewSingleStreamModel.getCursorUrl()));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        Log.e(TAG, "Inside the onCreateView for View Single Stream Fragment");
        View fragmentView = inflater.inflate(R.layout.view_single_stream_layout_fragment, container, false);

        mAdapter = new ViewSingleStreamImageAdapter(getActivity(), mItems);

        //Initialize the GridView
        GridView gridView = (GridView) fragmentView.findViewById(R.id.gridViewPictures);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(this);

        final Button morePicsButton = (Button) fragmentView.findViewById(R.id.morePicsBtnId);
        final Button uploadImageButton = (Button) fragmentView.findViewById(R.id.uploadImgBtnId);
        final Button streamsButton = (Button) fragmentView.findViewById(R.id.streamsBtnId);

        final String streamName = mItems.get(0).getStreamName();
        final String cursorUrl = mItems.get(0).getCursorUrl();
        Log.e(TAG, "Stream Name being Set is: "  + streamName);
        TextView viewSingleStreamName = (TextView)fragmentView.findViewById(R.id.viewSingleStreamTxtId);
        viewSingleStreamName.setText("Stream Name: " + streamName);

        //More Pictures Button Click Handler
        morePicsButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Log.e(TAG, "MORE PICS BUTTON CLICKED with StreamName: " + streamName);
                mOnMorePicturesButtonClickListenerCallback.onMorePicturesButtonClick(streamName, cursorUrl);
            }
        });

        //Upload Image Button Click Handler
        uploadImageButton.setOnClickListener(new View.OnClickListener(){
           @Override
            public void onClick(View view) {
                Log.e(TAG, "UPLOAD IMAGE BUTTON CLICKED with StreamName: " + streamName);
                mOnUploadImageButtonClickListenerCallback.onUploadImageButtonClick(streamName);
            }
        });

        //Streams Button Click Handler
        streamsButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Log.e(TAG, "ViewSingleStreamFragment: Streams Button was clicked. Gotta go back to the View All Streams Page...");
                mOnStreamsButtonClickListenerCallback.onStreamsButtonClick();
            }
        });

        return fragmentView;
    }


    private List<ViewSingleStreamModel> parseInputJson(){
        Log.e(TAG, "Inside the method parseInputJson...");

        Bundle bundle = getArguments();
        String rawResponseData = bundle.getString("ViewSingleStreamResponse");

        //Parse the JSON data to retrieve the information needed for the
        JsonElement jsonElement = new JsonParser().parse(rawResponseData);
        JsonObject jsonObject  = jsonElement.getAsJsonObject();

        List<ViewSingleStreamModel> listOfViewSingleStreamModel = new ArrayList<ViewSingleStreamModel>();

        JsonArray jsonArray  = jsonObject.get("uploadedImgsUrl").getAsJsonArray();
        Log.e(TAG, "Size of JSON Array retrieved is: " + jsonArray.size());
        for(JsonElement jsonElementTmp : jsonArray){
            ViewSingleStreamModel viewSingleStreamModel = new ViewSingleStreamModel();
            viewSingleStreamModel.setStreamName(jsonObject.get("streamName").getAsString());
            viewSingleStreamModel.setStreamId(jsonObject.get("streamId").getAsString());
            viewSingleStreamModel.setCursorUrl(jsonObject.get("cursorValue").getAsString());
            viewSingleStreamModel.setUploadedImgsUrl(jsonElementTmp.getAsString());
            listOfViewSingleStreamModel.add(viewSingleStreamModel);
        }

        Log.e(TAG, "STREAM NAME IS: " + jsonObject.get("streamName").getAsString() + "& STREAM ID IS: " + jsonObject.get("streamId").getAsString());

        if(listOfViewSingleStreamModel.size() == 0){
            Log.e(TAG, "Size of List is 0. Setting just the Stream Name and the Stream Id atleast");
            ViewSingleStreamModel viewSingleStreamModel = new ViewSingleStreamModel();
            viewSingleStreamModel.setStreamName(jsonObject.get("streamName").getAsString());
            viewSingleStreamModel.setStreamId(jsonObject.get("streamId").getAsString());
            viewSingleStreamModel.setUploadedImgsUrl("");
            viewSingleStreamModel.setCursorUrl(jsonObject.get("cursorValue").getAsString());
            listOfViewSingleStreamModel.add(viewSingleStreamModel);
        }

        Log.e(TAG, "Size of List is: " + listOfViewSingleStreamModel.size());
        return listOfViewSingleStreamModel;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    //Add Callback Handlers
    public interface OnMorePicturesButtonClickListener{
        public void onMorePicturesButtonClick(String streamName, String cursorUrl);
    }

    public interface OnUploadImageButtonClickListener{
        public void onUploadImageButtonClick(String streamName);
    }

    public interface OnStreamsButtonClickListener{
        public void onStreamsButtonClick();
    }
}