package com.example.training_weaver.ViewModels

import android.util.Log
import androidx.compose.animation.core.snap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.training_weaver.dataclass.Exercise
import com.example.training_weaver.utilities.FirebaseConnections
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


class ExerciseDatabaseViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val repo = FirebaseConnections()
    private val auth = FirebaseAuth.getInstance()
    private val _exercises = MutableLiveData<List<Exercise>>(emptyList())
    val exercises: LiveData<List<Exercise>> = _exercises

    val error = MutableLiveData<String?>()

    /** טוען את כל התרגילים של המשתמש הנוכחי */
    fun load() {
        repo.getExercises { list ->
            _exercises.postValue(list ?: emptyList())
        }
    }

    /** יצירת תרגיל חדש ושמירה בענן */
    fun create(exercise: Exercise, onDone: (Boolean) -> Unit = {}) {
        repo.uploadExercise(exercise) { ok ->
            if (!ok) error.postValue("Create exercise failed")
            load()
            onDone(ok)
        }
    }

    /** עדכון תרגיל קיים */
    fun updateExercise(exercise: Exercise, onDone: (Boolean) -> Unit = {}) {
        repo.updateExercise(exercise) { ok ->
            if (!ok) error.postValue("Update exercise failed")
            load()
            onDone(ok)
        }
    }

    /** מחיקת תרגיל */
    fun delete(exerciseId: String, onDone: (Boolean) -> Unit = {}) {
        repo.deleteExercise(exerciseId) { ok ->
            if (!ok) error.postValue("Delete exercise failed")
            load()
            onDone(ok)
        }
    }


    private var listener: ListenerRegistration? = null

    fun startListening() {
        val uid = auth.currentUser?.uid ?: return
        listener?.remove()
        listener = db.collection("users")
            .document(uid)
            .collection("exercises")
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    Log.e("ExercisesVM", "listen failed", e)
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { doc ->
                    val m = doc.data ?: return@mapNotNull null
                    Exercise(
                        exerciseID = m["exerciseID"] as? String ?: doc.id,
                        exerciseName = m["exerciseName"] as? String ?: return@mapNotNull null,
                        exerciseDescription = m["exerciseDescription"] as? String ?: "",
                        url = m["url"] as? String ?: ""
                    )
                } ?: emptyList()
                _exercises.postValue(list)
            }

    }

    fun stopListening() {
        listener?.remove()
        listener = null
    }

    override fun onCleared() {
        stopListening()
        super.onCleared()
    }
}
