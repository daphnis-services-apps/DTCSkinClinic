package com.daphnistech.dtcskinclinic.model;

import android.graphics.drawable.Drawable;

public class Advertisement {
    Drawable imageView;

    public Advertisement(Drawable imageView) {
        this.imageView = imageView;
    }

    public Drawable getImageView() {
        return imageView;
    }

    public void setImageView(Drawable imageView) {
        this.imageView = imageView;
    }
}
