package com.rezha.asesmen_jmp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Data (
   var id:String?="",
   var nama:String?="",
   var hp:String?="",
   var alamat:String?="",
   var gender:String?="",
   var foto:String?="",
   var lokasi:String?=""
):Parcelable