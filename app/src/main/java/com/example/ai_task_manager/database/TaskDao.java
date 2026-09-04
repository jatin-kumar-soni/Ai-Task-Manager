package com.example.ai_task_manager.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ai_task_manager.model.Task;

import java.util.List;

@Dao
public interface TaskDao {

    // =========================
    // CREATE
    // =========================

    @Insert
    void insert(Task task);


    // =========================
    // UPDATE
    // =========================

    @Update
    void update(Task task);


    // =========================
    // DELETE
    // =========================

    @Delete
    void delete(Task task);


    // =========================
    // GET ALL TASKS
    // =========================

    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    List<Task> getAllTasks();


    // =========================
    // GET TASK BY ID
    // =========================

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    Task getTaskById(int id);


    // =========================
    // FILTER BY STATUS
    // =========================

    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY dueDate ASC")
    List<Task> getTasksByStatus(String status);


    // =========================
    // FILTER BY PRIORITY
    // =========================

    @Query("SELECT * FROM tasks WHERE priority = :priority ORDER BY dueDate ASC")
    List<Task> getTasksByPriority(String priority);


    // =========================
    // SEARCH
    // =========================

    @Query("SELECT * FROM tasks " +
            "WHERE title LIKE '%' || :searchQuery || '%' " +
            "ORDER BY dueDate ASC")
    List<Task> searchTasks(String searchQuery);


    // =========================
    // DASHBOARD COUNTS
    // =========================

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'PENDING'")
    int getPendingCount();


    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'COMPLETED'")
    int getCompletedCount();


    @Query("SELECT COUNT(*) FROM tasks " +
            "WHERE status = 'PENDING' " +
            "AND dueDate < :today")
    int getOverdueCount(String today);


    // =========================
    // DELETE ALL
    // =========================

    @Query("DELETE FROM tasks")
    void deleteAllTasks();
}