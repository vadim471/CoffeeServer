package com.example.telemetry.service;

import java.io.IOException;
import java.io.OutputStream;

public class MessageSender {

    private final OutputStream out;

    public MessageSender(OutputStream out){
        this.out = out;
    }

    public void sendMessage(byte[] message) throws IOException{
        out.write(message);
        out.flush();
    }
}
