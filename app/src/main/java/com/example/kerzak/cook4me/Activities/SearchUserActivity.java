package com.example.kerzak.cook4me.Activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.example.kerzak.cook4me.R;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SearchUserActivity extends ListActivity  {

    private SearchTask mAuthTask = null;

    EditText searchUsersEditText;

    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    List<String> listItems=new ArrayList<String>();

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    ArrayAdapter<String> adapter;

    String myLogin;

    String json;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_search_user);
        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        setListAdapter(adapter);


        searchUsersEditText = (EditText) findViewById(R.id.searchUsersEditText);
        searchUsersEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mAuthTask = new SearchTask(s.toString());
                mAuthTask.execute();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ListView listView = getListView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent myIntent = new Intent(SearchUserActivity.this,RankingActivity.class);
                String name = (String)parent.getItemAtPosition(position);
                myIntent.putExtra("name", name);
                if (json != null) {
                    myIntent.putExtra("json", json);
                }
                SearchUserActivity.this.startActivity(myIntent);
            }
        });
        json = getIntent().getStringExtra("json");

    }

    @Override
    public void onBackPressed() {
        Intent myIntent = new Intent(SearchUserActivity.this,MapsActivity.class);
        if (json != null) {
            myIntent.putExtra("json", json);
        }
        SearchUserActivity.this.startActivity(myIntent);
    }


//    private void setUpBackButton() {
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.backToMenuFButton);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent myIntent = new Intent(SearchUserActivity.this,MapsActivity.class);
//                SearchUserActivity.this.startActivity(myIntent);
//            }
//        });
//    }

    public class SearchTask extends AsyncTask<Void, Void, Boolean> {


        String server_response;
        private final String mPattern;

        SearchTask(String pattern) {
            this.mPattern = pattern;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL("http://192.168.179.94:8090/searchusers?pattern=" + mPattern);

                urlConnection = (HttpURLConnection) url.openConnection();

                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    server_response = readStream(urlConnection.getInputStream());
                    Log.v("CatalogClient", server_response);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            return true;
        }

        private String readStream(InputStream in) {
            BufferedReader reader = null;
            StringBuffer response = new StringBuffer();
            try {
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return response.toString();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                Gson gson = new Gson();
                List<String> result = gson.fromJson(server_response, List.class);
                listItems.clear();
                listItems.addAll(result);
                adapter.notifyDataSetChanged();;
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

}
