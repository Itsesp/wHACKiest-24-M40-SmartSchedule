package com.example.smartschedule.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.smartschedule.R;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";  // Add a TAG for logging

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserPreferences", getContext().MODE_PRIVATE);

        String name = sharedPreferences.getString("name", null);
        String branch = sharedPreferences.getString("branch", null);
        String section = sharedPreferences.getString("section", null);
        String semester = sharedPreferences.getString("semester", null);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);


        if (toolbar != null) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.background));
            toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimary));
            toolbar.setTitle("Smart Schedule");
        }
        TextInputEditText edtName = view.findViewById(R.id.edtName);
        TextInputEditText edtBranch = view.findViewById(R.id.edtBranch);
        TextInputEditText edtSection = view.findViewById(R.id.edtSection);
        TextInputEditText edtSemester = view.findViewById(R.id.edtSemester);

        edtName.setText(name);
        edtBranch.setText(branch);
        edtSection.setText(section);
        edtSemester.setText(semester);

        return view;
    }
}
