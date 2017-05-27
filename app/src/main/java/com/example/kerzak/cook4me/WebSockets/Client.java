package com.example.kerzak.cook4me.WebSockets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements Runnable {

    Socket socket;
    private final String SERVER_IP = "192.168.179.94";
    private final int SERVER_PORT = 6666;
    PrintWriter writer;

    @Override
    public void run() {

        try {
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

            socket = new Socket(serverAddr, SERVER_PORT);

            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null){
                parseInput(line);
            }

        } catch (UnknownHostException e1){
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void sendMessage(String line) {
        writer.println(line);
    }

    private void parseInput(String line) {

    }
}