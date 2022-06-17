package com.frolo.core.ui.carousel;

import android.content.Context;

import androidx.annotation.NonNull;

import com.frolo.core.ui.R;


final class CardProperties {

    static float getBaseCardElevation(@NonNull Context context) {
        return context.getResources().getDimension(R.dimen.carousel_base_card_elevation);
    }

    static float getMaxCardElevation(@NonNull Context context) {
        return context.getResources().getDimension(R.dimen.carousel_max_card_elevation);
    }

    static float getRaisingCardElevation(@NonNull Context context) {
        return getMaxCardElevation(context) - getBaseCardElevation(context);
    }

    static float getCardCornerRadius(@NonNull Context context) {
        return context.getResources().getDimension(R.dimen.carousel_card_corner_radius);
    }

    private CardProperties() {
    }
}
