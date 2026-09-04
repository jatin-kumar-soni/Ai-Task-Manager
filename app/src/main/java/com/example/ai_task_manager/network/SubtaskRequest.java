package com.example.ai_task_manager.network;

public class SubtaskRequest {

    private String title;
    private String description;

    public SubtaskRequest(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}