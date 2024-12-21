package com.example.smartschedule.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartschedule.R;
import com.example.smartschedule.data.AttendanceItem;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private List<AttendanceItem> attendanceList;

    public AttendanceAdapter(List<AttendanceItem> attendanceList) {
        this.attendanceList = attendanceList;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        AttendanceItem item = attendanceList.get(position);
        int percentage = item.getPercentage();
        holder.circularProgress.setProgress(item.getPercentage());
        holder.percentageText.setText(item.getPercentage() + "%");
        holder.subjectName.setText(item.getSubjectName());
        if (percentage < 85) {
            holder.circularProgress.setIndicatorColor(holder.itemView.getContext().getResources().getColor(R.color.error)); // Set to red
        } else {
            holder.circularProgress.setIndicatorColor(holder.itemView.getContext().getResources().getColor(R.color.colorPrimary)); // Set to green (or other color)
        }
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        CircularProgressIndicator circularProgress;
        TextView percentageText;
        TextView subjectName;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            circularProgress = itemView.findViewById(R.id.circularProgress);
            percentageText = itemView.findViewById(R.id.percentageText);
            subjectName = itemView.findViewById(R.id.subjectName);
        }
    }
}

