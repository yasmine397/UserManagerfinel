package com.example.usermanagementmodule.book;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usermanagementmodule.Comment;
import com.example.usermanagementmodule.CommentAdapter;
import com.example.usermanagementmodule.R;
import com.example.usermanagementmodule.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;
import com.example.usermanagementmodule.utils.BookDownloadManager;
import com.example.usermanagementmodule.pdf.PdfViewerActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.util.Log;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BookDetail#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BookDetail extends Fragment {
    private EditText commentEditText;
    private Button sendButton, readButton;
    private RecyclerView commentsRecyclerView;
    private CommentAdapter commentAdapter;
    private ArrayList<Comment> commentList;
    private RatingBar ratingBarAverage, ratingBarUser;
    private TextView ratingValueText;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String bookId;
    private Spinner bookStatusSpinner;
    private String pdfUrl; // To store the PDF URL
    private String currentBookStatus;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public BookDetail() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BookDetail.
     */
    // TODO: Rename and change types and number of parameters
    public static BookDetail newInstance(String param1, String param2) {
        BookDetail fragment = new BookDetail();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_detail, container, false);

        // Book cover
        ImageView bookCover = view.findViewById(R.id.bookCover);

        // Get arguments
        Bundle args = getArguments();
        if (args != null) {
            bookId = args.getString("bookId");
            String title = args.getString("title");
            
            // If bookId is null, use the title as a fallback identifier
            if (bookId == null || bookId.isEmpty()) {
                bookId = title; // Using title as identifier if bookId is not available
            }
            
            // Check for both possible parameter names for the cover URL
            String coverUrl = args.getString("coverUrl");
            if (coverUrl == null || coverUrl.isEmpty()) {
                coverUrl = args.getString("imageUrl"); // Alternative parameter name
            }
            String description = args.getString("description");
            pdfUrl = args.getString("pdfUrl"); // Get the PDF URL from arguments
            String initialStatus = args.getString("status"); // Get initial status if available
            
            TextView bookTitleView = view.findViewById(R.id.bookTitle);
            TextView bookDescView = view.findViewById(R.id.bookDescription);
            
            if (bookTitleView != null) bookTitleView.setText(title);
            if (bookDescView != null) {
                // Handle null or empty description
                if (description == null || description.isEmpty()) {
                    description = "No description available.";
                } else {
                    // Format the description text
                    description = formatDescription(description);
                }
                
                bookDescView.setText(description);
                
                // Set up a click listener to toggle description expansion if it's long
                final String fullDescription = description;
                if (description.length() > 200) {
                    bookDescView.setOnClickListener(v -> {
                        TextView tv = (TextView) v;
                        if (tv.getMaxLines() == 15) {
                            // Expand
                            tv.setMaxLines(Integer.MAX_VALUE);
                            tv.setEllipsize(null);
                        } else {
                            // Collapse
                            tv.setMaxLines(15);
                            tv.setEllipsize(android.text.TextUtils.TruncateAt.END);
                        }
                    });
                    
                    // Add a hint that it's expandable
                    bookDescView.append("\n\n(Tap to expand)");
                }
                
                // Enable text view to be scrollable for long content
                bookDescView.setMovementMethod(android.text.method.ScrollingMovementMethod.getInstance());
            }
            if (coverUrl != null && !coverUrl.isEmpty()) {
                Picasso.get().load(coverUrl).into(bookCover);
            }
            
            // Remember the initial status if provided
            if (initialStatus != null && !initialStatus.isEmpty()) {
                // We'll use this later to set the spinner selection
                currentBookStatus = initialStatus;
            }
        }

        // Initialize read button
        readButton = view.findViewById(R.id.readButton);
        readButton.setOnClickListener(v -> openPdfViewer());
        
        // If no PDF URL is available, disable the read button
        if (pdfUrl == null || pdfUrl.isEmpty()) {
            readButton.setEnabled(false);
            readButton.setText("No PDF Available");
        }

        // Comments
        commentEditText = view.findViewById(R.id.commentInput);
        sendButton = view.findViewById(R.id.buttonSend);
        commentsRecyclerView = view.findViewById(R.id.recyclerViewComments);

        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(commentList);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentsRecyclerView.setAdapter(commentAdapter);

        bookStatusSpinner = view.findViewById(R.id.book_status_spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.book_status_options,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bookStatusSpinner.setAdapter(spinnerAdapter);
        
        // Set the initial selection if we have status from arguments
        if (currentBookStatus != null && !currentBookStatus.isEmpty()) {
            int position = getStatusPosition(currentBookStatus, spinnerAdapter);
            if (position >= 0) {
                bookStatusSpinner.setSelection(position);
            }
        }

        bookStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedStatus = parent.getItemAtPosition(position).toString();
                saveBookStatus(selectedStatus);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // Ratings
        ratingBarUser = view.findViewById(R.id.ratingBarUser);
        ratingBarAverage = view.findViewById(R.id.ratingBarAverage);
        ratingValueText = view.findViewById(R.id.ratingValueText);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Load average rating
        loadAverageRating();
        
        // Load comments
        loadComments();

        // Send comment
        sendButton.setOnClickListener(v -> {
            if (currentUser != null) {
                String username = currentUser.getDisplayName();
                String imageUrl = currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "";
                String commentText = commentEditText.getText().toString().trim();

                if (!commentText.isEmpty()) {
                    User user = new User(username, imageUrl);
                    Comment newComment = new Comment(user, commentText);

                    // Save to Firestore
                    Map<String, Object> commentData = new HashMap<>();
                    commentData.put("userId", currentUser.getUid());
                    commentData.put("userName", username);
                    commentData.put("userPhotoUrl", imageUrl);
                    commentData.put("bookId", bookId);
                    commentData.put("commentText", commentText);
                    commentData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

                    db.collection("comments")
                            .add(commentData)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(getContext(), "Comment added!", Toast.LENGTH_SHORT).show();
                                commentEditText.setText("");
                                loadComments();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to add comment.", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(getContext(), "Please enter a comment", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Please log in first", Toast.LENGTH_SHORT).show();
            }
        });

        // User rating
        ratingBarUser.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser && currentUser != null) {
                Map<String, Object> ratingData = new HashMap<>();
                ratingData.put("bookId", bookId);
                ratingData.put("userId", currentUser.getUid());
                ratingData.put("rating", (int) rating);

                db.collection("ratings")
                        .document(bookId + "_" + currentUser.getUid())
                        .set(ratingData, SetOptions.merge())
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(getContext(), "Rating submitted!", Toast.LENGTH_SHORT).show();
                            loadAverageRating();
                        });
            }
        });
        loadBookStatus();

        return view;
    }

    private void saveBookStatus(String status) {
        Log.d("BookDetail", "Saving status: " + status + " for bookId: " + bookId);
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && bookId != null) {
            // Sanitize the bookId by replacing spaces and special characters
            String sanitizedBookId = bookId.replace(" ", "_").replace("/", "_");
            
            // Create a document ID that's consistent and unique per user
            String documentId = currentUser.getUid() + "_" + sanitizedBookId;
            Log.d("BookDetail", "Using document ID: " + documentId + " for status update");
            
            Map<String, Object> statusData = new HashMap<>();
            statusData.put("userId", currentUser.getUid());
            statusData.put("bookId", sanitizedBookId);
            statusData.put("status", status);
            statusData.put("updatedAt", FieldValue.serverTimestamp());

            // Save the status to Firestore
            db.collection("user_book_status")
                    .document(documentId)
                    .set(statusData, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Log.d("BookDetail", "Status updated successfully");
                        // If status is "want to read" or "read", add book to library
                        if (status.equals("want to read") || status.equals("read")) {
                            addBookToLibrary(status, sanitizedBookId);
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Book added to your library", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("BookDetail", "Failed to update status: " + e.getMessage(), e);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Failed to update book status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.e("BookDetail", "Cannot save status - currentUser: " + (currentUser != null) + ", bookId: " + bookId);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error: Cannot save status - User or Book ID missing", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Add the current book to the user's library with the selected status
     * @param status The reading status (want to read/read)
     * @param sanitizedBookId The sanitized book ID to use for consistent document IDs
     */
    private void addBookToLibrary(String status, String sanitizedBookId) {
        if (currentUser == null) {
            Log.e("BookDetail", "Cannot add to library - currentUser is null");
            return;
        }

        View view = getView();
        if (view == null) {
            Log.e("BookDetail", "Cannot add to library - view is null");
            return;
        }

        // Get the book details from the UI
        String title = "";
        String description = "";
        
        TextView titleView = view.findViewById(R.id.bookTitle);
        TextView descView = view.findViewById(R.id.bookDescription);
        
        if (titleView != null && titleView.getText() != null) {
            title = titleView.getText().toString();
        }
        
        if (descView != null && descView.getText() != null) {
            description = descView.getText().toString();
            // Remove the "(Tap to expand)" text if present
            if (description.endsWith("(Tap to expand)")) {
                description = description.substring(0, description.lastIndexOf("\n\n(Tap to expand)"));
            }
        }
        
        // Get cover URL from arguments if available
        String coverUrl = "";
        Bundle args = getArguments();
        if (args != null) {
            coverUrl = args.getString("coverUrl");
            if (coverUrl == null || coverUrl.isEmpty()) {
                coverUrl = args.getString("imageUrl"); // Try alternative name
            }
        }
        
        // Create a library entry
        Map<String, Object> libraryEntry = new HashMap<>();
        libraryEntry.put("userId", currentUser.getUid());
        libraryEntry.put("bookId", sanitizedBookId);
        libraryEntry.put("title", title);
        libraryEntry.put("description", description);
        libraryEntry.put("coverUrl", coverUrl);
        libraryEntry.put("pdfUrl", pdfUrl);
        libraryEntry.put("status", status);
        libraryEntry.put("addedDate", FieldValue.serverTimestamp());
        libraryEntry.put("updatedDate", FieldValue.serverTimestamp());

        // Create a document ID that combines user ID and sanitized book ID (consistent with status)
        String documentId = currentUser.getUid() + "_" + sanitizedBookId;
        Log.d("BookDetail", "Adding to library with document ID: " + documentId);

        // Add to the user's library collection
        db.collection("user_library")
                .document(documentId)
                .set(libraryEntry, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // Update was successful
                    Log.d("BookDetail", "Book added to library successfully with ID: " + documentId);
                })
                .addOnFailureListener(e -> {
                    Log.e("BookDetail", "Failed to add to library: " + e.getMessage(), e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to add to library: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadBookStatus() {
        if (bookId == null) {
            Log.e("BookDetail", "Cannot load status - bookId is null");
            return;
        }
        
        Log.d("BookDetail", "Loading status for bookId: " + bookId);
        
        // Sanitize the bookId in the same way as in saveBookStatus
        String sanitizedBookId = bookId.replace(" ", "_").replace("/", "_");
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Use the same document ID format as saveBookStatus
            String documentId = currentUser.getUid() + "_" + sanitizedBookId;
            Log.d("BookDetail", "Looking up document ID: " + documentId);
            
            db.collection("user_book_status")
                    .document(documentId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String status = documentSnapshot.getString("status");
                            Log.d("BookDetail", "Found status: " + status);
                            if (status != null) {
                                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) bookStatusSpinner.getAdapter();
                                int position = getStatusPosition(status, adapter);
                                if (position >= 0) {
                                    bookStatusSpinner.setSelection(position);
                                }
                            }
                        } else {
                            Log.d("BookDetail", "No status document found");
                            // Try to find in user_library as an alternative
                            checkLibraryStatus(sanitizedBookId);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("BookDetail", "Error loading status: " + e.getMessage(), e);
                    });
        }
    }

    /**
     * Check if the book is in the user's library and get its status
     */
    private void checkLibraryStatus(String sanitizedBookId) {
        if (currentUser == null) return;
        
        Log.d("BookDetail", "Checking library status for sanitized bookId: " + sanitizedBookId);
        
        // Look for the document directly with our consistent ID format
        String documentId = currentUser.getUid() + "_" + sanitizedBookId;
        
        db.collection("user_library")
                .document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("status");
                        if (status != null) {
                            Log.d("BookDetail", "Found status in library: " + status);
                            // Set spinner selection
                            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) bookStatusSpinner.getAdapter();
                            int position = getStatusPosition(status, adapter);
                            if (position >= 0) {
                                bookStatusSpinner.setSelection(position);
                            }
                        }
                    } else {
                        // Fallback to query approach
                        db.collection("user_library")
                                .whereEqualTo("userId", currentUser.getUid())
                                .whereEqualTo("bookId", sanitizedBookId)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        DocumentSnapshot firstDoc = queryDocumentSnapshots.getDocuments().get(0);
                                        String status = firstDoc.getString("status");
                                        if (status != null) {
                                            Log.d("BookDetail", "Found status in library query: " + status);
                                            // Set spinner selection
                                            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) bookStatusSpinner.getAdapter();
                                            int position = getStatusPosition(status, adapter);
                                            if (position >= 0) {
                                                bookStatusSpinner.setSelection(position);
                                            }
                                        }
                                    } else {
                                        // One last attempt - try with the original non-sanitized bookId
                                        Log.d("BookDetail", "Trying fallback with original bookId: " + bookId);
                                        checkWithOriginalBookId();
                                    }
                                });
                    }
                });
    }

    /**
     * Last attempt to find status using original unsanitized bookId
     */
    private void checkWithOriginalBookId() {
        if (currentUser == null || bookId == null) return;
        
        String documentId = currentUser.getUid() + "_" + bookId;
        Log.d("BookDetail", "Checking with original bookId document ID: " + documentId);
        
        // Try direct document access first
        db.collection("user_library")
                .document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("status");
                        if (status != null) {
                            Log.d("BookDetail", "Found status with original document ID: " + status);
                            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) bookStatusSpinner.getAdapter();
                            int position = getStatusPosition(status, adapter);
                            if (position >= 0) {
                                bookStatusSpinner.setSelection(position);
                            }
                        }
                    } else {
                        // Fall back to query
                        db.collection("user_library")
                                .whereEqualTo("userId", currentUser.getUid())
                                .whereEqualTo("bookId", bookId)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        DocumentSnapshot firstDoc = queryDocumentSnapshots.getDocuments().get(0);
                                        String status = firstDoc.getString("status");
                                        if (status != null) {
                                            Log.d("BookDetail", "Found status with original bookId query: " + status);
                                            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) bookStatusSpinner.getAdapter();
                                            int position = getStatusPosition(status, adapter);
                                            if (position >= 0) {
                                                bookStatusSpinner.setSelection(position);
                                            }
                                        }
                                    } else {
                                        Log.d("BookDetail", "No status found in any collection");
                                    }
                                });
                    }
                });
    }

    private void loadComments() {
        db.collection("comments")
                .whereEqualTo("bookId", bookId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    commentList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String userName = doc.getString("userName");
                        String userPhotoUrl = doc.getString("userPhotoUrl");
                        String commentText = doc.getString("commentText");
                        User user = new User(userName, userPhotoUrl);
                        Comment comment = new Comment(user, commentText);
                        commentList.add(comment);
                    }
                    commentAdapter.notifyDataSetChanged();
                });
    }

    private void loadAverageRating() {
        db.collection("ratings")
                .whereEqualTo("bookId", bookId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalRating = 0;
                    int count = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Long rate = document.getLong("rating");
                        if (rate != null) {
                            totalRating += rate;
                            count++;
                        }
                    }

                    if (count > 0) {
                        float average = (float) totalRating / count;
                        ratingBarAverage.setRating(average);
                        ratingValueText.setText(String.format("%.1f", average));
                    } else {
                        ratingBarAverage.setRating(0);
                        ratingValueText.setText("0.0");
                    }
                });

    }

    /**
     * Open the PDF viewer activity to display the book's PDF
     */
    private void openPdfViewer() {
        if (pdfUrl == null || pdfUrl.isEmpty()) {
            Toast.makeText(getContext(), "No PDF available for this book", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            Intent intent = new Intent(getActivity(), PdfViewerActivity.class);
            intent.putExtra("pdf_url", pdfUrl);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error opening PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private int getStatusPosition(String status, ArrayAdapter<CharSequence> adapter) {
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equals(status)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Format the description text for better display
     * @param description The original description text
     * @return A formatted description
     */
    private String formatDescription(String description) {
        // Trim extra whitespace
        String formatted = description.trim();
        
        // Replace multiple spaces with single spaces
        formatted = formatted.replaceAll("\\s+", " ");
        
        // Ensure proper capitalization of first letter
        if (!formatted.isEmpty()) {
            formatted = formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
        }
        
        // Limit length if needed (with proper ellipsis)
        if (formatted.length() > 1000) {
            // Find a good break point (end of sentence or space)
            int breakPoint = formatted.lastIndexOf(". ", 997);
            if (breakPoint == -1) {
                breakPoint = formatted.lastIndexOf(" ", 997);
            }
            if (breakPoint == -1) {
                breakPoint = 997;
            }
            
            formatted = formatted.substring(0, breakPoint) + "...";
        }
        
        return formatted;
    }
}


