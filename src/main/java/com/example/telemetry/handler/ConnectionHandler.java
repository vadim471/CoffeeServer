package com.example.telemetry.handler;

import java.net.Socket;

public interface ConnectionHandler {
    void handleConnection(Socket socket);
}
