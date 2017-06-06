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

public class ClientThread extends Thread {

//    private String completeJSON;
    private Handler handler;
    private BufferedWriter writer;
    private BufferedReader reader;
    private Socket socket;

    public ClientThread(Handler handler) {
        this.handler = handler;
    }


    @Override
    public void run() {
        try {
            // Create Socket instance
            socket = new Socket("192.168.179.94", 6666);

            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//                    bw.write("Ciao server\n");
//                    bw.flush();
//            writer.write("cook#" + completeJSON + "\n");
//            writer.flush();
            // Get input buffer
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                String[] splitMsg = line.split("#");
                Message msg = new Message();
                if ("cook".equals(splitMsg[0])){
                    Gson gson = new Gson();

                    CookingData cookingData = gson.fromJson(splitMsg[1],CookingData.class);

                    msg.obj = cookingData;
                    msg.arg1 = 0;

                } else if ("remove".equals(splitMsg[0])) {
                    msg.arg1 = 1;
                    msg.obj = splitMsg[1];
                } else if ("registered".equals(splitMsg[0])){
                    msg.arg1 = 3;
                    msg.obj = splitMsg;
                }
                handler.sendMessage(msg);
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
            if (writer != null) {
                writer.write(line + "\n");
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
