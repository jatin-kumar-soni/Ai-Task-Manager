package com.example.ai_task_manager.ui.task;
import android.content.Intent;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ai_task_manager.R;
import com.example.ai_task_manager.model.Task;
import com.example.ai_task_manager.repository.TaskRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditTaskActivity extends AppCompatActivity {
    private MaterialButton btnBreakSubtasks;

    private TextInputEditText etTitle;
    private TextInputEditText etDescription;
    private TextInputEditText etDueDate;
    private TextInputEditText etAdditionalInfo;

    private MaterialButtonToggleGroup priorityGroup;

    private MaterialButton btnLow;
    private MaterialButton btnMedium;
    private MaterialButton btnHigh;
    private MaterialButton btnUpdateTask;
    private MaterialButton btnCancel;

    private TaskRepository taskRepository;

    private Task currentTask;

    private String selectedPriority = "MEDIUM";

    private int taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_task);

        initializeViews();

        taskRepository = new TaskRepository(this);

        taskId = getIntent().getIntExtra("TASK_ID", -1);

        if (taskId == -1) {
            Toast.makeText(
                    this,
                    "Task not found",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        setupPrioritySelection();

        setupDatePicker();

        setupButtons();

        loadTask();
    }

    private void initializeViews() {

        etTitle = findViewById(R.id.etTitle);

        etDescription =
                findViewById(R.id.etDescription);

        etDueDate =
                findViewById(R.id.etDueDate);

        etAdditionalInfo =
                findViewById(R.id.etAdditionalInfo);

        priorityGroup =
                findViewById(R.id.priorityGroup);

        btnLow =
                findViewById(R.id.btnLow);

        btnMedium =
                findViewById(R.id.btnMedium);

        btnHigh =
                findViewById(R.id.btnHigh);

        btnUpdateTask =
                findViewById(R.id.btnUpdateTask);

        btnCancel =
                findViewById(R.id.btnCancel);
        btnBreakSubtasks =
                findViewById(R.id.btnBreakSubtasks);
    }

    private void setupPrioritySelection() {

        priorityGroup.addOnButtonCheckedListener(
                (group, checkedId, isChecked) -> {

                    if (!isChecked) {
                        return;
                    }

                    if (checkedId == R.id.btnLow) {

                        selectedPriority = "LOW";

                    } else if (checkedId == R.id.btnMedium) {

                        selectedPriority = "MEDIUM";

                    } else if (checkedId == R.id.btnHigh) {

                        selectedPriority = "HIGH";
                    }
                }
        );
    }

    private void setupDatePicker() {

        etDueDate.setOnClickListener(
                v -> showDatePicker()
        );
    }

    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {

                            Calendar selectedDate =
                                    Calendar.getInstance();

                            selectedDate.set(
                                    year,
                                    month,
                                    dayOfMonth
                            );

                            SimpleDateFormat dateFormat =
                                    new SimpleDateFormat(
                                            "yyyy-MM-dd",
                                            Locale.getDefault()
                                    );

                            etDueDate.setText(
                                    dateFormat.format(
                                            selectedDate.getTime()
                                    )
                            );
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );

        dialog.show();
    }

    private void setupButtons() {

        btnUpdateTask.setOnClickListener(
                v -> updateTask()
        );

        btnCancel.setOnClickListener(
                v -> finish()
        );

        btnBreakSubtasks.setOnClickListener(v -> {

            if (currentTask == null) {

                Toast.makeText(
                        this,
                        "Task is still loading",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Intent intent = new Intent(
                    EditTaskActivity.this,
                    com.example.ai_task_manager.ui.ai.SubtaskActivity.class
            );

            // Pass unique Room Task ID
            intent.putExtra(
                    "TASK_ID",
                    currentTask.getId()
            );

            intent.putExtra(
                    "TASK_TITLE",
                    currentTask.getTitle()
            );

            intent.putExtra(
                    "TASK_DESCRIPTION",
                    currentTask.getDescription()
            );

            startActivity(intent);
        });
    }
    private void loadTask() {

        taskRepository.getTaskById(
                taskId,
                task -> {

                    runOnUiThread(() -> {

                        if (task == null) {

                            Toast.makeText(
                                    this,
                                    "Task not found",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();
                            return;
                        }

                        currentTask = task;

                        populateFields(task);
                    });
                }
        );
    }

    private void populateFields(Task task) {

        etTitle.setText(task.getTitle());

        etDescription.setText(
                task.getDescription()
        );

        etDueDate.setText(
                task.getDueDate()
        );

        etAdditionalInfo.setText(
                task.getAdditionalInfo()
        );

        String priority = task.getPriority();

        if ("LOW".equals(priority)) {

            selectedPriority = "LOW";
            priorityGroup.check(R.id.btnLow);

        } else if ("HIGH".equals(priority)) {

            selectedPriority = "HIGH";
            priorityGroup.check(R.id.btnHigh);

        } else {

            selectedPriority = "MEDIUM";
            priorityGroup.check(R.id.btnMedium);
        }
    }

    private void updateTask() {

        if (currentTask == null) {
            return;
        }

        String title =
                getTextValue(etTitle);

        String description =
                getTextValue(etDescription);

        String dueDate =
                getTextValue(etDueDate);

        String additionalInfo =
                getTextValue(etAdditionalInfo);

        // =========================
        // VALIDATION
        // =========================

        if (title.isEmpty()) {

            etTitle.setError(
                    "Title is required"
            );

            etTitle.requestFocus();

            return;
        }

        if (dueDate.isEmpty()) {

            etDueDate.setError(
                    "Due date is required"
            );

            etDueDate.requestFocus();

            return;
        }

        // =========================
        // UPDATE EXISTING TASK
        // =========================

        currentTask.setTitle(title);

        currentTask.setDescription(description);

        currentTask.setDueDate(dueDate);

        currentTask.setPriority(selectedPriority);

        currentTask.setAdditionalInfo(
                additionalInfo
        );

        currentTask.setUpdatedAt(
                System.currentTimeMillis()
        );

        // =========================
        // SAVE UPDATE
        // =========================

        taskRepository.updateTask(currentTask);

        Toast.makeText(
                this,
                "Task updated successfully",
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }

    private String getTextValue(
            TextInputEditText editText) {

        if (editText.getText() == null) {
            return "";
        }

        return editText
                .getText()
                .toString()
                .trim();
    }
}