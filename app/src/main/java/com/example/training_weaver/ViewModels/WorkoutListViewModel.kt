package com.example.training_weaver.ViewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.training_weaver.dataclass.ExerciseInRoutine
import com.example.training_weaver.dataclass.Routine
import com.example.training_weaver.utilities.FirebaseConnections
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkoutListViewModel : ViewModel() {

    private val repo = FirebaseConnections()

    private var routinesListener: ListenerRegistration? = null
    private val _routines = MutableLiveData<List<Routine>>(emptyList())
    val routines: LiveData<List<Routine>> = _routines

    init {
        reload()
    }

    /** טען מחדש את כל רשימות האימונים של המשתמש */
    fun reload() {
        repo.getRoutines { list ->
            _routines.postValue(list ?: emptyList())
        }
    }


    // יצירת רשימה חדשה (עם id חדש מה-Firestore)

    fun createRoutine(name: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val newRoutine = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    repo.createRoutine(name)   // suspend שמוסיף בפיירבייס ומחזיר Routine
                }
                // להכניס לראש הרשימה כדי שיופיע ראשון
                val cur = _routines.value?.toMutableList() ?: mutableListOf()
                cur.add(0, newRoutine)
                _routines.postValue(cur)
                onComplete(true)
            } catch (t: Throwable) {
                t.printStackTrace()
                onComplete(false)
            }
        }
    }

    fun addExerciseToRoutine(
        routineId: String,
        item: ExerciseInRoutine,
        onComplete: (Boolean) -> Unit = {}
    ) {
        // כתיבה לפיירבייס
        repo.addExerciseToRoutine(routineId, item) { ok ->
            if (ok) {
                // עדכון מהיר של ה-LiveData כדי שה־UI יתעדכן מיד
                val current = _routines.value.orEmpty()
                val updated = current.map { r ->
                    if (r.routineID == routineId) {
                        r.copy(exerciseIDs = (r.exerciseIDs + item).toMutableList())
                    } else r
                }
                _routines.postValue(updated)

                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    fun removeExerciseFromRoutine(
        routineId: String,
        exerciseId: String,
        onComplete: (Boolean) -> Unit = {}
    ) {
        repo.removeExerciseFromRoutine(routineId, exerciseId) { ok ->
            if (ok) {
                val current = _routines.value.orEmpty()
                val updated = current.map { r ->
                    if (r.routineID == routineId) {
                        r.copy(exerciseIDs = r.exerciseIDs
                            .filterNot { it.exerciseID == exerciseId }
                            .toMutableList()
                        )
                    } else r
                }
                _routines.postValue(updated)
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    /** מחיקת רשימה קיימת */
    fun deleteRoutine(routine: Routine) {
        repo.deleteRoutine(routine.routineID) { ok ->
            if (ok) reload()
        }
    }

    /** עדכון שם רשימה */
    fun renameRoutine(routineId: String, newName: String) {
        repo.updateRoutineName(routineId, newName) { ok ->
            if (ok) reload()
        }
    }

    fun loadRoutines() {
        repo.getRoutines { list ->
            _routines.postValue(list ?: emptyList())
        }
    }

    fun startRealtime() {
        stopRealtime() // להימנע מאזינים כפולים
        routinesListener = repo.listenRoutines { list ->
            _routines.postValue(list)
        }
    }

    fun stopRealtime() {
        routinesListener?.remove()
        routinesListener = null
    }

    override fun onCleared() {
        super.onCleared()
        stopRealtime()
    }
}

