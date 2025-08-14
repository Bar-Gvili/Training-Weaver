package com.example.training_weaver.dataclass

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExerciseInRoutine(
    val exerciseID: String,
    val sets: Int,
    val reps: Int,
    val restTimeSeconds: Int
): Parcelable