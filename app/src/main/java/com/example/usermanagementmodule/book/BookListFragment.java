package com.example.usermanagementmodule.book;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usermanagementmodule.utils.FirebaseServices;
import com.example.usermanagementmodule.R;
import com.example.usermanagementmodule.ui.library.BookDownloadManager;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class BookListFragment extends Fragment implements BookGridAdapter.OnBookClickListener {
    private static final String TAG = "BookListFragment";
    private static final int STORAGE_PERMISSION_CODE = 101;

    private RecyclerView rvBooks;
    private ProgressBar progressBar;
    private TextView tvEmptyList;

    private FirebaseServices fbs;
    private ArrayList<Book> bookList;
    private BookGridAdapter adapter;
    private boolean dataLoaded = false;

    public BookListFragment() {
        // Required empty public constructor
        Log.d(TAG, "BookListFragment constructor called");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
        
        // Initialize data early
        fbs = FirebaseServices.getInstance();
        bookList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_book_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called");
        
        try {
            setupViews();
            setupRecyclerView();
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated: " + e.getMessage(), e);
            showToast("Error setting up book list: " + e.getMessage());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart called");
        
        // Load books in onStart to match MainActivity's lifecycle approach
        if (!dataLoaded) {
            loadBooksFromFirebase();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
    }

    private void setupViews() {
        Log.d(TAG, "setupViews started");
        if (getView() == null) {
            Log.e(TAG, "getView() returned null in setupViews");
            return;
        }
        
        rvBooks = getView().findViewById(R.id.rvBooks);
        progressBar = getView().findViewById(R.id.progressBar);
        tvEmptyList = getView().findViewById(R.id.tvEmptyList);
        
        // Set up sign out button
        Button btnSignOut = getView().findViewById(R.id.btnSignOut);
        if (btnSignOut != null) {
            btnSignOut.setOnClickListener(v -> {
                // Use the enhanced logout method which navigates to WelcomeActivity
                FirebaseServices.getInstance()
                    .logoutAndRedirect(getActivity());
            });
        }
        
        Log.d(TAG, "setupViews completed successfully");
    }

    private void setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView started");
        if (rvBooks == null) {
            Log.e(TAG, "rvBooks is null in setupRecyclerView");
            return;
        }
        
        // Use 2 columns for the grid
        int numberOfColumns = 2;
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), numberOfColumns);
        rvBooks.setLayoutManager(layoutManager);
        
        adapter = new BookGridAdapter(getContext(), bookList);
        adapter.setOnBookClickListener(this);
        rvBooks.setAdapter(adapter);
        
        Log.d(TAG, "setupRecyclerView completed successfully");
    }

    private void loadBooksFromFirebase() {
        showLoading(true);
        Log.d(TAG, "Starting to load books from Firebase");
        
        // Check if we have direct access to Firestore, regardless of login status
        FirebaseFirestore db;
        if (fbs == null || fbs.getFire() == null) {
            // Fallback to direct Firestore instance if FirebaseServices isn't available
            Log.d(TAG, "Using direct Firestore instance as fallback");
            db = FirebaseFirestore.getInstance();
        } else {
            db = fbs.getFire();
        }
        
        // Query books collection - no authentication required for reading public data
        CollectionReference booksRef = db.collection("books");
        Log.d(TAG, "Querying 'books' collection from Firestore");
        
        booksRef.get().addOnCompleteListener(task -> {
            showLoading(false);
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                Log.d(TAG, "Firebase query completed successfully");
                
                if (querySnapshot != null) {
                    Log.d(TAG, "QuerySnapshot is not null, document count: " + querySnapshot.size());
                    
                    bookList.clear();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Log.d(TAG, "Processing document ID: " + document.getId());
                        
                        try {
                            Map<String, Object> data = document.getData();
                            if (data != null) {
                                Log.d(TAG, "Document data: " + data);
                            }
                            
                            Book book = document.toObject(Book.class);
                            if (book != null) {
                                // Make sure the book has its ID set for future reference
                                book.setBookId(document.getId());
                                Log.d(TAG, "Book converted successfully: " + book.getName());
                                bookList.add(book);
                            } else {
                                Log.e(TAG, "Book conversion returned null for document: " + document.getId());
                                
                                // Manual conversion as fallback
                                try {
                                    if (data != null) {
                                        String name = (String) data.get("name");
                                        String date = (String) data.get("realestDate");
                                        String desc = (String) data.get("deseridsion");
                                        String lang = (String) data.get("booklan");
                                        String photo = (String) data.get("photo");
                                        String pdfUrl = (String) data.get("pdfUrl");
                                        
                                        Book manualBook = new Book(name, date, desc, lang, photo);
                                        manualBook.setBookId(document.getId()); // Set the document ID as book ID
                                        if (pdfUrl != null && !pdfUrl.isEmpty()) {
                                            manualBook.setPdfUrl(pdfUrl);
                                        }
                                        Log.d(TAG, "Manually created book: " + manualBook);
                                        bookList.add(manualBook);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Manual conversion failed: " + e.getMessage());
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error converting document to Book: " + e.getMessage(), e);
                            Log.d(TAG, "Document data that failed: " + document.getData());
                        }
                    }
                    
                    Log.d(TAG, "Final book list size: " + bookList.size());
                    adapter.notifyDataSetChanged();
                    
                    if (bookList.isEmpty()) {
                        Log.d(TAG, "Book list is empty, showing empty view");
                        showEmptyView(true);
                    } else {
                        Log.d(TAG, "Book list has items, hiding empty view");
                        showEmptyView(false);
                    }
                    
                    dataLoaded = true;
                } else {
                    Log.d(TAG, "QuerySnapshot is null");
                    showEmptyView(true);
                }
            } else {
                Exception exception = task.getException();
                Log.e(TAG, "Error getting books: " + (exception != null ? exception.getMessage() : "Unknown error"), exception);
                
                // Check if it's a FirebaseFirestoreException to get more details
                if (exception instanceof FirebaseFirestoreException) {
                    FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) exception;
                    Log.e(TAG, "Firestore error code: " + firestoreException.getCode());
                }
                
                showToast("Error loading books: " + (exception != null ? exception.getMessage() : "Unknown error"));
                showEmptyView(true);
            }
        });
    }

    @Override
    public void onBookClick(int position, Book book) {
        // Check if we have permission to write to external storage
        if (checkStoragePermission()) {
            // Download the book
            BookDownloadManager.downloadBook(getContext(), book);
        } else {
            // Request permission
            requestStoragePermission();
        }
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToast("Storage permission granted. You can download books now.");
            } else {
                showToast("Storage permission denied. Cannot download books.");
            }
        }
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (rvBooks != null) {
            rvBooks.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        }
    }

    private void showEmptyView(boolean isEmpty) {
        if (tvEmptyList != null) {
            tvEmptyList.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        if (rvBooks != null) {
            rvBooks.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
} 