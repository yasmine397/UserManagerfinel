package com.example.usermanagementmodule.ui.comment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usermanagementmodule.model.Comment;
import com.example.usermanagementmodule.R; // Make sure this import is present
import com.bumptech.glide.Glide; // Assuming you are using Glide for image loading

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        // Use the userName and userPhotoUrl fields from the Comment object
        // which are populated directly from Firestore in BookDetail.java
        holder.usernameTextView.setText(
                comment.getUserName() != null && !comment.getUserName().isEmpty()
                        ? comment.getUserName()
                        : "Unknown User"
        );

        holder.commentTextView.setText(comment.getCommentText());

        // Load user photo using Glide
        // Add a placeholder just in case the URL is null or loading fails
        Glide.with(holder.itemView.getContext())
                .load(comment.getUserPhotoUrl())
                .placeholder(R.drawable.ic_profile_placeholder) // Make sure you have a placeholder drawable
                .error(R.drawable.ic_profile_placeholder) // Show placeholder on error too
                .into(holder.userImageView);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public void setComments(ArrayList<Comment> commentList) {

    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView, commentTextView;
        ImageView userImageView;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            userImageView = itemView.findViewById(R.id.userImageView);
        }
    }
}