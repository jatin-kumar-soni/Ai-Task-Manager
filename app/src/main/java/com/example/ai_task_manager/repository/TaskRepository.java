package com.example.ai_task_manager.repository;

import android.content.Context;

import com.example.ai_task_manager.database.AppDatabase;
import com.example.ai_task_manager.database.TaskDao;
import com.example.ai_task_manager.model.Task;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {

    private final TaskDao taskDao;
    private final ExecutorService executorService;

    public TaskRepository(Context context) {

        AppDatabase database = AppDatabase.getInstance(context);

        taskDao = database.taskDao();

        executorService = Executors.newSingleThreadExecutor();
    }

    // =========================
    // CREATE
    // =========================

    public void insertTask(Task task) {

        executorService.execute(() -> {
            taskDao.insert(task);
        });
    }

    // =========================
    // READ ALL
    // =========================

    public void getAllTasks(TaskListCallback callback) {

        executorService.execute(() -> {

            List<Task> tasks = taskDao.getAllTasks();

            callback.onResult(tasks);
        });
    }

    // =========================
    // READ BY ID
    // =========================

    public void getTaskById(int id, TaskSingleCallback callback) {

        executorService.execute(() -> {

            Task task = taskDao.getTaskById(id);

            callback.onResult(task);
        });
    }

    // =========================
    // UPDATE
    // =========================

    public void updateTask(Task task) {

        executorService.execute(() -> {
            taskDao.update(task);
        });
    }

    // =========================
    // DELETE
    // =========================

    public void deleteTask(Task task) {

        executorService.execute(() -> {
            taskDao.delete(task);
        });
    }

    // =========================
    // SEARCH
    // =========================

    public void searchTasks(
            String query,
            TaskListCallback callback) {

        executorService.execute(() -> {

            List<Task> tasks = taskDao.searchTasks(query);

            callback.onResult(tasks);
        });
    }

    // =========================
    // FILTER BY PRIORITY
    // =========================

    public void getTasksByPriority(
            String priority,
            TaskListCallback callback) {

        executorService.execute(() -> {

            List<Task> tasks =
                    taskDao.getTasksByPriority(priority);

            callback.onResult(tasks);
        });
    }

    // =========================
    // FILTER BY STATUS
    // =========================

    public void getTasksByStatus(
            String status,
            TaskListCallback callback) {

        executorService.execute(() -> {

            List<Task> tasks =
                    taskDao.getTasksByStatus(status);

            callback.onResult(tasks);
        });
    }
    public void getPendingCount(CountCallback callback) {

        executorService.execute(() -> {

            int count = taskDao.getPendingCount();

            callback.onResult(count);
        });
    }


    public void getCompletedCount(CountCallback callback) {

        executorService.execute(() -> {

            int count = taskDao.getCompletedCount();

            callback.onResult(count);
        });
    }


    public void getOverdueCount(
            String today,
            CountCallback callback) {

        executorService.execute(() -> {

            int count = taskDao.getOverdueCount(today);

            callback.onResult(count);
        });
    }

    // =========================
    // CALLBACKS
    // =========================
    public interface CountCallback {

        void onResult(int count);
    }
    public interface TaskListCallback {

        void onResult(List<Task> tasks);
    }

    public interface TaskSingleCallback {

        void onResult(Task task);
    }
}