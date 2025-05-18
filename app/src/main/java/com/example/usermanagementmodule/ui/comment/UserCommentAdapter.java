package com.example.usermanagementmodule.ui.comment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // or use Picasso if you prefer
import com.example.usermanagementmodule.R;
import com.example.usermanagementmodule.model.UserComment;

import java.util.List;

public class UserCommentAdapter extends RecyclerView.Adapter<UserCommentAdapter.UserCommentViewHolder> {
    private List<UserComment> commentList;

    public UserCommentAdapter(List<UserComment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public UserCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_comment, parent, false);
        return new UserCommentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserCommentViewHolder holder, int position) {
        UserComment comment = commentList.get(position);

        holder.userName.setText(comment.getUserName());
        holder.bookName.setText(comment.getBookName());
        holder.bookStatus.setText(comment.getBookStatus());
        holder.commentText.setText(comment.getCommentText());
        holder.userRating.setRating(comment.getUserRating());

        // Load images using Glide (or Picasso)
        Glide.with(holder.itemView.getContext())
                .load(comment.getUserPhotoUrl())
                .placeholder(R.drawable.ic_profile_placeholder)
                .into(holder.userPhoto);

        Glide.with(holder.itemView.getContext())
                .load(comment.getBookCoverUrl())
                .placeholder(R.drawable.ic_book_placeholder)
                .into(holder.bookCover);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class UserCommentViewHolder extends RecyclerView.ViewHolder {
        ImageView userPhoto, bookCover;
        TextView userName, bookName, bookStatus, commentText;
        RatingBar userRating;

        public UserCommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userPhoto = itemView.findViewById(R.id.user_photo);
            userName = itemView.findViewById(R.id.user_name);
            bookCover = itemView.findViewById(R.id.book_cover);
            bookName = itemView.findViewById(R.id.book_name);
            bookStatus = itemView.findViewById(R.id.book_status);
            commentText = itemView.findViewById(R.id.comment_text);
            userRating = itemView.findViewById(R.id.user_rating);
        }
    }
}