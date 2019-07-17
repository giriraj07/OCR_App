package com.example.ocr;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.ocr.MainActivity.stringBuilder;

public class Activity2 extends AppCompatActivity {   // second activity on clicking the search button
   private ProgressBar mProgressBar;
   private Handler mHandler=new Handler();
   private int mProgessStatus;
   private String whole = stringBuilder.toString();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);
        ListView listView = findViewById(R.id.listView);
        mProgressBar = findViewById(R.id.pb);
        showProgressForSomeTime();                    // Setting up the progress bar

        final ArrayList<Pair<String, String>> list = new ArrayList<>();
        if (whole.length() == 0) {
            Toast.makeText(this, "No Text Captured", Toast.LENGTH_SHORT).show();
            return;
        }
        String server_url = "https://www.googleapis.com/customsearch/v1?q=" + whole +
                "&cx=012842549497044129491%3Aewhicys_ogc&key=AIzaSyDtFbpeg5Qyrd1B7eq6og49xASR-yY6eC0";
        // this url is used for making any site's searhes to json format

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {   // on clicking item
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                Object clickItemObj = adapterView.getAdapter().getItem(index);
                Toast.makeText(Activity2.this, "You clicked " + clickItemObj.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
        final ArrayList<Map<String, String>> List = new ArrayList<>();

        final SimpleAdapter simpleAdapter = new SimpleAdapter(this, List, R.layout.item_row,  // declaring the adapter
                new String[]{"title", "link"}, new int[]{R.id.tv1, R.id.tv2});
        listView.setAdapter(simpleAdapter);
        setupTheNetWorking(List,server_url, simpleAdapter);
    }

    public void setupTheNetWorking(final ArrayList<Map<String, String>> List, String server_url,
                                   final SimpleAdapter simpleAdapter){
        final RequestQueue requestQueue = Volley.newRequestQueue(Activity2.this); // Using Volley library for networking
        JsonObjectRequest request = new JsonObjectRequest(Request.
                Method.GET, server_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONArray ja = null;
                try {
                    ja = response.getJSONArray("items");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(ja==null)
                {
                    try {
                        JSONObject jo=response.getJSONObject("spelling");
                        HashMap<String,String> hm=new HashMap<>();
                        hm.put("title",jo.getString("correctedQuery"));

                        String url= "https://?q="+ URLEncoder.encode(whole, "UTF-8");
                        hm.put("link",url);
                        List.add(hm);
                        simpleAdapter.notifyDataSetChanged();
                        requestQueue.stop();
                    }
                    catch (JSONException e) {
                        Toast.makeText(Activity2.this,"Oops! Something Went Wrong!",Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                if(ja!=null) {
                    for (int i = 0; i < ja.length(); i++) {
                        Map<String, String> listItemMap = new HashMap<String, String>();
                        try {
                            String ob1 = ja.getJSONObject(i).getString("title");
                            listItemMap.put("title", ob1);
                            Log.d("TAG", ob1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            String ob2 = ja.getJSONObject(i).getString("link");
                            listItemMap.put("link", ob2);
                            Log.d("TAG", ob2);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        List.add(listItemMap);
                        simpleAdapter.notifyDataSetChanged();
                        requestQueue.stop();
                    }
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {  // when network error occurs
                if (error instanceof NetworkError) {
                    Toast.makeText(getApplicationContext(), "No network available", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
        requestQueue.add(request);
        requestQueue.start();
    }

    private void showProgressForSomeTime() {  // module for waiting of showprogress

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(mProgessStatus<100)
                {
                    mProgessStatus++;
                    android.os.SystemClock.sleep(20);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                             mProgressBar.setProgress(mProgessStatus);
                        }
                    });
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                       findViewById(R.id.listView).setVisibility(View.VISIBLE);
                    }
                });

            }
        }).start();
    }
}