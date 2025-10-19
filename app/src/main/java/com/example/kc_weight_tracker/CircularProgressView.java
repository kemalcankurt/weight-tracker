package com.example.kc_weight_tracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * CircularProgressView is a custom view that displays a circular progress bar.
 * It is used to display the progress of a goal.
 */
public class CircularProgressView extends View {
    // The paint objects for the background, progress, and text
    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint textPaint;
    private RectF rectF;
    
    private float progress = 0f;
    private float maxProgress = 100f;
    private String centerText = "";
    
    // Constructors
    public CircularProgressView(Context context) {
        super(context);
        init();
    }
    
    public CircularProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public CircularProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    /**
     * Initializes the CircularProgressView
     */
    private void init() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(getContext().getColor(R.color.gray_200));
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(12f);
        
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(getContext().getColor(R.color.blue_600));
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(12f);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(getContext().getColor(R.color.gray_800));
        textPaint.setTextSize(32f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        rectF = new RectF();
    }
    
    /**
     * Draws the CircularProgressView
     * 
     * @param canvas The canvas to draw on
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) - 20f;
        
        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        
        // Draw background circle
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint);
        
        // Draw progress arc
        float sweepAngle = (progress / maxProgress) * 360f;
        canvas.drawArc(rectF, -90f, sweepAngle, false, progressPaint);
        
        // Draw center text
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float textY = centerY + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent;
        canvas.drawText(centerText, centerX, textY, textPaint);
    }
    
    /**
     * Sets the progress of the CircularProgressView
     * 
     * @param progress The progress to set
     */
    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }
    
    /**
     * Sets the center text of the CircularProgressView
     * 
     * @param text The text to set
     */
    public void setCenterText(String text) {
        this.centerText = text;
        invalidate();
    }
    
    /**
     * Sets the max progress of the CircularProgressView
     * 
     * @param maxProgress The max progress to set
     */
    public void setMaxProgress(float maxProgress) {
        this.maxProgress = maxProgress;
        invalidate();
    }
}
