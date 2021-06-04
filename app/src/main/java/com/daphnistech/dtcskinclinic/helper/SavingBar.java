package com.daphnistech.dtcskinclinic.helper;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;

import com.daphnistech.dtcskinclinic.R;

public class SavingBar extends Dialog {
    private static SavingBar mCustomProgressbar;
    private SavingBar mProgressbar;
    private OnDismissListener mOnDissmissListener;

    private SavingBar(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dailog_saving_bar);
        this.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    public SavingBar(Context context, Boolean instance) {
        super(context);
        mProgressbar = new SavingBar(context);
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
        if (mCustomProgressbar != null && mCustomProgressbar.isShowing()) {
            mCustomProgressbar.cancel();
        }
        mCustomProgressbar = new SavingBar(context);
        mCustomProgressbar.setCancelable(cancelable);
        mCustomProgressbar.show();

    }

    public static void showProgressBar(Context context, OnDismissListener listener) {

        if (mCustomProgressbar != null && mCustomProgressbar.isShowing()) {
            mCustomProgressbar.cancel();
        }
        mCustomProgressbar = new SavingBar(context);
        mCustomProgressbar.setListener(listener);
        mCustomProgressbar.setCancelable(Boolean.TRUE);
        mCustomProgressbar.show();
    }

    public static void hideProgressBar() {
        if (mCustomProgressbar != null) {
            mCustomProgressbar.dismiss();
        }
    }

    private void setListener(OnDismissListener listener) {
        mOnDissmissListener = listener;

    }

    public static void showListViewBottomProgressBar(View view) {
        if (mCustomProgressbar != null) {
            mCustomProgressbar.dismiss();
        }

        view.setVisibility(View.VISIBLE);
    }

    public static void hideListViewBottomProgressBar(View view) {
        if (mCustomProgressbar != null) {
            mCustomProgressbar.dismiss();
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
