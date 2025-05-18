package com.example.usermanagementmodule.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.example.usermanagementmodule.model.User;
import com.example.usermanagementmodule.ui.main.MainActivity;
import com.example.usermanagementmodule.ui.main.WelcomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

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
}
