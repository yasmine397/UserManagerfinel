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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usermanagementmodule.model.Comment;
import com.example.usermanagementmodule.ui.comment.CommentAdapter;
import com.example.usermanagementmodule.ui.library.LibraryFragment;
import com.example.usermanagementmodule.R;
import com.example.usermanagementmodule.model.User;
import com.example.usermanagementmodule.ui.auth.LoginFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;
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
    private Button sendButton, readButton, addToLibraryButton;
    private RecyclerView commentsRecyclerView;
    private CommentAdapter commentAdapter;
    private ArrayList<Comment> commentList;
    private RatingBar ratingBarAverage, ratingBarUser;
    private TextView ratingValueText;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String bookId;
    private String pdfUrl; // To store the PDF URL
    private boolean isInLibrary = false;

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
        }

        // Initialize read button
        readButton = view.findViewById(R.id.readButton);
        readButton.setOnClickListener(v -> openPdfViewer());
        
        // If no PDF URL is available, disable the read button
        if (pdfUrl == null || pdfUrl.isEmpty()) {
            readButton.setEnabled(false);
            readButton.setText("No PDF Available");
        }

        // Add to Library button
        addToLibraryButton = view.findViewById(R.id.addToLibraryButton);
        addToLibraryButton.setOnClickListener(v -> addBookToLibrary());

        // Comments
        commentEditText = view.findViewById(R.id.commentInput);
        sendButton = view.findViewById(R.id.buttonSend);
        commentsRecyclerView = view.findViewById(R.id.recyclerViewComments);
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(commentList);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentsRecyclerView.setAdapter(commentAdapter);

        // Ratings
        ratingBarUser = view.findViewById(R.id.ratingBarUser);
        ratingBarAverage = view.findViewById(R.id.ratingBarAverage);
        ratingValueText = view.findViewById(R.id.ratingValueText);
        if (currentUser != null) {
            db.collection("ratings")
                    .document(bookId + "_" + currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Long userRating = documentSnapshot.getLong("rating");
                            if (userRating != null) {
                                ratingBarUser.setRating(userRating);
                            }
                        }
                    });
        }
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Load average rating
        loadAverageRating();

        // Load comments
        loadComments();

        // Check if book is already in library
        checkIfInLibrary();

        // Send comment
        sendButton.setOnClickListener(v -> {
            if (currentUser != null) {
                String username = currentUser.getDisplayName(); // Getting username from FirebaseAuth
                String imageUrl = currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : ""; // Getting photo URL from FirebaseAuth
                String commentText = commentEditText.getText().toString().trim();

                if (!commentText.isEmpty()) {
                    // ... (Comment model creation is commented out, data is put directly into map)
                    // Save to Firestore
                    Map<String, Object> commentData = new HashMap<>();
                    commentData.put("userId", currentUser.getUid());
                    commentData.put("userName", username ); // Putting username here
                    commentData.put("userPhotoUrl", imageUrl); // Putting photo URL here
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
        ImageButton buttonBack = view.findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
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

        return view;
    }

    private void checkIfInLibrary() {
        if (currentUser == null || bookId == null) return;
        
        String sanitizedBookId = bookId.replace(" ", "_").replace("/", "_");
        
        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ArrayList<Map<String, Object>> books = (ArrayList<Map<String, Object>>) documentSnapshot.get("books");
                        if (books != null) {
                            for (Map<String, Object> book : books) {
                                String storedBookId = (String) book.get("bookId");
                                String storedTitle = (String) book.get("title");
                                
                                // Check if book matches by bookId
                                if (sanitizedBookId.equals(storedBookId)) {
                                    isInLibrary = true;
                                    updateAddToLibraryButton();
                                    break;
                                }
                                
                                // Also check by title if available (case insensitive)
                                if (storedTitle != null && !storedTitle.isEmpty() && 
                                    bookId.equalsIgnoreCase(storedTitle)) {
                                    isInLibrary = true;
                                    updateAddToLibraryButton();
                                    break;
                                }
                                
                                // Check alternative sanitization
                                String altSanitizedBookId = bookId.replace("_", " ").replace(" ", "_").replace("/", "_");
                                if (altSanitizedBookId.equals(storedBookId)) {
                                    isInLibrary = true;
                                    updateAddToLibraryButton();
                                    break;
                                }
                            }
                        }
                    }
                    // We don't need to create a user document here - that will happen when adding a book
                })
                .addOnFailureListener(e -> {
                    Log.e("BookDetail", "Error checking if book is in library: " + e.getMessage());
                });
    }
    
    private void updateAddToLibraryButton() {
        if (addToLibraryButton != null) {
            if (isInLibrary) {
                addToLibraryButton.setText("In Library");
                addToLibraryButton.setEnabled(false);
            } else {
                addToLibraryButton.setText("Add to Library");
                addToLibraryButton.setEnabled(true);
            }
        }
    }

    /**
     * Add the current book to the user's library
     */
    private void addBookToLibrary() {
        try {
            if (currentUser == null) {
                Log.d("BookDetail", "User not logged in, redirecting to login");
                // Save book details to temporary storage (could use SharedPreferences in a real implementation)
                Toast.makeText(getContext(), "Please log in to add books to your library", Toast.LENGTH_SHORT).show();
                
                // Navigate to login fragment
                if (getActivity() != null) {
                    // Get the book details to pass to login
                    Bundle bookData = new Bundle();
                    bookData.putString("pendingBookId", bookId);
                    bookData.putString("pendingAction", "addToLibrary");
                    
                    // Navigate to the login fragment with book data
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new LoginFragment())
                            .addToBackStack(null)
                            .commit();
                }
                return;
            }

            // Rest of your existing implementation for logged in users
            // Sanitize the bookId
            String sanitizedBookId = bookId.replace(" ", "_").replace("/", "_");
            // Also have the original bookId for lookup
            String originalBookId = bookId;
            
            // First, get the complete book details from Firestore's books collection
            db.collection("books")
                    .whereEqualTo("name", originalBookId)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Found the book by name
                            DocumentSnapshot bookDoc = queryDocumentSnapshots.getDocuments().get(0);
                            addBookWithDataFromFirestore(bookDoc, sanitizedBookId);
                        } else {
                            // Try looking up by document ID
                            db.collection("books")
                                    .document(originalBookId)
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            addBookWithDataFromFirestore(documentSnapshot, sanitizedBookId);
                                        } else {
                                            // Book not found in collection, use UI values as fallback
                                            addBookWithUIValues(sanitizedBookId);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("BookDetail", "Failed to fetch book by ID: " + e.getMessage());
                                        // Fall back to UI values
                                        addBookWithUIValues(sanitizedBookId);
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("BookDetail", "Failed to fetch book by name: " + e.getMessage());
                        // Fall back to UI values
                        addBookWithUIValues(sanitizedBookId);
                    });
        } catch (Exception e) {
            Log.e("BookDetail", "Error in addBookToLibrary: " + e.getMessage(), e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Add book using data from Firestore books collection
     */
    private void addBookWithDataFromFirestore(DocumentSnapshot bookDoc, String sanitizedBookId) {
        try {
            // Create book entry with data from Firestore
            final Map<String, Object> bookEntry = new HashMap<>();
            bookEntry.put("bookId", sanitizedBookId);
            
            // Get book details from Firestore document
            String title = bookDoc.getString("name");
            String description = bookDoc.getString("deseridsion");
            String coverUrl = bookDoc.getString("photo");
            String pdfUrl = bookDoc.getString("pdfUrl");
            String realestDate = bookDoc.getString("realestDate");
            
            if (title != null) bookEntry.put("title", title);
            if (description != null) bookEntry.put("description", description);
            if (coverUrl != null) bookEntry.put("coverUrl", coverUrl);
            if (pdfUrl != null) bookEntry.put("pdfUrl", pdfUrl);
            if (realestDate != null) bookEntry.put("realestDate", realestDate);
            
            bookEntry.put("addedDate", new java.util.Date());
            bookEntry.put("addedByApp", true);
            
            Log.d("BookDetail", "Adding book with Firestore data: " + bookEntry);
            
            // Check if user document exists and update it
            updateUserWithBookEntry(bookEntry);
        } catch (Exception e) {
            Log.e("BookDetail", "Error in addBookWithDataFromFirestore: " + e.getMessage(), e);
            // Fall back to UI values
            addBookWithUIValues(sanitizedBookId);
        }
    }

    /**
     * Add book using data from UI (fallback method)
     */
    private void addBookWithUIValues(String sanitizedBookId) {
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
        
        // Create a library book entry
        final Map<String, Object> bookEntry = new HashMap<>();
        bookEntry.put("bookId", sanitizedBookId);
        bookEntry.put("title", title);
        bookEntry.put("description", description);
        bookEntry.put("coverUrl", coverUrl);
        bookEntry.put("pdfUrl", pdfUrl);
        bookEntry.put("addedDate", new java.util.Date());
        bookEntry.put("addedByApp", true);
        
        Log.d("BookDetail", "Adding book with UI data (fallback): " + bookEntry);
        
        // Update user document with this book entry
        updateUserWithBookEntry(bookEntry);
    }

    /**
     * Update user document with book entry
     */
    private void updateUserWithBookEntry(final Map<String, Object> bookEntry) {
        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> books = (List<Map<String, Object>>) documentSnapshot.get("books");
                        
                        if (books == null) {
                            // If books field doesn't exist, create it with this book
                            List<Map<String, Object>> newBooks = new ArrayList<>();
                            newBooks.add(bookEntry);
                            
                            db.collection("users")
                                    .document(currentUser.getUid())
                                    .update("books", newBooks)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("BookDetail", "Book added to library successfully (created array)");
                                        Toast.makeText(getContext(), "Book added to your library", Toast.LENGTH_SHORT).show();
                                        isInLibrary = true;
                                        updateAddToLibraryButton();
                                        
                                        // Navigate to library
                                        if (getActivity() != null) {
                                            getActivity().getSupportFragmentManager()
                                                    .beginTransaction()
                                                    .replace(R.id.fragment_container, new LibraryFragment())
                                                    .addToBackStack(null)
                                                    .commit();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("BookDetail", "Failed to add to library: " + e.getMessage(), e);
                                        if (getContext() != null) {
                                            // Show more detailed error with specific code
                                            String errorMsg = "Failed to add to library. ";
                                            if (e.getMessage() != null) {
                                                if (e.getMessage().contains("PERMISSION_DENIED")) {
                                                    errorMsg += "You don't have permission to update this user.";
                                                } else if (e.getMessage().contains("NOT_FOUND")) {
                                                    errorMsg += "User document not found.";
                                                } else {
                                                    errorMsg += e.getMessage();
                                                }
                                            }
                                            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            // If books field exists, update it with arrayUnion
                            db.collection("users")
                                    .document(currentUser.getUid())
                                    .update("books", FieldValue.arrayUnion(bookEntry))
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("BookDetail", "Book added to library successfully");
                                        Toast.makeText(getContext(), "Book added to your library", Toast.LENGTH_SHORT).show();
                                        isInLibrary = true;
                                        updateAddToLibraryButton();
                                        
                                        // Navigate to library
                                        if (getActivity() != null) {
                                            getActivity().getSupportFragmentManager()
                                                    .beginTransaction()
                                                    .replace(R.id.fragment_container, new LibraryFragment())
                                                    .addToBackStack(null)
                                                    .commit();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("BookDetail", "Failed to add to library: " + e.getMessage(), e);
                                        if (getContext() != null) {
                                            // Show more detailed error with specific code
                                            String errorMsg = "Failed to add to library. ";
                                            if (e.getMessage() != null) {
                                                if (e.getMessage().contains("PERMISSION_DENIED")) {
                                                    errorMsg += "You don't have permission to update this user.";
                                                } else if (e.getMessage().contains("NOT_FOUND")) {
                                                    errorMsg += "User document not found.";
                                                } else {
                                                    errorMsg += e.getMessage();
                                                }
                                            }
                                            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        // Document doesn't exist - create it
                        Log.d("BookDetail", "User document not found, creating a new one");
                        
                        // First create the books array with this book
                        List<Map<String, Object>> newBooks = new ArrayList<>();
                        newBooks.add(bookEntry);
                        
                        // Create basic user document
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("books", newBooks);
                        userData.put("userId", currentUser.getUid());
                        userData.put("email", currentUser.getEmail());
                        if (currentUser.getDisplayName() != null) {
                            userData.put("name", currentUser.getDisplayName());
                        }
                        
                        // Create the user document
                        db.collection("users")
                                .document(currentUser.getUid())
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("BookDetail", "Created new user document with book");
                                    Toast.makeText(getContext(), "Book added to your library", Toast.LENGTH_SHORT).show();
                                    isInLibrary = true;
                                    updateAddToLibraryButton();
                                    
                                    // Navigate to library
                                    if (getActivity() != null) {
                                        getActivity().getSupportFragmentManager()
                                                .beginTransaction()
                                                .replace(R.id.fragment_container, new LibraryFragment())
                                                .addToBackStack(null)
                                                .commit();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("BookDetail", "Failed to create user document: " + e.getMessage(), e);
                                    if (getContext() != null) {
                                        // Show more detailed error with specific code
                                        String errorMsg = "Failed to create user profile. ";
                                        if (e.getMessage() != null) {
                                            if (e.getMessage().contains("PERMISSION_DENIED")) {
                                                errorMsg += "You don't have permission to create a user profile.";
                                            } else {
                                                errorMsg += e.getMessage();
                                            }
                                        }
                                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("BookDetail", "Error checking user document: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Error checking user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadComments() {
        Log.d("BookDetail", "Loading comments for bookId: " + bookId);

        db.collection("comments")
                .whereEqualTo("bookId", bookId)
                //.orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    commentList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String commentText = doc.getString("commentText");
                        String userId = doc.getString("userId");
                        String userName = doc.getString("userName");
                        String userPhotoUrl = doc.getString("userPhotoUrl");
                        String bookIdFromDoc = doc.getString("bookId");

                        if (commentText != null && userId != null && userName != null && bookIdFromDoc != null) {
                            Comment comment = new Comment();
                            comment.setCommentText(commentText);
                            comment.setUserId(userId);
                            comment.setUserName(userName);
                            comment.setUserPhotoUrl(userPhotoUrl);
                            comment.setBookId(bookIdFromDoc);
                            commentList.add(comment);
                        }
                    }
                    commentAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("BookDetail", "Error loading comments: " + e.getMessage());
                    Toast.makeText(getContext(), "Error loading comments.", Toast.LENGTH_SHORT).show();
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
            // Get book title
            String bookTitle = "";
            TextView titleView = getView().findViewById(R.id.bookTitle);
            if (titleView != null) {
                bookTitle = titleView.getText().toString();
            }
            
            // Create and start the PDF viewer activity with proper parameters
            // No login check needed - PDF viewing is allowed for all users
            Intent intent = new Intent(getActivity(), PdfViewerActivity.class);
            intent.putExtra("pdf_url", pdfUrl);
            intent.putExtra("BOOK_TITLE", bookTitle);
            startActivity(intent);
            
            Log.d("BookDetail", "Opening PDF viewer with URL: " + pdfUrl);
        } catch (Exception e) {
            Log.e("BookDetail", "Error opening PDF: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error opening PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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


