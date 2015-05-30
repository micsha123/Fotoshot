package com.ssp.testapp.fotoshot;

import android.app.Fragment;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class MainFragment extends Fragment {

    private ArrayList<Image> images;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        Button button = (Button) rootView.findViewById(R.id.checkbutton);
        try {
            images = parseJSON();
        } catch (Exception e) {
            images = new ArrayList<Image>();
            Log.e("VASSA", "Error loading crimes: ", e);
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < images.size(); i++) {
                }
            }
        });
        return rootView;
    }

    private ArrayList<Image> parseJSON() throws IOException, JSONException {
        ArrayList<Image> images = new ArrayList<Image>();
        BufferedReader reader = null;
        try {
            AssetManager manager = getActivity().getAssets();
            InputStream in = manager.open("images.json");
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder jsonString = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            Log.d("VASSA", jsonString.toString());
            JSONArray array = new JSONObject(jsonString.toString()).getJSONArray("images");
            for (int i = 0; i < array.length(); i++) {
                images.add(new Image(array.getJSONObject(i)));
            }
        } catch (FileNotFoundException e) {
        } finally {
            if (reader != null)
                reader.close();
        }
        return images;
    }
}
