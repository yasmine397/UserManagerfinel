package com.example.usermanagementmodule.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Button;

import com.example.usermanagementmodule.R;
import com.example.usermanagementmodule.model.UserComment;
import com.example.usermanagementmodule.ui.auth.LoginFragment;
import com.example.usermanagementmodule.ui.comment.UserCommentAdapter;
import com.example.usermanagementmodule.ui.main.WelcomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserProfileFragment extends Fragment {
    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private ImageView profileImageView;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserProfileFragment newInstance(String param1, String param2) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    List<UserComment> userComments = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        
        // Check if user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // User not logged in, show login prompt
            return createLoginPromptView(inflater, container);
        }
        
        // User is logged in, show profile page
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        // Initialize profile image and set click listener
        profileImageView = view.findViewById(R.id.profile_image);
        profileImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        });

        // Add Sign Out button functionality
        Button signOutButton = view.findViewById(R.id.btnSignOut);
        if (signOutButton != null) {
            signOutButton.setOnClickListener(v -> {
                // Sign out from Firebase
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();
                
                // Navigate to WelcomeActivity (login screen)
                if (getActivity() != null) {
                    Intent intent = new Intent(getActivity(), WelcomeActivity.class);
                    // Clear the back stack
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finish();
                }
            });
        }

        // Initialize RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.comments_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userComments = new ArrayList<>();
        UserCommentAdapter adapter = new UserCommentAdapter(userComments);
        recyclerView.setAdapter(adapter);

        // Load user comments from Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("comments")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userComments.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        UserComment comment = doc.toObject(UserComment.class);
                        userComments.add(comment);
                    }
                    adapter.notifyDataSetChanged();
                });

        return view;
    }

    /**
     * Creates a view with login prompt when user is not logged in
     */
    private View createLoginPromptView(LayoutInflater inflater, ViewGroup container) {
        View loginPromptView = inflater.inflate(R.layout.fragment_login_prompt, container, false);
        
        // If the layout doesn't exist yet, create a simple one
        if (loginPromptView == null) {
            View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
            
            // We'll use the entire view as our root
            ViewGroup rootView = (ViewGroup) view;
            if (rootView != null) {
                // Clear any existing views
                rootView.removeAllViews();
                
                // Add login prompt
                View loginPrompt = inflater.inflate(R.layout.fragment_login_prompt, rootView, false);
                rootView.addView(loginPrompt);
            }
            
            return view;
        }
        
        // Set up login button click listener
        Button loginButton = loginPromptView.findViewById(R.id.loginButton);
        if (loginButton != null) {
            loginButton.setOnClickListener(v -> {
                // Navigate to login fragment
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new LoginFragment())
                            .addToBackStack(null)
                            .commit();
                }
            });
        }
        
        return loginPromptView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            // Here you can upload imageUri to Firebase Storage
            // For now, just show it in the ImageView:
            profileImageView.setImageURI(imageUri);
        }
    }
}