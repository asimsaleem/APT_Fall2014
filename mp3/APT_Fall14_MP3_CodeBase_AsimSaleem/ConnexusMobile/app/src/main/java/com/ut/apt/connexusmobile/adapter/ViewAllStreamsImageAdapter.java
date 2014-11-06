package com.ut.apt.connexusmobile.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.StrictMode;
import java.io.InputStream;
import java.util.List;

import com.ut.apt.connexusmobile.R;
import com.ut.apt.connexusmobile.model.ViewAllStreamModel;

public class ViewAllStreamsImageAdapter extends BaseAdapter{

    private String TAG  = "ViewAllStreamsImageAdapter";

    private Context mContext;
    private List<ViewAllStreamModel> mListOfViewAllStreamModel;

	public ViewAllStreamsImageAdapter(Context context, List<ViewAllStreamModel> listOfViewAllStreamModel) {
		mContext = context;
        mListOfViewAllStreamModel = listOfViewAllStreamModel;
	}

	@Override
	public int getCount() {
        return mListOfViewAllStreamModel.size();
	}

	@Override
	public Object getItem(int position) {
		return mListOfViewAllStreamModel.get(position);
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
        ViewAllStreamModel item = mListOfViewAllStreamModel.get(position);
        viewHolder.ivIcon.setImageBitmap(doInBackground(item.getCoverImgUrl()));
        Log.e(TAG, "Stream Name being Set is: "  + item.getStreamName());
        viewHolder.tvTitle.setText(item.getStreamName());

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
            //Set the Image to be displayed
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