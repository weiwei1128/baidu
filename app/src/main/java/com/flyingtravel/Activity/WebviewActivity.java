package com.flyingtravel.Activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.flyingtravel.HomepageActivity;
import com.flyingtravel.R;
import com.flyingtravel.Utility.Functions;

public class WebviewActivity extends AppCompatActivity {
    WebView webView;
    Boolean ifWevView=false;
    String link;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_activity);
        webView = (WebView)findViewById(R.id.news_webview);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle.containsKey("NewsLink")) {
            link = bundle.getString("NewsLink");
            setupWebview(link);
        }
    }
    void setupWebview(String link){
        ifWevView=true;
        final ProgressDialog dialog = new ProgressDialog(WebviewActivity.this);
        dialog.setMessage(WebviewActivity.this.getResources().getString(R.string.loading_text));
        dialog.setCancelable(true);
        dialog.show();
        WebSettings websettings = webView.getSettings();
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                dialog.dismiss();
            }

        });
        websettings.setSupportZoom(true);
        websettings.setBuiltInZoomControls(true);
        websettings.setJavaScriptEnabled(true);
        webView.loadUrl(link);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //make the webview go back
            if (ifWevView && webView.canGoBack())
                webView.goBack();

            else
                Functions.go(true, WebviewActivity.this, WebviewActivity.this,
                        HomepageActivity.class, null);
        }
        return false;
    }
}
