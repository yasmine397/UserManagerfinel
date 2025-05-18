package com.example.usermanagementmodule.pdf;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.usermanagementmodule.R;

public class PdfViewerActivity extends AppCompatActivity {
    private static final String TAG = "PdfViewerActivity";
    
    private WebView webView;
    private ProgressBar progressBar;
    private String pdfUrl;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);
        
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        
        // Get PDF URL from intent
        pdfUrl = getIntent().getStringExtra("pdf_url");
        String bookTitle = getIntent().getStringExtra("BOOK_TITLE");
        
        if (pdfUrl == null || pdfUrl.isEmpty()) {
            Toast.makeText(this, "Invalid PDF URL", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        if (bookTitle != null && !bookTitle.isEmpty()) {
            setTitle(bookTitle);
        } else {
            setTitle("PDF Viewer");
        }
        
        // Make WebView visible immediately - we'll show a progress bar on top
        webView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        
        // Configure WebView for better performance and usability
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setDomStorageEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setUseWideViewPort(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        
        // Better progress tracking
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                // When progress is high enough (70%), we can consider it as loading
                if (newProgress >= 70) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "onPageFinished: " + url);
            }
            
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                Log.e(TAG, "WebView error: " + error.toString());
                // If we get an error, try direct PDF URL as fallback
                if (request.isForMainFrame()) {
                    handleLoadError();
                }
            }
        });
        
        loadPdf();
    }
    
    private void loadPdf() {
        try {
            // First try Google Docs viewer for a better reading experience
            String googleDocsUrl = "https://docs.google.com/gview?embedded=true&url=";
            String fullUrl = googleDocsUrl + pdfUrl;
            
            Log.d(TAG, "Loading PDF via Google Docs: " + fullUrl);
            webView.loadUrl(fullUrl);
            
            // Set a fallback timeout - if page doesn't finish loading properly in 8 seconds, try direct URL
            new Handler().postDelayed(() -> {
                if (progressBar.getVisibility() == View.VISIBLE) {
                    Log.d(TAG, "Google Docs viewer timeout - trying direct PDF URL");
                    handleLoadError();
                }
            }, 8000);
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading PDF: " + e.getMessage());
            Toast.makeText(this, "Error loading PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void handleLoadError() {
        // When Google Docs viewer fails, try loading the PDF directly
        // This works for many modern devices
        try {
            progressBar.setVisibility(View.VISIBLE);
            
            Log.d(TAG, "Trying direct PDF URL: " + pdfUrl);
            webView.loadUrl(pdfUrl);
            
            // Hide progress bar after a short delay
            new Handler().postDelayed(() -> {
                progressBar.setVisibility(View.GONE);
            }, 3000);
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading direct PDF: " + e.getMessage());
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Could not load PDF", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
} 