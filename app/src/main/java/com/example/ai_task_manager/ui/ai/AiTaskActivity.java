package com.example.ai_task_manager.ui.ai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ai_task_manager.MainActivity;
import com.example.ai_task_manager.R;
import com.example.ai_task_manager.model.Task;
import com.example.ai_task_manager.network.AiApiService;
import com.example.ai_task_manager.network.AiTaskRequest;
import com.example.ai_task_manager.network.AiTaskResponse;
import com.example.ai_task_manager.network.RetrofitClient;
import com.example.ai_task_manager.repository.TaskRepository;
import com.example.ai_task_manager.ui.task.TaskListActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AiTaskActivity extends AppCompatActivity {

    private TextInputEditText etAiInput;

    private LinearLayout aiLoadingContainer;
    private LinearLayout aiResultContainer;

    private TaskRepository taskRepository;

    private TextInputEditText etAiTitle;
    private TextInputEditText etAiDescription;
    private TextInputEditText etAiDueDate;
    private TextInputEditText etAiAdditionalInfo;

    private MaterialButtonToggleGroup aiPriorityGroup;

    private MaterialButton aiBtnLow;
    private MaterialButton aiBtnMedium;
    private MaterialButton aiBtnHigh;

    private MaterialButton btnGenerateAI;
    private MaterialButton btnSaveAITask;
    private MaterialButton btnRegenerateAI;

    private BottomNavigationView bottomNavigation;

    private AiTaskResponse.TaskData generatedTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ai_task);

        taskRepository = new TaskRepository(this);

        initializeViews();
        setupBottomNavigation();
        setupGenerateButton();
        setupRegenerateButton();
        setupSaveButton();
    }

    private void initializeViews() {

        etAiInput = findViewById(R.id.etAiInput);

        aiLoadingContainer =
                findViewById(R.id.aiLoadingContainer);

        aiResultContainer =
                findViewById(R.id.aiResultContainer);

        etAiTitle =
                findViewById(R.id.etAiTitle);

        etAiDescription =
                findViewById(R.id.etAiDescription);

        etAiDueDate =
                findViewById(R.id.etAiDueDate);

        etAiAdditionalInfo =
                findViewById(R.id.etAiAdditionalInfo);

        aiPriorityGroup =
                findViewById(R.id.aiPriorityGroup);

        aiBtnLow =
                findViewById(R.id.aiBtnLow);

        aiBtnMedium =
                findViewById(R.id.aiBtnMedium);

        aiBtnHigh =
                findViewById(R.id.aiBtnHigh);

        btnGenerateAI =
                findViewById(R.id.btnGenerateAI);

        btnSaveAITask =
                findViewById(R.id.btnSaveAITask);

        btnRegenerateAI =
                findViewById(R.id.btnRegenerateAI);

        bottomNavigation =
                findViewById(R.id.bottomNavigation);
    }

    private void setupGenerateButton() {

        btnGenerateAI.setOnClickListener(v -> {

            String input =
                    etAiInput.getText()
                            .toString()
                            .trim();

            if (input.isEmpty()) {

                etAiInput.setError(
                        "Please describe your task"
                );

                etAiInput.requestFocus();

                return;
            }

            generateTask(input);
        });
    }

    private void generateTask(String input) {

        setLoading(true);

        // Hide any previous result while generating
        aiResultContainer.setVisibility(View.GONE);

        AiApiService apiService =
                RetrofitClient
                        .getAiApiService();

        AiTaskRequest request =
                new AiTaskRequest(input);

        apiService.createTask(request)
                .enqueue(new Callback<AiTaskResponse>() {

                    @Override
                    public void onResponse(
                            Call<AiTaskResponse> call,
                            Response<AiTaskResponse> response
                    ) {

                        setLoading(false);

                        // HTTP error from backend
                        if (!response.isSuccessful()) {

                            Log.e(
                                    "AI_API_ERROR",
                                    "HTTP error: " + response.code()
                            );

                            Toast.makeText(
                                    AiTaskActivity.this,
                                    "AI service is currently unavailable. Please try again.",
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        // Empty response from backend
                        if (response.body() == null) {

                            Log.e(
                                    "AI_API_ERROR",
                                    "Empty response from server"
                            );

                            Toast.makeText(
                                    AiTaskActivity.this,
                                    "AI service returned an invalid response. Please try again.",
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        AiTaskResponse result =
                                response.body();

                        // Backend returned success=false
                        if (!result.isSuccess()
                                || result.getTask() == null) {

                            String message =
                                    result.getMessage();

                            if (message == null
                                    || message.trim().isEmpty()) {

                                message =
                                        "AI could not create the task. Please try again.";
                            }

                            Toast.makeText(
                                    AiTaskActivity.this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        // Successfully generated task
                        generatedTask =
                                result.getTask();

                        displayAIResult(generatedTask);
                    }

                    @Override
                    public void onFailure(
                            Call<AiTaskResponse> call,
                            Throwable t
                    ) {

                        setLoading(false);

                        Log.e(
                                "AI_API_ERROR",
                                "Network request failed",
                                t
                        );

                        /*
                         * Do NOT show t.getMessage() to the user.
                         * It may contain technical information such as:
                         *
                         * Failed to connect to /10.0.2.2
                         *
                         * Instead show a simple user-friendly message.
                         */

                        Toast.makeText(
                                AiTaskActivity.this,
                                "Unable to connect to AI service. Please check your connection and try again.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void displayAIResult(
            AiTaskResponse.TaskData task
    ) {

        if (task == null) {
            Toast.makeText(
                    this,
                    "Invalid AI response. Please try again.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        etAiTitle.setText(
                task.getTitle() != null
                        ? task.getTitle()
                        : ""
        );

        etAiDescription.setText(
                task.getDescription() != null
                        ? task.getDescription()
                        : ""
        );

        etAiDueDate.setText(
                task.getDueDate() != null
                        ? task.getDueDate()
                        : ""
        );

        etAiAdditionalInfo.setText(
                task.getAdditionalInfo() != null
                        ? task.getAdditionalInfo()
                        : ""
        );

        String priority =
                task.getPriority();

        if ("LOW".equalsIgnoreCase(priority)) {

            aiPriorityGroup.check(
                    R.id.aiBtnLow
            );

        } else if ("HIGH".equalsIgnoreCase(priority)) {

            aiPriorityGroup.check(
                    R.id.aiBtnHigh
            );

        } else {

            aiPriorityGroup.check(
                    R.id.aiBtnMedium
            );
        }

        aiResultContainer.setVisibility(View.VISIBLE);

// AI result is ready, so allow the user to save it
        btnSaveAITask.setEnabled(true);
        btnSaveAITask.setAlpha(1.0f);
    }

    private void setLoading(boolean loading) {

        if (loading) {

            aiLoadingContainer.setVisibility(View.VISIBLE);

            btnGenerateAI.setEnabled(false);
            btnRegenerateAI.setEnabled(false);
            btnSaveAITask.setEnabled(false);
            btnSaveAITask.setAlpha(0.5f);

        } else {

            aiLoadingContainer.setVisibility(View.GONE);

            btnGenerateAI.setEnabled(true);
            btnRegenerateAI.setEnabled(true);

            if (generatedTask != null) {
                btnSaveAITask.setEnabled(true);
                btnSaveAITask.setAlpha(1.0f);
            } else {
                btnSaveAITask.setEnabled(false);
                btnSaveAITask.setAlpha(0.5f);
            }
        }
    }

    private void setupRegenerateButton() {

        btnRegenerateAI.setOnClickListener(v -> {

            String input =
                    etAiInput.getText()
                            .toString()
                            .trim();

            if (input.isEmpty()) {

                etAiInput.setError(
                        "Please describe your task"
                );

                etAiInput.requestFocus();

                return;
            }

            generateTask(input);
        });
    }

    private void setupSaveButton() {

        btnSaveAITask.setOnClickListener(v -> {

            if (generatedTask == null) {

                Toast.makeText(
                        AiTaskActivity.this,
                        "Please generate a task first",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            String title =
                    etAiTitle.getText()
                            .toString()
                            .trim();

            String description =
                    etAiDescription.getText()
                            .toString()
                            .trim();

            String dueDate =
                    etAiDueDate.getText()
                            .toString()
                            .trim();

            String additionalInfo =
                    etAiAdditionalInfo.getText()
                            .toString()
                            .trim();

            if (title.isEmpty()) {

                etAiTitle.setError(
                        "Task title is required"
                );

                etAiTitle.requestFocus();

                return;
            }

            if (dueDate.isEmpty()) {

                etAiDueDate.setError(
                        "Due date is required"
                );

                etAiDueDate.requestFocus();

                return;
            }

            String priority = "MEDIUM";

            int selectedId =
                    aiPriorityGroup.getCheckedButtonId();

            if (selectedId == R.id.aiBtnLow) {

                priority = "LOW";

            } else if (selectedId == R.id.aiBtnHigh) {

                priority = "HIGH";
            }

            long currentTime =
                    System.currentTimeMillis();

            Task task = new Task(
                    title,
                    description,
                    dueDate,
                    priority,
                    "PENDING",
                    additionalInfo,
                    currentTime,
                    currentTime
            );

            taskRepository.insertTask(task);

            Toast.makeText(
                    AiTaskActivity.this,
                    "Task saved successfully",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        });
    }

    private void setupBottomNavigation() {

        bottomNavigation.setSelectedItemId(
                R.id.nav_ai
        );

        bottomNavigation.setOnItemSelectedListener(
                item -> {

                    int itemId =
                            item.getItemId();

                    if (itemId == R.id.nav_home) {

                        startActivity(
                                new Intent(
                                        AiTaskActivity.this,
                                        MainActivity.class
                                )
                        );

                        finish();

                        return true;
                    }

                    if (itemId == R.id.nav_tasks) {

                        startActivity(
                                new Intent(
                                        AiTaskActivity.this,
                                        TaskListActivity.class
                                )
                        );

                        finish();

                        return true;
                    }

                    if (itemId == R.id.nav_ai) {

                        return true;
                    }

                    return false;
                }
        );
    }
}