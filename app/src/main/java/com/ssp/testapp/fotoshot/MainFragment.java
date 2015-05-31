package com.ssp.testapp.fotoshot;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MainFragment extends Fragment implements View.OnClickListener {

    private Button button;
    private Button buttonFav;
    private TextView textOnPic;

    private ArrayList<Image> images;

    // for preferences
    private boolean enableAutoswitch;
    private String advOrder;
    private String advFavourites;
    private String advAnimation;
    private int intervalAutoswitch;

    // for gestures
    private static final int SWIPE_MIN_DISTANCE = 10;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;

    // flags, drugs and so on..
    private boolean sorted = false;
    int currentButton;
    private int orderNum;
    private Handler handler;
    private int handlerFlag;
    private boolean swipeLeft = true;
    // for slides
    private ImageView imageView;

    public MainFragment() {
    }

    @Override
    public void onStart() {
        getPrefs();
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        // "Start"/"Stop" button
        button = (Button) rootView.findViewById(R.id.checkbutton);
        button.setText(getActivity().getString(R.string.start));

        // "Favourites" button
        buttonFav = (Button) rootView.findViewById(R.id.favouritebutton);
        buttonFav.setVisibility(View.GONE);

        // textView on pictures for favourites
        textOnPic = (TextView) rootView.findViewById(R.id.textImage);
        // Slideshow imageview
        imageView = (ImageView) rootView.findViewById(R.id.imageView);
        try {
            images = parseJSON();
        } catch (Exception e) {
            Log.e("VASSA", "Error loading crimes: ", e);
        }

        // handler for looping
        handler = new Handler();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < images.size(); i++) {
                    // preloading images to cache
                    Picasso.with(getActivity()).load(images.get(i).getUrl()).fetch();
                }
                //enable autoswitch
                if (enableAutoswitch) {
                    if (handlerFlag == 1) {
                        button.setText(getActivity().getString(R.string.start));
                        handlerFlag = 0;
                        handler.removeCallbacks(runnable);
                    } else {
                        handlerFlag = 1;
                        button.setText(getActivity().getString(R.string.stop));
                        handler.post(runnable);
                    }
                } else {
                    swipeImage();
                }
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

    // task for autoswitch
    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            swipeImage();
            handler.postDelayed(runnable, intervalAutoswitch * 1000);
        }
    };

    /* method for downloading and setting pics to imageview with
     custom animations */
    private void swipeImage() {
        if (advOrder.equals("1")) {
            Random rand = new Random();
            orderNum = rand.nextInt(images.size());  // random picture
        } else {
            sortArray();   // sort by index!
        }
        Toast.makeText(getActivity(), Integer.toString(orderNum), Toast.LENGTH_SHORT).show();
        Picasso.with(getActivity()).load(images.get(orderNum).getUrl()).resize(imageView.getWidth(), imageView.getWidth())
                .centerCrop().into(imageView);

        buttonFav.setVisibility(View.VISIBLE);

        int animNumber;
        Techniques techs;
        //gettin' random animation via daimajia/AndroidViewAnimations library
        if (advAnimation.equals("8")) {
            Random rand = new Random();
            // 7 - number of animations
            animNumber = rand.nextInt(images.size()) % 7;
        } else {
            animNumber = Integer.parseInt(advAnimation);
        }
        switch (animNumber) {
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

        //
        currentButton = orderNum;
//        if(images.get(orderNum).isFavourite()){
//            textOnPic.setVisibility(View.VISIBLE);
//            textOnPic.setText(images.get(orderNum).getComment());
//            buttonFav.setText(getActivity().getString(R.string.del_fav));
//            buttonFav.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    images.get(currentButton).setFavourite(false);
//                }
//            });
//        } else{
//            textOnPic.setVisibility(View.INVISIBLE);
//            buttonFav.setText(getActivity().getString(R.string.add_fav));
//            buttonFav.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    images.get(currentButton).setFavourite(true);
//                    setFavoriteText(currentButton);
//                }
//            });
//        }
        refreshWhenFavourites(currentButton);


        // for ordered array
        if (advOrder.equals("0")) {
            int direction;
            if (swipeLeft) {
                direction = 1;
            } else {
                if (orderNum == 0) {
                    orderNum = images.size();
                }
                direction = -1;
            }
            orderNum = (orderNum + direction) % images.size(); // needs for swipe in both directions
        }
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
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                    Toast.makeText(getActivity(), "Left Swipe", Toast.LENGTH_SHORT).show();
                    swipeLeft = true;
                    swipeImage();
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                    Toast.makeText(getActivity(), "Right Swipe", Toast.LENGTH_SHORT).show();
                    swipeLeft = false;
                    swipeImage();
                }
            } catch (Exception e) {
            }
            return false;
        }
    }

    private void sortArray() {
        if (!sorted) {
            ImageComparator comparator = new ImageComparator();
            Collections.sort(images, comparator);
            sorted = true;
        }
    }

    private void setFavoriteText(final int item) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        final EditText edittext = new EditText(getActivity());
        if (images.get(item).getComment() != null) {
            edittext.setText(images.get(item).getComment());
        }
        alert.setMessage(getActivity().getString(R.string.add_text));
        alert.setTitle(getActivity().getString(R.string.add_fav));

        alert.setView(edittext);

        alert.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String text = edittext.getText().toString();
                images.get(item).setComment(text);
                refreshWhenFavourites(currentButton);
            }
        });

        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
    }

    private void refreshWhenFavourites(final int currentButton) {
        if (images.get(currentButton).isFavourite()) {
            textOnPic.setVisibility(View.VISIBLE);
            textOnPic.setText(images.get(currentButton).getComment());
            buttonFav.setText(getActivity().getString(R.string.del_fav));
            buttonFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    images.get(currentButton).setFavourite(false);
                    refreshWhenFavourites(currentButton);
                }
            });
        } else {
            textOnPic.setVisibility(View.INVISIBLE);
            buttonFav.setText(getActivity().getString(R.string.add_fav));
            buttonFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    images.get(currentButton).setFavourite(true);
                    setFavoriteText(currentButton);
                }
            });
        }
    }

    private void getPrefs() {
        // Get the xml/preferences.xml preferences
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity().getBaseContext());
        enableAutoswitch = prefs.getBoolean("autoswitch_checkbox", true);
        intervalAutoswitch = prefs.getInt("autoswitch_interval", 5);
        advOrder = prefs.getString("adv_order", "0");
        advFavourites = prefs.getString("adv_favourites", "0");
        advAnimation = prefs.getString("adv_animation", "0");
    }

    // parsing json to Image objects
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

    @Override
    public void onPause() {
        super.onPause();
        try {
            saveImagesJSON(images);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* in the task on test app were said about json located in resources of .apk (which only readble),
    so it's non writable, but it is necessary feature so I write here method to save changes
    which called in onPause() method..
     */
    public void saveImagesJSON(ArrayList<Image> images) throws JSONException, IOException {

        JSONArray array = new JSONArray();
        for (Image i : images)
            array.put(i.toJSON());

        Writer writer = null;
        try {
            OutputStream out = getActivity()
                    .openFileOutput("images.json", Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(out);
            writer.write(array.toString());
        } finally {
            if (writer != null)
                writer.close();
        }
    }

}
