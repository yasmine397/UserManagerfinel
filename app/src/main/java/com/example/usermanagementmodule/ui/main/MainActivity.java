package com.example.usermanagementmodule.ui.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.usermanagementmodule.utils.FirebaseServices;
import com.example.usermanagementmodule.ui.library.LibraryFragment;
import com.example.usermanagementmodule.R;
import com.example.usermanagementmodule.ui.profile.UserProfileFragment;
import com.example.usermanagementmodule.book.BookDetail;
import com.example.usermanagementmodule.book.BookListFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Log.d("MainActivity", "onCreate started - initializing main activity");

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Set default fragment to BookListFragment instead of HomeFragment
        if (savedInstanceState == null) {
            Log.d("MainActivity", "Loading initial BookListFragment");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new BookListFragment())
                    .commit();
        }

        bottomNav.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                Log.d("MainActivity", "Navigation: Home tab selected, loading BookListFragment");
                selectedFragment = new BookListFragment(); // Changed to open BookListFragment instead of HomeFragment
            } else if (itemId == R.id.nav_library) {
                Log.d("MainActivity", "Navigation: Library tab selected, loading LibraryFragment");
                selectedFragment = new LibraryFragment();
            } else if (itemId == R.id.nav_profile) {
                Log.d("MainActivity", "Navigation: Profile tab selected, loading UserProfileFragment");
                selectedFragment = new UserProfileFragment();
            }
            
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

        // Hide bottom nav on BookDetail
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof BookDetail) {
                Log.d("MainActivity", "BookDetail fragment detected, hiding bottom navigation");
                bottomNav.setVisibility(View.GONE);
            } else {
                Log.d("MainActivity", "Non-detail fragment detected, showing bottom navigation");
                bottomNav.setVisibility(View.VISIBLE);
            }
        });
        
        Log.d("MainActivity", "onCreate completed");
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // Sign out using the enhanced FirebaseServices method
            FirebaseServices.getInstance().logoutAndRedirect(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
