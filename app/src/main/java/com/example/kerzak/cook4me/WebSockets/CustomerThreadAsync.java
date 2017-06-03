package com.example.kerzak.cook4me.WebSockets;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.example.kerzak.cook4me.Activities.MapsActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Kerzak on 02-Jun-17.
 */

    public class CustomerThreadAsync extends AsyncTask<Void,LatLng,LatLng> {
    public GoogleMap googleMap;

    public CustomerThreadAsync(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }
    // Define Handler object
    private Handler handler = new Handler() {
        @Override
        // When there is message, execute this method
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // Update UI
        }
    };

    @Override
    protected LatLng doInBackground(Void... params) {
        try {
            // Create Socket instance
            Socket socket = new Socket("192.168.179.94", 6666);

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            // Get input buffer
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            String line = "";

            line = br.readLine();
            {
//                loggerView.setText(line);
                String[] splitMsg = line.split("#");
                Gson gson = new Gson();
                LatLng cookPosition = gson.fromJson(splitMsg[2],LatLng.class);
                CookingData cookingData = gson.fromJson(splitMsg[1],CookingData.class);
                publishProgress(cookPosition);
                return cookPosition;
//                        newCook.setVisible(true);
//                        newCook.setTitle(cookingData.getName());
            }

//            br.close();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        handler.sendEmptyMessage(0);
        return null;

    }

    @Override
    protected void onProgressUpdate(LatLng... hotspots) {
        googleMap.addMarker(new MarkerOptions().position(hotspots[0]).icon(BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_ORANGE)));
    }

    @Override
    protected void onPostExecute(LatLng latLng) {
        googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_ORANGE)));
    }
}
