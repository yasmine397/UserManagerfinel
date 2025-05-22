package com.example.usermanagementmodule.ui.library;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Or use Picasso if you prefer
import com.example.usermanagementmodule.R;
import com.example.usermanagementmodule.book.Book;
import com.example.usermanagementmodule.book.BookDetail;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        holder.btnDelete.setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String bookIdToDelete = bookList.get(position).getBookId();

            FirebaseFirestore.getInstance().collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        List<Map<String, Object>> books = (List<Map<String, Object>>) documentSnapshot.get("books");
                        if (books == null) return;

                        // Create a new list without the book to delete
                        List<Map<String, Object>> updatedBooks = new ArrayList<>();
                        for (Map<String, Object> b : books) {
                            if (!bookIdToDelete.equals(b.get("bookId"))) {
                                updatedBooks.add(b);
                            }
                        }

                        // Update Firestore with the new list
                        FirebaseFirestore.getInstance().collection("users")
                                .document(userId)
                                .update("books", updatedBooks)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(v.getContext(), "Book deleted", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(v.getContext(), "Failed to delete book", Toast.LENGTH_SHORT).show();
                                });
                    });
        });
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
        public View btnDelete;
        ImageView bookCover;
        TextView bookName;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.book_cover);
            bookName = itemView.findViewById(R.id.book_name);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}