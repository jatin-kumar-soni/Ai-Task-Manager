package com.example.ai_task_manager.ui.task;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_task_manager.R;
import com.example.ai_task_manager.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private final OnTaskActionListener listener;

    public TaskAdapter(OnTaskActionListener listener) {
        this.taskList = new ArrayList<>();
        this.listener = listener;
    }

    public void setTasks(List<Task> tasks) {
        this.taskList = tasks != null ? tasks : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);

        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull TaskViewHolder holder,
            int position) {

        Task task = taskList.get(position);

        // Title
        holder.tvTaskTitle.setText(task.getTitle());

        // Description
        if (task.getDescription() != null
                && !task.getDescription().trim().isEmpty()) {

            holder.tvTaskDescription.setText(task.getDescription());
            holder.tvTaskDescription.setVisibility(View.VISIBLE);

        } else {
            holder.tvTaskDescription.setVisibility(View.GONE);
        }

        // Priority
        holder.tvPriority.setText(task.getPriority());

        // Due date
        holder.tvDueDate.setText("Due: " + task.getDueDate());

        // Additional information
        if (task.getAdditionalInfo() != null
                && !task.getAdditionalInfo().trim().isEmpty()) {

            holder.tvAdditionalInfo.setText(task.getAdditionalInfo());
            holder.tvAdditionalInfo.setVisibility(View.VISIBLE);

        } else {
            holder.tvAdditionalInfo.setVisibility(View.GONE);
        }

        // Completed state
        holder.checkCompleted.setOnCheckedChangeListener(null);

        holder.checkCompleted.setChecked(
                "COMPLETED".equals(task.getStatus())
        );

        holder.checkCompleted.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (listener != null) {
                        listener.onTaskCompletionChanged(
                                task,
                                isChecked
                        );
                    }
                }
        );

        // More button
        holder.btnMore.setOnClickListener(v -> {

            if (listener != null) {
                listener.onMoreClicked(task, v);
            }
        });

        // Task click
        holder.itemView.setOnClickListener(v -> {

            if (listener != null) {
                listener.onTaskClicked(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        CheckBox checkCompleted;
        TextView tvTaskTitle;
        TextView tvTaskDescription;
        TextView tvPriority;
        TextView tvDueDate;
        TextView tvAdditionalInfo;
        ImageButton btnMore;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            checkCompleted =
                    itemView.findViewById(R.id.checkCompleted);

            tvTaskTitle =
                    itemView.findViewById(R.id.tvTaskTitle);

            tvTaskDescription =
                    itemView.findViewById(R.id.tvTaskDescription);

            tvPriority =
                    itemView.findViewById(R.id.tvPriority);

            tvDueDate =
                    itemView.findViewById(R.id.tvDueDate);

            tvAdditionalInfo =
                    itemView.findViewById(R.id.tvAdditionalInfo);

            btnMore =
                    itemView.findViewById(R.id.btnMore);
        }
    }

    public interface OnTaskActionListener {

        void onTaskClicked(Task task);

        void onMoreClicked(Task task, View anchor);

        void onTaskCompletionChanged(
                Task task,
                boolean isCompleted
        );
    }
}