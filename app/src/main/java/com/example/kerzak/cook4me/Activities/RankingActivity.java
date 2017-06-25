package com.example.kerzak.cook4me.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.kerzak.cook4me.DataStructures.Ranking;
import com.example.kerzak.cook4me.R;
import com.example.kerzak.cook4me.Serialization.GsonTon;
import com.example.kerzak.cook4me.Sockets.ClientThread;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class RankingActivity extends ListActivity {

    UpdateRankingTask updateRankingTask = null;

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
    String json;

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
        json = extras.getString("json");
        TextView nameTextView = (TextView) findViewById(R.id.userNameTextView);
        nameTextView.setText(cookName);
        rankingTextView = (TextView) findViewById(R.id.rankingOfUserTextView);

        TextView myNickTextView = (TextView) findViewById(R.id.myNick);
        myNickTextView.setText(LoginActivity.nickname + ":");

        updateRanking = (Button) findViewById(R.id.updateRanking);
        updateRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int stars = (int) ratingBar.getRating();
                String comment = myComment.getText().toString();
                updateRanking.setVisibility(View.GONE);
                updateRankingTask = new UpdateRankingTask(cookName, stars, comment);
                updateRankingTask.execute();
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
        ClientThread.getInstance(null).requestRanking(cookName, rankingHandler);
//        RankingTask rankingTask = new RankingTask(cookName);
//        rankingTask.execute();
    }

    private Handler rankingHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            showProgress(false);
            Gson gson = GsonTon.getInstance().getGson();
            Ranking result = gson.fromJson(String.valueOf(msg.obj), Ranking.class);
            rankingTextView.setText(result.getRanking() + "/5");
            listItems.clear();
            HashMap<String, String> comments = result.getComments();
            HashMap<String, Integer> rankings = result.getRankings();
            for (String user : comments.keySet()) {
                String comment = comments.get(user);
                if (user.equals(LoginActivity.nickname)) {
                    myComment.setText(comment);
                    ratingBar.setRating(rankings.get(user));
                } else {
                    listItems.add(user + " (" + rankings.get(user) + "/5): " + comment);
                }
            }
            adapter.notifyDataSetChanged();
            updateRanking.setVisibility(View.GONE);
        }
    };

    @Override
    public void onBackPressed() {
        Intent myIntent = new Intent(RankingActivity.this,SearchUserActivity.class);
        if (json != null) {
            myIntent.putExtra("json", json);
        }
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

    public class UpdateRankingTask extends AsyncTask<Void, Void, Boolean> {


        String server_response = "";
        private final String cook;
        private final int stars;
        private final String comment;

        UpdateRankingTask(String cook, int stars, String comment) {
            this.cook = cook;
            this.stars = stars;
            this.comment = comment;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL("http://" + LoginActivity.SERVER_IP + ":8090/updateuserranking?name=" + LoginActivity.email + "&pass=" + LoginActivity.password +
                        "&cook=" + cook + "&stars=" + stars);

                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("comment",comment);
                urlConnection.setDoOutput(true);

                HashMap<String,String> postDataParams = new HashMap<>();
                postDataParams.put("comment",comment);
                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();
                int responseCode=urlConnection.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        server_response+=line;
                    }
                }
                else {
                    server_response="";

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

        private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for(Map.Entry<String, String> entry : params.entrySet()){
                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }

            return result.toString();
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
            updateRankingTask = null;
        }

        @Override
        protected void onCancelled() {
            updateRankingTask = null;
        }
    }

}
