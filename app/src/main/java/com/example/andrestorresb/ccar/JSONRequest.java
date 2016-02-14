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
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JSONRequest extends AsyncTask<String, Void, JSONObject> {

    private Activity activity;
    private JSONListener listener;

    public JSONRequest(Activity activity, JSONListener listener){

        this.activity = activity;
        this.listener = listener;
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        HttpGet get = new HttpGet(params[0]);
        StringBuilder sb = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        JSONObject theArray = null;

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

            theArray = new JSONObject(sb.toString());

        }catch (Exception e) {

            e.printStackTrace();
        }
        return theArray;
    }


    @Override
    protected void onPostExecute(JSONObject jsonArray) {

        if(jsonArray != null) {
            //Successfully read JSON
            listener.doSomething(jsonArray);

            return;
        }

        //Error reading JSON

    }

    public interface JSONListener {

        void doSomething(JSONObject array);
    }
}
