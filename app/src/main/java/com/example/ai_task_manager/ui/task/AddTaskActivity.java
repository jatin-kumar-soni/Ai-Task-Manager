package com.example.ai_task_manager.ui.task;

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

public class AddTaskActivity extends AppCompatActivity {

    private TextInputEditText etTitle;
    private TextInputEditText etDescription;
    private TextInputEditText etDueDate;
    private TextInputEditText etAdditionalInfo;

    private MaterialButtonToggleGroup priorityToggleGroup;

    private MaterialButton btnLow;
    private MaterialButton btnMedium;
    private MaterialButton btnHigh;

    private MaterialButton btnSaveTask;
    private MaterialButton btnCancel;

    private TaskRepository taskRepository;

    private String selectedPriority = "MEDIUM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_task);

        initializeViews();

        taskRepository = new TaskRepository(this);

        setupPrioritySelection();

        setupDatePicker();

        setupButtons();
    }

    private void initializeViews() {

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etDueDate = findViewById(R.id.etDueDate);
        etAdditionalInfo = findViewById(R.id.etAdditionalInfo);

        priorityToggleGroup =
                findViewById(R.id.priorityGroup);

        btnLow = findViewById(R.id.btnLow);
        btnMedium = findViewById(R.id.btnMedium);
        btnHigh = findViewById(R.id.btnHigh);

        btnSaveTask = findViewById(R.id.btnSaveTask);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupPrioritySelection() {

        priorityToggleGroup.addOnButtonCheckedListener(
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

        // Default priority
        priorityToggleGroup.check(R.id.btnMedium);
    }

    private void setupDatePicker() {

        etDueDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {

                    Calendar selectedDate = Calendar.getInstance();

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
                            dateFormat.format(selectedDate.getTime())
                    );
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void setupButtons() {

        btnSaveTask.setOnClickListener(v -> saveTask());

        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveTask() {

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

            etTitle.setError("Title is required");
            etTitle.requestFocus();

            return;
        }

        if (dueDate.isEmpty()) {

            etDueDate.setError("Due date is required");
            etDueDate.requestFocus();

            return;
        }

        // =========================
        // CREATE TASK
        // =========================

        long currentTime =
                System.currentTimeMillis();

        Task task = new Task(
                title,
                description,
                dueDate,
                selectedPriority,
                "PENDING",
                additionalInfo,
                currentTime,
                currentTime
        );

        // =========================
        // SAVE TO ROOM
        // =========================

        taskRepository.insertTask(task);

        Toast.makeText(
                this,
                "Task saved successfully",
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }

    private String getTextValue(TextInputEditText editText) {

        if (editText.getText() == null) {
            return "";
        }

        return editText
                .getText()
                .toString()
                .trim();
    }
}