package com.example.smartschedule.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartschedule.R;
import com.example.smartschedule.data.Club;
import com.example.smartschedule.data.Event;

import java.util.List;

public class ClubAdapter extends RecyclerView.Adapter<ClubAdapter.ClubViewHolder> {
    private List<Club> clubList;
    private OnUpcomingEventClickListener onUpcomingEventClickListener;

    public ClubAdapter(List<Club> clubList, OnUpcomingEventClickListener onUpcomingEventClickListener) {
        this.clubList = clubList;
        this.onUpcomingEventClickListener = onUpcomingEventClickListener;
    }

    @NonNull
    @Override
    public ClubViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_club, parent, false);
        return new ClubViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClubViewHolder holder, int position) {
        Club club = clubList.get(position);
        holder.clubNameTextView.setText(club.getName());
        holder.clubDescriptionTextView.setText(club.getDescription());
        String imageUrl = club.getUrl(); // Assuming Club has an image URL
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.img) // Placeholder image
                .into(holder.clubImageView);
        Event upcomingEvent = club.getUpcomingEvent();
        if (upcomingEvent != null) {
            holder.upcomingEventButton.setVisibility(View.VISIBLE);
            holder.upcomingEventButton.setOnClickListener(v -> onUpcomingEventClickListener.onUpcomingEventClick(upcomingEvent));
        } else {
            holder.upcomingEventButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return clubList.size();
    }

    public static class ClubViewHolder extends RecyclerView.ViewHolder {
        TextView clubNameTextView;
        TextView clubDescriptionTextView;
        CardView upcomingEventButton;
        ImageView clubImageView;

        public ClubViewHolder(View itemView) {
            super(itemView);
            clubNameTextView = itemView.findViewById(R.id.clubName);
            clubDescriptionTextView = itemView.findViewById(R.id.clubDescription);
            upcomingEventButton = itemView.findViewById(R.id.upcomingEventButton);
            clubImageView = itemView.findViewById(R.id.clubImage);
        }
    }

    public interface OnUpcomingEventClickListener {
        void onUpcomingEventClick(Event event);
    }
}

