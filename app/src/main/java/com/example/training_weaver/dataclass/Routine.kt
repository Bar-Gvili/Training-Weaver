package com.example.training_weaver.dataclass
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID


@Parcelize
data class Routine(
    val routineID: String= UUID.randomUUID().toString(),
    val routineName :String,
    val exerciseIDs: MutableList<ExerciseInRoutine>,
):Parcelable
