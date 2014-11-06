package com.ut.apt.connexusmobile.model;

import android.view.View;

import java.util.List;

/**
 * Created by asim on 10/18/14.
 */
public class ViewAllStreamModel {


    private List<String> subscriberList;
    private int viewCount;
    private String coverImgUrl;
    private String pictureUpdateDate;
    private String streamName;
    private String streamAccessDate;
    private String inviteMsg;
    private String streamId;
    private String owner;
    private List<String> tagList;
    private int pictureCount;

    public ViewAllStreamModel(String coverImgUrl, String streamName){
       this.coverImgUrl = coverImgUrl;
       this.streamName = streamName;
    }

    public ViewAllStreamModel(){
    }

    public List<String> getSubscriberList() {
        return subscriberList;
    }

    public void setSubscriberList(List<String> subscriberList) {
        this.subscriberList = subscriberList;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public String getCoverImgUrl() {
        return coverImgUrl;
    }

    public void setCoverImgUrl(String coverImgUrl) {
        this.coverImgUrl = coverImgUrl;
    }

    public String getPictureUpdateDate() {
        return pictureUpdateDate;
    }

    public void setPictureUpdateDate(String pictureUpdateDate) {
        this.pictureUpdateDate = pictureUpdateDate;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public String getStreamAccessDate() {
        return streamAccessDate;
    }

    public void setStreamAccessDate(String streamAccessDate) {
        this.streamAccessDate = streamAccessDate;
    }

    public String getInviteMsg() {
        return inviteMsg;
    }

    public void setInviteMsg(String inviteMsg) {
        this.inviteMsg = inviteMsg;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<String> getTagList() {
        return tagList;
    }

    public void setTagList(List<String> tagList) {
        this.tagList = tagList;
    }

    public int getPictureCount() {
        return pictureCount;
    }

    public void setPictureCount(int pictureCount) {
        this.pictureCount = pictureCount;
    }


}
