package com.example.smartschedule;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.smartschedule.database.AppDatabase;
import com.example.smartschedule.database.AppDatabaseProvider;
import com.example.smartschedule.database.TimetableEntry;
import com.example.smartschedule.fragment.ClubsFragment;
import com.example.smartschedule.fragment.EventFragment;
import com.example.smartschedule.fragment.HomeFragment;
import com.example.smartschedule.fragment.ProfileFragment;
import com.example.smartschedule.fragment.TimeTableFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

public class HomeActivity extends NavigationDrawer {
    private NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    private FirebaseAuth firebaseAuth;
    Toolbar toolbar;
    TextView txtUserName;
    LinearLayout llNav;
    CircularImageView imgUser;
    RelativeLayout mainView;
    private boolean doubleBackToExitPressedOnce = false;
    private AppDatabase database;
    private static final String TAG = "MainActivity";


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        setContentView(R.layout.activity_home);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        firebaseAuth = FirebaseAuth.getInstance();
        setLayout();
        loadFragment(new HomeFragment());
        database = AppDatabaseProvider.getDatabase(this);
        fetchTimetableFromFirebase();

    }
    private void setLayout() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mainView = findViewById(R.id.mainView);
        drawer = findViewById(R.id.drawer_layout);
        drawer.setScrimColor(Color.TRANSPARENT);

        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerClosed(View view) {
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                float scaleFactor = 6f;

                float slideX = drawerView.getWidth() * slideOffset;
                mainView.setTranslationX(slideX);
                mainView.setScaleX(1 - (slideOffset / scaleFactor));
                mainView.setScaleY(1 - (slideOffset / scaleFactor));
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String name = sharedPreferences.getString("name", null);
        String branch = sharedPreferences.getString("branch", null);
        String section = sharedPreferences.getString("section", null);
        String semester = sharedPreferences.getString("semester", null);
        Log.d(TAG, "Name: " + name + ", Branch: " + branch + ", Section: " + section + ", Semester: " + semester);


        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);

        View hView = navigationView.getHeaderView(0);
        SwitchMaterial switchBtn = hView.findViewById(R.id.switchBtn);
        txtUserName = hView.findViewById(R.id.txtUserName);
        imgUser = hView.findViewById(R.id.imgUser);
        llNav = hView.findViewById(R.id.llNav);
        if (name != null && branch != null && section != null && semester != null) {
            txtUserName.setText(name);
        } else {

            fetchDetailsFromFirestore();
            String name2 = sharedPreferences.getString("name", null);
            txtUserName.setText(name2);
        }

        llNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.closeDrawer(GravityCompat.START);
                loadFragment(new ProfileFragment());
            }
        });
        int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        switchBtn.setChecked(currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES);

        switchBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); // Enable dark mode
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // Enable light mode
            }
        });

    }

    public void loadFragment(Fragment fragment) {

        String backStateName = fragment.getClass().getName();
        String fragmentTag = backStateName;

        FragmentManager manager = getSupportFragmentManager();
        boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

        if (!fragmentPopped && manager.findFragmentByTag(fragmentTag) == null) { //fragment not in back stack, create it.
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(R.id.flContent, fragment, fragmentTag);
            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            ft.addToBackStack(backStateName);
            ft.commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void openDrawer() {
        if (!drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.openDrawer(GravityCompat.START);
        }
    }

    public void selectMenuItem(String menu) {
        if (menu.equalsIgnoreCase("Home")) {
            navigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
        }else if (menu.equalsIgnoreCase("Clubs")) {
            navigationView.getMenu().findItem(R.id.nav_clubs).setChecked(true);
        }else if (menu.equalsIgnoreCase("time_table")) {
            navigationView.getMenu().findItem(R.id.nav_time_table).setChecked(true);
        }else if (menu.equalsIgnoreCase("Events")) {
            navigationView.getMenu().findItem(R.id.nav_events).setChecked(true);
        }else {
            navigationView.getMenu().findItem(R.id.nav_clubs).setChecked(true);
            navigationView.getMenu().findItem(R.id.nav_home).setChecked(false);
            navigationView.getMenu().findItem(R.id.nav_time_table).setChecked(false);
            navigationView.getMenu().findItem(R.id.nav_events).setChecked(false);

        }
    }
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            if (doubleBackToExitPressedOnce) {
                finish();
                System.exit(0);
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
//
        } else {
            super.onBackPressed();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.e("Main", "OnResume");


        this.doubleBackToExitPressedOnce = false;

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.flContent);
        if (currentFragment instanceof HomeFragment) {
            selectMenuItem("home");
        }else if (currentFragment instanceof ClubsFragment) {
            selectMenuItem("clubs");
        }else if (currentFragment instanceof EventFragment) {
            selectMenuItem("events");
        }else if (currentFragment instanceof ProfileFragment) {
            selectMenuItem("profile");
        }else if (currentFragment instanceof TimeTableFragment) {
            selectMenuItem("time_table");
        }


    }
    public void removeAllPreferenceOnLogout() {
        try {
            firebaseAuth.signOut();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void fetchTimetableFromFirebase() {
        new Thread(() -> {
            List<TimetableEntry> existingEntries = database.timetableDao().getAllTimetable();
            if (existingEntries.isEmpty()) {

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("J");

                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot daySnapshot : dataSnapshot.getChildren()) {
                            String day = daySnapshot.getKey();
                            for (DataSnapshot subjectSnapshot : daySnapshot.getChildren()) {
                                Log.d(TAG, "Snapshot: " + subjectSnapshot.toString());
                                String subjectName = subjectSnapshot.getKey();
                                String startTime = subjectSnapshot.child("start").getValue(String.class);
                                String endTime = subjectSnapshot.child("end").getValue(String.class);

                                TimetableEntry timetableEntry = new TimetableEntry(day, subjectName, startTime, endTime);
                                insertTimetableEntry(timetableEntry);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Failed to read timetable data", databaseError.toException());
                    }
                });
            } else {
                Log.d(TAG, "Timetable already exists in Room Database");
            }
        }).start();
    }
    private void insertTimetableEntry(TimetableEntry entry) {
        new Thread(() -> {
            database.timetableDao().insert(entry);
        }).start();
    }
    private void fetchDetailsFromFirestore() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String usn = sharedPreferences.getString("usn", null);

        if (usn == null) {
            Toast.makeText(this, "USN not found in local storage!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference docRef = firestore.collection("usn").document(usn);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String name = document.getString("Name");
                    String branch = document.getString("Branch");
                    String section = document.getString("Section");
                    String semester = document.getString("Semester");
                    saveToLocal(name, branch, section, semester);
                    Log.d(TAG, "Name: " + name + ", Branch: " + branch + ", Section: " + section + ", Semester: " + semester);


                } else {
                    Toast.makeText(this, "Document not found in Firestore!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Error fetching data: " + task.getException(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void saveToLocal(String name, String branch, String section, String semester) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.putString("branch", branch);
        editor.putString("section", section);
        editor.putString("semester", semester);
        editor.apply();
    }

}