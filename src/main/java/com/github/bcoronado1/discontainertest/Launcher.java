package com.github.bcoronado1.discontainertest;

public class Launcher {

    public static void main(String[] args) {
        Thread sendThread = new Thread(new SendDIS(args));
        Thread receiveThread = new Thread(new ReceiveDIS(args));
        sendThread.start();
        receiveThread.start();
    }
}
