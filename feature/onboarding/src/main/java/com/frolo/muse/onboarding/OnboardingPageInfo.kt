package com.frolo.muse.onboarding

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes


internal data class OnboardingPageInfo(
    @DrawableRes
    val imageId: Int,
    @StringRes
    val titleId: Int,
    @StringRes
    val descriptionId: Int,
    @ColorRes
    val colorId: Int
): Parcelable {

    constructor(parcel: Parcel) : this(
        imageId = parcel.readInt(),
        titleId = parcel.readInt(),
        descriptionId = parcel.readInt(),
        colorId = parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(imageId)
        parcel.writeInt(titleId)
        parcel.writeInt(descriptionId)
        parcel.writeInt(colorId)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<OnboardingPageInfo> {
        override fun createFromParcel(parcel: Parcel): OnboardingPageInfo =
            OnboardingPageInfo(parcel)
        override fun newArray(size: Int): Array<OnboardingPageInfo?> =
            arrayOfNulls(size)
    }

}