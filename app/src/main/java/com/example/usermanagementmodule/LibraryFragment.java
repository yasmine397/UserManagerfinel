package com.example.usermanagementmodule;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.usermanagementmodule.book.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LibraryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LibraryFragment extends Fragment {
    private RecyclerView recyclerView;
    private LibraryBookAdapter adapter;
    private List<Book> bookList = new ArrayList<>();

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

        recyclerView = view.findViewById(R.id.library_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3)); // 3 columns
        adapter = new LibraryBookAdapter(bookList);
        recyclerView.setAdapter(adapter);

        loadLibraryBooks();

        return view;
    }

    private void loadLibraryBooks() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        // Query the user_library collection directly
        db.collection("user_library")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            // Create a book object from the library data
                            Book book = new Book();
                            
                            // Get the book details
                            book.setName(doc.getString("title"));
                            book.setDeseridsion(doc.getString("description"));
                            book.setPhoto(doc.getString("coverUrl"));
                            book.setPdfUrl(doc.getString("pdfUrl"));
                            book.setStatus(doc.getString("status"));
                            
                            // Set the bookId from the document data
                            String savedBookId = doc.getString("bookId");
                            if (savedBookId != null && !savedBookId.isEmpty()) {
                                book.setBookId(savedBookId);
                            } else {
                                // Fallback to document ID without user ID prefix
                                String docId = doc.getId();
                                if (docId.contains("_")) {
                                    // Try to extract bookId from document ID (userId_bookId format)
                                    book.setBookId(docId.substring(docId.indexOf("_") + 1));
                                } else {
                                    book.setBookId(docId);
                                }
                            }
                            
                            bookList.add(book);
                        } catch (Exception e) {
                            // Log error but continue with other books
                            e.printStackTrace();
                        }
                    }
                    
                    // Update UI after all books are loaded
                    adapter.notifyDataSetChanged();
                    
                    // If we still have the old method data, add a fallback for backward compatibility
                    if (bookList.isEmpty()) {
                        loadLegacyLibraryBooks();
                    }
                })
                .addOnFailureListener(e -> {
                    // Fallback to old method in case of failure
                    e.printStackTrace();
                    loadLegacyLibraryBooks();
                });
    }

    /**
     * Legacy method to load library books for backward compatibility
     */
    private void loadLegacyLibraryBooks() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        db.collection("user_book_status")
                .whereEqualTo("userId", currentUser.getUid())
                .whereIn("status", Arrays.asList("want to read", "read"))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String bookId = doc.getString("bookId");
                        String status = doc.getString("status");
                        if (bookId != null) {
                            db.collection("books").document(bookId).get()
                                    .addOnSuccessListener(bookDoc -> {
                                        Book book = bookDoc.toObject(Book.class);
                                        if (book != null) {
                                            book.setStatus(status);
                                            book.setBookId(bookId);
                                            bookList.add(book);
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                        }
                    }
                });
    }
}