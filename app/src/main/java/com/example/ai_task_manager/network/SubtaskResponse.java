package com.example.ai_task_manager.network;

import java.util.List;

public class SubtaskResponse {

    private boolean success;
    private List<Subtask> subtasks;
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public String getMessage() {
        return message;
    }

    public static class Subtask {

        private String title;

        public String getTitle() {
            return title;
        }
    }
}