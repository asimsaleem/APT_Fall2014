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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ut.apt.connexusmobile.R;
import com.ut.apt.connexusmobile.adapter.ViewAllStreamsImageAdapter;
import com.ut.apt.connexusmobile.model.ViewAllStreamModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asim on 10/20/14.
 */
public class MySubscribedStreamsFragment extends Fragment implements AdapterView.OnItemClickListener{

    private String TAG = "MySubscribedStreamsFragment";

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnImageStreamClickListener/OnSearchButtonClickListener/OnNearbyButtonClickListener/OnMySubscribedStreamButtonClickListener");
        }
    }

    private List<ViewAllStreamModel> mItems;
    private ViewAllStreamsImageAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.e(TAG, "Inside onCreate Method of MySubscribedStreamsFragment");

        // initialize the items list
        mItems = new ArrayList<ViewAllStreamModel>();

        Log.e(TAG, "Setting up the Input for the ViewAllStreamModel Object by Parsing the Json first...");
        List<ViewAllStreamModel> viewAllStreamModelList = parseInputJson();
        for(ViewAllStreamModel viewAllStreamModel : viewAllStreamModelList){
            mItems.add(new ViewAllStreamModel(viewAllStreamModel.getCoverImgUrl(), viewAllStreamModel.getStreamName()));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the root view of the fragment
        Log.e(TAG, "Inside the onCreateView method of the MySubscribedStreamsFragment - START");
        final View fragmentView = inflater.inflate(R.layout.view_subscribed_streams_layout_fragment, container, false);

        //Initialize the adapter
        mAdapter = new ViewAllStreamsImageAdapter(getActivity(), mItems);

        //Initialize the GridView
        GridView gridView = (GridView) fragmentView.findViewById(R.id.gridView);
        gridView.setAdapter(mAdapter);

        Log.e(TAG, "Inside the onCreateView method of the MySubscribedStreamsFragment - END");
        return fragmentView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        //Retrieve the GridView item
        ViewAllStreamModel item = mItems.get(position);

        Log.e(TAG, "Inside the onItemClick Method of MySubscribedStreamsFragment - START");
        Log.e(TAG, "Clicked Stream Name is:" +  item.getStreamName());

        //Display Toast
        Toast.makeText(getActivity(), "Opening Stream: " + item.getStreamName(), Toast.LENGTH_SHORT).show();

        //Invoke the Callback method to the Main Activity here in order to display the View Single Stream Page
        String streamId = item.getStreamName().replace(" ", "").trim();
        Log.e(TAG, "Converting Stream Name {" + item.getStreamName() + "} to Stream Id: {" + streamId + "}");
    }

    private List<ViewAllStreamModel> parseInputJson(){

        Log.e(TAG, "Inside the method parseInputJson...");

        Bundle bundle = getArguments();
        String rawResponseData = bundle.getString("MySubscribedStreamsResponse");
        Log.e(TAG, "Raw Response Data is: " + rawResponseData);

        //Parse the JSON data to retrieve the information needed for the
        JsonElement jsonElement = new JsonParser().parse(rawResponseData);
        JsonObject jsonObject  = jsonElement.getAsJsonObject();
        JsonArray jsonArray  = jsonObject.get("subscribed_streams").getAsJsonArray();
        Log.e(TAG, "Size of JSON Array retrieved is: " + jsonArray.size());

        List<ViewAllStreamModel> listOfViewAllStreamModel = new ArrayList<ViewAllStreamModel>();
        for(JsonElement json : jsonArray){

            ViewAllStreamModel viewAllStreamModel = new ViewAllStreamModel();
            viewAllStreamModel.setStreamId(json.getAsJsonObject().get("streamId").getAsString());
            viewAllStreamModel.setStreamName(json.getAsJsonObject().get("streamName").getAsString());
            viewAllStreamModel.setCoverImgUrl(json.getAsJsonObject().get("coverImgUrl").getAsString());
            listOfViewAllStreamModel.add(viewAllStreamModel);
            Log.e(TAG, "parseInputJson Stream Name is: " +  json.getAsJsonObject().get("streamName").getAsString());
        }

        return listOfViewAllStreamModel;
    }
}