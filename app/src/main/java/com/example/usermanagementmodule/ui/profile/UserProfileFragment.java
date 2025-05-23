package com.example.usermanagementmodule.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Button;
import com.bumptech.glide.Glide;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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
    private TextView userNameTextView;
    private TextView booksCountTextView;
    private EditText bioEditText;

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

        userNameTextView = view.findViewById(R.id.user_name);
        booksCountTextView = view.findViewById(R.id.books_count);
        bioEditText = view.findViewById(R.id.bio);


        // Initialize profile image and set click listener
        profileImageView = view.findViewById(R.id.profile_image);
        profileImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        });

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null || documentSnapshot == null || !documentSnapshot.exists()) return;

                    // Load and set the user name
                    String name = documentSnapshot.getString("username");
                    if (name != null && !name.isEmpty()) {
                        userNameTextView.setText(name);
                    }

                    // Load and set the profile image
                    String imageUrl = documentSnapshot.getString("profileImage");
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this).load(imageUrl).into(profileImageView);
                    }

                    // Load and set the bio
                    String bio = documentSnapshot.getString("bio");
                    if (bio != null && !bio.isEmpty()) {
                        bioEditText.setText(bio);
                    } else {
                        bioEditText.setText("");
                    }

                    // Get the books array and update the count
                    List<?> books = (List<?>) documentSnapshot.get("books");
                    int count = (books != null) ? books.size() : 0;
                    booksCountTextView.setText("Books: " + count);
                });


        //book counter
        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null || documentSnapshot == null || !documentSnapshot.exists()) return;
                    // Get the books array and update the count
                    List<?> books = (List<?>) documentSnapshot.get("books");
                    int count = (books != null) ? books.size() : 0;
                    booksCountTextView.setText("Books: " + count);
                });

        // Save bio on focus lost
        bioEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String newBio = bioEditText.getText().toString();
                FirebaseFirestore.getInstance().collection("users")
                        .document(userId)
                        .update("bio", newBio)
                        .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Bio updated!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update bio", Toast.LENGTH_SHORT).show());
            }
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
        UserCommentAdapter adapter = new UserCommentAdapter(userComments); // Make adapter final or a class member
        recyclerView.setAdapter(adapter);

        // Load user comments from Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("comments")
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING) // Order by timestamp, latest first
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userComments.clear();
                    List<com.example.usermanagementmodule.model.Comment> rawComments = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        // Convert raw comment data
                        String commentText = doc.getString("commentText");
                        String bookId = doc.getString("bookId");
                        String commentAuthorId = doc.getString("userId"); // Renamed variable
                        String userName = doc.getString("userName"); // Get username from comment doc
                        String userPhotoUrl = doc.getString("userPhotoUrl"); // Get photo URL from comment doc

                        if (commentText != null && bookId != null && commentAuthorId != null && userName != null) {
                            // Temporarily store raw comment data
                            // We'll fetch book/rating details next
                            com.example.usermanagementmodule.model.Comment rawComment = new com.example.usermanagementmodule.model.Comment(null, commentText);
                            // Add bookId and userId for later lookup
                            rawComment.setBookId(bookId);
                            rawComment.setUserId(commentAuthorId); // Use the renamed variable
                            rawComment.setUserName(userName);
                            rawComment.setUserPhotoUrl(userPhotoUrl);

                            rawComments.add(rawComment);
                        }
                    }

                    // Now, for each raw comment, fetch the additional details needed for UserComment
                    if (!rawComments.isEmpty()) {
                        // Fetch user document once to get books array and potentially current username/photo if not in comment doc
                        db.collection("users").document(currentUserId)
                                .get()
                                .addOnSuccessListener(userDocSnapshot -> {
                                    List<java.util.Map<String, Object>> userBooks = null;
                                    if (userDocSnapshot.exists()) {
                                        userBooks = (List<java.util.Map<String, Object>>) userDocSnapshot.get("books");
                                        // You can also get the official username and photo URL from here if preferred
                                        String officialUserName = userDocSnapshot.getString("name"); // Assuming 'name' is the field for username in user doc
                                        String officialUserPhotoUrl = userDocSnapshot.getString("photoUrl"); // Assuming 'photoUrl' is the field for photo URL in user doc

                                        // Removed problematic reassignments
                                    }

                                    // Create a list to hold the final UserComment objects temporarily
                                    List<UserComment> finalUserComments = new ArrayList<>();
                                    // Counter to track completed rating fetches
                                    final int[] completedRatingFetches = {0};

                                    // Process each raw comment
                                    for (com.example.usermanagementmodule.model.Comment rawComment : rawComments) {
                                        String bookId = rawComment.getBookId();
                                        String userIdFromComment = rawComment.getUserId(); // Get the author's user ID from the raw comment
                                        String commentText = rawComment.getCommentText();

                                        // Determine the final username and photo URL to use
                                        String finalUserName = rawComment.getUserName(); // Start with the value from the comment
                                        String finalUserPhotoUrl = rawComment.getUserPhotoUrl(); // Start with the value from the comment

                                        // If official user data is available from the user document, use it
                                        if (userDocSnapshot.exists()) {
                                            String officialUserNameFromDoc = userDocSnapshot.getString("name");
                                            String officialUserPhotoUrlFromDoc = userDocSnapshot.getString("photoUrl");

                                            if (officialUserNameFromDoc != null && !officialUserNameFromDoc.isEmpty()) {
                                                finalUserName = officialUserNameFromDoc;
                                            }
                                            if (officialUserPhotoUrlFromDoc != null && !officialUserPhotoUrlFromDoc.isEmpty()) {
                                                finalUserPhotoUrl = officialUserPhotoUrlFromDoc;
                                            }
                                        }

                                        String bookName = "Unknown Book";
                                        String bookCoverUrl = "";
                                        String bookStatus = "Not in Library";
                                        int userRating = 0;

                                        // Try to find book details and status in the user's books array
                                        if (userBooks != null) {
                                            for (java.util.Map<String, Object> userBook : userBooks) {
                                                if (userBook.containsKey("bookId") && userBook.get("bookId").equals(bookId)) {
                                                    if (userBook.containsKey("title")) bookName = (String) userBook.get("title");
                                                    if (userBook.containsKey("coverUrl")) bookCoverUrl = (String) userBook.get("coverUrl");
                                                    if (userBook.containsKey("status")) bookStatus = (String) userBook.get("status"); // Assuming status field exists
                                                    break; // Found the book in user's library
                                                } else if (userBook.containsKey("title") && bookId.equalsIgnoreCase((String) userBook.get("title"))) {
                                                    // Also try matching by title as a fallback, case-insensitive
                                                    if (userBook.containsKey("title")) bookName = (String) userBook.get("title");
                                                    if (userBook.containsKey("coverUrl")) bookCoverUrl = (String) userBook.get("coverUrl");
                                                    if (userBook.containsKey("status")) bookStatus = (String) userBook.get("status"); // Assuming status field exists
                                                    break; // Found the book by title
                                                }
                                            }
                                        }

                                        // Create final copies of variables needed in the async callback
                                        final String finalBookName = bookName;
                                        final String finalBookCoverUrl = bookCoverUrl;
                                        final String finalBookStatus = bookStatus;
                                        final String finalCommentText = commentText;

                                        // Fetch the user's rating for this book
                                        String finalUserName1 = finalUserName;
                                        String finalUserPhotoUrl1 = finalUserPhotoUrl;
                                        String finalUserName2 = finalUserName;
                                        String finalUserPhotoUrl2 = finalUserPhotoUrl;
                                        db.collection("ratings")
                                                .document(bookId + "_" + userIdFromComment) // Use userIdFromComment here
                                                .get()
                                                .addOnSuccessListener(ratingDocSnapshot -> {
                                                    int rating = 0; // Declare rating variable within this scope
                                                    if (ratingDocSnapshot.exists() && ratingDocSnapshot.get("rating") != null) {
                                                        Long ratingLong = ratingDocSnapshot.getLong("rating");
                                                        if (ratingLong != null) {
                                                            rating = ratingLong.intValue(); // Assign to the local rating variable
                                                        }
                                                    }

                                                    // Create the UserComment object with all gathered details
                                                    UserComment userComment = new UserComment(
                                                            finalUserName1, // Use the determined username
                                                            finalUserPhotoUrl1, // Use the determined photo URL
                                                            finalBookName, // Use final copies
                                                            finalBookCoverUrl, // Use final copies
                                                            finalBookStatus, // Use final copies
                                                            finalCommentText, // Use final copies
                                                            rating // Use the rating variable from this scope
                                                    );
                                                    finalUserComments.add(userComment); // Add to the temporary list

                                                    // Increment the completed fetches counter
                                                    completedRatingFetches[0]++;
                                                    // Check if all ratings have been fetched
                                                    if (completedRatingFetches[0] == rawComments.size()) {
                                                        // All data is ready, update the main list and notify adapter
                                                        userComments.addAll(finalUserComments);
                                                        adapter.notifyDataSetChanged();
                                                    }

                                                })
                                                .addOnFailureListener(e -> {
                                                    // Handle failure to fetch rating, perhaps log or use default 0
                                                    Log.e("UserProfile", "Error fetching rating for book " + bookId + ": " + e.getMessage());
                                                    // Still create the UserComment with default rating
                                                    UserComment userComment = new UserComment(
                                                            finalUserName2, // Use the determined username
                                                            finalUserPhotoUrl2, // Use the determined photo URL
                                                            finalBookName,
                                                            finalBookCoverUrl,
                                                            finalBookStatus,
                                                            finalCommentText,
                                                            0 // Use default rating on failure
                                                    );
                                                    finalUserComments.add(userComment); // Add to the temporary list

                                                    // Increment the completed fetches counter
                                                    completedRatingFetches[0]++;
                                                    // Check if all ratings have been fetched
                                                    if (completedRatingFetches[0] == rawComments.size()) {
                                                        // All data is ready, update the main list and notify adapter
                                                        userComments.addAll(finalUserComments);
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                });
                                    }
                                });
                    } else {
                        adapter.notifyDataSetChanged(); // No comments found
                    }

                })
                .addOnFailureListener(e -> {
                    // Handle failure to fetch comments
                    Toast.makeText(getContext(), "Error loading comments: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("UserProfile", "Error loading comments: " + e.getMessage());
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

    private void uploadImageToFirebase(Uri imageUri) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_images/" + userId + ".jpg");
        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Save the download URL to Firestore
                    FirebaseFirestore.getInstance().collection("users")
                            .document(userId)
                            .update("profileImage", uri.toString())
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profile image updated!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update profile image URL", Toast.LENGTH_SHORT).show());
                }))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            profileImageView.setImageURI(imageUri); // Show preview
            uploadImageToFirebase(imageUri);        // Upload to Firebase
        }
    }
}