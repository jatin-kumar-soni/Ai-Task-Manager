package com.example.ai_task_manager.ui.ai;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ai_task_manager.MainActivity;
import com.example.ai_task_manager.R;
import com.example.ai_task_manager.network.AiApiService;
import com.example.ai_task_manager.network.RetrofitClient;
import com.example.ai_task_manager.network.SubtaskRequest;
import com.example.ai_task_manager.network.SubtaskResponse;
import com.example.ai_task_manager.ui.task.TaskListActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubtaskActivity extends AppCompatActivity {

    private LinearLayout subtaskContainer;
    private BottomNavigationView bottomNavigation;

    private TextView tvSubtaskProgress;
    private ProgressBar subtaskProgressBar;

    private int totalSubtasks = 0;
    private int completedSubtasks = 0;

    private SharedPreferences preferences;

    private int taskId;

    private String taskTitle;
    private String taskDescription;

    private List<String> subtaskTitles =
            new ArrayList<>();

    private boolean[] completedStates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_subtask);

        // Initialize views
        subtaskContainer =
                findViewById(R.id.subtaskContainer);

        bottomNavigation =
                findViewById(R.id.bottomNavigation);

        tvSubtaskProgress =
                findViewById(R.id.tvSubtaskProgress);

        subtaskProgressBar =
                findViewById(R.id.subtaskProgressBar);

        // SharedPreferences
        preferences =
                getSharedPreferences(
                        "subtask_preferences",
                        MODE_PRIVATE
                );

        setupBottomNavigation();

        // Get Task ID
        taskId =
                getIntent().getIntExtra(
                        "TASK_ID",
                        -1
                );

        // Get task information
        taskTitle =
                getIntent().getStringExtra(
                        "TASK_TITLE"
                );

        taskDescription =
                getIntent().getStringExtra(
                        "TASK_DESCRIPTION"
                );

        // Validate Task ID
        if (taskId == -1) {

            Toast.makeText(
                    this,
                    "Task ID is missing",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        // Validate Task title
        if (taskTitle == null ||
                taskTitle.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Task information is missing",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        /*
         * Check whether subtasks already
         * exist for this task.
         */
        if (loadSavedSubtasks()) {

            displaySavedSubtasks();

        } else {

            generateSubtasks(
                    taskTitle,
                    taskDescription
            );
        }
    }

    /**
     * Creates a unique preference key
     * using the Room Task ID.
     */
    private String getPreferenceKey() {

        return "task_" + taskId;
    }

    /**
     * Load previously generated subtasks.
     */
    private boolean loadSavedSubtasks() {

        String key =
                getPreferenceKey();

        int count =
                preferences.getInt(
                        key + "_count",
                        0
                );

        if (count == 0) {
            return false;
        }

        subtaskTitles.clear();

        for (int i = 0; i < count; i++) {

            String title =
                    preferences.getString(
                            key + "_title_" + i,
                            null
                    );

            if (title == null) {
                return false;
            }

            subtaskTitles.add(title);
        }

        completedStates =
                new boolean[count];

        for (int i = 0; i < count; i++) {

            completedStates[i] =
                    preferences.getBoolean(
                            key + "_completed_" + i,
                            false
                    );
        }

        return true;
    }

    /**
     * Save subtasks and completion states.
     */
    private void saveSubtasks() {

        String key =
                getPreferenceKey();

        SharedPreferences.Editor editor =
                preferences.edit();

        editor.putInt(
                key + "_count",
                subtaskTitles.size()
        );

        for (int i = 0;
             i < subtaskTitles.size();
             i++) {

            editor.putString(
                    key + "_title_" + i,
                    subtaskTitles.get(i)
            );

            editor.putBoolean(
                    key + "_completed_" + i,
                    completedStates[i]
            );
        }

        editor.apply();
    }

    /**
     * Generate subtasks using AI.
     */
    private void generateSubtasks(
            String title,
            String description
    ) {

        Toast.makeText(
                this,
                "Generating subtasks...",
                Toast.LENGTH_SHORT
        ).show();

        AiApiService apiService =
                RetrofitClient.getAiApiService();

        SubtaskRequest request =
                new SubtaskRequest(
                        title,
                        description
                );

        apiService
                .createSubtasks(request)
                .enqueue(
                        new Callback<SubtaskResponse>() {

                            @Override
                            public void onResponse(
                                    Call<SubtaskResponse> call,
                                    Response<SubtaskResponse> response
                            ) {

                                /*
                                 * Handle HTTP errors such as
                                 * 400, 404, 500, etc.
                                 */
                                if (!response.isSuccessful()) {

                                    Log.e(
                                            "SUBTASK_API_ERROR",
                                            "HTTP error: "
                                                    + response.code()
                                    );

                                    Toast.makeText(
                                            SubtaskActivity.this,
                                            "AI service is currently unavailable. Please try again.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    return;
                                }

                                /*
                                 * Handle empty response.
                                 */
                                if (response.body() == null) {

                                    Log.e(
                                            "SUBTASK_API_ERROR",
                                            "Empty response from server"
                                    );

                                    Toast.makeText(
                                            SubtaskActivity.this,
                                            "AI service returned an invalid response. Please try again.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    return;
                                }

                                SubtaskResponse result =
                                        response.body();

                                /*
                                 * Handle backend success=false.
                                 */
                                if (!result.isSuccess()
                                        || result.getSubtasks() == null
                                        || result.getSubtasks().isEmpty()) {

                                    String message =
                                            result.getMessage();

                                    if (message == null
                                            || message.trim().isEmpty()) {

                                        message =
                                                "AI could not generate subtasks. Please try again.";
                                    }

                                    Toast.makeText(
                                            SubtaskActivity.this,
                                            message,
                                            Toast.LENGTH_LONG
                                    ).show();

                                    return;
                                }

                                subtaskTitles.clear();

                                /*
                                 * Store AI-generated
                                 * subtask titles.
                                 */
                                for (
                                        SubtaskResponse.Subtask subtask :
                                        result.getSubtasks()
                                ) {

                                    if (subtask.getTitle() != null
                                            && !subtask.getTitle()
                                            .trim()
                                            .isEmpty()) {

                                        subtaskTitles.add(
                                                subtask.getTitle()
                                                        .trim()
                                        );
                                    }
                                }

                                /*
                                 * Make sure AI actually
                                 * returned valid subtasks.
                                 */
                                if (subtaskTitles.isEmpty()) {

                                    Toast.makeText(
                                            SubtaskActivity.this,
                                            "AI could not generate valid subtasks. Please try again.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    return;
                                }

                                totalSubtasks =
                                        subtaskTitles.size();

                                completedStates =
                                        new boolean[
                                                totalSubtasks
                                                ];

                                completedSubtasks = 0;

                                // Persist generated subtasks
                                saveSubtasks();

                                displaySubtasks();

                                Toast.makeText(
                                        SubtaskActivity.this,
                                        "Subtasks generated successfully",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                            @Override
                            public void onFailure(
                                    Call<SubtaskResponse> call,
                                    Throwable t
                            ) {

                                /*
                                 * Log technical details for
                                 * developers, but don't expose
                                 * them to the user.
                                 */
                                Log.e(
                                        "SUBTASK_API_ERROR",
                                        "Network request failed",
                                        t
                                );

                                /*
                                 * Friendly message when the
                                 * backend is stopped or the
                                 * device cannot connect.
                                 */
                                Toast.makeText(
                                        SubtaskActivity.this,
                                        "Unable to connect to AI service. Please check your connection and try again.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }

    /**
     * Display previously saved subtasks.
     */
    private void displaySavedSubtasks() {

        totalSubtasks =
                subtaskTitles.size();

        completedSubtasks = 0;

        for (boolean completed :
                completedStates) {

            if (completed) {
                completedSubtasks++;
            }
        }

        displaySubtasks();
    }

    /**
     * Display subtasks with checkboxes.
     */
    private void displaySubtasks() {

        subtaskContainer.removeAllViews();

        updateProgress();

        for (int i = 0;
             i < subtaskTitles.size();
             i++) {

            final int index = i;

            CheckBox checkBox =
                    new CheckBox(this);

            checkBox.setText(
                    (i + 1)
                            + ". "
                            + subtaskTitles.get(i)
            );

            checkBox.setTextSize(16);

            checkBox.setPadding(
                    0,
                    10,
                    0,
                    10
            );

            /*
             * Restore saved checkbox state.
             */
            checkBox.setChecked(
                    completedStates[i]
            );

            checkBox.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> {

                        /*
                         * Prevent duplicate count
                         * changes during restoration.
                         */
                        if (completedStates[index]
                                == isChecked) {

                            return;
                        }

                        completedStates[index] =
                                isChecked;

                        if (isChecked) {

                            completedSubtasks++;

                        } else {

                            completedSubtasks--;
                        }

                        // Save immediately
                        saveSubtasks();

                        // Update progress
                        updateProgress();
                    }
            );

            subtaskContainer.addView(
                    checkBox
            );
        }
    }

    /**
     * Update progress text and progress bar.
     */
    private void updateProgress() {

        tvSubtaskProgress.setText(
                completedSubtasks
                        + " / "
                        + totalSubtasks
                        + " completed"
        );

        int progress = 0;

        if (totalSubtasks > 0) {

            progress =
                    (completedSubtasks * 100)
                            / totalSubtasks;
        }

        subtaskProgressBar.setProgress(
                progress
        );
    }

    /**
     * Bottom navigation.
     */
    private void setupBottomNavigation() {

        bottomNavigation.setSelectedItemId(
                R.id.nav_ai
        );

        bottomNavigation.setOnItemSelectedListener(
                item -> {

                    int id =
                            item.getItemId();

                    if (id == R.id.nav_home) {

                        startActivity(
                                new Intent(
                                        this,
                                        MainActivity.class
                                )
                        );

                        finish();

                        return true;
                    }

                    if (id == R.id.nav_tasks) {

                        startActivity(
                                new Intent(
                                        this,
                                        TaskListActivity.class
                                )
                        );

                        finish();

                        return true;
                    }

                    if (id == R.id.nav_ai) {

                        return true;
                    }

                    return false;
                }
        );
    }
}