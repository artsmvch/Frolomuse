package com.frolo.muse.ui.main.audiofx.eq;

import androidx.annotation.NonNull;


public interface EqualizerProvider {
    /**
     * Returns the number of bands.
     * @return number of bands
     */
    int getNumberOfBands();

    /**
     * Returns the min permissible band level value.
     * @return the min permissible band level value
     */
    short getMinBandLevel();

    /**
     * Returns the max permissible band level value.
     * @return the max permissible band level value
     */
    short getMaxBandLevel();

    /**
     * Returns DbRange of the band at <code>index</code>.
     * @param bandIndex index of the band
     * @return DbRange of the band
     */
    @NonNull
    DbRange getDbRange(int bandIndex);

    /**
     * Returns the current value of the band level at <code>index</code> position.
     * @return the current value of the band level at <code>index</code> position
     */
    short getBandLevel(int bandIndex);

    /**
     * Sets the band level at <code>index</code> position to <code>level</code> value.
     */
    void setBandLevel(int bandIndex, short level);
}