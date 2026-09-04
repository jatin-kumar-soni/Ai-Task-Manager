package com.example.ai_task_manager.network;

public class AiTaskResponse {

    private boolean success;
    private TaskData task;
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public TaskData getTask() {
        return task;
    }

    public String getMessage() {
        return message;
    }

    public static class TaskData {

        private String title;
        private String description;
        private String dueDate;
        private String priority;
        private String additionalInfo;

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getDueDate() {
            return dueDate;
        }

        public String getPriority() {
            return priority;
        }

        public String getAdditionalInfo() {
            return additionalInfo;
        }
    }
}