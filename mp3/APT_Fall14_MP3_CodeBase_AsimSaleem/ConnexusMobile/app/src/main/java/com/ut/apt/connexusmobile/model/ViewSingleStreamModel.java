package com.ut.apt.connexusmobile.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asim on 10/21/14.
 */
public class ViewSingleStreamModel {

    private String uploadedImgsUrl;
    private String streamName;
    private String streamId;
    private String cursorUrl;

    public String getCursorUrl() {
        return cursorUrl;
    }

    public void setCursorUrl(String cursorUrl) {
        this.cursorUrl = cursorUrl;
    }

    public ViewSingleStreamModel(){

    }

    public ViewSingleStreamModel(String streamId, String streamName, String uploadedImgsUrl, String cursorUrl){
        this.streamId = streamId;
        this.streamName = streamName;
        this.uploadedImgsUrl = uploadedImgsUrl;
        this.cursorUrl = cursorUrl;
    }

    public String getUploadedImgsUrl() {
        return uploadedImgsUrl;
    }

    public void setUploadedImgsUrl(String uploadedImgsUrl) {
        this.uploadedImgsUrl = uploadedImgsUrl;
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