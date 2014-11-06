package com.ut.apt.connexusmobile.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ut.apt.connexusmobile.R;
import com.ut.apt.connexusmobile.adapter.SearchStreamsImageAdapter;
import com.ut.apt.connexusmobile.adapter.ViewAllStreamsImageAdapter;
import com.ut.apt.connexusmobile.model.SearchStreamModel;
import com.ut.apt.connexusmobile.model.ViewAllStreamModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asim on 10/20/14.
 */


public class SearchResultsStreamFragment extends Fragment implements AdapterView.OnItemClickListener{

    private String TAG  = "SearchResultsStreamFragment";

    OnImageStreamClickListener mOnImageStreamClickListenerCallback;
    OnSearchButtonClickListener mOnSearchStreamButtonClickListenerCallback;
    OnMoreSearchResultsClickListener mOnMoreSearchResultsClickListenerCallBack;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mOnImageStreamClickListenerCallback = (OnImageStreamClickListener) activity;
            mOnMoreSearchResultsClickListenerCallBack = (OnMoreSearchResultsClickListener) activity;
            mOnSearchStreamButtonClickListenerCallback = (OnSearchButtonClickListener) activity;
         } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnImageStreamClickListener/OnMoreSearchResultsClickListener/OnSearchButtonClickListener");
        }
    }


    private List<SearchStreamModel> mItems;
    private SearchStreamsImageAdapter mAdapter;
    private String mSearchTerm;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.e(TAG, "Inside onCreate Method of SearchResultsStreamFragment - START");

        //Initialize the Search Term here
        Bundle bundle = getArguments();
        if(bundle.getString("searchTerm") != null)
            mSearchTerm = bundle.getString("searchTerm");
        else
            mSearchTerm = "";

        // initialize the items list
        mItems = new ArrayList<SearchStreamModel>();
        Log.e(TAG, "Setting up the Input for the SearchStreamModel Object by Parsing the Json first...");
        List<SearchStreamModel> searchStreamModelList = parseInputJson();
        for(SearchStreamModel searchStreamModel : searchStreamModelList){
            mItems.add(new SearchStreamModel(searchStreamModel.getStreamId(), searchStreamModel.getStreamName(),searchStreamModel.getCoverImgUrl()));
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        Log.e(TAG, "Inside the onCreateView for View Stream Fragment");
        final View fragmentView  = inflater.inflate(R.layout.view_search_results_layout_fragment, container, false);

        //Initialize the adapter
        mAdapter = new SearchStreamsImageAdapter(getActivity(), mItems);

        //Initialize the GridView
        GridView gridView = (GridView) fragmentView.findViewById(R.id.gridView);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(this);

        final EditText searchText = (EditText) fragmentView.findViewById(R.id.searchTxtId);
        final Button searchButton = (Button) fragmentView.findViewById(R.id.searchBtnId);
        final Button moreSearchResultsButton = (Button) fragmentView.findViewById(R.id.moreSrchResultsBtnId);
        final TextView searchResultsText = (TextView) fragmentView.findViewById(R.id.srchResultsTxtId);

        searchResultsText.setText(mItems.size() + " results for the search term \"" + mSearchTerm +  "\". Click on an image to view stream");

        //Search Button Click Handler
        searchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.e(TAG, "SEARCH TERM ENTERED IS: " + searchText.getText());
                mOnSearchStreamButtonClickListenerCallback.onSearchStreamButtonClick(searchText.getText().toString());
            }
        });

        //More Search Results Button Click Handler
        moreSearchResultsButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Log.e(TAG, "MORE SEARCH RESULTS BUTTON CLICKED");
                mOnMoreSearchResultsClickListenerCallBack.onMoreSearchResultsButtonClick();
            }
        });

        return fragmentView;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        //Retrieve the GridView item
        SearchStreamModel item = mItems.get(position);

        Log.e(TAG, "Inside the onItemClick Method of SearchResultsFragment - START");
        Log.e(TAG, "Clicked Stream Name is:" +  item.getStreamName());

        //Display Message to the User
        Toast.makeText(getActivity(), "Opening Stream: " + item.getStreamName(), Toast.LENGTH_SHORT).show();

        //Invoke the Callback method to the Main Activity here in order to display the View Single Stream Page
        String streamId = item.getStreamName().replace(" ", "").trim();
        Log.e(TAG, "Converting Stream Name {" + item.getStreamName() + "} to Stream Id: {" + streamId + "}");
        mOnImageStreamClickListenerCallback.onImageStreamButtonClick(streamId);
    }



    private List<SearchStreamModel> parseInputJson(){

        Log.e(TAG, "Inside the method parseInputJson...");

        Bundle bundle = getArguments();
        String rawResponseData = bundle.getString("searchResultsStreamResponse");
        Log.e(TAG, "Raw Response Data is: " + rawResponseData);

        //Parse the JSON data to retrieve the information needed for the
        JsonElement jsonElement = new JsonParser().parse(rawResponseData);
        JsonObject jsonObject  = jsonElement.getAsJsonObject();
        JsonArray jsonArray  = jsonObject.get("searchedStreams").getAsJsonArray();
        Log.e(TAG, "Size of JSON Array retrieved is: " + jsonArray.size());

        List<SearchStreamModel> listOfSearchStreamModel = new ArrayList<SearchStreamModel>();
        for(JsonElement json : jsonArray){

            SearchStreamModel searchStreamModel = new SearchStreamModel();
            searchStreamModel.setStreamId(json.getAsJsonObject().get("streamId").getAsString());
            searchStreamModel.setStreamName(json.getAsJsonObject().get("streamName").getAsString());
            searchStreamModel.setCoverImgUrl(json.getAsJsonObject().get("coverImgUrl").getAsString());
            listOfSearchStreamModel.add(searchStreamModel);
            Log.e(TAG, "parseInputJson Stream Name is: " +  json.getAsJsonObject().get("streamName").getAsString());
        }

       return listOfSearchStreamModel;
    }


    //Add Callback Handlers
    public interface OnImageStreamClickListener{
        public void onImageStreamButtonClick(String streamId);
    }

    public interface OnMoreSearchResultsClickListener{
        public void onMoreSearchResultsButtonClick();
    }

    public interface OnSearchButtonClickListener{
        public void onSearchStreamButtonClick(String searchTerm);
    }
}