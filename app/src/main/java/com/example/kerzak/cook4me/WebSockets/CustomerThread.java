package com.example.kerzak.cook4me.WebSockets;

import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Kerzak on 03-Jun-17.
 */

public class CustomerThread extends Thread {

    private Handler handler;
    BufferedReader reader;
    BufferedWriter writer;

    public CustomerThread(Handler handler) {
        this.handler = handler;
    }

    public void run() {
        try {
            // Create Socket instance
            Socket socket = new Socket("192.168.179.94", 6666);

            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            // Get input buffer
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            String line = "";

            while ((line = reader.readLine()) != null) {
//                        loggerView.setText(line);
                String[] splitMsg = line.split("#");
                Gson gson = new Gson();

                CookingData cookingData = gson.fromJson(splitMsg[1],CookingData.class);
//                        LatLng cookPosition = cookingData.getLocation();
//                        Marker newCook = mMap.addMarker(new MarkerOptions().position(cookPosition).icon(BitmapDescriptorFactory.defaultMarker(
//                                BitmapDescriptorFactory.HUE_ORANGE)));
                Message msg = new Message();
                msg.obj = cookingData;

                handler.sendMessage(msg);
//                        newCook.setVisible(true);
//                        newCook.setTitle(cookingData.getName());
            }

            reader.close();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//                serverMessageHandler.sendEmptyMessage(0);
    }

    public void writeLine(String line) {
        try {
            writer.write(line + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
