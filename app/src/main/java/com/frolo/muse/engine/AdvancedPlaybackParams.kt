package com.frolo.muse.engine


/**
 * Advanced playback params to control speed and pitch of the playback.
 */
interface AdvancedPlaybackParams {
    /**
     * Returns true if the speed is persisted when switching audio sources.
     */
    fun isSpeedPersisted(): Boolean
    /**
     * Determines whether the speed should be persisted when switching audio sources.
     * Pass false to reset the speed to normal every time the audio source is changed.
     */
    fun setSpeedPersisted(isPersisted: Boolean)
    fun getSpeed(): Float
    fun setSpeed(speed: Float)
    /**
     * Returns true if the pitch is persisted when switching audio sources.
     */
    fun isPitchPersisted(): Boolean
    /**
     * Determines whether the pitch should be persisted when switching audio sources.
     * Pass false to reset the pitch to normal every time the audio source is changed.
     */
    fun setPitchPersisted(isPersisted: Boolean)
    fun getPitch(): Float
    fun setPitch(pitch: Float)
}