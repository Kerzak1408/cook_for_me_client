package com.example.kerzak.cook4me.Activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.kerzak.cook4me.DataStructures.Ranking;
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
import java.util.HashMap;
import java.util.List;

public class RankingActivity extends ListActivity {

    RankingTask mAuthTask = null;

    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    List<String> listItems=new ArrayList<String>();

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    ArrayAdapter<String> adapter;

    String cookName;
    TextView rankingTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);
        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        setListAdapter(adapter);

        Bundle extras = getIntent().getExtras();
        cookName = extras.getString("name");
        TextView nameTextView = (TextView) findViewById(R.id.userNameTextView);
        nameTextView.setText(cookName);
        rankingTextView = (TextView) findViewById(R.id.rankingOfUserTextView);

        RankingTask rankingTask = new RankingTask(cookName);
        rankingTask.execute();
    }

    public class RankingTask extends AsyncTask<Void, Void, Boolean> {


        String server_response;
        private final String mName;

        RankingTask(String name) {
            this.mName = name;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL("http://192.168.179.94:8090/getuserranking?name=" + mName);

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
                Ranking result = gson.fromJson(server_response, Ranking.class);
                rankingTextView.setText(result.getRanking() + "/5");
                listItems.clear();
                HashMap<String, String> comments = result.getComments();
                for (String user : comments.keySet()) {
                    String comment = comments.get(user);
                    listItems.add(user + ": " + comment);
                }

                adapter.notifyDataSetChanged();;
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

}
