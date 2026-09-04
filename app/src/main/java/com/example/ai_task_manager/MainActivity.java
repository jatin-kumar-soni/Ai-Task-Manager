package com.example.ai_task_manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ai_task_manager.model.Task;
import com.example.ai_task_manager.repository.TaskRepository;
import com.example.ai_task_manager.ui.ai.AiTaskActivity;
import com.example.ai_task_manager.ui.task.AddTaskActivity;
import com.example.ai_task_manager.ui.task.TaskListActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvPendingCount;
    private TextView tvCompletedCount;
    private TextView tvOverdueCount;

    private MaterialButton btnAddTask;
    private MaterialButton btnCreateWithAI;
    private MaterialButton btnViewAll;

    private BottomNavigationView bottomNavigation;

    private LinearLayout recentTasksContainer;
    private View emptyState;

    private TaskRepository taskRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dashboard);

        initializeViews();

        taskRepository = new TaskRepository(this);

        setupNavigation();
    }

    private void initializeViews() {

        tvPendingCount =
                findViewById(R.id.tvPendingCount);

        tvCompletedCount =
                findViewById(R.id.tvCompletedCount);

        tvOverdueCount =
                findViewById(R.id.tvOverdueCount);

        btnAddTask =
                findViewById(R.id.btnAddTask);

        btnCreateWithAI =
                findViewById(R.id.btnCreateWithAI);

        btnViewAll =
                findViewById(R.id.btnViewAll);

        bottomNavigation =
                findViewById(R.id.bottomNavigation);

        recentTasksContainer =
                findViewById(R.id.recentTasksContainer);

        emptyState =
                findViewById(R.id.emptyState);
    }

    private void setupNavigation() {

        // Add Task
        btnAddTask.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            AddTaskActivity.class
                    );

            startActivity(intent);
        });

        // View All
        btnViewAll.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            TaskListActivity.class
                    );

            startActivity(intent);
        });

        // Create Task with AI
        btnCreateWithAI.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            AiTaskActivity.class
                    );

            startActivity(intent);
        });

        // Bottom Navigation
        bottomNavigation.setOnItemSelectedListener(item -> {

            int itemId =
                    item.getItemId();

            if (itemId == R.id.nav_home) {

                return true;

            } else if (itemId == R.id.nav_tasks) {

                Intent intent =
                        new Intent(
                                MainActivity.this,
                                TaskListActivity.class
                        );

                startActivity(intent);

                return true;

            } else if (itemId == R.id.nav_ai) {

                Intent intent =
                        new Intent(
                                MainActivity.this,
                                AiTaskActivity.class
                        );

                startActivity(intent);

                return true;
            }

            return false;
        });

        bottomNavigation.setSelectedItemId(
                R.id.nav_home
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (taskRepository != null) {
            loadDashboardData();
        }
    }

    private void loadDashboardData() {

        loadPendingCount();

        loadCompletedCount();

        loadOverdueCount();

        loadRecentTasks();
    }

    private void loadPendingCount() {

        taskRepository.getPendingCount(count -> {

            runOnUiThread(() -> {

                tvPendingCount.setText(
                        String.valueOf(count)
                );
            });
        });
    }

    private void loadCompletedCount() {

        taskRepository.getCompletedCount(count -> {

            runOnUiThread(() -> {

                tvCompletedCount.setText(
                        String.valueOf(count)
                );
            });
        });
    }

    private void loadOverdueCount() {

        Calendar calendar =
                Calendar.getInstance();

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                );

        String today =
                dateFormat.format(
                        calendar.getTime()
                );

        taskRepository.getOverdueCount(
                today,
                count -> {

                    runOnUiThread(() -> {

                        tvOverdueCount.setText(
                                String.valueOf(count)
                        );
                    });
                }
        );
    }

    /**
     * Load tasks for the Recent Tasks section.
     */
    private void loadRecentTasks() {

        taskRepository.getAllTasks(tasks -> {

            runOnUiThread(() -> {

                if (tasks == null ||
                        tasks.isEmpty()) {

                    showEmptyState();

                    return;
                }

                showRecentTasks(tasks);
            });
        });
    }

    /**
     * Display empty state when there are no tasks.
     */
    private void showEmptyState() {

        emptyState.setVisibility(
                View.VISIBLE
        );

        recentTasksContainer.setVisibility(
                View.GONE
        );
    }

    /**
     * Display recent tasks.
     */
    private void showRecentTasks(
            List<Task> tasks
    ) {

        emptyState.setVisibility(
                View.GONE
        );

        recentTasksContainer.setVisibility(
                View.VISIBLE
        );

        recentTasksContainer.removeAllViews();

        /*
         * Show maximum 3 tasks on dashboard.
         */
        int taskCount =
                Math.min(
                        tasks.size(),
                        3
                );

        for (int i = 0;
             i < taskCount;
             i++) {

            Task task =
                    tasks.get(i);

            addTaskCard(task);
        }
    }

    /**
     * Create a task card dynamically.
     */
    private void addTaskCard(Task task) {

        MaterialCardView card = new MaterialCardView(this);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                0,
                0,
                0,
                12
        );

        card.setLayoutParams(cardParams);

        // Theme-aware card appearance
        card.setRadius(18f);
        card.setCardElevation(0f);
        card.setStrokeWidth(1);
        card.setCardBackgroundColor(
                getColor(R.color.card_background)
        );
        card.setStrokeColor(
                getColor(R.color.border)
        );

        LinearLayout content = new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        content.setPadding(
                18,
                18,
                18,
                18
        );

        // -----------------------------
        // Task title
        // -----------------------------

        TextView title = new TextView(this);

        title.setText(
                task.getTitle()
        );

        title.setTextSize(17);

        title.setTextColor(
                getColor(R.color.text_primary)
        );

        title.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        title.setMaxLines(2);

        content.addView(title);


        // -----------------------------
        // Description
        // -----------------------------

        if (task.getDescription() != null &&
                !task.getDescription().trim().isEmpty()) {

            TextView description = new TextView(this);

            description.setText(
                    task.getDescription()
            );

            description.setTextSize(14);

            description.setTextColor(
                    getColor(R.color.text_secondary)
            );

            description.setMaxLines(4);

            description.setEllipsize(
                    android.text.TextUtils.TruncateAt.END
            );

            LinearLayout.LayoutParams descriptionParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            descriptionParams.setMargins(
                    0,
                    8,
                    0,
                    0
            );

            description.setLayoutParams(
                    descriptionParams
            );

            content.addView(description);
        }


        // -----------------------------
        // Bottom information
        // -----------------------------

        LinearLayout infoRow =
                new LinearLayout(this);

        infoRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        LinearLayout.LayoutParams infoParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        infoParams.setMargins(
                0,
                14,
                0,
                0
        );

        infoRow.setLayoutParams(infoParams);


        // Priority

        TextView priority =
                new TextView(this);

        priority.setText(
                "Priority: " +
                        task.getPriority()
        );

        priority.setTextSize(13);

        priority.setTextColor(
                getColor(R.color.text_secondary)
        );

        LinearLayout.LayoutParams priorityParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        priority.setLayoutParams(
                priorityParams
        );

        infoRow.addView(priority);


        // Due date

        TextView dueDate =
                new TextView(this);

        dueDate.setText(
                "Due: " +
                        task.getDueDate()
        );

        dueDate.setTextSize(13);

        dueDate.setTextColor(
                getColor(R.color.text_secondary)
        );

        infoRow.addView(dueDate);

        content.addView(infoRow);

        card.addView(content);


        // -----------------------------
        // Open Edit Task on click
        // -----------------------------

        card.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            com.example.ai_task_manager.ui.task.EditTaskActivity.class
                    );

            intent.putExtra(
                    "TASK_ID",
                    task.getId()
            );

            startActivity(intent);
        });

        recentTasksContainer.addView(card);
    }
}