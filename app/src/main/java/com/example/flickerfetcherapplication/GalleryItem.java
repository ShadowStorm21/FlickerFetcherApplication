package com.example.flickerfetcherapplication;

import java.util.Objects;

public class GalleryItem {

    private String mID;
    private String mCaption;
    private String mUrl;

    public GalleryItem(String mID, String mCaption, String mUrl) {
        this.mID = mID;
        this.mCaption = mCaption;
        this.mUrl = mUrl;
    }

    @Override
    public String toString() {
        return mCaption;
    }

    public String getID() {
        return mID;
    }

    public void setID(String mID) {
        this.mID = mID;
    }

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String mCaption) {
        this.mCaption = mCaption;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }


}
