package com.example.telemetry.model;

public class MachineRequest {
    private byte[] header;
    private byte[] body;

    public byte[] getHeader() {
        return header;
    }

    public byte[] getBody() {
        return body;
    }

    public void setHeader(byte[] header) {
        this.header = header;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
