package com.example.usermanagementmodule.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usermanagementmodule.ui.library.LibraryFragment;
import com.example.usermanagementmodule.utils.FirebaseServices;
import com.example.usermanagementmodule.R;
import com.example.usermanagementmodule.ui.profile.UserProfileFragment;
import com.example.usermanagementmodule.book.BookDetail;
import com.example.usermanagementmodule.model.User;
import com.example.usermanagementmodule.ui.main.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment"; // Tag for logging
    
    private EditText etUsername, etPassword, etForgot;
    private FirebaseServices fbs;
    private TextView tvSignupLink;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String pendingAction;
    private String pendingBookId;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
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
            // Check for any pending actions
            pendingAction = getArguments().getString("pendingAction");
            pendingBookId = getArguments().getString("pendingBookId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        //connecting components
        fbs = FirebaseServices.getInstance();
        etUsername = getView().findViewById(R.id.etUsernameLogin);
        etPassword = getView().findViewById(R.id.etPasswordLogin);
        Button btnLogin = getView().findViewById(R.id.btnLoginLogin);
        tvSignupLink = getView().findViewById(R.id.tvSignupLogin);
        TextView tvForgotLink = getView().findViewById(R.id.etForgot);
        
        // Check if user is already logged in
        if (fbs.getAuth().getCurrentUser() != null) {
            // User is already signed in, fetch their data
            loadUserData(fbs.getAuth().getCurrentUser().getUid());
        }
        
        tvForgotLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoForgotpassFragment();
            }
        });
        
        tvSignupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoSignupFragment();
            }
        });
        
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Data validation
                String email = etUsername.getText().toString();
                String pass = etPassword.getText().toString();
                
                if (email.trim().isEmpty() || pass.trim().isEmpty()) {
                    Toast.makeText(getActivity(), "Email and password cannot be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Login procedure
                fbs.getAuth().signInWithEmailAndPassword(email, pass).addOnSuccessListener(
                        new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Toast.makeText(getActivity(), "Login successful", Toast.LENGTH_SHORT).show();
                                // Get the user data from Firestore
                                String userId = fbs.getAuth().getCurrentUser().getUid();
                                loadUserData(userId);
                            }
                        }
                ).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    
    private void loadUserData(String userId) {
        fbs.getFire().collection("users").document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Convert to DataUser object
                            User user = documentSnapshot.toObject(User.class);
                            
                            // Set the current user in FirebaseServices
                            FirebaseServices.getInstance().setCurrentUser(user);
                            
                            // Handle any pending actions
                            if (pendingAction != null && getActivity() != null) {
                                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                
                                switch (pendingAction) {
                                    case "addToLibrary":
                                        // Return to the book detail page with the pending book ID
                                        if (pendingBookId != null) {
                                            // Try to find the book in Firestore and navigate to book detail
                                            fbs.getFire().collection("books")
                                                .whereEqualTo("name", pendingBookId)
                                                .limit(1)
                                                .get()
                                                .addOnSuccessListener(querySnapshot -> {
                                                    if (!querySnapshot.isEmpty()) {
                                                        // Got the book
                                                        DocumentSnapshot bookDoc = querySnapshot.getDocuments().get(0);
                                                        
                                                        // Create bundle with book data
                                                        Bundle args = new Bundle();
                                                        args.putString("bookId", pendingBookId);
                                                        args.putString("title", bookDoc.getString("name"));
                                                        args.putString("description", bookDoc.getString("deseridsion"));
                                                        args.putString("imageUrl", bookDoc.getString("photo"));
                                                        args.putString("pdfUrl", bookDoc.getString("pdfUrl"));
                                                        
                                                        // Navigate to book detail
                                                        BookDetail bookDetail = new BookDetail();
                                                        bookDetail.setArguments(args);
                                                        
                                                        ft.replace(R.id.fragment_container, bookDetail);
                                                        ft.commit();
                                                        return;
                                                    } else {
                                                        // Fallback to library if book not found
                                                        ft.replace(R.id.fragment_container, new LibraryFragment());
                                                        ft.commit();
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Fallback to library on error
                                                    ft.replace(R.id.fragment_container, new LibraryFragment());
                                                    ft.commit();
                                                });
                                        } else {
                                            // No book ID, go to library
                                            ft.replace(R.id.fragment_container, new LibraryFragment());
                                            ft.commit();
                                        }
                                        break;
                                    case "viewLibrary":
                                        // Go to library fragment
                                        ft.replace(R.id.fragment_container, new LibraryFragment());
                                        ft.commit();
                                        break;
                                    case "viewProfile":
                                        // Go to user profile
                                        ft.replace(R.id.fragment_container, new UserProfileFragment());
                                        ft.commit();
                                        break;
                                    default:
                                        // Default to book list
                                        gotoHomeFragment();
                                        break;
                                }
                            } else {
                                // No pending action, go to home
                                gotoHomeFragment();
                            }
                        } else {
                            Toast.makeText(getActivity(), "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Error loading user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void gotoHomeFragment() {
        try {
            if (getActivity() == null) {
                Log.e(TAG, "getActivity() returned null. Fragment may be detached.");
                return;
            }
            
            Log.d(TAG, "Navigating to BookListFragment after login");
            
            // Start MainActivity which will load BookListFragment by default
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            
            // Finish the current activity to prevent going back to login
            if (getActivity() != null) {
                getActivity().finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to BookListFragment: " + e.getMessage(), e);
        }
    }
    
    private void gotoAddBookFragment() {
        gotoHomeFragment();
    }

    private void gotoSignupFragment() {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameLayout2, new SignupFragment());
        ft.commit();
    }

    private void gotoForgotpassFragment() {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameLayout2, new ForgotFragment());
        ft.commit();
    }
}