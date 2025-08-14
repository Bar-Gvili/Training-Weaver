package com.example.training_weaver.utilities

import com.example.training_weaver.dataclass.Exercise
import com.example.training_weaver.dataclass.ExerciseInRoutine
import com.example.training_weaver.dataclass.Routine
import com.example.training_weaver.dataclass.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ListenerRegistration




/**
 * מבנה הנתונים:
 * users/{UID}/
 * ├─ exercises/{exerciseId} → { exerciseID, exerciseName, exerciseDescription, url }
 * └─ routines/{routineId}  → { routineID, routineName, list: [ { exerciseID, sets, reps, restTimeSeconds } ] }
 */
class FirebaseConnections {

    // --- Firebase singletons ---
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // --- Helpers ---
    private fun currentUid(): String =
        uidOrNull() ?: throw IllegalStateException("User not logged in")

    private fun uidOrNull(): String? =
        FirebaseAuth.getInstance().currentUser?.uid

    // --- Mappers ---
    private fun exerciseToMap(ex: Exercise) = mapOf(
        "exerciseID" to ex.exerciseID,
        "exerciseName" to ex.exerciseName,
        "exerciseDescription" to ex.exerciseDescription,
        "url" to ex.url
    )

    private fun mapToExercise(doc: DocumentSnapshot): Exercise? {
        val id = doc.getString("exerciseID") ?: doc.id
        val name = doc.getString("exerciseName") ?: return null
        val desc = doc.getString("exerciseDescription") ?: ""
        val url = doc.getString("url") ?: ""
        return Exercise(id, name, desc, url)
    }

    private fun exerciseInRoutineToMap(item: ExerciseInRoutine) = mapOf(
        "exerciseID" to item.exerciseID,
        "sets" to item.sets,
        "reps" to item.reps,
        "restTimeSeconds" to item.restTimeSeconds
    )

    private fun mapToExerciseInRoutine(map: Map<*, *>): ExerciseInRoutine? {
        val exId = map["exerciseID"] as? String ?: return null
        val sets = (map["sets"] as? Number)?.toInt() ?: return null
        val reps = (map["reps"] as? Number)?.toInt() ?: return null
        val rest = (map["restTimeSeconds"] as? Number)?.toInt() ?: 0
        return ExerciseInRoutine(exId, sets, reps, rest)
    }

    private fun routineToMap(r: Routine) = mapOf(
        "routineID" to r.routineID,
        "routineName" to r.routineName,
        "list" to r.exerciseIDs.map { exerciseInRoutineToMap(it) }
    )

    private fun mapToRoutine(doc: DocumentSnapshot): Routine? {
        val id = doc.getString("routineID") ?: doc.id
        val name = doc.getString("routineName") ?: return null
        val rawList = doc.get("list") as? List<*> ?: emptyList<Any>()
        val parsed = rawList.mapNotNull { it as? Map<*, *> }
            .mapNotNull { mapToExerciseInRoutine(it) }
            .toMutableList()
        return Routine(
            routineID = id,
            routineName = name,
            exerciseIDs = parsed
        )
    }

    // ---------- Internal (עם UID מפורש) ----------
    private fun uploadExercise(
        UID: String,
        exercise: Exercise,
        onComplete: (Boolean) -> Unit
    ) {
        db.collection("users").document(UID)
            .collection("exercises")
            .document(exercise.exerciseID)
            .set(exerciseToMap(exercise))
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    private fun updateExercise(
        UID: String,
        exercise: Exercise,
        onComplete: (Boolean) -> Unit
    ) {
        db.collection("users").document(UID)
            .collection("exercises")
            .document(exercise.exerciseID)
            .update(exerciseToMap(exercise))
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    private fun deleteExercise(
        UID: String,
        exerciseID: String,
        onComplete: (Boolean) -> Unit
    ) {
        db.collection("users").document(UID)
            .collection("exercises")
            .document(exerciseID)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    private fun getExercises(
        UID: String,
        onComplete: (List<Exercise>?) -> Unit
    ) {
        db.collection("users").document(UID)
            .collection("exercises")
            .get()
            .addOnSuccessListener { snap ->
                onComplete(snap.documents.mapNotNull { mapToExercise(it) })
            }
            .addOnFailureListener { onComplete(null) }
    }

    private fun uploadRoutine(
        UID: String,
        routine: Routine,
        onComplete: (Boolean) -> Unit
    ) {
        db.collection("users").document(UID)
            .collection("routines")
            .document(routine.routineID)
            .set(routineToMap(routine))
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    private fun updateRoutine(
        UID: String,
        routine: Routine,
        onComplete: (Boolean) -> Unit
    ) {
        db.collection("users").document(UID)
            .collection("routines")
            .document(routine.routineID)
            .update(routineToMap(routine))
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    private fun getRoutines(
        UID: String,
        onComplete: (List<Routine>?) -> Unit
    ) {
        db.collection("users").document(UID)
            .collection("routines")
            .get()
            .addOnSuccessListener { snap ->
                onComplete(snap.documents.mapNotNull { mapToRoutine(it) })
            }
            .addOnFailureListener { onComplete(null) }
    }

    private fun deleteRoutine(
        UID: String,
        routineID: String,
        onComplete: (Boolean) -> Unit
    ) {
        db.collection("users").document(UID)
            .collection("routines")
            .document(routineID)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    private fun updateRoutineName(
        UID: String,
        routineID: String,
        newName: String,
        onComplete: (Boolean) -> Unit
    ) {
        db.collection("users").document(UID)
            .collection("routines")
            .document(routineID)
            .update("routineName", newName)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    private fun addExerciseToRoutine(
        UID: String,
        routineID: String,
        item: ExerciseInRoutine,
        onComplete: (Boolean) -> Unit
    ) {

        if (routineID.isBlank()) {
            android.util.Log.e("Firestore", "addExerciseToRoutine: empty routineID")
            onComplete(false); return
        }


        val ref = db.collection("users").document(UID)
            .collection("routines")
            .document(routineID)

        db.runTransaction { tr ->
            val snap = tr.get(ref)
            val current = (snap.get("list") as? List<*>)?.mapNotNull { it as? Map<*, *> } ?: emptyList()
            val updated = current + exerciseInRoutineToMap(item)
            tr.update(ref, "list", updated)
        }.addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    private fun removeExerciseFromRoutine(
        UID: String,
        routineID: String,
        exerciseID: String,
        onComplete: (Boolean) -> Unit
    ) {
        val ref = db.collection("users").document(UID)
            .collection("routines")
            .document(routineID)

        db.runTransaction { tr ->
            val snap = tr.get(ref)
            val current = (snap.get("list") as? List<*>)?.mapNotNull { it as? Map<*, *> } ?: emptyList()
            val updated = current.filterNot { it["exerciseID"] == exerciseID }
            tr.update(ref, "list", updated)
        }.addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    private fun updateExerciseInRoutine(
        UID: String,
        routineID: String,
        item: ExerciseInRoutine,
        onComplete: (Boolean) -> Unit
    ) {
        val ref = db.collection("users").document(UID)
            .collection("routines")
            .document(routineID)

        db.runTransaction { tr ->
            val snap = tr.get(ref)
            val current = (snap.get("list") as? List<*>)?.mapNotNull { it as? Map<*, *> } ?: emptyList()
            val updated = current.map {
                if (it["exerciseID"] == item.exerciseID) exerciseInRoutineToMap(item) else it
            }
            tr.update(ref, "list", updated)
        }.addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // ---------- Public wrappers (לשימוש ה-ViewModels) ----------

    fun uploadExercise(ex: Exercise, onComplete: (Boolean) -> Unit) {
        val uid = uidOrNull()
        if (uid == null) { onComplete(false); return }

        val coll = db.collection("users").document(uid).collection("exercises")

        // אם ה-ID ריק/מרווחים – ניצור מסמך חדש ונשתמש ב-ID שלו
        val docRef = if (ex.exerciseID.isBlank()) coll.document() else coll.document(ex.exerciseID)
        val toSave = ex.copy(exerciseID = docRef.id)

        docRef.set(exerciseToMap(toSave))
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { e ->
                // טוב לצרף לוג כדי לראות למה נכשל
                android.util.Log.e("Firestore", "uploadExercise failed", e)
                onComplete(false)
            }
    }

    fun updateExercise(ex: Exercise, onComplete: (Boolean) -> Unit) {
        val uid = uidOrNull()
        if (uid == null) { onComplete(false); return }
        updateExercise(uid, ex, onComplete)
    }

    fun deleteExercise(exerciseId: String, onComplete: (Boolean) -> Unit) {
        val uid = uidOrNull()
        if (uid == null) { onComplete(false); return }
        deleteExercise(uid, exerciseId, onComplete)
    }

    fun getExercises(onResult: (List<Exercise>?) -> Unit) {
        val uid = uidOrNull()
        if (uid == null) { onResult(emptyList()); return }
        getExercises(uid, onResult)
    }

    fun uploadRoutine(routine: Routine, onComplete: (Boolean) -> Unit) {
        val uid = uidOrNull()
        if (uid == null) { onComplete(false); return }
        uploadRoutine(uid, routine, onComplete)
    }

    fun updateRoutine(routine: Routine, onComplete: (Boolean) -> Unit) {
        val uid = uidOrNull()
        if (uid == null) { onComplete(false); return }
        updateRoutine(uid, routine, onComplete)
    }

    fun deleteRoutine(routineId: String, onComplete: (Boolean) -> Unit) {
        val uid = uidOrNull()
        if (uid == null) { onComplete(false); return }
        deleteRoutine(uid, routineId, onComplete)
    }

    fun getRoutines(onResult: (List<Routine>?) -> Unit) {
        val uid = uidOrNull()
        if (uid == null) { onResult(emptyList()); return }
        getRoutines(uid, onResult)
    }

    fun updateRoutineName(routineId: String, newName: String, onComplete: (Boolean) -> Unit) {
        val uid = uidOrNull()
        if (uid == null) { onComplete(false); return }
        updateRoutineName(uid, routineId, newName, onComplete)
    }

    fun addExerciseToRoutine(
        routineId: String,
        item: ExerciseInRoutine,
        onComplete: (Boolean) -> Unit
    ) {
        val uid = uidOrNull()
        if (uid == null) {
            android.util.Log.e("Firestore", "addExerciseToRoutine: uid is null (not logged in)")
            onComplete(false); return
        }
        if (routineId.isBlank()) {
            android.util.Log.e("Firestore", "addExerciseToRoutine: routineId is blank")
            onComplete(false); return
        }
        addExerciseToRoutine(uid, routineId, item, onComplete)
    }


    fun removeExerciseFromRoutine(routineId: String, exerciseId: String, onComplete: (Boolean) -> Unit) {
        val uid = uidOrNull()
        if (uid == null) { onComplete(false); return }
        removeExerciseFromRoutine(uid, routineId, exerciseId, onComplete)
    }

    fun updateExerciseInRoutine(routineId: String, item: ExerciseInRoutine, onComplete: (Boolean) -> Unit) {
        val uid = uidOrNull()
        if (uid == null) { onComplete(false); return }
        updateExerciseInRoutine(uid, routineId, item, onComplete)
    }

  // ---------- אופציונלי: פונקציות suspend לשימוש בקורוטינות ----------
  suspend fun createRoutine(routineName: String): Routine = withContext(Dispatchers.IO) {
      // זורק אם אין התחברות — דאג שקודם ביצעת Login
      val uid = currentUid()

      val doc = Firebase.firestore
          .collection("users")
          .document(uid)
          .collection("routines")
          .document() // id חדש


      
      val routine = Routine(
          routineID = doc.id,
          routineName = routineName,
          exerciseIDs = mutableListOf() // לפי המודל שלך: MutableList<ExerciseInRoutine>
      )

      // אפשר לשמור ישירות את ה-data class
      doc.set(routineToMap(routine)).await()

      routine
  }

    fun uploadUser(user: User, onComplete: (Boolean) -> Unit) {
        val userMap = hashMapOf(
            "userId" to user.userID,
            "UID" to user.userID,
            "name" to user.name
        )
        db.collection("users").document(user.userID)
            .set(userMap)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun listenRoutines(onChange: (List<Routine>) -> Unit): ListenerRegistration {
        val uid = currentUid()
        return db.collection("users")
            .document(uid)
            .collection("routines")
            .addSnapshotListener { snap, e ->
                if (e != null || snap == null) return@addSnapshotListener
                val list = snap.documents.mapNotNull { mapToRoutine(it) }
                onChange(list)
            }
    }

    private fun DocumentSnapshot.toRoutine(): Routine {
        val rawList = get("list") as? List<*> ?: emptyList<Any>()
        val parsed = rawList.mapNotNull { it as? Map<*, *> }
            .mapNotNull { mapToExerciseInRoutine(it) }
            .toMutableList()
        return Routine(
            routineID = id,
            routineName = getString("routineName") ?: "",
            exerciseIDs = parsed
        )
    }

}
