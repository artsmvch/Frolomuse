package com.frolo.mediabutton;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;


public class PlayButton extends androidx.appcompat.widget.AppCompatImageView {

    private final Drawable pause;
    private final Drawable resume;
    private final AnimatedVectorDrawableCompat pauseToResume;
    private final AnimatedVectorDrawableCompat resumeToPause;

    private State state = State.PAUSE;

    private AnimatedVectorDrawableCompat animatedVector;

    public PlayButton(Context context) {
        this(context, null);
    }

    public PlayButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        resumeToPause = AnimatedVectorDrawableCompat.create(context, R.drawable.ic_resume_to_pause);
        pauseToResume = AnimatedVectorDrawableCompat.create(context, R.drawable.ic_pause_to_resume);
        resume = ContextCompat.getDrawable(context, R.drawable.ic_resume);
        pause = ContextCompat.getDrawable(context, R.drawable.ic_pause);
    }

    public State getState() {
        return state;
    }

    public void setState(State state, boolean animate) {
        if (this.state != state) {
            this.state = state;
            setStateInternal(state, animate);
        }
    }

    private void setStateInternal(final State state, final boolean animate) {
        if (animatedVector != null) {
            animatedVector.stop();
            animatedVector = null;
        }

        final boolean actuallyAnimate = animate && isAttachedToWindow();

        if (actuallyAnimate) {
            final AnimatedVectorDrawableCompat animation;
            if (state == State.PAUSE) animation = resumeToPause;
            else animation = pauseToResume;

            setImageDrawable(animation);
            animation.start();

            animatedVector = animation;
        } else {
            if (state == State.PAUSE) setImageDrawable(pause);
            else setImageDrawable(resume);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setStateInternal(state, false);
    }

    public enum State {
        RESUME,
        PAUSE
    }
}

