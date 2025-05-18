package com.example.usermanagementmodule.ui.main;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usermanagementmodule.utils.FirebaseServices;
import com.example.usermanagementmodule.R;
import com.example.usermanagementmodule.book.AddDataFragment;
import com.example.usermanagementmodule.book.BookListFragment;
import com.example.usermanagementmodule.model.User;
import com.example.usermanagementmodule.ui.auth.LoginFragment;
import com.squareup.picasso.Picasso;

/**
 * Home screen fragment that displays user information and navigation options.
 */
public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private TextView  tvUserGreeting;
    private ImageView ivUserPhoto;
    private Button btnViewBooks, btnAddBook, btnUserProfile, btnSignOut;
    private FirebaseServices fbs;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            Log.d(TAG, "onStart called in HomeFragment");
            
            // Initialize Firebase services
            fbs = FirebaseServices.getInstance();
            if (fbs == null) {
                Log.e(TAG, "Failed to initialize Firebase services in HomeFragment");
            }
            
            // Find views

            ivUserPhoto = getView().findViewById(R.id.ivUserPhoto);
            btnViewBooks = getView().findViewById(R.id.btnViewBooks);
            btnAddBook = getView().findViewById(R.id.btnAddBook);
            btnUserProfile = getView().findViewById(R.id.btnUserProfile);
            btnSignOut = getView().findViewById(R.id.btnSignOut);
            
            // Update UI with user data if available
            updateUserInfo();
            
            // Set button click listeners
            setupButtonListeners();
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing HomeFragment: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error loading home screen", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateUserInfo() {
        User user = fbs.getCurrentUser();
        if (user != null) {
            // Display user's name
            if (user.getUsername() != null && !user.getUsername().isEmpty()) {
                tvUserGreeting.setText("Hello, " + user.getUsername() + "!");
            }
            
            // Display user's profile photo if available
            if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                try {
                    Picasso.get().load(user.getImageUrl()).into(ivUserPhoto);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading user profile image: " + e.getMessage());
                }
            }
        }
    }
    
    private void setupButtonListeners() {
        // View All Books button
        btnViewBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToBookListScreen();
            }
        });
        
        // Add New Book button
        btnAddBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAddBookScreen();
            }
        });
        
        // User Profile button
        btnUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to user profile (add implementation for profile fragment later)
                Toast.makeText(getContext(), "Profile feature coming soon", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Sign Out button
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }
    
    private void goToBookListScreen() {
        try {
            Log.d(TAG, "Attempting to navigate to BookListFragment");
            if (getActivity() == null) {
                Log.e(TAG, "getActivity() returned null");
                return;
            }
            
            BookListFragment bookListFragment = new BookListFragment();
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, bookListFragment);
            ft.addToBackStack("HomeFragment");
            ft.commit();
            
            Log.d(TAG, "Successfully initiated transaction to BookListFragment");
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to Book List screen: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error opening book list", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void goToAddBookScreen() {
        try {
            if (getActivity() == null) {
                Log.e(TAG, "getActivity() returned null");
                return;
            }
            
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, new AddDataFragment());
            ft.addToBackStack(null);
            ft.commit();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to Add Book screen: " + e.getMessage(), e);
        }
    }
    
    private void signOut() {
        try {
            // Sign out from Firebase
            fbs.logout();
            
            // Navigate back to login screen
            if (getActivity() != null) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, new LoginFragment());
                ft.commit();
                
                Toast.makeText(getContext(), "Successfully signed out", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error signing out: " + e.getMessage(), e);
        }
    }
}