package com.ut.apt.connexusmobile.model;

import android.graphics.drawable.Drawable;

/**
 * Created by asim on 10/21/14.
 */
public class GridViewItemModel {

    public final Drawable icon;       // the drawable for the ListView item ImageView
    public final String title;        // the text for the GridView item title
    public final String coverImgUrl;  // the url location for the Image

    public GridViewItemModel(Drawable icon, String title, String coverImgUrl) {
        this.icon = icon;
        this.title = title;
        this.coverImgUrl = coverImgUrl;
    }
}



