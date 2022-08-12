package com.frolo.muse.ui.main.settings.theme

import android.os.Parcel
import android.os.Parcelable
import com.frolo.muse.model.Theme
import com.frolo.music.model.Album


data class ThemePage(
    // The corresponding theme model.
    val theme: Theme,
    // Indicates whether the theme is currently set for the UI.
    val isApplied: Boolean,
    // Indicates whether the theme is only for premium users,
    // and the user has not purchased the premium yet.
    val hasProBadge: Boolean,
    // Used for the theme preview screen.
    val album: Album
): Parcelable {

    val uniqueId: Long get() = theme.id.toLong()

    constructor(parcel: Parcel) : this(
        theme = Theme.valueOf(parcel.readString()!!),
        isApplied = parcel.readByte() != 0.toByte(),
        hasProBadge = parcel.readByte() != 0.toByte(),
        album = parcel.readSerializable() as Album
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(theme.name)
        parcel.writeByte(if (isApplied) 1 else 0)
        parcel.writeByte(if (hasProBadge) 1 else 0)
        parcel.writeSerializable(album)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ThemePage> {
        override fun createFromParcel(parcel: Parcel): ThemePage {
            return ThemePage(parcel)
        }

        override fun newArray(size: Int): Array<ThemePage?> {
            return arrayOfNulls(size)
        }
    }

}