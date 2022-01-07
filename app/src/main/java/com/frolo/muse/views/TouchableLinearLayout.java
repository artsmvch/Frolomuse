package com.frolo.muse.views;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;


public class TouchableLinearLayout extends LinearLayout {

    private boolean enabled = true;
    private OnTryTouchingListener listener;

    public interface OnTryTouchingListener {
        void onTryTouching(TouchableLinearLayout layout);
    }

    public TouchableLinearLayout(Context context) {
        super(context);
    }

    public TouchableLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchableLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TouchableLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setOnTryTouchingListener(OnTryTouchingListener listener) {
        this.listener = listener;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean onInterceptTouchEvent (MotionEvent event) {
        if (!enabled) {
            OnTryTouchingListener l = listener;
            if (l != null) {
                l.onTryTouching(this);
            }
            return true;
        } else return super.onInterceptTouchEvent(event);
    }
}
