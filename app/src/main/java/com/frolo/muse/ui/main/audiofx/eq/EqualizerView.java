package com.frolo.muse.ui.main.audiofx.eq;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.graphics.ColorUtils;

import com.frolo.muse.R;
import com.frolo.muse.StyleUtil;
import com.frolo.muse.engine.AudioFx;
import com.frolo.muse.engine.AudioFxObserver;
import com.frolo.muse.engine.SimpleAudioFxObserver;

import java.util.ArrayList;
import java.util.List;


public final class EqualizerView extends FrameLayout {

    private static final int DEFAULT_STYLE_RES_ID = R.style.EqualizerView_Default;

    private static final int DEFAULT_SLIDER_TINT = Color.parseColor("#65D3D3D3");

    private static final long DEBOUNCE_SET_BAND_LEVEL = 300L;

    private static String getFrequencyLabel(int freq) {
        if (freq > 1_000_000)
            return (freq / 1_000_000) + "kkHz";

        return (freq / 1000) + "kHz";
    }

    private static float dpToPx(Context context, float dp){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * Special handler for delaying band level setting.
     * {@link Message#what} is used as band index.
     * {@link Message#arg1} is used as level value.
     */
    private class EqHandler extends Handler {

        EqHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            final short band = (short) msg.what;
            final short level = (short) msg.arg1;

            AudioFx audioFx = EqualizerView.this.audioFx;
            if (audioFx != null) {
                final int numberOfBands = audioFx.getNumberOfBands();
                if (band >= 0 && band < numberOfBands) {
                    audioFx.setBandLevel(band, level);
                }
            }
        }
    }

    private final Context childContext;

    private final LinearLayout bandsContainer;

    private final EqHandler handler;

    private AudioFx audioFx;

    private final AudioFxObserver audioFxObserver = new SimpleAudioFxObserver() {
        @Override
        public void onBandLevelChanged(AudioFx audioFx, short band, short level) {
            ViewGroup container = getBandsContainer();
            if (band >= 0 && band < container.getChildCount()) {
                DbSlider slider = (DbSlider) container.getChildAt(band);
                slider.setValue(level);
            }
        }
    };

    private final boolean isVisualEnabled = true;

    private final float gridLineThickness;
    @ColorInt
    private final int gridTint;

    // Background visual tools
    private final Paint visualPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint visualNeutralPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final List<VisualPath> visualPaths;

    public EqualizerView(Context context) {
        this(context, null);
    }

    public EqualizerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.equalizerViewStyle, DEFAULT_STYLE_RES_ID);
    }

    public EqualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        int styleId = attrs != null
                ? attrs.getAttributeIntValue(null, "style", DEFAULT_STYLE_RES_ID)
                : DEFAULT_STYLE_RES_ID;
        childContext = new ContextThemeWrapper(context, styleId);

        final TypedArray a = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.EqualizerView, defStyleAttr, defStyleRes);
        try {
            gridLineThickness = a.getDimension(R.styleable.EqualizerView_gridLineThickness, 0f);
            gridTint = a.getColor(R.styleable.EqualizerView_gridTint, DEFAULT_SLIDER_TINT);
        } finally {
            a.recycle();
        }

        bandsContainer = new LinearLayout(context);
        bandsContainer.setOrientation(LinearLayout.HORIZONTAL);
        addView(bandsContainer, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        handler = new EqHandler(context.getMainLooper());

        visualPaint.setStyle(Paint.Style.STROKE);
        visualPaint.setStrokeWidth(dpToPx(context, 2f));

        visualNeutralPaint.setStrokeWidth(dpToPx(context, 1.6f));

        int accentColor = StyleUtil.readColorAttrValue(getContext(), R.attr.colorAccent);
        visualPaths = new ArrayList<>(3);
        visualPaths.add(new VisualPath(ColorUtils.setAlphaComponent(accentColor, 102), dpToPx(context, 3f)));
        visualPaths.add(new VisualPath(ColorUtils.setAlphaComponent(accentColor, 78), dpToPx(context, 1.2f)));
        visualPaths.add(new VisualPath(ColorUtils.setAlphaComponent(accentColor, 48), dpToPx(context, 1f)));

        setWillNotDraw(!isVisualEnabled);
    }

    private ViewGroup getBandsContainer() {
        return bandsContainer;
    }

    /**
     * Returns the current levels set by user.
     * @return current levels
     */
    public short[] getCurrentLevels() {
        ViewGroup container = getBandsContainer();
        final int numberOfBands = container.getChildCount();
        final short[] levels = new short[numberOfBands];
        for (int i = 0; i < numberOfBands; i++) {
            final DbSlider slider = (DbSlider) container.getChildAt(i);
            levels[i] = (short) slider.getValue();
        }
        return levels;
    }

    /**
     * Setups the view with the given <code>audioFx</code>.
     * @param audioFx to bind with
     */
    public void setup(@Nullable AudioFx audioFx) {
        setup(audioFx, true);
    }

    /**
     * Setups the view with the given <code>audioFx</code>.
     * @param audioFx to bind with
     * @param animate if true, then the changes will be animated
     */
    public void setup(@Nullable AudioFx audioFx, boolean animate) {
        AudioFx oldAudioFx = this.audioFx;
        if (oldAudioFx != null) {
            oldAudioFx.unregisterObserver(audioFxObserver);
        }

        this.audioFx = audioFx;

        final ViewGroup container = getBandsContainer();

        if (audioFx == null) {
            // No AudioFx - no sliders
            container.removeAllViews();
            return;
        }

        if (isAttachedToWindow()) {
            audioFx.registerObserver(audioFxObserver);
        }

        final int numberOfBands = audioFx.getNumberOfBands();

        final int minBandLevel = audioFx.getMinBandLevelRange();
        final int maxBandLevel = audioFx.getMaxBandLevelRange();

        int addedBandCount = 0;
        for (short bandIndex = 0; bandIndex < numberOfBands; bandIndex++) {

            final int currentValue = audioFx.getBandLevel(bandIndex);

            final int[] freqRange = audioFx.getBandFreqRange(bandIndex);

            final DbSlider slider;
            if (bandIndex >= container.getChildCount())  {
                DbSlider newSlider = createDbSlider();
                container.addView(newSlider, container.getChildCount());
                slider = newSlider;
            } else {
                slider = (DbSlider) container.getChildAt(bandIndex);
            }

            addedBandCount++;

            final int finalBandIndex = bandIndex;
            // Set it first, for debug purpose
            slider.setSliderIndex(finalBandIndex);

            final DbSlider.OnDbValueChangeListener l1 =
                new DbSlider.OnDbValueChangeListener() {
                    @Override
                    public void onDbValueChange(DbSlider slider, int value, boolean fromUser) {
                        invalidate();
                        if (fromUser) setBandLevelInternal(finalBandIndex, value);
                    }
                };
            slider.setOnDbValueChangeListener(l1);

            final DbSlider.OnDbValueAnimatedListener l2 =
                    new DbSlider.OnDbValueAnimatedListener() {
                        @Override
                        public void onDbValueAnimated(DbSlider slider, int animatedValue) {
                            invalidate();
                        }
                    };
            slider.setOnDbValueAnimatedListener(l2);

            slider.setRange(minBandLevel, maxBandLevel);
            slider.setValue(currentValue, animate);

            if (freqRange != null && freqRange.length == 2) {
                final int max = freqRange[1];
                slider.setTopLabel(getFrequencyLabel(max));
            }
        }

        // removing views those weren't bounded to any band
        while (addedBandCount < container.getChildCount()) {
            container.removeViewAt(container.getChildCount() - 1);
        }
    }

    /**
     * Creates a new DbSlider and applies the right LayoutParams to it.
     * @return a new DbSlider with the right LayoutParams
     */
    private DbSlider createDbSlider() {
        final DbSlider dbSlider = new DbSlider(childContext);

        dbSlider.setTrackTint(gridTint);

        final LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
        lp.weight = 1;

        dbSlider.setLayoutParams(lp);

        return dbSlider;
    }

    private void setBandLevelInternal(int bandIndex, int level) {
        handler.removeMessages(bandIndex);
        Message message = handler.obtainMessage(bandIndex, level, 0);
        handler.sendMessageDelayed(message, DEBOUNCE_SET_BAND_LEVEL);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        AudioFx currAudioFx = this.audioFx;
        if (currAudioFx != null) {
            currAudioFx.registerObserver(audioFxObserver);

            ViewGroup container = getBandsContainer();
            final int numberOfBands = audioFx.getNumberOfBands();
            final int viewChildCount = container.getChildCount();
            // Actually, numberOfBands must be equal viewChildCount
            for (short i = 0; i < Math.min(numberOfBands, viewChildCount); i++) {
                DbSlider slider = (DbSlider) container.getChildAt(i);
                slider.setValue(audioFx.getBandLevel(i));
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        AudioFx currAudioFx = this.audioFx;
        if (currAudioFx != null) {
            currAudioFx.unregisterObserver(audioFxObserver);
        }

        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (isVisualEnabled) {
            drawVisuals(canvas);
        }
        super.dispatchDraw(canvas);
        if (isVisualEnabled) {
            drawVisualsOver(canvas);
        }
    }

    private void drawVisuals(Canvas canvas) {
        ViewGroup container = getBandsContainer();
        int sliderCount = container.getChildCount();

        if (sliderCount < 1) {
            return;
        }

        DbSlider firstSlider = (DbSlider) container.getChildAt(0);

        // The y of the center
        float neutralY = container.getTop() + container.getPaddingTop() + firstSlider.getCenterY();

        // The centered horizontal line
        visualNeutralPaint.setStrokeWidth(gridLineThickness);
        visualNeutralPaint.setColor(gridTint);
        //firstSlider.setupProgressBackgroundPaint(visualNeutralPaint);
        canvas.drawLine(getPaddingLeft(), neutralY - visualNeutralPaint.getStrokeWidth() / 2,
                getMeasuredWidth() - getPaddingRight(), neutralY - visualNeutralPaint.getStrokeWidth() / 2, visualNeutralPaint);
    }

    private void drawVisualsOver(Canvas canvas) {
        ViewGroup container = getBandsContainer();
        int sliderCount = container.getChildCount();

        if (sliderCount < 1) {
            return;
        }

        DbSlider firstSlider = (DbSlider) container.getChildAt(0);

        // The y of the center
        float neutralY = container.getTop() + container.getPaddingTop() + firstSlider.getCenterY();

        // Visual paths
        for (VisualPath visualPath : visualPaths) {
            visualPath.path.reset();
            visualPath.tmpCx1 = getPaddingLeft();
            visualPath.tmpCy1 = neutralY;
            visualPath.path.moveTo(visualPath.tmpCx1, visualPath.tmpCy1);
        }

        for (int i = 0; i <= sliderCount; i++) {
            DbSlider slider = (DbSlider) container.getChildAt(i);

            for (int visualPathIndex = 0; visualPathIndex < visualPaths.size(); visualPathIndex++) {
                VisualPath visualPath = visualPaths.get(visualPathIndex);
                final float yCoefficient;
                switch (visualPathIndex) {
                    case 0: {
                        yCoefficient = 0f;
                        break;
                    }
                    case 1: {
                        yCoefficient = 0.2f;
                        break;
                    }
                    case 2: {
                        yCoefficient = 0.3f;
                        break;
                    }
                    default: {
                        yCoefficient = 0.5f;
                        break;
                    }
                }

                if (i < sliderCount) {
                    int centerY = slider.getThumbCenterY();
                    visualPath.tmpCx2 = slider.getLeft() + slider.getThumbCenterX();
                    visualPath.tmpCy2 = slider.getTop() + centerY + (neutralY - centerY) * yCoefficient;
                } else {
                    visualPath.tmpCx2 = getMeasuredWidth() - getPaddingRight();
                    visualPath.tmpCy2 = neutralY;
                }

                float x1 = visualPath.tmpCx1 + (visualPath.tmpCx2 - visualPath.tmpCx1) / 2f;
                float y1 = visualPath.tmpCy1;
                float x2 = visualPath.tmpCx1 + (visualPath.tmpCx2 - visualPath.tmpCx1) / 2f;
                float y2 = visualPath.tmpCy2;
                visualPath.path.cubicTo(x1, y1, x2, y2, visualPath.tmpCx2, visualPath.tmpCy2);

                visualPath.tmpCx1 = visualPath.tmpCx2;
                visualPath.tmpCy1 = visualPath.tmpCy2;
            }

        }

        for (VisualPath visualPath : visualPaths) {
            visualPaint.setColor(visualPath.color);
            visualPaint.setStrokeWidth(visualPath.strokeWidth);
            // This offset helps to draw the path centered at its Y coors according to the stroke width
            visualPath.path.offset(0f, -visualPath.strokeWidth / 2f);
            canvas.drawPath(visualPath.path, visualPaint);
        }
    }

    /**
     * Internal state of a visual path.
     */
    private static class VisualPath {
        final Path path;
        @ColorInt
        final int color;
        final float strokeWidth;

        // tmp values used to calculate pixel positions while drawing
        float tmpCx1;
        float tmpCy1;
        float tmpCx2;
        float tmpCy2;

        VisualPath(@ColorInt int color, float strokeWidth) {
            this.path = new Path();
            this.color = color;
            this.strokeWidth = strokeWidth;
        }
    }

}
