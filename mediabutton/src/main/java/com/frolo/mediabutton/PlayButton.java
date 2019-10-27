package com.frolo.mediabutton;


import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;

public class PlayButton extends androidx.appcompat.widget.AppCompatImageView {
    private Drawable pause;
    private Drawable resume;
    private AnimatedVectorDrawableCompat pauseToResume;
    private AnimatedVectorDrawableCompat resumeToPause;

    private State state = State.PAUSE;

    private AnimatedVectorDrawableCompat animated;

    public PlayButton(Context context) {
        this(context, null);
    }

    public PlayButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void cancelAnimated() {
        if (animated != null) {
            animated.stop();
        }
    }

    private void init(Context context, AttributeSet attrs) {
        resumeToPause = AnimatedVectorDrawableCompat.create(context, R.drawable.ic_resume_to_pause);
        pauseToResume = AnimatedVectorDrawableCompat.create(context, R.drawable.ic_pause_to_resume);
        resume = ContextCompat.getDrawable(context, R.drawable.ic_resume);
        pause = ContextCompat.getDrawable(context, R.drawable.ic_pause);
    }

    public State getState() {
        return state;
    }

    public void setState(State state, boolean animate) {
        this.state = state;
        checkStateInternal(state, animate);
    }

    private synchronized void checkStateInternal(State state, boolean animate) {
        cancelAnimated();
        if (state == State.PAUSE) {
            if (animate && isAttachedToWindow()) {
                animated = resumeToPause;
                setImageDrawable(animated);
                animated.start();
            } else {
                animated = null;
                setImageDrawable(pause);
            }
        } else {
            if (animate && isAttachedToWindow()) {
                animated = pauseToResume;
                setImageDrawable(animated);
                animated.start();
            } else {
                animated = null;
                setImageDrawable(resume);
            }
        }
    }

    public enum State {
        RESUME,
        PAUSE
    }
}

