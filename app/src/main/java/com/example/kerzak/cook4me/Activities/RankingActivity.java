package com.example.kerzak.cook4me.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
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


    EditText myComment;
    TextView rankingTextView;
    RatingBar ratingBar;
    private View mProgressView;
    private View mRankingView;
    Button updateRanking;

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

        TextView myNickTextView = (TextView) findViewById(R.id.myNick);
        myNickTextView.setText(LoginActivity.nickname + ":");

        updateRanking = (Button) findViewById(R.id.updateRanking);
        updateRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRanking.setVisibility(View.GONE);
            }
        });
        myComment = (EditText) findViewById(R.id.editMyComment);
        myComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateRanking.setVisibility(View.VISIBLE);
            }
        });
        ratingBar = (RatingBar) findViewById(R.id.myRanking);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                updateRanking.setVisibility(View.VISIBLE);
            }
        });

        mProgressView = findViewById(R.id.rankingProgress);
        mRankingView = findViewById(R.id.rankingView);

        showProgress(true);

        RankingTask rankingTask = new RankingTask(cookName);
        rankingTask.execute();
    }

    @Override
    public void onBackPressed() {
        Intent myIntent = new Intent(RankingActivity.this,SearchUserActivity.class);
        RankingActivity.this.startActivity(myIntent);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRankingView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRankingView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRankingView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRankingView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
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
            showProgress(false);
            if (success) {
                Gson gson = new Gson();
                Ranking result = gson.fromJson(server_response, Ranking.class);
                rankingTextView.setText(result.getRanking() + "/10");
                listItems.clear();
                HashMap<String, String> comments = result.getComments();
                HashMap<String, Integer> rankings = result.getRankings();
                for (String user : comments.keySet()) {
                    String comment = comments.get(user);
                    if (user.equals(LoginActivity.nickname)) {
                        myComment.setText(comment);
                        ratingBar.setProgress(rankings.get(user));
                    } else {
                        listItems.add(user + " (" + rankings.get(user) + "/10): " + comment);
                    }
                }
                adapter.notifyDataSetChanged();;
            }
            updateRanking.setVisibility(View.GONE);
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

}
