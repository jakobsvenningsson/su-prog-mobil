package com.example.mprog_project

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

data class Conversion(val from: String, val to: String, val fromIndex: Int, val toIndex: Int): Parcelable, Comparable<Conversion> {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "from is null.",
        parcel.readString() ?: "to is null",
        parcel.readInt() ?: 0,
        parcel.readInt() ?: 0

    ) {}

    override fun toString(): String {
        return "${from} -> ${to}"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(from)
        parcel.writeString(to)
        parcel.writeInt(fromIndex)
        parcel.writeInt(toIndex)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Conversion> {
        override fun createFromParcel(parcel: Parcel): Conversion {
            return Conversion(parcel)
        }

        override fun newArray(size: Int): Array<Conversion?> {
            return arrayOfNulls(size)
        }
    }

    override fun compareTo(other: Conversion): Int {
        if(this.from == other.from && this.to == other.to) {
            return 0
        }
        if(this.from > other.from) {
            return 1
        }
        return -1
    }
}
