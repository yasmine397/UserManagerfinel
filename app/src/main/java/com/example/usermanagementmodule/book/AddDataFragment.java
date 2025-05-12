package com.example.usermanagementmodule.book;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usermanagementmodule.Main.sampledata.FirebaseServices;
import com.example.usermanagementmodule.HomeFragment;
import com.example.usermanagementmodule.R;
import com.example.usermanagementmodule.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.squareup.picasso.Picasso;

/**
 * Fragment for adding new books to the collection
 */
public class AddDataFragment extends Fragment {
    private static final String TAG = "AddDataFragment"; // Tag for logging
    private static final int GALLARY_REQUEST_CODE = 123;
    private EditText etBName, etBDate, etBDeserid, etBLan, etImageUrl, etPdfUrl;
    private Button btnAdd;
    private FirebaseServices fbs;
    private TextView Start;
    private ImageView img;
    private Utils msg;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AddDataFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static AddDataFragment newInstance(String param1, String param2) {
        AddDataFragment fragment = new AddDataFragment();
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
        Log.d(TAG, "onCreateView: Inflating layout");
        return inflater.inflate(R.layout.fragment_add_book, container, false);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Fragment started");
        try {
            connectComponents();
            Log.d(TAG, "onStart: Components connected successfully");
        } catch (Exception e) {
            Log.e(TAG, "onStart: Error connecting components", e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error initializing screen: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void connectComponents() {
        try {
            Log.d(TAG, "connectComponents: Finding views...");
            
            if (getActivity() == null) {
                Log.e(TAG, "connectComponents: getActivity is null");
                return;
            }
            
            if (getView() == null) {
                Log.e(TAG, "connectComponents: getView is null");
                return;
            }
            
            // Use getView instead of getActivity to find views
            etBName = getView().findViewById(R.id.etMainAddBookN);
            etBDate = getView().findViewById(R.id.etrealestDate);
            etBDeserid = getView().findViewById(R.id.etMainDeseridsion);
            etBLan = getView().findViewById(R.id.etMainBookLan);
            etImageUrl = getView().findViewById(R.id.etImageUrl);
            etPdfUrl = getView().findViewById(R.id.etPdfUrl);
            btnAdd = getView().findViewById(R.id.btnAdd);
            img = getView().findViewById(R.id.ivBookImage);
            Start = getView().findViewById(R.id.tvStart);
            
            fbs = FirebaseServices.getInstance();
            
            Log.d(TAG, "connectComponents: Views found successfully");
            
            // Set click listener to preview the image URL
            if (etImageUrl != null) {
                etImageUrl.setOnFocusChangeListener((v, hasFocus) -> {
                    if (!hasFocus) {
                        previewImageFromUrl();
                    }
                });
            }

            if (btnAdd != null) {
                btnAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addBook();
                    }
                });
            } else {
                Log.e(TAG, "connectComponents: Button (btnAdd) is null");
            }

            if (Start != null) {
                Start.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        navigateToHome();
                    }
                });
            } else {
                Log.e(TAG, "connectComponents: TextView (Start) is null");
            }
            
            if (img != null) {
                img.setOnClickListener(v -> previewImageFromUrl());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "connectComponents: Error connecting components", e);
            // Rethrow to be caught in onStart
            throw e;
        }
    }
    
    private void previewImageFromUrl() {
        if (etImageUrl != null && img != null) {
            String imageUrl = etImageUrl.getText().toString().trim();
            if (!imageUrl.isEmpty()) {
                try {
                    // Show loading preview
                    img.setImageResource(R.drawable.ic_book_placeholder);
                    // Load image from URL using Picasso
                    Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_book_placeholder)
                        .error(R.drawable.ic_book_placeholder)
                        .into(img);
                    showProgressMessage("Image preview loaded");
                } catch (Exception e) {
                    Log.e(TAG, "Error loading image preview: " + e.getMessage());
                    img.setImageResource(R.drawable.ic_book_placeholder);
                }
            }
        }
    }
    
    private void addBook() {
        try {
            if (etBName == null || etBDate == null || etBDeserid == null || etBLan == null || etImageUrl == null) {
                Log.e(TAG, "addBook: One or more EditText fields are null");
                return;
            }
            
            String name = etBName.getText().toString().trim();
            String date = etBDate.getText().toString().trim();
            String deserid = etBDeserid.getText().toString().trim();
            String blan = etBLan.getText().toString().trim();
            String imageUrl = etImageUrl.getText().toString().trim();
            String pdfUrl = etPdfUrl != null ? etPdfUrl.getText().toString().trim() : "";

            if (name.isEmpty() || date.isEmpty() || deserid.isEmpty() || blan.isEmpty()) {
                Toast.makeText(getActivity(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }
            
            showProgressMessage("Adding book...");
            
            // Save the book directly with the provided image URL and PDF URL
            saveBookToFirestore(name, date, deserid, blan, imageUrl, pdfUrl);
            
        } catch (Exception e) {
            Log.e(TAG, "addBook: Error adding book", e);
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void saveBookToFirestore(String name, String date, String deserid, String blan, String photoUrl, String pdfUrl) {
        Book book = new Book(name, date, deserid, blan, photoUrl, pdfUrl);
        
        fbs.getFire().collection("books").add(book)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Book added successfully with ID: " + documentReference.getId());
                        Toast.makeText(getActivity(), "Book added successfully", Toast.LENGTH_SHORT).show();
                        clearFields();
                        navigateToHome();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to add book", e);
                        Toast.makeText(getActivity(), "Failed to add book: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void showProgressMessage(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void navigateToHome() {
        try {
            if (getActivity() == null) {
                Log.e(TAG, "navigateToHome: getActivity is null");
                return;
            }
            
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.frameLayout2, new HomeFragment());
            transaction.commit();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to HomeFragment", e);
        }
    }
    
    private void clearFields() {
        if (etBName != null) etBName.setText("");
        if (etBDate != null) etBDate.setText("");
        if (etBDeserid != null) etBDeserid.setText("");
        if (etBLan != null) etBLan.setText("");
        if (etImageUrl != null) etImageUrl.setText("");
        if (img != null) img.setImageResource(R.drawable.ic_book_placeholder);
    }
}



