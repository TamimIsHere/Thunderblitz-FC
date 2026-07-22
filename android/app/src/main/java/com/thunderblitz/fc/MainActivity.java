package com.thunderblitz.fc;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onBackPressed() {
        if (this.bridge.getWebView().canGoBack()) {
            this.bridge.getWebView().goBack();
        } else {
            super.onBackPressed();
        }
    }
}
