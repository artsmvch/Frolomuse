package com.frolo.mediabutton;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;


/**
 * ImageView that represents a music play button.
 * The button has two possible states: {@link State#RESUME} and {@link State#PAUSE}.
 * The state is set using {@link PlayButton#setState(State, boolean)} method.
 * The change of the state may be animated.
 *
 * NOTE: setting direct attributes in xml does not work for this widget.
 * If you want to define some attributes then create a theme and apply with android:theme.
 */
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

        // TODO: make sure all needed attributes are defined

        final Context styledContext = context; // new ContextThemeWrapper(context, R.style.PlayButton_Default)

        resumeToPause = AnimatedVectorDrawableCompat.create(styledContext, R.drawable.ic_resume_to_pause);
        pauseToResume = AnimatedVectorDrawableCompat.create(styledContext, R.drawable.ic_pause_to_resume);
        resume = ContextCompat.getDrawable(styledContext, R.drawable.ic_resume);
        pause = ContextCompat.getDrawable(styledContext, R.drawable.ic_pause);
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
            if (state == State.PAUSE) {
                animation = resumeToPause;
            } else {
                animation = pauseToResume;
            }

            super.setImageDrawable(animation);
            animation.start();

            animatedVector = animation;
        } else {
            if (state == State.PAUSE) {
                super.setImageDrawable(pause);
            } else {
                super.setImageDrawable(resume);
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setStateInternal(state, false);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        throw new UnsupportedOperationException();
    }

    public enum State {
        RESUME,
        PAUSE
    }
}

