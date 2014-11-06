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
import com.ut.apt.connexusmobile.model.ViewAllStreamModel;
import com.ut.apt.connexusmobile.model.ViewSingleStreamModel;

import java.io.InputStream;
import java.util.List;

public class ViewSingleStreamImageAdapter extends BaseAdapter{

    private String TAG  = "ViewSingleStreamImageAdapter";

    private Context mContext;
    private List<ViewSingleStreamModel> mListOfViewSingleStreamModel;

	public ViewSingleStreamImageAdapter(Context context, List<ViewSingleStreamModel> viewSingleStreamModelList) {
		mContext = context;
        mListOfViewSingleStreamModel = viewSingleStreamModelList;
	}

	@Override
	public int getCount() {
        return mListOfViewSingleStreamModel.size();
	}

	@Override
	public Object getItem(int position) {
		return mListOfViewSingleStreamModel.get(position);
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

            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.gridview_pictures, parent, false);

            // initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.picIcon = (ImageView) convertView.findViewById(R.id.picIcon);
            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // update the item view
        ViewSingleStreamModel item = mListOfViewSingleStreamModel.get(position);
        viewHolder.picIcon.setImageBitmap(doInBackground(item.getUploadedImgsUrl()));

        return convertView;
    }

    private Bitmap doInBackground(String... urls) {

        Log.e(TAG,"Retrieving IMAGE FROM URL in the background now....");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String urldisplay = urls[0];
        Bitmap imgIcon = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            imgIcon = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imgIcon;
    }


    private static class ViewHolder {
        ImageView picIcon;
        TextView tvTitle;
    }
}