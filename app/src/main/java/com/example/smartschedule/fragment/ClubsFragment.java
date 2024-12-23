package com.example.smartschedule.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartschedule.R;
import com.example.smartschedule.adapter.ClubAdapter;
import com.example.smartschedule.data.Club;
import com.example.smartschedule.data.Event;
import com.example.smartschedule.manager.NotificationHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ClubsFragment extends Fragment implements ClubAdapter.OnUpcomingEventClickListener {

    private RecyclerView recyclerView;
    private ClubAdapter clubAdapter;
    private List<Club> clubList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_clubs, container, false);
        recyclerView = rootView.findViewById(R.id.clubRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

        if (toolbar != null) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.background));
            toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimary));
            toolbar.setTitle("Clubs");
        }
        clubAdapter = new ClubAdapter(clubList, this);
        recyclerView.setAdapter(clubAdapter);

        fetchClubDetails();

        return rootView;
    }

    private void fetchClubDetails() {
        db.collection("Clubs")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getId();
                            String description = document.getString("description");
                            String url = document.getString("url");
                            List<Event> events = new ArrayList<>();

                            // Fetch events for each club
                            CollectionReference eventsRef = db.collection("Clubs").document(name).collection("events");
                            eventsRef.get()
                                    .addOnCompleteListener(eventsTask -> {
                                        if (eventsTask.isSuccessful()) {
                                            for (QueryDocumentSnapshot eventDoc : eventsTask.getResult()) {
                                                String eventName = eventDoc.getString("event");
                                                Timestamp eventDate = eventDoc.getTimestamp("date");
                                                String eventDescription = eventDoc.getString("description");
                                                Event event = new Event(eventName, eventDate,eventDescription);
                                                Log.d("Clubs",eventDate.toDate().toString());
                                                events.add(event);
                                            }

                                            Club club = new Club(name, description,url, events);
                                            clubList.add(club);
                                            clubAdapter.notifyDataSetChanged();
                                        }
                                    });
                        }
                    }
                });
    }

    @Override
    public void onUpcomingEventClick(Event event) {
        Dialog bottomDialog = new Dialog(getContext());

        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_event_details_bottom_sheet, null);

        TextView eventNameTextView = view.findViewById(R.id.eventNameTextView);
        TextView eventDateTextView = view.findViewById(R.id.eventDateTextView);
        TextView eventDescription = view.findViewById(R.id.eventDescription);
        ImageView iconNotify = view.findViewById(R.id.notifyIcon);
        eventDescription.setText(event.getEventDescription());
        iconNotify.setOnClickListener(v -> {
            if (event.getEventDate() != null) {
                long eventTime = event.getEventDate().toDate().getTime();
                String title = event.getEventName(); // Use the event name as the title
                String message = "Reminder: " + event.getEventName() + " is coming up!"; // Custom message

                // Schedule notification
                NotificationHelper.scheduleNotification(getContext(), title, message, eventTime);
            } else {
                // Handle case where the event date is missing
                Toast.makeText(getContext(), "Event date is missing", Toast.LENGTH_SHORT).show();
            }
        });

        eventNameTextView.setText(event.getEventName());
        eventDateTextView.setText("Date: " + event.getFormattedEventDate());

        bottomDialog.setContentView(view);

        Window window = bottomDialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
        }

        bottomDialog.show();
    }


}
