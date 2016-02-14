package com.example.andrestorresb.ccar;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;



// AsyncTask <params, progress, result>
// asynchronous task - a task that runs concurrently without stopping the main thread
// normally used for streams - retrieve from web, read a big file, etc

//
public class JSONRequest extends AsyncTask<String, Void, JSONArray> {

    private Activity activity;
    private JSONListener listener;

    public JSONRequest(Activity activity, JSONListener listener){

        this.activity = activity;
        this.listener = listener;
    }

    // doInBackground("a", "b", "c")
    // doInBackground("a", "b")

    @Override
    protected JSONArray doInBackground(String... params) {

        HttpGet get = new HttpGet(params[0]);
        StringBuilder sb = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        JSONArray theArray = null;

        try {

            HttpResponse response = client.execute(get);
            StatusLine sl = response.getStatusLine();
            int code = sl.getStatusCode();

            if(code == 200){


                // object that contains data from response
                HttpEntity entity = response.getEntity();

                InputStream is = entity.getContent();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));

                String currentLine;
                while((currentLine = br.readLine()) != null){

                    sb.append(currentLine);
                    Log.i("JSON", "reading...");
                }
            }

            theArray = new JSONArray(sb.toString());

        }catch (Exception e) {

            e.printStackTrace();
        }
        return theArray;
    }


    @Override
    protected void onPostExecute(JSONArray jsonArray) {

        if(jsonArray != null) {
            Toast.makeText(activity, "JSON READ succesfully", Toast.LENGTH_SHORT).show();
            listener.doSomething(jsonArray);
        } else
            Toast.makeText(activity, "FAILURE ='(", Toast.LENGTH_SHORT).show();
    }

    public interface JSONListener {

        void doSomething(JSONArray array);
    }
}
