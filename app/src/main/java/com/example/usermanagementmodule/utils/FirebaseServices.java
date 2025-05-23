package com.example.usermanagementmodule.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.example.usermanagementmodule.model.User;
import com.example.usermanagementmodule.model.UserComment;
import com.example.usermanagementmodule.ui.main.MainActivity;
import com.example.usermanagementmodule.ui.main.WelcomeActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.android.gms.tasks.OnCompleteListener;


public class FirebaseServices {

    private static final String TAG = "FirebaseServices";
    private static FirebaseServices instance;
    private FirebaseAuth auth;
    private FirebaseFirestore fire;
    private FirebaseStorage storage;
    private Uri selectedImageURL;
    private User currentUser;
    private boolean userChangeFlag;

    public Uri getSelectedImageURL() {
        return selectedImageURL;
    }

    public FirebaseServices() {
        auth = FirebaseAuth.getInstance();
        fire = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        userChangeFlag = false;
    }
    
    public static FirebaseServices getInstance() {
        if (instance == null) {
            instance = new FirebaseServices();
        }
        return instance;
    }
    public FirebaseAuth getAuth() {
        return auth;
    }
    public FirebaseFirestore getFire() {
        return fire;
    }
    public FirebaseStorage getStorage() {
        return storage;
    }
    public User getCurrentUser() {
        return currentUser;
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        this.userChangeFlag = true;
    }
    
    public boolean isUserChanged() {
        return userChangeFlag;
    }
    
    public void resetUserChangeFlag() {
        this.userChangeFlag = false;
    }

    public void setSelectedImageURL(Uri uri) {
        this.selectedImageURL = uri;
    }
    
    /**
     * Logs out the current user
     */
    public void logout() {
        auth.signOut();
        currentUser = null;
    }
    
    /**
     * Logs out the current user and navigates to the WelcomeActivity
     * @param context The current context
     */
    public void logoutAndRedirect(Context context) {
        // Sign out from Firebase
        auth.signOut();
        currentUser = null;
        
        if (context != null) {
            // Navigate to WelcomeActivity (login screen)
            Log.d(TAG, "Logging out and redirecting to WelcomeActivity");
            Intent intent = new Intent(context, WelcomeActivity.class);
            // Clear the back stack
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            
            // Finish the current activity if applicable
            if (context instanceof Activity) {
                ((Activity) context).finish();
            }
        }
    }
    
    /**
     * Checks if a user is currently logged in
     * @return boolean indicating if user is logged in
     */
    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }
    
    /**
     * Updates the current user data in Firestore
     */
    public void updateUserData() {
        if (currentUser != null && auth.getCurrentUser() != null) {
            fire.collection("users")
                .document(auth.getCurrentUser().getUid())
                .set(currentUser);
        }
    }

    // Add this method to directly navigate to MainActivity which shows BookListFragment
    public void navigateToMainActivity(Context context) {
        if (context != null) {
            Log.d(TAG, "Navigating to MainActivity which displays BookListFragment");
            Intent intent = new Intent(context, MainActivity.class);
            context.startActivity(intent);
            
            // If the context is an activity, finish it to prevent going back
            if (context instanceof Activity) {
                ((Activity) context).finish();
            }
        }
    }

    public void addCommentToUserSubcollection(UserComment comment, OnSuccessListener<DocumentReference> onSuccessListener, OnFailureListener onFailureListener) {
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            // No need to set userId field on comment object if storing in user's subcollection,
            // but it might be useful for later queries if you ever switch to a top-level collection.
            // For consistency with the previous suggestion, I'll keep setting it, but it's optional here.
            comment.setUserId(userId); // Still good practice to include userId in the document data

            fire.collection("users") // Navigate to the users collection
                    .document(userId)      // Get the current user's document
                    .collection("comments") // Navigate to the 'comments' subcollection
                    .add(comment)         // Add the comment document to the subcollection
                    .addOnSuccessListener(documentReference -> {
                        // Set the generated commentId back to the comment object if needed
                        comment.setCommentId(documentReference.getId());
                        Log.d(TAG, "Comment added to user subcollection with ID: " + documentReference.getId());
                        if (onSuccessListener != null) {
                            onSuccessListener.onSuccess(documentReference);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error adding comment to user subcollection", e);
                        if (onFailureListener != null) {
                            onFailureListener.onFailure(e);
                        }
                    });
        } else {
            Log.w(TAG, "No user logged in to add comment to subcollection.");
            if (onFailureListener != null) {
                onFailureListener.onFailure(new Exception("No user logged in"));
            }
        }
    }

    /**
     * Fetches comments from a specific user's subcollection in Firestore.
     * @param userId The ID of the user whose comments to fetch from their subcollection.
     * @param listener Listener for the query results.
     */
    public void getCommentsFromUserSubcollection(String userId, OnCompleteListener<QuerySnapshot> listener) {
        fire.collection("users") // Navigate to the users collection
                .document(userId)      // Get the specific user's document
                .collection("comments") // Navigate to the 'comments' subcollection
                .get()
                .addOnCompleteListener(listener);
    }

}
