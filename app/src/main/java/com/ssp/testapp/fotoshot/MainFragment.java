package com.ssp.testapp.fotoshot;

import android.app.Fragment;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;


public class MainFragment extends Fragment implements View.OnClickListener {

    private ArrayList<Image> images;

    private static final int SWIPE_MIN_DISTANCE = 10;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;

    ImageView imageView;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        // "Start" button
        Button button = (Button) rootView.findViewById(R.id.checkbutton);
        // Slideshow imageview
        imageView = (ImageView) rootView.findViewById(R.id.imageView);
        try {
            images = parseJSON();
        } catch (Exception e) {
            Log.e("VASSA", "Error loading crimes: ", e);
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < images.size(); i++){
//                    Picasso.with(getActivity()).load(images.get(i).getUrl());
                    // preloading images to cache
                    Picasso.with(getActivity()).load(images.get(i).getUrl()).fetch();
                }
                swipeImage();
            }
        });
        // gestures for swiping
        imageView.setOnClickListener(this);
        gestureDetector = new GestureDetector(getActivity(), new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
        imageView.setOnTouchListener(gestureListener);
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

    private void swipeImage(){
        Random rand = new Random();
        int randomNum = rand.nextInt(images.size());
        Toast.makeText(getActivity(), Integer.toString(randomNum), Toast.LENGTH_SHORT).show();
        Picasso.with(getActivity()).load(images.get(randomNum).getUrl()).resize(imageView.getWidth(), imageView.getWidth())
                .centerCrop().into(imageView);
        Techniques techs;
        randomNum = rand.nextInt(images.size());
        //gettin' random animation via daimajia/AndroidViewAnimations library
        switch(randomNum % 7){
            case 0:
                techs = Techniques.FadeIn;
                break;
            case 1:
                techs = Techniques.RollIn;
                break;
            case 2:
                techs = Techniques.StandUp;
                break;
            case 3:
                techs = Techniques.ZoomIn;
                break;
            case 4:
                techs = Techniques.BounceIn;
                break;
            case 5:
                techs = Techniques.SlideInDown;
                break;
            default:
                techs = Techniques.Tada;
        }
        YoYo.with(techs).duration(700).playOn(imageView);
    }

    @Override
    public void onClick(View v) {
    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                    Toast.makeText(getActivity(), "Left Swipe", Toast.LENGTH_SHORT).show();
                    swipeImage();
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                    Toast.makeText(getActivity(), "Right Swipe", Toast.LENGTH_SHORT).show();
                    swipeImage();
                }
            } catch (Exception e) {
            }
            return false;
        }
    }
}
