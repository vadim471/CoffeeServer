package com.example.telemetry.model;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class Task {
    private String command;
    private ObjectNode body;

    public Task(String command, ObjectNode body) {
        this.command = command;
        this.body = body;
    }

    public String getCommand() {
        return command;
    }

    public ObjectNode getBody() {
        return body;
    }

    public void setCommand(String command){
        this.command = command;
    }

    public void setBody(ObjectNode body) {
        this.body = body;
    }
}
