package com.example.andrestorresb.ccar;

import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by andrestorres on 5/2/16.
 */
public class GetDirections extends AsyncTask<Map<String,String>, Object,ArrayList> {
    public static final String USER_CURRENT_LAT = "user_current_lat";
    public static final String USER_CURRENT_LONG = "user_current_long";
    public static final String DESTINATION_LAT = "destination_lat";
    public static final String DESTINATION_LONG = "destination_long";
    public static final String DIRECTIONS_MODE = "directions_mode";
    private MainActivity activity;
    private Exception exception;

    public GetDirections(MainActivity activity){
        super();
        this.activity=activity;
    }

    @Override
    protected ArrayList doInBackground(Map<String, String>... params) {
        Map<String, String> paramMap = params[0];
        try {
            LatLng fromPosition = new LatLng(Double.valueOf(paramMap.get(USER_CURRENT_LAT)) , Double.valueOf(paramMap.get(USER_CURRENT_LONG)));
            LatLng toPosition = new LatLng(Double.valueOf(paramMap.get(DESTINATION_LAT)) , Double.valueOf(paramMap.get(DESTINATION_LONG)));
            GMapRuta md = new GMapRuta();
            Document doc = md.getDocument(fromPosition, toPosition, paramMap.get(DIRECTIONS_MODE));
            ArrayList<LatLng> directionPoints = md.getDirection(doc);
            return directionPoints;
        }
        catch (Exception e) {
            exception = e;
            return null;
        }
    }

    @Override
    public void onPostExecute(ArrayList result) {
        if (exception == null) {
            activity.handleGetDirectionsResult(result);
        }
        else {
            Toast.makeText(activity, "Error obteniendo los datos",Toast.LENGTH_SHORT).show();

        }
    }



}
