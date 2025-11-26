package com.example.qfoodly.ui.scan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class ScanningOverlayView extends View {

    private Paint paint;
    private Rect scanRect;

    public ScanningOverlayView(Context context) {
        super(context);
        init();
    }

    public ScanningOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScanningOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(0x99000000); // 60% przezroczysty czarny
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Definiuj obszar skanowania (50% szerokości i 40% wysokości, wyśrodkowany)
        int scanWidth = (int) (width * 0.6);  // 60% szerokości ekranu
        int scanHeight = (int) (height * 0.4); // 40% wysokości ekranu
        int left = (width - scanWidth) / 2;
        int top = (height - scanHeight) / 2;
        int right = left + scanWidth;
        int bottom = top + scanHeight;

        scanRect = new Rect(left, top, right, bottom);

        // Narysuj całą przezroczystość
        canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);

        // Narysuj przyciemnione tło
        canvas.drawColor(0x99000000);

        // Wyczyść obszar skanowania (przezroczyste okienko)
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawRect(scanRect, paint);
        paint.setXfermode(null);

        // Narysuj biały kontur wokół obszaru
        Paint outlinePaint = new Paint();
        outlinePaint.setColor(0xFFFFFFFF);
        outlinePaint.setStrokeWidth(3);
        outlinePaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(scanRect, outlinePaint);

        // Narysuj narożniki (dekoracja)
        int cornerSize = 50;
        Paint cornerPaint = new Paint();
        cornerPaint.setColor(0xFFFFFFFF);
        cornerPaint.setStrokeWidth(5);
        cornerPaint.setStyle(Paint.Style.STROKE);

        // Top-left corner
        canvas.drawLine(left, top + cornerSize, left, top, cornerPaint);
        canvas.drawLine(left, top, left + cornerSize, top, cornerPaint);

        // Top-right corner
        canvas.drawLine(right - cornerSize, top, right, top, cornerPaint);
        canvas.drawLine(right, top, right, top + cornerSize, cornerPaint);

        // Bottom-left corner
        canvas.drawLine(left, bottom - cornerSize, left, bottom, cornerPaint);
        canvas.drawLine(left, bottom, left + cornerSize, bottom, cornerPaint);

        // Bottom-right corner
        canvas.drawLine(right - cornerSize, bottom, right, bottom, cornerPaint);
        canvas.drawLine(right, bottom, right, bottom - cornerSize, cornerPaint);
    }

    public Rect getScanRect() {
        if (scanRect == null) {
            int width = getWidth();
            int height = getHeight();
            int scanWidth = (int) (width * 0.6);
            int scanHeight = (int) (height * 0.4);
            int left = (width - scanWidth) / 2;
            int top = (height - scanHeight) / 2;
            int right = left + scanWidth;
            int bottom = top + scanHeight;
            scanRect = new Rect(left, top, right, bottom);
        }
        return scanRect;
    }
}