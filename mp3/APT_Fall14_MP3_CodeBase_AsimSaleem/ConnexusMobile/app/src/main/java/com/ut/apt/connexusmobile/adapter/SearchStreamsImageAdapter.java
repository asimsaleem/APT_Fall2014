package com.ut.apt.connexusmobile.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ut.apt.connexusmobile.R;
import com.ut.apt.connexusmobile.model.SearchStreamModel;
import com.ut.apt.connexusmobile.model.ViewSingleStreamModel;

import java.io.InputStream;
import java.util.List;

/**
 * Created by asim on 10/22/14.
 */


public class SearchStreamsImageAdapter extends BaseAdapter {

    private String TAG  = "SearchStreamsImageAdapter";

    private Context mContext;
    private List<SearchStreamModel> mListOfSearchStreamModel;

    public SearchStreamsImageAdapter(Context context, List<SearchStreamModel> searchStreamModelList) {
        mContext = context;
        mListOfSearchStreamModel = searchStreamModelList;
    }


    @Override
    public int getCount() {
        return mListOfSearchStreamModel.size();
    }

    @Override
    public Object getItem(int position) {
        return mListOfSearchStreamModel.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Log.e(TAG, "Inside the getView Method");

        ViewHolder viewHolder;
        if(convertView == null) {

            Log.e(TAG, "Inflate the GRID VIEW Item that was created newly");

            //Inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.gridview_item, parent, false);

            //Initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);

            convertView.setTag(viewHolder);
        } else {
            //Recycle the already inflated view
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //Update the item view
        SearchStreamModel item = mListOfSearchStreamModel.get(position);
        viewHolder.ivIcon.setImageBitmap(doInBackground(item.getCoverImgUrl()));
        Log.e(TAG, "Stream Name being Set is: "  + item.getStreamName());
        viewHolder.tvTitle.setText(item.getStreamName());

        return convertView;
    }

    private Bitmap doInBackground(String... urls) {

        Log.e(TAG,"Retrieving Image from the URL in the background now....");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String urldisplay = urls[0];
        Bitmap imgIcon = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            //Set the Image Icon
            imgIcon = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imgIcon;
    }


    private static class ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
    }
}