package com.along.complicated_jsonurl;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    String urlJson = "https://fetch-hiring.s3.amazonaws.com/hiring.json";

    //this class will sort List first, then name
    public static JSONArray getSortedList(JSONArray array) throws JSONException {
        List<JSONObject> list = new ArrayList<JSONObject>();
        for (int i = 0; i < array.length(); i++) {
            list.add(array.getJSONObject(i));
        }
        Collections.sort(list, new sortListId());
        Collections.sort(list, new sortNameAfterListId());

        JSONArray resultArray = new JSONArray(list);

        return resultArray;
    }

    //this class will sort the ListId in ascending order
    public static class sortListId implements Comparator<JSONObject> {

        @Override
        public int compare(JSONObject lhs, JSONObject rhs) {
            try {
                return lhs.getInt("listId") - rhs.getInt("listId");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

    //this class will sort the order of name in ascending order without altering ListId
    public static class sortNameAfterListId implements Comparator<JSONObject> {

        @Override
        public int compare(JSONObject lhs, JSONObject rhs) {
            try {
                if (lhs.getInt("listId") == rhs.getInt("listId")){
                    String valA = lhs.getString("name");
                    String valB = rhs.getString("name");
                    Integer a = Integer.parseInt(valA.replaceAll("[^0-9]", ""));
                    Integer b =Integer.parseInt(valB.replaceAll("[^0-9]", ""));

                return a-b;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return 0;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView)findViewById(R.id.txt);

        new backgroundTask().execute();
    }

    public class backgroundTask extends AsyncTask<Void, Void, String>{

        ProgressDialog pd;

        //this should pops first when the user first enter the app and trying to load the JSON URL
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(MainActivity.this);
            pd.setTitle("Wait!");
            pd.setMessage("Downloading...");
            pd.show();
        }

        //parsing JSON objects from URL
        @Override
        protected String doInBackground(Void... voids) {
            StringBuilder builder = null;
            try {
                URL url = new URL(urlJson);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                InputStreamReader reader = new InputStreamReader(con.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line = "";
                builder = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null){
                    builder.append(line);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            }
            return builder.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            StringBuilder stringBuilder = new StringBuilder();

            //filter out JSONArray that is "" or null
            try {
                JSONArray array = new JSONArray(s);
                JSONArray array2 = new JSONArray();
                for (int i = 0; i<array.length();i++){
                    JSONObject object = array.getJSONObject(i);
                    JSONObject object2 = new JSONObject();

                    String ids = object.getString("id");
                    String listIds = object.getString("listId");
                    String names = object.getString("name");

                    if (names != "null" && names.length() != 0) {
                        object2.put("id", ids);
                        object2.put("listId", listIds);
                        object2.put("name", names);

                        array2.put(object2);
                    }
                }

                //sort the order of array2 by listId first, then by name.
                JSONArray array3 = getSortedList(array2);

                //print out the JSON Array in to the main screen of the app.
                for(int j =0;j<array3.length();j++){
                    JSONObject object = array3.getJSONObject(j);
                    String ids = object.getString("id");
                    String listIds = object.getString("listId");
                    String names = object.getString("name");
                    stringBuilder.append("id: " + ids + " listId: " + listIds + " name: " + names + "\n");
                    textView.setText(stringBuilder.toString());

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}