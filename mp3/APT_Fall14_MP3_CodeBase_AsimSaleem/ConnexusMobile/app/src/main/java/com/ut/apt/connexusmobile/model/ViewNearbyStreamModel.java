package com.ut.apt.connexusmobile.model;

/**
 * Created by asim on 10/22/14.
 */
public class ViewNearbyStreamModel {

    private String coverImgUrl;
    private String streamName;
    private String streamId;
    private String streamDistance;


    public ViewNearbyStreamModel(){}

    public String getStreamDistance() {
        return streamDistance;
    }

    public void setStreamDistance(String streamDistance) {
        this.streamDistance = streamDistance;
    }

    public ViewNearbyStreamModel(String streamId, String streamName, String coverImgUrl, String streamDistance){
        this.streamId = streamId;
        this.streamName = streamName;
        this.coverImgUrl = coverImgUrl;
        this.streamDistance = streamDistance;
    }

    public String getCoverImgUrl() {
        return coverImgUrl;
    }

    public void setCoverImgUrl(String coverImgUrl) {
        this.coverImgUrl = coverImgUrl;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

}