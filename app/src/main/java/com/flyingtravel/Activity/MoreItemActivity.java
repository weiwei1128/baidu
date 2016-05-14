package com.flyingtravel.Activity;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flyingtravel.HomepageActivity;
import com.flyingtravel.R;
import com.flyingtravel.Utility.Functions;
import com.flyingtravel.Utility.GlobalVariable;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoreItemActivity extends AppCompatActivity {
    int position = 0;
    TextView header;
    WebView webView;
    Boolean ifWebview = false;
    /*GA*/
    public static Tracker tracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_fragment);
        /**GA**/
        GlobalVariable globalVariable = (GlobalVariable) getApplication();
        tracker = globalVariable.getDefaultTracker();
        /**GA**/
        LinearLayout content = (LinearLayout) findViewById(R.id.checkschedule_content);
        LinearLayout backLayout = (LinearLayout) findViewById(R.id.linearLayout2);
        header = (TextView) findViewById(R.id.more_text);
        backLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true, MoreItemActivity.this, getBaseContext(), HomepageActivity.class, null);
            }
        });
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(this.getResources().getString(R.string.loading_text));
        dialog.show();
        Bundle bundle = this.getIntent().getExtras();
        if (bundle.containsKey("position")) {
            position = bundle.getInt("position");
            webView = new WebView(this);
            ifWebview = true;
            WebSettings websettings = webView.getSettings();
            websettings.setSupportZoom(true);
            websettings.setBuiltInZoomControls(true);
            websettings.setJavaScriptEnabled(true);

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    dialog.dismiss();
                }
            });
            String myURL = null;
            switch (position) {
                case 0://關於我們
                    /***GA**/
                    tracker.send(new HitBuilders.EventBuilder().setCategory("查看關於我們")
//                .setAction("click")
//                .setLabel("submit")
                            .build());
                    /***GA**/
                    myURL = "http://zhiyou.lin366.com/help.aspx?tid=84";
                    header.setText(this.getResources().getString(R.string.aboutUs_text));
                    break;
                case 1://規劃行程
                    /***GA**/
                    tracker.send(new HitBuilders.EventBuilder().setCategory("行程規劃")
//                .setAction("click")
//                .setLabel("submit")
                            .build());
                    /***GA**/
                    myURL = "http://zhiyou.lin366.com/diy/";
                    header.setText(this.getResources().getString(R.string.planschedule_text));
                    break;
            }
            webView.loadUrl(myURL);
            content.addView(webView);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //make the webview go back
            if (ifWebview && webView.canGoBack())
                webView.goBack();
            else
                Functions.go(true, MoreItemActivity.this, MoreItemActivity.this, HomepageActivity.class, null);
        }
        return false;
    }
}
