package com.ut.apt.connexusmobile.model;

/**
 * Created by asim on 10/22/14.
 */
public class SearchStreamModel {

    private String coverImgUrl;
    private String streamName;
    private String streamId;

    public SearchStreamModel(){}

    public SearchStreamModel(String streamId, String streamName, String coverImgUrl){
        this.streamId = streamId;
        this.streamName = streamName;
        this.coverImgUrl = coverImgUrl;
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
