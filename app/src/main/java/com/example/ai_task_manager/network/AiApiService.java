package com.example.ai_task_manager.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AiApiService {

    @POST("api/ai/create-task")
    Call<AiTaskResponse> createTask(
            @Body AiTaskRequest request
    );
    @POST("api/ai/create-subtasks")
    Call<SubtaskResponse> createSubtasks(
            @Body SubtaskRequest request
    );
}