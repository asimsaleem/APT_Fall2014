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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ut.apt.connexusmobile.R;
import com.ut.apt.connexusmobile.adapter.SearchStreamsImageAdapter;
import com.ut.apt.connexusmobile.adapter.ViewNearbyStreamsImageAdapter;
import com.ut.apt.connexusmobile.model.SearchStreamModel;
import com.ut.apt.connexusmobile.model.ViewNearbyStreamModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asim on 10/20/14.
 */
public class ViewNearbyStreamsFragment extends Fragment implements AdapterView.OnClickListener{

    private String TAG  = "ViewNearbyStreamsFragment";

    OnMoreButtonClickListener mOnMoreButtonClickListenerCallback;
    OnViewAllStreamsClickListener mOnViewAllStreamsClickListenerCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mOnMoreButtonClickListenerCallback = (OnMoreButtonClickListener) activity;
            mOnViewAllStreamsClickListenerCallback = (OnViewAllStreamsClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMoreButtonClickListener/OnViewAllStreamsClickListener");
        }
    }

    private List<ViewNearbyStreamModel> mItems;
    private ViewNearbyStreamsImageAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.e(TAG, "Inside onCreate Method of SearchResultsStreamFragment - START");

        //Initialize the items list
        mItems = new ArrayList<ViewNearbyStreamModel>();
        Log.e(TAG, "Setting up the Input for the SearchStreamModel Object by Parsing the Json first...");
        List<ViewNearbyStreamModel> viewNearbyStreamModelList = parseInputJson();
        for(ViewNearbyStreamModel viewNearbyStreamModel : viewNearbyStreamModelList){
            mItems.add(new ViewNearbyStreamModel(viewNearbyStreamModel.getStreamId(), viewNearbyStreamModel.getStreamName(),
                                                 viewNearbyStreamModel.getCoverImgUrl(), viewNearbyStreamModel.getStreamDistance()));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        Log.e(TAG, "Inside the onCreateView for View Stream Fragment");
        final View fragmentView = inflater.inflate(R.layout.view_nearby_stream_layout_fragment, container, false);

        //Initialize the adapter
        mAdapter = new ViewNearbyStreamsImageAdapter(getActivity(), mItems);

        //Initialize the GridView
        GridView gridView = (GridView) fragmentView.findViewById(R.id.gridView);
        gridView.setAdapter(mAdapter);

        final Button moreButton = (Button) fragmentView.findViewById(R.id.moreBtnId);
        final Button viewAllStreamsButton = (Button) fragmentView.findViewById(R.id.streamsBtnId);

        //More Button Click Handler
        moreButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Log.e(TAG, "MORE BUTTON IS CLICKED");
                mOnMoreButtonClickListenerCallback.onMoreButtonClick();
            }
        });

        //View All Streams Button Click Handler
        viewAllStreamsButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Log.e(TAG, "NEARBY SEARCH BUTTON CLICKED");
                mOnViewAllStreamsClickListenerCallback.onViewAllStreamsButtonClick();
            }
        });

      return fragmentView;
    }

    @Override
    public void onClick(View view) {

    }

    private List<ViewNearbyStreamModel> parseInputJson(){
        Log.e(TAG, "Inside the method parseInputJson...");

        Bundle bundle = getArguments();
        String rawResponseData = bundle.getString("nearbyStreamsResponse");
        Log.e(TAG, "Raw Response Data is: " + rawResponseData);

        //Parse the JSON data to retrieve the information needed for the
        JsonElement jsonElement = new JsonParser().parse(rawResponseData);
        JsonObject jsonObject  = jsonElement.getAsJsonObject();
        JsonArray jsonArray  = jsonObject.get("searchedStreams").getAsJsonArray();
        Log.e(TAG, "Size of JSON Array retrieved is: " + jsonArray.size());

        List<ViewNearbyStreamModel> listOfViewNearbyStreamModel = new ArrayList<ViewNearbyStreamModel>();
        for(JsonElement json : jsonArray){

            ViewNearbyStreamModel viewNearbyStreamModel = new ViewNearbyStreamModel();
            viewNearbyStreamModel.setStreamId(json.getAsJsonObject().get("streamId").getAsString());
            viewNearbyStreamModel.setStreamName(json.getAsJsonObject().get("streamName").getAsString());
            viewNearbyStreamModel.setCoverImgUrl(json.getAsJsonObject().get("coverImgUrl").getAsString());
            viewNearbyStreamModel.setStreamDistance(json.getAsJsonObject().get("distance").getAsString());
            listOfViewNearbyStreamModel.add(viewNearbyStreamModel);
            Log.e(TAG, "parseInputJson Stream Name is: " +  json.getAsJsonObject().get("streamName").getAsString());
        }

        return listOfViewNearbyStreamModel;
    }


    //Add Callback Handlers
    public interface OnMoreButtonClickListener{
        public void onMoreButtonClick();
    }

    public interface OnViewAllStreamsClickListener{
        public void onViewAllStreamsButtonClick();
    }
}