package com.example.telemetry.service;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Класс, реализующий отправку сообщений из буфера в стрим
 */
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
