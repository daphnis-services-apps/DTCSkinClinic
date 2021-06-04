package com.daphnistech.dtcskinclinic.helper;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.daphnistech.dtcskinclinic.R;

public class PolicyOpener extends Dialog {
    private static PolicyOpener policyOpener;
    static WebView webView;

    public PolicyOpener(@NonNull Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dailog_policy);
        webView = findViewById(R.id.webView);
        this.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    public static void showPolicy(Context context, boolean cancelable, String url) {
        if (policyOpener != null && policyOpener.isShowing()) {
            policyOpener.cancel();
        }
        policyOpener = new PolicyOpener(context);
        webView.loadUrl(url);
        webView.requestFocus();
        policyOpener.setCancelable(cancelable);
        policyOpener.show();
    }

    public static void hideProgressBar() {
        if (policyOpener != null) {
            policyOpener.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        hideProgressBar();
    }
}
