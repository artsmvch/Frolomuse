package com.frolo.muse.views.text;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.frolo.muse.R;


/**
 * TextView that has only one single line.
 * The text size is automatically calculated so that it fits the content height of the widget.
 * The text size can also be limited using {@link FitSingleLineTextView#setMaxTextSize(int)} method.
 *
 * Any attempts to manipulate the line count using {@link FitSingleLineTextView#setLines(int)} or {@link FitSingleLineTextView#setMaxLines(int)} methods
 * will result in an exception.
 */
public class FitSingleLineTextView extends AppCompatTextView {

    private final TextPaint mTestPaint = new TextPaint();
    private final Rect mTestBounds = new Rect();

    static final int MAX_TEXT_SIZE_NOT_SET = -1;

    private int mMaxTextSize = MAX_TEXT_SIZE_NOT_SET;

    /**
     * The last calculated value of text size to be applied to this TextView.
     * Int type is used for not exact, but approximate comparison of text sizes.
     */
    private int mCalculatedRawTextSize = -1;

    public FitSingleLineTextView(final Context context) {
        this(context, null);
    }

    public FitSingleLineTextView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        super.setLines(1);
        super.setEllipsize(TextUtils.TruncateAt.END);

        final TypedArray arr = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.FitSingleLineTextView, 0, 0);

        if (arr.hasValue(R.styleable.FitSingleLineTextView_maxTextSize)) {
            setMaxTextSize((int) arr.getDimension(R.styleable.FitSingleLineTextView_maxTextSize, MAX_TEXT_SIZE_NOT_SET));
        }

        arr.recycle();
    }

    @Nullable
    private String getCurrentTextString() {
        CharSequence chs = getText();
        if (chs == null || chs.length() == 0)
            return null;

        return chs.toString();
    }

    @Override
    protected void onTextChanged(
        final CharSequence text,
        final int start,
        final int before,
        final int after
    ) {
        final int heightToFit = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        refitText(getCurrentTextString(), heightToFit);
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        final int currRawTextSize = (int) getPaint().getTextSize();
        if (w != oldw || h != oldh || currRawTextSize != mCalculatedRawTextSize) {
            final int heightToFit = h - getPaddingTop() - getPaddingBottom();
            refitText(getCurrentTextString(), heightToFit);
        }
    }

    /**
     * Limits the text size to the given <code>maxTextSize</code>>.
     * NOTE: the value is set in pixels.
     * @param maxTextSize max text size in pixels
     */
    public void setMaxTextSize(int maxTextSize) {
        if (mMaxTextSize != maxTextSize) {
            mMaxTextSize = maxTextSize;
            if (maxTextSize > 0) {
                final int heightToFit = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
                refitText(getCurrentTextString(), heightToFit);
            }
        }
    }

    private void refitText(@Nullable final String text, final int targetHeight) {
        if (targetHeight <= 0)
            return;

        if (text == null || text.isEmpty()) {
            return;
        }

        final TextPaint testPaint = mTestPaint;
        final Rect testBounds = mTestBounds;

        float hi = 100;
        float lo = 2;
        final float threshold = 0.5f; // How close we have to be

        testPaint.set(this.getPaint());

        while ((hi - lo) > threshold) {
            float size = (hi + lo) / 2;
            testPaint.setTextSize(size);

            testPaint.getTextBounds(text, 0, text.length(), testBounds);

            if(testBounds.height() >= targetHeight) {
                hi = size; // too big
            } else {
                lo = size; // too small
            }
        }

        // Use lo so that we undershoot rather than overshoot
        final float calculatedTextSize = lo;

        final float resultTextSize =
                mMaxTextSize > 0 ? Math.min(calculatedTextSize, mMaxTextSize) : lo;

        mCalculatedRawTextSize = (int) resultTextSize;

        super.setTextSize(TypedValue.COMPLEX_UNIT_PX, resultTextSize);
    }

    @Override
    public void setTextSize(float size) {
        // Just ignore this, because text size is automatically calculated
    }

    @Override
    public void setTextSize(int unit, float size) {
        // Just ignore this, because text size is automatically calculated
    }

    @Override
    public void setLines(int lines) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMaxLines(int maxLines) {
        throw new UnsupportedOperationException();
    }

}
