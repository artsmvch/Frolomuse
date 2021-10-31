package com.frolo.muse.ui.main.player.carousel;

import android.content.Context;

import androidx.annotation.NonNull;

import com.frolo.muse.R;


final class AlbumCardProperties {

    static float getBaseCardElevation(@NonNull Context context) {
        return context.getResources().getDimension(R.dimen.album_carousel_base_card_elevation);
    }

    static float getMaxCardElevation(@NonNull Context context) {
        return context.getResources().getDimension(R.dimen.album_carousel_max_card_elevation);
    }

    static float getRaisingCardElevation(@NonNull Context context) {
        return getMaxCardElevation(context) - getBaseCardElevation(context);
    }

    static float getCardCornerRadius(@NonNull Context context) {
        return context.getResources().getDimension(R.dimen.album_carousel_card_corner_radius);
    }

    private AlbumCardProperties() {
    }
}
