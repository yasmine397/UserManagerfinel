package com.example.usermanagementmodule.ui.library;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;

import com.example.usermanagementmodule.R;
import com.example.usermanagementmodule.book.Book;
import com.example.usermanagementmodule.ui.auth.LoginFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fragment that displays the user's personal book library
 */
public class LibraryFragment extends Fragment {
    private static final String TAG = "LibraryFragment";
    private RecyclerView recyclerView;
    private LibraryBookAdapter adapter;
    private List<Book> bookList = new ArrayList<>();
    private TextView emptyLibraryText;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LibraryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LibraryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LibraryFragment newInstance(String param1, String param2) {
        LibraryFragment fragment = new LibraryFragment();
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
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        // Get Firebase user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Check if user is logged in
        if (currentUser == null) {
            // User is not logged in, show login prompt instead of library
            return createLoginPromptView(inflater, container);
        }

        // User is logged in, show normal library view
        recyclerView = view.findViewById(R.id.library_recycler_view);
        emptyLibraryText = view.findViewById(R.id.empty_library_text);
        
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new LibraryBookAdapter(bookList);
        recyclerView.setAdapter(adapter);

        listenForLibraryBooks();
        return view;
    }

    /**
     * Creates a view with login prompt when user is not logged in
     */
    private View createLoginPromptView(LayoutInflater inflater, ViewGroup container) {
        View loginPromptView = inflater.inflate(R.layout.fragment_login_prompt, container, false);
        
        // If the layout doesn't exist yet, inflate a simple one with text and button
        if (loginPromptView == null) {
            // Fallback to creating a simple view if custom layout doesn't exist
            FrameLayout frameLayout = new FrameLayout(getContext());
            frameLayout.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            frameLayout.setBackgroundColor(Color.parseColor("#181818")); // Match app background

            LinearLayout loginPromptLayout = new LinearLayout(getContext());
            loginPromptLayout.setOrientation(LinearLayout.VERTICAL);
            loginPromptLayout.setGravity(Gravity.CENTER);
            loginPromptLayout.setPadding(32, 32, 32, 32);
            
            TextView messageText = new TextView(getContext());
            messageText.setText("Please login to view your library");
            messageText.setTextColor(Color.WHITE);
            messageText.setTextSize(18);
            messageText.setGravity(Gravity.CENTER);
            messageText.setPadding(0, 0, 0, 32);
            
            Button loginButton = new Button(getContext());
            loginButton.setText("Log In");
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
            
            loginPromptLayout.addView(messageText);
            loginPromptLayout.addView(loginButton);
            frameLayout.addView(loginPromptLayout, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER));
            
            return frameLayout;
        }
        
        // Set up the login button click listener
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

    private void loadLibraryBooks() {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                showEmptyLibrary();
                Log.d(TAG, "No current user, showing empty library");
                return;
            }

            Log.d(TAG, "Loading library books for user: " + currentUser.getUid());
            
            // Get the user document to access the books array
            db.collection("users")
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        bookList.clear();
                        
                        if (!documentSnapshot.exists()) {
                            Log.d(TAG, "User document doesn't exist");
                            showEmptyLibrary();
                            return;
                        }
                        
                        // Get the books array from the user document
                        Object booksObject = documentSnapshot.get("books");
                        
                        if (booksObject == null) {
                            Log.d(TAG, "Books field is null");
                            showEmptyLibrary();
                            return;
                        }
                        
                        if (!(booksObject instanceof List)) {
                            Log.e(TAG, "Books field is not a List: " + booksObject.getClass().getName());
                            showEmptyLibrary();
                            return;
                        }
                        
                        List<Object> booksList = (List<Object>) booksObject;
                        if (booksList.isEmpty()) {
                            Log.d(TAG, "Books list is empty");
                            showEmptyLibrary();
                            return;
                        }
                        
                        Log.d(TAG, "Found " + booksList.size() + " books in library");
                        
                        for (Object bookObj : booksList) {
                            try {
                                if (!(bookObj instanceof Map)) {
                                    Log.e(TAG, "Book entry is not a Map: " + bookObj.getClass().getName());
                                    continue;
                                }
                                
                                Map<String, Object> bookData = (Map<String, Object>) bookObj;
                                
                                // Create a book object from the data
                                Book book = new Book();
                                Log.d(TAG, "Processing book: " + bookData);
                                
                                // Get the book details
                                if (bookData.containsKey("title")) {
                                    book.setName((String) bookData.get("title"));
                                } else if (bookData.containsKey("name")) {
                                    book.setName((String) bookData.get("name"));
                                }
                                
                                if (bookData.containsKey("description")) {
                                    book.setDeseridsion((String) bookData.get("description"));
                                } else if (bookData.containsKey("deseridsion")) {
                                    book.setDeseridsion((String) bookData.get("deseridsion"));
                                }
                                
                                if (bookData.containsKey("coverUrl")) {
                                    book.setPhoto((String) bookData.get("coverUrl"));
                                } else if (bookData.containsKey("photo")) {
                                    book.setPhoto((String) bookData.get("photo"));
                                }
                                
                                if (bookData.containsKey("pdfUrl")) {
                                    book.setPdfUrl((String) bookData.get("pdfUrl"));
                                }
                                
                                // Set the bookId
                                String savedBookId = null;
                                if (bookData.containsKey("bookId")) {
                                    savedBookId = (String) bookData.get("bookId");
                                } else if (bookData.containsKey("id")) {
                                    savedBookId = (String) bookData.get("id");
                                }
                                
                                if (savedBookId != null && !savedBookId.isEmpty()) {
                                    book.setBookId(savedBookId);
                                } else {
                                    // Generate a fallback ID if none exists
                                    book.setBookId("book_" + bookList.size());
                                }
                                
                                // Only add book if it has a title
                                if (book.getName() != null && !book.getName().isEmpty()) {
                                    bookList.add(book);
                                    Log.d(TAG, "Added book to display list: " + book.getName());
                                } else {
                                    Log.d(TAG, "Skipped book with no title");
                                }
                            } catch (Exception e) {
                                // Log error but continue with other books
                                Log.e(TAG, "Error processing library book: " + e.getMessage(), e);
                            }
                        }
                        
                        // Update UI after all books are loaded
                        if (bookList.isEmpty()) {
                            Log.d(TAG, "No valid books after processing");
                            showEmptyLibrary();
                        } else {
                            Log.d(TAG, "Displaying " + bookList.size() + " books");
                            hideEmptyLibrary();
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading library: " + e.getMessage(), e);
                        showEmptyLibrary();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadLibraryBooks: " + e.getMessage(), e);
            showEmptyLibrary();
        }
    }
    
    private void showEmptyLibrary() {
        if (emptyLibraryText != null) {
            emptyLibraryText.setVisibility(View.VISIBLE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
    }
    private void listenForLibraryBooks() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            showEmptyLibrary();
            Log.d(TAG, "No current user, showing empty library");
            return;
        }

        db.collection("users")
                .document(currentUser.getUid())
                .addSnapshotListener((documentSnapshot, e) -> {
                    bookList.clear();

                    if (e != null || documentSnapshot == null || !documentSnapshot.exists()) {
                        showEmptyLibrary();
                        return;
                    }

                    Object booksObject = documentSnapshot.get("books");
                    if (booksObject == null || !(booksObject instanceof List)) {
                        showEmptyLibrary();
                        return;
                    }

                    List<Object> booksList = (List<Object>) booksObject;
                    if (booksList.isEmpty()) {
                        showEmptyLibrary();
                        return;
                    }

                    for (Object bookObj : booksList) {
                        try {
                            if (!(bookObj instanceof Map)) continue;
                            Map<String, Object> bookData = (Map<String, Object>) bookObj;
                            Book book = new Book();

                            if (bookData.containsKey("title")) {
                                book.setName((String) bookData.get("title"));
                            } else if (bookData.containsKey("name")) {
                                book.setName((String) bookData.get("name"));
                            }

                            if (bookData.containsKey("description")) {
                                book.setDeseridsion((String) bookData.get("description"));
                            } else if (bookData.containsKey("deseridsion")) {
                                book.setDeseridsion((String) bookData.get("deseridsion"));
                            }

                            if (bookData.containsKey("coverUrl")) {
                                book.setPhoto((String) bookData.get("coverUrl"));
                            } else if (bookData.containsKey("photo")) {
                                book.setPhoto((String) bookData.get("photo"));
                            }

                            if (bookData.containsKey("pdfUrl")) {
                                book.setPdfUrl((String) bookData.get("pdfUrl"));
                            }

                            String savedBookId = null;
                            if (bookData.containsKey("bookId")) {
                                savedBookId = (String) bookData.get("bookId");
                            } else if (bookData.containsKey("id")) {
                                savedBookId = (String) bookData.get("id");
                            }

                            if (savedBookId != null && !savedBookId.isEmpty()) {
                                book.setBookId(savedBookId);
                            } else {
                                book.setBookId("book_" + bookList.size());
                            }

                            if (book.getName() != null && !book.getName().isEmpty()) {
                                bookList.add(book);
                            }
                        } catch (Exception ex) {
                            // Log error but continue with other books
                        }
                    }

                    if (bookList.isEmpty()) {
                        showEmptyLibrary();
                    } else {
                        hideEmptyLibrary();
                        adapter.notifyDataSetChanged();
                    }
                });
    }
    private void hideEmptyLibrary() {
        if (emptyLibraryText != null) {
            emptyLibraryText.setVisibility(View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}