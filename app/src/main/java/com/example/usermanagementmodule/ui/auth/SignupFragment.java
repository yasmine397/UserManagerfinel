package com.example.usermanagementmodule.ui.auth;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usermanagementmodule.ui.main.HomeFragment;
import com.example.usermanagementmodule.utils.FirebaseServices;
import com.example.usermanagementmodule.R;
import com.example.usermanagementmodule.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignupFragment extends Fragment {

    private static final String TAG = "SignupFragment"; // Tag for logging
    
    private EditText etUsername, etPassword, etName, etPhone, etDateBirth;
    private Button btnSignup;
    private FirebaseServices fbs;
    private TextView tvLoginLink;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SignupFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SignupFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SignupFragment newInstance(String param1, String param2) {
        SignupFragment fragment = new SignupFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        // connecting components
        fbs = FirebaseServices.getInstance();
        etUsername = getView().findViewById(R.id.etUsernameSignup);
        etPassword = getView().findViewById(R.id.etPasswordSignup);
        etName = getView().findViewById(R.id.etNameSignup);
        etPhone = getView().findViewById(R.id.etPhoneSignup);
        etDateBirth = getView().findViewById(R.id.etDateBirthSignup);
        btnSignup = getView().findViewById(R.id.btnSignupSignup);
        tvLoginLink = getView().findViewById(R.id.tvLoginLink);
        
        // Set login link click listener
        tvLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoLoginFragment();
            }
        });
        
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Data validation
                String email = etUsername.getText().toString();
                String pass = etPassword.getText().toString();
                String name = etName.getText().toString();
                String phone = etPhone.getText().toString();
                String dateBirth = etDateBirth.getText().toString();
                
                if (email.trim().isEmpty() || pass.trim().isEmpty()) {
                    Toast.makeText(getActivity(), "Email and password cannot be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (pass.length() < 6) {
                    Toast.makeText(getActivity(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                Log.d(TAG, "Attempting to create user with email: " + email);
                
                // Signup procedure with Firebase Authentication
                fbs.getAuth().createUserWithEmailAndPassword(email, pass).addOnSuccessListener(
                        new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Log.d(TAG, "Firebase authentication successful. Creating user data in Firestore...");
                                
                                // Create a new user with a name, email, phone, etc.
                                String userId = fbs.getAuth().getCurrentUser().getUid();
                                
                                // Create DataUser object
                                User user = new User();
                                user.setEmail(email);
                                user.setUsername(name);
                                user.setPhoneNumber(phone);
                                user.setDateOfBirth(dateBirth);
                                user.setBooks(new ArrayList<>());
                                user.setImageUrl("");
                                
                                // Add user to Firestore
                                fbs.getFire().collection("users").document(userId)
                                        .set(user)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "User data saved to Firestore successfully. Navigating to home.");
                                                    Toast.makeText(getActivity(), "Account created successfully!", Toast.LENGTH_SHORT).show();
                                                    gotoHomeFragment();
                                                } else {
                                                    Log.e(TAG, "Failed to create user data: " + task.getException().getMessage());
                                                    Toast.makeText(getActivity(), "Failed to create user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        }
                ).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // if it fails
                        Log.e(TAG, "Authentication failed: " + e.getMessage());
                        Toast.makeText(getActivity(), "Authentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void gotoHomeFragment() {
        try {
            if (getActivity() == null) {
                Log.e(TAG, "getActivity() returned null. Fragment may be detached.");
                return;
            }
            
            if (!isAdded()) {
                Log.e(TAG, "Fragment is not currently added to its activity.");
                return;
            }
            
            Log.d(TAG, "Starting navigation to HomeFragment");
            
            // Ensure we're on the UI thread
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Create the fragment first to catch any initialization errors
                        HomeFragment homeFragment = new HomeFragment();
                        
                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.frameLayout2, homeFragment);
                        
                        // Add to back stack so user can navigate back
                        ft.addToBackStack(null);
                        
                        // Commit with allowing state loss to prevent IllegalStateException
                        ft.commitAllowingStateLoss();
                        
                        Log.d(TAG, "Successfully navigated to HomeFragment");
                    } catch (Exception e) {
                        Log.e(TAG, "Error creating or showing HomeFragment: " + e.getMessage(), e);
                        Toast.makeText(getActivity(), "Error navigating to home screen", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception in gotoHomeFragment: " + e.getMessage(), e);
            // Try to show a toast if we can
            if (getContext() != null) {
                Toast.makeText(getContext(), "An error occurred. Please try again.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void gotoLoginFragment() {
        try {
            if (getActivity() == null || !isAdded()) {
                Log.e(TAG, "Cannot navigate: fragment not properly attached to activity");
                return;
            }
            
            Log.d(TAG, "Navigating to LoginFragment");
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.frameLayout2, new LoginFragment());
            ft.commit();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to LoginFragment: " + e.getMessage(), e);
        }
    }
}