package com.example.usermanagementmodule.pdf;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);
        
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        
        // Get PDF URL from intent
        String pdfUrl = getIntent().getStringExtra("pdf_url");
        if (pdfUrl == null || pdfUrl.isEmpty()) {
            Toast.makeText(this, "Invalid PDF URL", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Configure WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }
        });
        
        try {
            // Use Google Docs viewer for compatibility
            String googleDocsUrl = "https://docs.google.com/gview?embedded=true&url=";
            webView.loadUrl(googleDocsUrl + pdfUrl);
            Log.d(TAG, "Loading PDF: " + pdfUrl);
        } catch (Exception e) {
            Log.e(TAG, "Error loading PDF: " + e.getMessage());
            Toast.makeText(this, "Error loading PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
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