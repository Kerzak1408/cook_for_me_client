package com.example.kerzak.cook4me.WebSockets;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

class ClientThread implements Runnable {

    Socket socket;
    private final String SERVER_IP = "192.168.179.94";
    private final int SERVER_PORT = 6666;

    @Override
    public void run() {

        try {
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

            socket = new Socket(serverAddr, SERVER_PORT);

        } catch (UnknownHostException e1){
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}