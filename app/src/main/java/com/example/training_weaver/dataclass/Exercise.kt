package com.example.training_weaver.dataclass
import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import java.util.UUID

@Parcelize
data class Exercise(
    open val exerciseID:String= UUID.randomUUID().toString(),
    open val exerciseName: String,
    open val exerciseDescription: String,
    open val url: String


) :Parcelable


