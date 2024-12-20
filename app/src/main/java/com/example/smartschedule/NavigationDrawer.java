package com.example.smartschedule;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.smartschedule.fragment.ClubsFragment;
import com.example.smartschedule.fragment.EventFragment;
import com.example.smartschedule.fragment.HomeFragment;
import com.example.smartschedule.fragment.ProfileFragment;
import com.example.smartschedule.fragment.TimeTableFragment;
import com.google.android.material.navigation.NavigationView;


public class NavigationDrawer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        drawer = findViewById(R.id.drawer_layout);

    }

    @Override
    public void onBackPressed() {
        drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            ((HomeActivity)NavigationDrawer.this).loadFragment(new HomeFragment());
        }else if (id == R.id.nav_clubs) {
            ((HomeActivity)NavigationDrawer.this).loadFragment(new ClubsFragment());
        }else if (id == R.id.nav_time_table) {
            ((HomeActivity)NavigationDrawer.this).loadFragment(new TimeTableFragment());
        }else if (id == R.id.nav_events) {
            ((HomeActivity)NavigationDrawer.this).loadFragment(new EventFragment());
        }else if (id == R.id.nav_logout) {
            ((HomeActivity) NavigationDrawer.this).removeAllPreferenceOnLogout();
        }

        drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
