package com.acbelter.collager.ui;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class ScaledButton extends Button {
    private static final String CUSTOM_NS =
            "http://schemas.android.com/apk/res/com.acbelter.collager.ui";

    private static final String ATTR_PRESSED_SCALE = "pressedScale";
    private static final float DEFAULT_PRESSED_SCALE = .8f;

    private float mPressedScale;
    private Rect mBoundsRect;

    public ScaledButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPressedScale = attrs.getAttributeFloatValue(CUSTOM_NS, ATTR_PRESSED_SCALE,
                DEFAULT_PRESSED_SCALE);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isEnabled() || !isClickable()) {
                    return false;
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mBoundsRect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                    setScaleX(mPressedScale);
                    setScaleY(mPressedScale);
                }
                if (event.getAction() == MotionEvent.ACTION_UP ||
                        event.getAction() == MotionEvent.ACTION_CANCEL ||
                        event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    setScaleX(1f);
                    setScaleY(1f);
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (!mBoundsRect.contains(v.getLeft() + (int) event.getX(),
                            v.getTop() + (int) event.getY())) {
                        setScaleX(1f);
                        setScaleY(1f);
                    }
                }
                return false;
            }
        });
    }
}

