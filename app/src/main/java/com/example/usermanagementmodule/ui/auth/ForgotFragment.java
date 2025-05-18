package com.example.usermanagementmodule.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.usermanagementmodule.utils.FirebaseServices;
import com.example.usermanagementmodule.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * A simple {@link Fragment} subclass for password reset functionality.
 */
public class ForgotFragment extends Fragment {

    private EditText etEmail;
    private Button btnReset, btnBack;
    private FirebaseServices fbs;

    public ForgotFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forgot, container, false);

    }

    @Override
    public void onStart() {
        super.onStart();
        
        // Connect components
        fbs = FirebaseServices.getInstance();
        etEmail = getView().findViewById(R.id.etEmailForgot);
        btnReset = getView().findViewById(R.id.btnResetForgot);
        btnBack = getView().findViewById(R.id.btnBackForgot);
        
        // Button listeners
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get email
                String email = etEmail.getText().toString().trim();
                
                // Validate email
                if (email.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter your email address", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Send password reset email
                fbs.getAuth().sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getActivity(), "Password reset email sent", Toast.LENGTH_SHORT).show();
                                    // Go back to login screen
                                    gotoLoginFragment();
                                } else {
                                    Toast.makeText(getActivity(), "Error sending password reset email: " + 
                                            task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoLoginFragment();
            }
        });
    }
    
    private void gotoLoginFragment() {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameLayout2, new LoginFragment());
        ft.commit();
    }
}