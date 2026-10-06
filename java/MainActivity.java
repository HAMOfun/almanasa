package com.almanasa.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.*;
import android.widget.EditText;
import android.widget.FrameLayout;
import androidx.webkit.WebViewAssetLoader;

public class MainActivity extends Activity {
    WebView w; ValueCallback<Uri[]> fileCb; View custom; WebChromeClient.CustomViewCallback customCb; FrameLayout root;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE); // منع السكرين شوت والتسجيل
        root = new FrameLayout(this);
        w = new WebView(this);
        root.addView(w, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
        WebSettings s = w.getSettings();
        s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        final WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();
        w.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) {
                return loader.shouldInterceptRequest(r.getUrl());
            }
        });
        w.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams p) {
                if (fileCb != null) fileCb.onReceiveValue(null);
                fileCb = cb;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT); i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("*/*");
                startActivityForResult(Intent.createChooser(i, "اختر ملف"), 1);
                return true;
            }
            @Override public boolean onJsConfirm(WebView v, String u, String m, final JsResult r) {
                new AlertDialog.Builder(MainActivity.this).setMessage(m).setCancelable(false)
                    .setPositiveButton("تأكيد", (d, x) -> r.confirm()).setNegativeButton("إلغاء", (d, x) -> r.cancel()).show();
                return true;
            }
            @Override public boolean onJsPrompt(WebView v, String u, String m, String def, final JsPromptResult r) {
                final EditText e = new EditText(MainActivity.this); e.setText(def);
                new AlertDialog.Builder(MainActivity.this).setMessage(m).setView(e).setCancelable(false)
                    .setPositiveButton("موافق", (d, x) -> r.confirm(e.getText().toString())).setNegativeButton("إلغاء", (d, x) -> r.cancel()).show();
                return true;
            }
            @Override public void onShowCustomView(View v, CustomViewCallback cb) {
                custom = v; customCb = cb; w.setVisibility(View.GONE);
                root.addView(v, new FrameLayout.LayoutParams(-1, -1));
            }
            @Override public void onHideCustomView() {
                if (custom == null) return;
                root.removeView(custom); custom = null; customCb.onCustomViewHidden(); w.setVisibility(View.VISIBLE);
            }
        });
        w.loadUrl("https://appassets.androidplatform.net/assets/index.html");
    }

    @Override protected void onActivityResult(int rq, int rs, Intent d) {
        if (rq == 1 && fileCb != null) {
            Uri[] res = (rs == RESULT_OK && d != null && d.getData() != null) ? new Uri[]{d.getData()} : null;
            fileCb.onReceiveValue(res); fileCb = null;
        }
    }

    @Override public void onBackPressed() {
        if (custom != null) { w.getWebChromeClient().onHideCustomView(); }
        else if (w.canGoBack()) w.goBack();
        else super.onBackPressed();
    }
}
