package com.thunderblitz.fc;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    private View transitionOverlay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        enableHighRefreshRate();

        WebSettings settings = this.bridge.getWebView().getSettings();
        settings.setUseWideViewPort(false);
        settings.setLoadWithOverviewMode(false);
        settings.setSupportZoom(false);
        settings.setTextZoom(100);

        this.bridge.getWebView().setLayerType(View.LAYER_TYPE_HARDWARE, null);

        transitionOverlay = new View(this);
        transitionOverlay.setBackgroundColor(Color.parseColor("#0a0a0a"));
        transitionOverlay.setAlpha(0f);
        transitionOverlay.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addContentView(transitionOverlay, params);

        this.bridge.getWebView().setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                transitionOverlay.animate()
                    .alpha(1f)
                    .setDuration(120)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                view.evaluateJavascript(
                    "if (window.Capacitor && window.Capacitor.Plugins && window.Capacitor.Plugins.SplashScreen) { window.Capacitor.Plugins.SplashScreen.hide({fadeOutDuration: 250}); }",
                    null
                );

                view.evaluateJavascript(
                    "(function(){ if (document.getElementById('tb-tap-anim')) return; var s = document.createElement('style'); s.id = 'tb-tap-anim'; s.innerHTML = 'a, button, [role=button] { transition: transform .12s cubic-bezier(.25,.8,.25,1), opacity .12s ease; } a:active, button:active, [role=button]:active { transform: scale(0.96); opacity: 0.85; } * { -webkit-tap-highlight-color: transparent; }'; document.head.appendChild(s); })();",
                    null
                );

                transitionOverlay.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }

    private void enableHighRefreshRate() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        Window window = getWindow();
        window.getDecorView().post(() -> {
            Display display = window.getDecorView().getDisplay();
            if (display == null) return;
            Display.Mode current = display.getMode();
            Display.Mode best = current;
            for (Display.Mode mode : display.getSupportedModes()) {
                if (mode.getRefreshRate() > best.getRefreshRate()) {
                    best = mode;
                }
            }
            WindowManager.LayoutParams attrs = window.getAttributes();
            attrs.preferredDisplayModeId = best.getModeId();
            window.setAttributes(attrs);
        });
    }

    @Override
    public void onBackPressed() {
        if (this.bridge.getWebView().canGoBack()) {
            this.bridge.getWebView().goBack();
        } else {
            super.onBackPressed();
        }
    }
}
