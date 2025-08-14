package com.training_weaver.utilities

import com.example.training_weaver.dataclass.Exercise
import com.example.training_weaver.dataclass.ExerciseInRoutine


data class RoutineRow(
    val key: String,                   // למשל "<exerciseId>#<index>"
    val exercise: Exercise,            // הנתונים הסטטיים של התרגיל (שם/תיאור/וידאו וכו')
    val meta: ExerciseInRoutine,       // המופע הספציפי בתוך הרוטינה (sets/reps/rest וכו')
    val indexInRoutine: Int            // האינדקס של המופע בתוך ה-Routine (מאפשר מחיקה מדויקת)
)
