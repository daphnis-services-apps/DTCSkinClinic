package com.daphnistech.dtcskinclinic.helper;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;

import com.daphnistech.dtcskinclinic.R;

public class CustomProgressBar extends Dialog {
    private static CustomProgressBar mCustomProgressbarBar;
    private CustomProgressBar mProgressbar;
    private OnDismissListener mOnDissmissListener;

    private CustomProgressBar(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_progressbar);
        this.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    public CustomProgressBar(Context context, Boolean instance) {
        super(context);
        mProgressbar = new CustomProgressBar(context);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mOnDissmissListener != null) {
            mOnDissmissListener.onDismiss(this);
        }
    }

    public static void showProgressBar(Context context, boolean cancelable) {
        showProgressBar(context, cancelable, null);
    }

    public static void showProgressBar(Context context, boolean cancelable, String message) {
        if (mCustomProgressbarBar != null && mCustomProgressbarBar.isShowing()) {
            mCustomProgressbarBar.cancel();
        }
        mCustomProgressbarBar = new CustomProgressBar(context);
        mCustomProgressbarBar.setCancelable(cancelable);
        mCustomProgressbarBar.show();

    }

    public static void showProgressBar(Context context, OnDismissListener listener) {

        if (mCustomProgressbarBar != null && mCustomProgressbarBar.isShowing()) {
            mCustomProgressbarBar.cancel();
        }
        mCustomProgressbarBar = new CustomProgressBar(context);
        mCustomProgressbarBar.setListener(listener);
        mCustomProgressbarBar.setCancelable(Boolean.TRUE);
        mCustomProgressbarBar.show();
    }

    public static void hideProgressBar() {
        if (mCustomProgressbarBar != null) {
            mCustomProgressbarBar.dismiss();
        }
    }

    private void setListener(OnDismissListener listener) {
        mOnDissmissListener = listener;

    }

    public static void showListViewBottomProgressBar(View view) {
        if (mCustomProgressbarBar != null) {
            mCustomProgressbarBar.dismiss();
        }

        view.setVisibility(View.VISIBLE);
    }

    public static void hideListViewBottomProgressBar(View view) {
        if (mCustomProgressbarBar != null) {
            mCustomProgressbarBar.dismiss();
        }

        view.setVisibility(View.GONE);
    }

    public void showProgress(Context context, boolean cancelable, String message) {

        if (mProgressbar != null && mProgressbar.isShowing()) {
            mProgressbar.cancel();
        }
        mProgressbar.setCancelable(cancelable);
        mProgressbar.show();
    }

}
