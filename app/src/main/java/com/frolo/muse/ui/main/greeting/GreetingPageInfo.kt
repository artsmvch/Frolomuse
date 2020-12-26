package com.frolo.muse.ui.main.greeting

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes


data class GreetingPageInfo(
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

    companion object CREATOR : Parcelable.Creator<GreetingPageInfo> {
        override fun createFromParcel(parcel: Parcel): GreetingPageInfo = GreetingPageInfo(parcel)
        override fun newArray(size: Int): Array<GreetingPageInfo?> = arrayOfNulls(size)
    }

}