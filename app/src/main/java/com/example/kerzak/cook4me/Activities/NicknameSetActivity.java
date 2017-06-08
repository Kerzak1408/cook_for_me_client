package com.example.kerzak.cook4me.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.kerzak.cook4me.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class NicknameSetActivity extends AppCompatActivity {

    private NicknameTask mAuthTask = null;
    private View mProgressView;
    private View mLoginFormView;
    private TextView mLog;
    private Button confirmNicknameButton;

    private String mEmail;
    private String mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nickname_set);
        mProgressView = findViewById(R.id.nickProgressBar);
        mLoginFormView = findViewById(R.id.nick_form);
        mLog = (TextView) findViewById(R.id.errorTextView);
        Bundle extras = getIntent().getExtras();
        mEmail = extras.getString("login");
        mPassword = extras.getString("pass");
        setConfirmNicknameButtonLogic();
    }

    private void setConfirmNicknameButtonLogic() {
        confirmNicknameButton = (Button) findViewById(R.id.confirmNicknameButton);
        confirmNicknameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText nicknameInput = (EditText) findViewById(R.id.nicknameInput);
                String nickname = nicknameInput.getText().toString();

                showProgress(true);
                mAuthTask = new NicknameTask(mEmail, mPassword, nickname);
                mAuthTask.execute((Void) null);
            }
        });
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

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    public class NicknameTask extends AsyncTask<Void, Void, Boolean> {

        String server_response;
        private final String mEmail;
        private final String mPassword;
        private final String mNickname;

        NicknameTask(String email, String password, String nickname) {
            mEmail = email;
            mPassword = password;
            mNickname = nickname;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL("http://192.168.179.94:8090/setnickname?name=" + mEmail + "&pass=" + mPassword + "&nickname=" + mNickname);

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
                if ("OK".equals(server_response)) {
                    finish();
                    Intent myIntent = new Intent(NicknameSetActivity.this, MapsActivity.class);
                    myIntent.putExtra("login", mEmail);
                    NicknameSetActivity.this.startActivity(myIntent);
                } else if ("UNIQUE".equals(server_response)) {
                    mLog.setTextColor(Color.RED);
                    mLog.setText("We are sorry but nickname " + mNickname + " is already taken. Choose another one please. ");
                } else {
                    mLog.setTextColor(Color.RED);
                    mLog.setText("Something went wrong. Check your connection.");
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
