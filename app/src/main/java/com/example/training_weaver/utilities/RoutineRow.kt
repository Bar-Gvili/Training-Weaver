package com.training_weaver.utilities

import com.example.training_weaver.dataclass.Exercise
import com.example.training_weaver.dataclass.ExerciseInRoutine



data class RoutineRow(
    /** מזהה ייחודי לשורת מופע (משלב exerciseID+index) */
    val key: String,
    /** אובייקט התרגיל המלא (שם/תיאור/תמונה וכו’) */
    val exercise: Exercise,
    /** מטא־דאטה של המופע בתוך הרוטינה (סטים/ראפס/מנוחה וכו’) */
    val meta: ExerciseInRoutine,
    /** אינדקס המופע בתוך routine.list – חשוב למחיקה/עדכון */
    val indexInRoutine: Int
)