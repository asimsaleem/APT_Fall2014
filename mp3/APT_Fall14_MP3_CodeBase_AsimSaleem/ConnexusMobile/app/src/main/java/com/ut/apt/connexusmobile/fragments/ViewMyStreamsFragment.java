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
public class ViewMyStreamsFragment extends Fragment implements AdapterView.OnItemClickListener{

    private String TAG = "ViewMyStreamsFragment";

    OnViewMyImageStreamClickListener mViewMyImageStreamClickListenerCallback;
    OnViewMySubscribedStreamButtonClickListener mViewMySubscribedStreamButtonClickListenerCallback;
    OnSearchButtonClickListener mOnSearchStreamButtonClickListenerCallback;
    OnNearbyButtonClickListener mOnNearbyStreamButtonClickListenerCallback;


    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mViewMySubscribedStreamButtonClickListenerCallback = (OnViewMySubscribedStreamButtonClickListener) activity;
            mOnSearchStreamButtonClickListenerCallback = (OnSearchButtonClickListener) activity;
            mOnNearbyStreamButtonClickListenerCallback = (OnNearbyButtonClickListener) activity;
            mViewMyImageStreamClickListenerCallback = (OnViewMyImageStreamClickListener) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnViewMySubscribedStreamButtonClickListener/OnViewMyImageStreamClickListener");
        }
    }

    private List<ViewAllStreamModel> mItems;
    private ViewAllStreamsImageAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.e(TAG, "Inside onCreate Method of ViewMyStreamsFragment - START");

        //Initialize the items list
        mItems = new ArrayList<ViewAllStreamModel>();

        Log.e(TAG, "Setting up the Input for the SearchStreamModel Object by Parsing the Json first...");
        List<ViewAllStreamModel> viewMyStreamModelList = parseInputJson();
        for(ViewAllStreamModel viewMyStreamModel : viewMyStreamModelList){
            mItems.add(new ViewAllStreamModel(viewMyStreamModel.getCoverImgUrl(), viewMyStreamModel.getStreamName()));
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        Log.e(TAG, "Inside the onCreateView for View Stream Fragment");
        final View fragmentView = inflater.inflate(R.layout.view_my_streams_layout_fragment, container, false);

        //Initialize the adapter
        mAdapter = new ViewAllStreamsImageAdapter(getActivity(), mItems);

        //Initialize the GridView
        GridView gridView = (GridView) fragmentView.findViewById(R.id.gridView);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(this);

        final String streamName = mItems.get(0).getStreamName();
        final Button mySubscribedStreamButton = (Button) fragmentView.findViewById(R.id.mySubscribedStreamsBtnId);
        final EditText searchText = (EditText) fragmentView.findViewById(R.id.searchTxtId);
        final Button searchButton = (Button) fragmentView.findViewById(R.id.searchBtnId);
        final Button nearbySearchButton = (Button) fragmentView.findViewById(R.id.nearbyBtnId);

        //My Subscribed Streams Button Handler
        mySubscribedStreamButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Log.e(TAG, "mySubscribedStreamButton Clicked....");
                mViewMySubscribedStreamButtonClickListenerCallback.onViewMySubscribedStreamButtonClick();
            }
        });

        //Search Button Click Handler
        searchButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Log.e(TAG, "SEARCH TERM ENTERED IS: " + searchText.getText());
                mOnSearchStreamButtonClickListenerCallback.onSearchStreamButtonClick(searchText.getText().toString());
            }
        });

        //Nearby Search Button Click Handler
        nearbySearchButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Log.e(TAG, "NEARBY SEARCH BUTTON CLICKED");
                mOnNearbyStreamButtonClickListenerCallback.onNearbyStreamButtonClick();
            }
        });

        return fragmentView;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // retrieve the GridView item
        ViewAllStreamModel item = mItems.get(position);

        Log.e(TAG, "Inside the onItemClick Method of ViewAllStreamsFragment - START");
        Log.e(TAG, "Clicked Stream Name is:" +  item.getStreamName());

        //Display message to the User
        Toast.makeText(getActivity(), "Opening Stream: " + item.getStreamName(), Toast.LENGTH_SHORT).show();

        //Invoke the Callback method to the Main Activity here in order to display the View Single Stream Page
        String streamId = item.getStreamName().replace(" ", "").trim();
        Log.e(TAG, "Converting Stream Name {" + item.getStreamName() + "} to Stream Id: {" + streamId + "}");
        mViewMyImageStreamClickListenerCallback.onViewMyImageStreamButtonClick(streamId);
    }

    private List<ViewAllStreamModel> parseInputJson(){

        Log.e(TAG, "Inside the method parseInputJson...");

        Bundle bundle = getArguments();
       String rawResponseData = bundle.getString("myStreamsResponse");
        Log.e(TAG, "Raw Response Data is: " + rawResponseData);

        //Parse the JSON data to retrieve the information needed for the
        JsonElement jsonElement = new JsonParser().parse(rawResponseData);
        JsonObject jsonObject  = jsonElement.getAsJsonObject();
        JsonArray jsonArray  = jsonObject.get("owned_streams").getAsJsonArray();
        Log.e(TAG, "Size of JSON Array retrieved is: " + jsonArray.size());

        List<ViewAllStreamModel> listOfMyViewAllStreamModel = new ArrayList<ViewAllStreamModel>();
        for(JsonElement json : jsonArray){

            ViewAllStreamModel viewMyAllStreamModel = new ViewAllStreamModel();
            viewMyAllStreamModel.setStreamId(json.getAsJsonObject().get("streamId").getAsString());
            viewMyAllStreamModel.setStreamName(json.getAsJsonObject().get("streamName").getAsString());
            viewMyAllStreamModel.setCoverImgUrl(json.getAsJsonObject().get("coverImgUrl").getAsString());
            listOfMyViewAllStreamModel.add(viewMyAllStreamModel);
            Log.e(TAG, "parseInputJson Stream Name is: " +  json.getAsJsonObject().get("streamName").getAsString());
        }

        return listOfMyViewAllStreamModel;
    }


    //Add Callback Handlers
    public interface OnViewMyImageStreamClickListener{
        public void onViewMyImageStreamButtonClick(String streamId);
    }

    public interface OnViewMySubscribedStreamButtonClickListener{
        public void onViewMySubscribedStreamButtonClick();
    }

    public interface OnSearchButtonClickListener{
        public void onSearchStreamButtonClick(String searchTerm);
    }

    public interface OnNearbyButtonClickListener{
        public void onNearbyStreamButtonClick();
    }
}