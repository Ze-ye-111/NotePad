package com.example.notepad;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class ZoomableImageView extends AppCompatImageView {
    private static final float MAX_ZOOM_FACTOR = 6f;
    private static final float DOUBLE_TAP_ZOOM_FACTOR = 2.8f;

    private final Matrix drawMatrix = new Matrix();
    private final Matrix valueMatrix = new Matrix();
    private final float[] matrixValues = new float[9];
    private final RectF imageBounds = new RectF();
    private final ScaleGestureDetector scaleDetector;
    private final GestureDetector gestureDetector;
    private float minScale = 1f;
    private float maxScale = 1f;

    public ZoomableImageView(@NonNull Context context) {
        this(context, null);
    }

    public ZoomableImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setScaleType(ScaleType.MATRIX);
        scaleDetector = new ScaleGestureDetector(context,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(@NonNull ScaleGestureDetector detector) {
                        if (getDrawable() == null) {
                            return false;
                        }
                        float current = currentScale();
                        if (current <= 0f) {
                            return false;
                        }
                        float target = clamp(current * detector.getScaleFactor(), minScale, maxScale);
                        float factor = target / current;
                        drawMatrix.postScale(
                                factor, factor, detector.getFocusX(), detector.getFocusY());
                        constrainAndApply();
                        return true;
                    }
                });
        gestureDetector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDown(@NonNull MotionEvent event) {
                        return true;
                    }

                    @Override
                    public boolean onDoubleTap(@NonNull MotionEvent event) {
                        if (currentScale() > minScale * 1.15f) {
                            fitToView();
                        } else {
                            zoomTo(minScale * DOUBLE_TAP_ZOOM_FACTOR,
                                    event.getX(), event.getY());
                        }
                        return true;
                    }

                    @Override
                    public boolean onScroll(@Nullable MotionEvent first,
                                            @NonNull MotionEvent current,
                                            float distanceX, float distanceY) {
                        if (currentScale() <= minScale + 0.01f) {
                            return false;
                        }
                        drawMatrix.postTranslate(-distanceX, -distanceY);
                        constrainAndApply();
                        return true;
                    }
                });
    }

    public void setBitmap(Bitmap bitmap) {
        super.setImageBitmap(bitmap);
        post(this::fitToView);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        if (width > 0 && height > 0 && getDrawable() != null) {
            post(this::fitToView);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        return true;
    }

    private void fitToView() {
        Drawable drawable = getDrawable();
        int width = getWidth();
        int height = getHeight();
        if (drawable == null || width <= 0 || height <= 0) {
            return;
        }
        float imageWidth = drawable.getIntrinsicWidth();
        float imageHeight = drawable.getIntrinsicHeight();
        if (imageWidth <= 0f || imageHeight <= 0f) {
            return;
        }

        minScale = Math.min(width / imageWidth, height / imageHeight);
        maxScale = minScale * MAX_ZOOM_FACTOR;
        drawMatrix.reset();
        drawMatrix.postScale(minScale, minScale);
        drawMatrix.postTranslate(
                (width - imageWidth * minScale) / 2f,
                (height - imageHeight * minScale) / 2f);
        setImageMatrix(drawMatrix);
    }

    private void zoomTo(float targetScale, float focusX, float focusY) {
        float current = currentScale();
        if (current <= 0f) {
            return;
        }
        float factor = clamp(targetScale, minScale, maxScale) / current;
        drawMatrix.postScale(factor, factor, focusX, focusY);
        constrainAndApply();
    }

    private void constrainAndApply() {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }
        float scale = currentScale();
        if (scale <= minScale + 0.01f) {
            fitToView();
            return;
        }
        imageBounds.set(
                0f, 0f,
                drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawMatrix.mapRect(imageBounds);

        float dx = 0f;
        float dy = 0f;
        if (imageBounds.width() <= getWidth()) {
            dx = (getWidth() - imageBounds.width()) / 2f - imageBounds.left;
        } else if (imageBounds.left > 0f) {
            dx = -imageBounds.left;
        } else if (imageBounds.right < getWidth()) {
            dx = getWidth() - imageBounds.right;
        }
        if (imageBounds.height() <= getHeight()) {
            dy = (getHeight() - imageBounds.height()) / 2f - imageBounds.top;
        } else if (imageBounds.top > 0f) {
            dy = -imageBounds.top;
        } else if (imageBounds.bottom < getHeight()) {
            dy = getHeight() - imageBounds.bottom;
        }
        drawMatrix.postTranslate(dx, dy);
        setImageMatrix(drawMatrix);
    }

    private float currentScale() {
        valueMatrix.set(drawMatrix);
        valueMatrix.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X];
    }

    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(value, maximum));
    }
}
