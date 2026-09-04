package com.example.ai_task_manager.ui.task;
import com.example.ai_task_manager.MainActivity;
import com.example.ai_task_manager.ui.ai.AiTaskActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_task_manager.R;
import com.example.ai_task_manager.model.Task;
import com.example.ai_task_manager.repository.TaskRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class TaskListActivity extends AppCompatActivity
        implements TaskAdapter.OnTaskActionListener {

    private RecyclerView recyclerTasks;
    //private LinearLayout emptyTaskState;
    private BottomNavigationView bottomNavigation;

    private TextInputEditText etSearch;

    private MaterialButton btnPriorityFilter;
    private MaterialButton btnStatusFilter;
    private MaterialButton btnAddTask;

    private View emptyTaskState;

    private android.widget.TextView tvTaskCount;

    private TaskAdapter taskAdapter;

    private TaskRepository taskRepository;

    private final List<Task> allTasks = new ArrayList<>();

    private String selectedPriority = "ALL";
    private String selectedStatus = "ALL";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_task_list);

        initializeViews();

        setupRecyclerView();

        taskRepository = new TaskRepository(this);

        setupSearch();

        setupFilters();

        setupAddTaskButton();
        setupBottomNavigation();
        loadTasks();
    }

    // =====================================================
    // INITIALIZE VIEWS
    // =====================================================

    private void initializeViews() {

        recyclerTasks =
                findViewById(R.id.recyclerTasks);

        etSearch =
                findViewById(R.id.etSearch);

        btnPriorityFilter =
                findViewById(R.id.btnPriorityFilter);

        btnStatusFilter =
                findViewById(R.id.btnStatusFilter);

        btnAddTask =
                findViewById(R.id.btnAddTask);

        emptyTaskState =
                findViewById(R.id.emptyTaskState);

        tvTaskCount =
                findViewById(R.id.tvTaskCount);
        bottomNavigation =
                findViewById(R.id.bottomNavigation);
    }


    // =====================================================
    // RECYCLER VIEW
    // =====================================================

    private void setupRecyclerView() {

        taskAdapter = new TaskAdapter(this);

        recyclerTasks.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerTasks.setAdapter(taskAdapter);
    }

    // =====================================================
    // LOAD TASKS
    // =====================================================

    private void loadTasks() {

        taskRepository.getAllTasks(tasks -> {

            runOnUiThread(() -> {

                allTasks.clear();

                if (tasks != null) {
                    allTasks.addAll(tasks);
                }

                applyFilters();
            });
        });
    }

    // =====================================================
    // SEARCH
    // =====================================================

    private void setupSearch() {

        etSearch.addTextChangedListener(
                new android.text.TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        searchQuery =
                                s.toString()
                                        .trim()
                                        .toLowerCase();

                        applyFilters();
                    }

                    @Override
                    public void afterTextChanged(
                            android.text.Editable s) {
                    }
                }
        );
    }

    // =====================================================
    // FILTERS
    // =====================================================

    private void setupFilters() {

        btnPriorityFilter.setOnClickListener(
                v -> showPriorityFilter()
        );

        btnStatusFilter.setOnClickListener(
                v -> showStatusFilter()
        );
    }

    // =====================================================
    // PRIORITY FILTER
    // =====================================================

    private void showPriorityFilter() {

        PopupMenu popupMenu =
                new PopupMenu(
                        this,
                        btnPriorityFilter
                );

        popupMenu.getMenu().add("All");

        popupMenu.getMenu().add("Low");

        popupMenu.getMenu().add("Medium");

        popupMenu.getMenu().add("High");

        popupMenu.setOnMenuItemClickListener(item -> {

            String selected =
                    item.getTitle()
                            .toString();

            if (selected.equals("All")) {

                selectedPriority = "ALL";

            } else {

                selectedPriority =
                        selected.toUpperCase();
            }

            btnPriorityFilter.setText(
                    "Priority: " + selected
            );

            applyFilters();

            return true;
        });

        popupMenu.show();
    }

    // =====================================================
    // STATUS FILTER
    // =====================================================

    private void showStatusFilter() {

        PopupMenu popupMenu =
                new PopupMenu(
                        this,
                        btnStatusFilter
                );

        popupMenu.getMenu().add("All");

        popupMenu.getMenu().add("Pending");

        popupMenu.getMenu().add("Completed");

        popupMenu.setOnMenuItemClickListener(item -> {

            String selected =
                    item.getTitle()
                            .toString();

            if (selected.equals("All")) {

                selectedStatus = "ALL";

            } else {

                selectedStatus =
                        selected.toUpperCase();
            }

            btnStatusFilter.setText(
                    "Status: " + selected
            );

            applyFilters();

            return true;
        });

        popupMenu.show();
    }

    // =====================================================
    // APPLY ALL FILTERS
    // =====================================================

    private void applyFilters() {

        List<Task> filteredTasks =
                new ArrayList<>();

        for (Task task : allTasks) {

            // -------------------------
            // Search filter
            // -------------------------

            boolean matchesSearch = true;

            if (!searchQuery.isEmpty()) {

                String title =
                        task.getTitle() == null
                                ? ""
                                : task.getTitle().toLowerCase();

                String description =
                        task.getDescription() == null
                                ? ""
                                : task.getDescription().toLowerCase();

                matchesSearch =
                        title.contains(searchQuery)
                                || description.contains(searchQuery);
            }

            // -------------------------
            // Priority filter
            // -------------------------

            boolean matchesPriority =
                    selectedPriority.equals("ALL")
                            || selectedPriority.equals(
                            task.getPriority()
                    );

            // -------------------------
            // Status filter
            // -------------------------

            boolean matchesStatus =
                    selectedStatus.equals("ALL")
                            || selectedStatus.equals(
                            task.getStatus()
                    );

            // -------------------------
            // Add if all match
            // -------------------------

            if (matchesSearch
                    && matchesPriority
                    && matchesStatus) {

                filteredTasks.add(task);
            }
        }

        taskAdapter.setTasks(filteredTasks);

        updateTaskCount(filteredTasks);

        updateEmptyState(filteredTasks);
    }

    // =====================================================
    // TASK COUNT
    // =====================================================

    private void updateTaskCount(
            List<Task> tasks) {

        int count =
                tasks == null
                        ? 0
                        : tasks.size();

        tvTaskCount.setText(
                count + " task" +
                        (count == 1 ? "" : "s")
        );
    }

    // =====================================================
    // EMPTY STATE
    // =====================================================

    private void updateEmptyState(
            List<Task> tasks) {

        boolean empty =
                tasks == null || tasks.isEmpty();

        if (empty) {

            emptyTaskState.setVisibility(
                    View.VISIBLE
            );

            recyclerTasks.setVisibility(
                    View.GONE
            );

        } else {

            emptyTaskState.setVisibility(
                    View.GONE
            );

            recyclerTasks.setVisibility(
                    View.VISIBLE
            );
        }
    }

    // =====================================================
    // ADD TASK
    // =====================================================

    private void setupAddTaskButton() {

        btnAddTask.setOnClickListener(v -> {

            Intent intent = new Intent(
                    TaskListActivity.this,
                    AddTaskActivity.class
            );

            startActivity(intent);
        });
    }

    // =====================================================
    // TASK CLICK
    // =====================================================

    @Override
    public void onTaskClicked(Task task) {

        openEditTask(task);
    }

    // =====================================================
    // MORE BUTTON
    // =====================================================

    @Override
    public void onMoreClicked(
            Task task,
            View anchor) {

        PopupMenu popupMenu =
                new PopupMenu(
                        this,
                        anchor
                );

        popupMenu.getMenu().add(
                "Edit Task"
        );

        popupMenu.getMenu().add(
                "Delete Task"
        );

        popupMenu.setOnMenuItemClickListener(
                item -> {

                    String selected =
                            item.getTitle()
                                    .toString();

                    if (selected.equals(
                            "Edit Task")) {

                        openEditTask(task);

                    } else if (selected.equals(
                            "Delete Task")) {

                        confirmDelete(task);
                    }

                    return true;
                }
        );

        popupMenu.show();
    }

    // =====================================================
    // EDIT
    // =====================================================

    private void openEditTask(Task task) {

        Intent intent =
                new Intent(
                        TaskListActivity.this,
                        EditTaskActivity.class
                );

        intent.putExtra(
                "TASK_ID",
                task.getId()
        );

        startActivity(intent);
    }

    // =====================================================
    // DELETE
    // =====================================================

    private void confirmDelete(Task task) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage(
                        "Are you sure you want to delete \"" +
                                task.getTitle() +
                                "\"?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> {

                            taskRepository.deleteTask(
                                    task
                            );

                            Toast.makeText(
                                    this,
                                    "Task deleted",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadTasks();
                        }
                )
                .show();
    }

    // =====================================================
    // COMPLETE / REOPEN
    // =====================================================

    @Override
    public void onTaskCompletionChanged(
            Task task,
            boolean isCompleted) {

        if (isCompleted) {

            task.setStatus("COMPLETED");

        } else {

            task.setStatus("PENDING");
        }

        task.setUpdatedAt(
                System.currentTimeMillis()
        );

        taskRepository.updateTask(task);

        Toast.makeText(
                this,
                isCompleted
                        ? "Task completed"
                        : "Task reopened",
                Toast.LENGTH_SHORT
        ).show();

        loadTasks();
    }

    // =====================================================
    // REFRESH WHEN RETURNING
    // =====================================================

    @Override
    protected void onResume() {

        super.onResume();

        if (taskRepository != null) {
            loadTasks();
        }
    }
    private void setupBottomNavigation() {

        bottomNavigation.setSelectedItemId(
                R.id.nav_tasks
        );

        bottomNavigation.setOnItemSelectedListener(
                item -> {

                    int itemId = item.getItemId();

                    if (itemId == R.id.nav_home) {

                        Intent intent = new Intent(
                                TaskListActivity.this,
                                MainActivity.class
                        );

                        startActivity(intent);

                        finish();

                        return true;

                    } else if (itemId == R.id.nav_tasks) {

                        return true;

                    } else if (itemId == R.id.nav_ai) {

                        Intent intent = new Intent(
                                TaskListActivity.this,
                                AiTaskActivity.class
                        );

                        startActivity(intent);

                        finish();

                        return true;
                    }

                    return false;
                }
        );
    }

}