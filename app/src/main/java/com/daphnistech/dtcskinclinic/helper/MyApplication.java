package com.daphnistech.dtcskinclinic.helper;

import android.app.Application;

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
}

