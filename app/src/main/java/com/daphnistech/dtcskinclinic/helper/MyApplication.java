package com.daphnistech.dtcskinclinic.helper;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;

import com.daphnistech.dtcskinclinic.R;

public class MyApplication extends Application {

    public static final String TAG = MyApplication.class
            .getSimpleName();

    //private RequestQueue mRequestQueue;

    private static MyApplication mInstance;

    private PreferenceManager pref;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    /*public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }*/

    public PreferenceManager getPrefManager() {
        if (pref == null) {
            pref = new PreferenceManager(this,Constant.NOTIFICATIONS);
        }

        return pref;
    }

    /*public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }*/

    public void cancelPendingRequests(Object tag) {
        /*if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }*/
    }

    public void logout() {
        /*pref.clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);*/
    }

    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable();
        }
        return false;
    }

    public static void exitOnBackPressed(View view, Activity context) {
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener((v, keyCode, event) -> {

            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                // handle back button's click listener
                Dialog dialog = new Dialog(context);
                // Removing the features of Normal Dialogs
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_confirm_exit);
                dialog.setCancelable(true);
                dialog.show();

                dialog.findViewById(R.id.confirm).setOnClickListener(confirm -> context.finish());
                dialog.findViewById(R.id.cancel).setOnClickListener(cancel -> dialog.dismiss());

                return true;
            }
            return false;
        });
    }
}

