package com.daphnistech.dtcskinclinic.helper;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.daphnistech.dtcskinclinic.R;

public class ChatOptions extends Dialog {
    private static ChatOptions mCustomProgressbar;
    private ChatOptions mProgressbar;
    private OnDismissListener mOnDissmissListener;
    TextView textView;

    private ChatOptions(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.chat_options);
        this.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }


    public ChatOptions(Context context, Boolean instance) {
        super(context);
        mProgressbar = new ChatOptions(context);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mOnDissmissListener != null) {
            mOnDissmissListener.onDismiss(this);
        }
    }

    public static void showProgressBar(Context context, boolean cancelable, boolean open) {
        if (mCustomProgressbar != null && mCustomProgressbar.isShowing()) {
            mCustomProgressbar.cancel();
        }
        mCustomProgressbar = new ChatOptions(context);
        mCustomProgressbar.textView = mCustomProgressbar.findViewById(R.id.markClosed);
        if (!open)
            mCustomProgressbar.textView.setText("Reopen Appointment");
        mCustomProgressbar.setCancelable(cancelable);
        mCustomProgressbar.show();

    }

    public static void showProgressBar(Context context, OnDismissListener listener) {

        if (mCustomProgressbar != null && mCustomProgressbar.isShowing()) {
            mCustomProgressbar.cancel();
        }
        mCustomProgressbar = new ChatOptions(context);
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
