package com.example.ai_task_manager.network;

public class AiTaskRequest {

    private String input;

    public AiTaskRequest(String input) {
        this.input = input;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }
}