package com.example.ai_task_manager.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;

import com.example.ai_task_manager.model.Task;

@Database(
        entities = {Task.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends androidx.room.RoomDatabase {

    public abstract TaskDao taskDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {

        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {

                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "ai_task_manager_database"
                    ).build();
                }
            }
        }

        return INSTANCE;
    }
}