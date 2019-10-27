package com.frolo.muse.views.spring;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.Property;
import android.widget.EdgeEffect;

import androidx.annotation.NonNull;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import androidx.recyclerview.widget.RecyclerView;


@Deprecated
public class SpringEdgeEffectCompat extends EdgeEffect {

    private interface Max {
        int get();
    }

    private static final Property<SpringEdgeEffectCompat, Float> SHIFT =
            new Property<SpringEdgeEffectCompat, Float>(float.class, "shift") {
                @Override
                public Float get(SpringEdgeEffectCompat object) {
                    return object.mShift;
                }

                @Override
                public void set(SpringEdgeEffectCompat object, Float value) {
                    object.mShift = value;
                }
            };

    private static final FloatPropertyCompat<SpringEdgeEffectCompat> SHIFT_COMPAT =
            new FloatPropertyCompat<SpringEdgeEffectCompat>("shift") {
                @Override
                public float getValue(SpringEdgeEffectCompat object) {
                    return object.mShift;
                }

                @Override
                public void setValue(SpringEdgeEffectCompat object, float value) {
                    object.mShift = value;
                }
            };

    private static final boolean ENABLE_PHYSICS = true;

    private final boolean mEnablePhysics = ENABLE_PHYSICS;

    private final RecyclerView mView;
    private final Max mMax;

    private final float mVelocityMultiplier;
    private final boolean mReverseAbsorb;
    private float mShift = 0f;

    private float mDistance = 0f;

    private final SpringAnimation mSpring;

    private SpringEdgeEffectCompat(
            RecyclerView view,
            Context context,
            Max max,
            float velocityMultiplier,
            boolean reverseAbsorb) {
        super(context);

        this.mView = view;
        this.mMax = max;

        this.mVelocityMultiplier = velocityMultiplier;
        this.mReverseAbsorb = reverseAbsorb;

        SpringAnimation anim = new SpringAnimation(this, SHIFT_COMPAT, 0f);
        anim.setSpring(
                new SpringForce(0f)
                        .setStiffness(850f)
                        .setDampingRatio(0.5f)
        );
        mSpring = anim;
    }

        private void releaseSpring(float velocity) {
        if (mEnablePhysics) {
            mSpring.setStartVelocity(velocity);
            mSpring.setStartValue(mShift);
            mSpring.start();
        } else {
            ObjectAnimator.ofFloat(this, SHIFT, 0f)
                    .setDuration(100)
                    .start();
        }
    }

    @Override
    public void onAbsorb(int velocity) {
        if (mReverseAbsorb) {
            releaseSpring(-mVelocityMultiplier * velocity);
        } else {
            releaseSpring(mVelocityMultiplier * velocity);
        }
    }

    @Override
    public void onPull(float deltaDistance, float displacement) {
        mDistance += deltaDistance * (mVelocityMultiplier * 2);
        mShift = OverScroll.dampedScroll(mDistance * mMax.get(), mMax.get());
    }

    @Override
    public void onRelease() {
        mDistance = 0f;
        releaseSpring(0f);
    }

    @Override
    public boolean draw(Canvas canvas) {
        return false;
    }

    @Override
    public boolean isFinished() {
        return !mSpring.isRunning();
    }

    public static class Factory extends RecyclerView.EdgeEffectFactory {

        private static Factory INSTANCE = null;

        private float mVelocityMultiplier = 0.3f;
        private boolean mReverseAbsorb = false;

        public static Factory get() {
            if (INSTANCE == null) {
                INSTANCE = new Factory();
            }
            return INSTANCE;
        }

        @NonNull
        @Override
        protected SpringEdgeEffectCompat createEdgeEffect(
                @NonNull final RecyclerView view,
                @EdgeDirection int direction) {
            final Max max;
            switch (direction) {
                case RecyclerView.EdgeEffectFactory.DIRECTION_LEFT:
                case RecyclerView.EdgeEffectFactory.DIRECTION_RIGHT: {
                    max = new Max() {
                        @Override
                        public int get() {
                            return view.getWidth();
                        }
                    };
                    break;
                }

                case RecyclerView.EdgeEffectFactory.DIRECTION_TOP:
                case RecyclerView.EdgeEffectFactory.DIRECTION_BOTTOM: {
                    max = new Max() {
                        @Override
                        public int get() {
                            return view.getHeight();
                        }
                    };
                    break;
                }

                default: {
                    throw new IllegalArgumentException(
                            "Unknown direction value: " + direction);
                }
            }

            return new SpringEdgeEffectCompat(
                    view,
                    view.getContext(),
                    max,
                    mVelocityMultiplier,
                    mReverseAbsorb);
        }
    }
}
