package com.daphnistech.dtcskinclinic.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class Signature extends View {

    private static final float STROKE_WIDTH = 10f;
    private final Paint paint = new Paint();
    private float lastTouchX;
    private float lastTouchY;
    private List<Float> arrayLastTouchX = new ArrayList<>();
    private List<Float> arrayLastTouchY = new ArrayList<>();

    public Signature(Context context) {
        super(context);
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(STROKE_WIDTH);
    }

    public void clear() {
        arrayLastTouchX = new ArrayList<>();
        arrayLastTouchY = new ArrayList<>();
        lastTouchX = 0.0f;
        lastTouchY = 0.0f;
        invalidate();
    }

    public void undo() {
        if (arrayLastTouchX.size() > 1) {
            arrayLastTouchX.remove(arrayLastTouchX.size() - 1);
            lastTouchX = arrayLastTouchX.get(arrayLastTouchX.size()-1);
            arrayLastTouchX.remove(arrayLastTouchX.size() - 1);
            arrayLastTouchY.remove(arrayLastTouchY.size() - 1);
            lastTouchY = arrayLastTouchY.get(arrayLastTouchY.size()-1);
            arrayLastTouchY.remove(arrayLastTouchY.size() - 1);
        } else {
            clear();
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (lastTouchX != 0.0 && lastTouchY != 0.0) {
            arrayLastTouchX.add(lastTouchX);
            arrayLastTouchY.add(lastTouchY);
            for (int i = 0; i <= arrayLastTouchX.size() - 1; i++) {
                canvas.drawCircle(arrayLastTouchX.get(i), arrayLastTouchY.get(i), 15, paint);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            lastTouchX = event.getX();
            lastTouchY = event.getY();
            invalidate();
            return true;
        }
        return true;
    }
}
