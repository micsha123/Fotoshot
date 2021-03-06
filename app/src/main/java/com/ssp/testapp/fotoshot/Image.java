package com.ssp.testapp.fotoshot;

// class for image objects

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;

public class Image {

    private static final String JSON_ID = "id";
    private static final String JSON_INDEX = "index";
    private static final String JSON_URL = "url";
    private static final String JSON_FAV = "fav";
    private static final String JSON_COMMENT = "comment";

    private int id;
    private int index;
    private String url;
    private boolean favourite;
    private String comment;

    public Image(int id, int index, String url) {
        this.id = id;
        this.index = index;
        this.url = url;
    }

    public Image(JSONObject json) throws JSONException {
        id = Integer.parseInt(json.getString(JSON_ID));
        index = Integer.parseInt(json.getString(JSON_INDEX));
        url = json.getString(JSON_URL);
        favourite = Boolean.parseBoolean(json.getString(JSON_FAV));
        comment = json.getString(JSON_COMMENT);
    }

    public String getUrl() {
        return url;
    }

    public int getIndex() {
        return index;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_ID, Integer.toString(id));
        json.put(JSON_INDEX, Integer.toString(index));
        json.put(JSON_URL, url);
        json.put(JSON_FAV, Boolean.toString(favourite));
        json.put(JSON_COMMENT, comment);
        return json;
    }
}

class ImageComparator implements Comparator<Image> {

    @Override
    public int compare(Image o1, Image o2) {
        if (o1.getIndex() > o2.getIndex()) {
            return 1;
        }
        if (o1.getIndex() < o2.getIndex()) {
            return -1;
        }
        return 0;
    }
}