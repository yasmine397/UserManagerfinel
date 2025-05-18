package com.example.usermanagementmodule.ui.library;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Or use Picasso if you prefer
import com.example.usermanagementmodule.R;
import com.example.usermanagementmodule.book.Book;
import com.example.usermanagementmodule.book.BookDetail;

import java.util.List;

public class LibraryBookAdapter extends RecyclerView.Adapter<LibraryBookAdapter.BookViewHolder> {
    private static final String TAG = "LibraryBookAdapter";
    private List<Book> bookList;

    public LibraryBookAdapter(List<Book> bookList) {
        this.bookList = bookList;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_library_book, parent, false);
        return new BookViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        try {
            Book book = bookList.get(position);
            
            // Set book title
            if (book.getName() != null) {
                holder.bookName.setText(book.getName());
            } else {
                holder.bookName.setText("Unknown Title");
            }
            
            // Set book status if available, otherwise hide the status view
            if (holder.bookStatus != null) {
                if (book.getStatus() != null && !book.getStatus().isEmpty()) {
                    holder.bookStatus.setText(book.getStatus());
                    holder.bookStatus.setVisibility(View.VISIBLE);
                } else {
                    // Hide status view if no status available
                    holder.bookStatus.setVisibility(View.GONE);
                }
            }
            
            // Load cover image (use Glide or Picasso)
            if (book.getPhoto() != null && !book.getPhoto().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(book.getPhoto())
                        .placeholder(R.drawable.ic_book_placeholder)
                        .into(holder.bookCover);
            } else {
                // Set placeholder if no image URL
                holder.bookCover.setImageResource(R.drawable.ic_book_placeholder);
            }
            
            // Set click listener for the entire item
            holder.itemView.setOnClickListener(v -> openBookDetail(book, holder.itemView));
        } catch (Exception e) {
            Log.e(TAG, "Error binding view holder: " + e.getMessage(), e);
        }
    }

    /**
     * Open the book detail view when a library book is clicked
     */
    private void openBookDetail(Book book, View itemView) {
        try {
            // Create arguments bundle with book details
            Bundle bundle = new Bundle();
            bundle.putString("title", book.getName());
            bundle.putString("description", book.getDeseridsion());
            bundle.putString("imageUrl", book.getPhoto());
            bundle.putString("pdfUrl", book.getPdfUrl());
            
            // Only add status if it exists
            if (book.getStatus() != null && !book.getStatus().isEmpty()) {
                bundle.putString("status", book.getStatus());
            }
            
            // Pass the book ID if available, otherwise fall back to name
            if (book.getBookId() != null && !book.getBookId().isEmpty()) {
                bundle.putString("bookId", book.getBookId());
            } else {
                bundle.putString("bookId", book.getName());
            }
            
            // Create and show the book detail fragment
            BookDetail bookDetailFragment = new BookDetail();
            bookDetailFragment.setArguments(bundle);
            
            // Start fragment transaction
            FragmentTransaction transaction = ((FragmentActivity) itemView.getContext())
                    .getSupportFragmentManager()
                    .beginTransaction();
            
            transaction.replace(R.id.fragment_container, bookDetailFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } catch (Exception e) {
            Log.e(TAG, "Error opening book detail: " + e.getMessage(), e);
        }
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView bookCover;
        TextView bookName, bookStatus;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.book_cover);
            bookName = itemView.findViewById(R.id.book_name);
            bookStatus = itemView.findViewById(R.id.book_status);
        }
    }
}