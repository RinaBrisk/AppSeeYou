package com.example.rina.testapp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {
    private Socket socket;
    private OutputStream socketOutput;
    private BufferedReader socketInput;

    private String ip;
    private int port;

    private static final String TAG = "MyLog";

    Client(String ip, int port){
        this.ip=ip;
        this.port=port;
    }

    public void connect(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                socket = new Socket();
                InetSocketAddress socketAddress = new InetSocketAddress(ip, port);
                try {
                    socket.connect(socketAddress);
                    socketOutput = socket.getOutputStream();
                    socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    // в socketInput записывается прочтенное сообщение из внешнего потока

                    new ReceiveThread().start();

                    onConnect();
                } catch (IOException e) {
                    onConnectError(e.getMessage());
                }
            }
        }).start();

    }

    private void disconnect(){
        try {
            socket.close();
        } catch (IOException e) {
            onDisconnect(e.getMessage());
        }
    }

    private void send(String message){
        try {
            socketOutput.write(message.getBytes());
        } catch (IOException e) {
            onSend(e.getMessage());
        }
    }

    private class ReceiveThread extends Thread implements Runnable{
        public void run(){
            String message;
            try {
                while((message = socketInput.readLine()) != null) { // часть, которая получает сообщения
                    onMessage(message);
                }
            } catch (IOException e) {
                onDisconnect(e.getMessage());
            }
        }
    }

    private void onMessage(String message) {
        Log.d(TAG, "onMessage: " + message);

        //MainActivity.changeText(message);
    }

    private void onConnect() {
        Log.d(TAG, "onConnect \n");

        send("Hello Server");
    }

    private void onSend(String message) {
        Log.d(TAG,"onSend: " + message);
    }

    private void onDisconnect(String message) {
        Log.d(TAG,"onDisconnect: " + message);

        disconnect();
    }

    private void onConnectError(String message){
        Log.d(TAG,"onConnectError: " + message);
    }
}

