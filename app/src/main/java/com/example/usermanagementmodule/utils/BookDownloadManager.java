package com.example.usermanagementmodule.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.usermanagementmodule.book.Book;

/**
 * Utility class to handle book downloading functionality from external URLs
 */
public class BookDownloadManager {
    private static final String TAG = "BookDownloadManager";
    
    /**
     * Download a book cover from its URL
     * @param context Application context
     * @param book Book to download cover for
     * @return Download reference ID or -1 if failed
     */
    public static long downloadBook(Context context, Book book) {
        if (context == null || book == null) {
            Log.e(TAG, "Context or book is null");
            return -1;
        }
        
        String pdfUrl = book.getPdfUrl();
        String bookName = book.getName();
        
        if (pdfUrl == null || pdfUrl.isEmpty()) {
            Log.e(TAG, "PDF URL is empty or null");
            Toast.makeText(context, "No PDF URL available for this book", Toast.LENGTH_SHORT).show();
            return -1;
        }
        
        try {
            // Validate URL format
            Uri uri = Uri.parse(pdfUrl);
            if (uri.getScheme() == null || !(uri.getScheme().equals("http") || uri.getScheme().equals("https"))) {
                Toast.makeText(context, "Invalid URL format: " + pdfUrl, Toast.LENGTH_SHORT).show();
                return -1;
            }
            
            // Create download request
            DownloadManager.Request request = new DownloadManager.Request(uri)
                    .setTitle("Downloading " + bookName)
                    .setDescription("Downloading book PDF")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, 
                            "Book_" + bookName.replaceAll("[^a-zA-Z0-9]", "_") + ".pdf");
            
            // Get download service and enqueue the request
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            long downloadId = downloadManager.enqueue(request);
            
            Toast.makeText(context, "Downloading " + bookName + " PDF", Toast.LENGTH_SHORT).show();
            return downloadId;
        } catch (Exception e) {
            Log.e(TAG, "Error downloading book PDF: " + e.getMessage(), e);
            Toast.makeText(context, "Error starting PDF download: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return -1;
        }
    }
    
    /**
     * Download a book cover image
     * @param context Application context
     * @param book Book to download cover for
     * @return Download reference ID or -1 if failed
     */
    public static long downloadBookCover(Context context, Book book) {
        if (context == null || book == null) {
            Log.e(TAG, "Context or book is null");
            return -1;
        }
        
        String bookUrl = book.getPhoto();
        String bookName = book.getName();
        
        if (bookUrl == null || bookUrl.isEmpty()) {
            Log.e(TAG, "Book cover URL is empty or null");
            Toast.makeText(context, "No cover image URL available for this book", Toast.LENGTH_SHORT).show();
            return -1;
        }
        
        try {
            // Validate URL format
            Uri uri = Uri.parse(bookUrl);
            if (uri.getScheme() == null || !(uri.getScheme().equals("http") || uri.getScheme().equals("https"))) {
                Toast.makeText(context, "Invalid URL format: " + bookUrl, Toast.LENGTH_SHORT).show();
                return -1;
            }
            
            // Create download request
            DownloadManager.Request request = new DownloadManager.Request(uri)
                    .setTitle("Downloading " + bookName + " Cover")
                    .setDescription("Downloading book cover image")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, 
                            "BookCover_" + bookName.replaceAll("[^a-zA-Z0-9]", "_") + ".jpg");
            
            // Get download service and enqueue the request
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            long downloadId = downloadManager.enqueue(request);
            
            Toast.makeText(context, "Downloading " + bookName + " cover", Toast.LENGTH_SHORT).show();
            return downloadId;
        } catch (Exception e) {
            Log.e(TAG, "Error downloading book cover: " + e.getMessage(), e);
            Toast.makeText(context, "Error starting cover download: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return -1;
        }
    }
} 