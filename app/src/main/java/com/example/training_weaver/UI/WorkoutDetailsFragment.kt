package com.example.training_weaver.UI

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.training_weaver.dataclass.Exercise
import com.example.training_weaver.utilities.ExerciseAdapter
import com.example.training_weaver.ViewModels.ExerciseDatabaseViewModel
import com.example.training_weaver.ViewModels.WorkoutListViewModel
import com.example.training_weaver.utilities.FirebaseConnections
import com.example.training_weaver.utilities.RoutineExercisesAdapter
import com.training_weaver.databinding.FragmentWorkoutDetailBinding

class WorkoutDetailsFragment : Fragment() {

    // 1) קבלת ה-Args וה-ViewModels
    private val args: WorkoutDetailsFragmentArgs by navArgs()
    private val vmRoutines: WorkoutListViewModel           by activityViewModels()
    private val vmExercises: ExerciseDatabaseViewModel     by activityViewModels()
    private lateinit var adapterr: RoutineExercisesAdapter
    private var _b: FragmentWorkoutDetailBinding? = null
    private val b get() = _b!!
    private val repo = FirebaseConnections() 


    private lateinit var adapter: ExerciseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentWorkoutDetailBinding
        .inflate(inflater, container, false)
        .also { _b = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 2) מאפסים את ה-RecyclerView
        adapter = ExerciseAdapter(object : ExerciseAdapter.OnItemActionListener {
            override fun onEdit(ex: Exercise) {
                // כאן תוכל לפתוח מסך עריכה אם תרצה
            }
            override fun onDelete(ex: Exercise) {
                // 3) מחיקת תרגיל מהרשימה בעבודה עם ה-ViewModel של ה-Routine
                val updatedIds = args.routine
                    .exerciseIDs
                    .filter { it.exerciseID != ex.exerciseID }
                    .toMutableList()

                val newRoutine = args.routine.copy(
                    exerciseIDs = updatedIds
                )
               // vmRoutines.addWorkout(newRoutine) // מעדכן/מוחק ב-ViewModel
            }
        })
        b.recyclerViewWorkoutExercises.layoutManager = LinearLayoutManager(requireContext())
        b.recyclerViewWorkoutExercises.adapter = adapter

        // 4) מאזינים למסד התרגילים וממפים IDs -> אובייקטים
        vmExercises.exercises.observe(viewLifecycleOwner) { allExercises ->
            val inThisRoutine = allExercises.filter { exercise ->
                args.routine.exerciseIDs
                    .any { it.exerciseID == exercise.exerciseID }
            }
            adapter.submitList(inThisRoutine)
        }

        // 5) FAB להוספת תרגיל חדש
        val routine = args.routine
        b.fabAddExercise.setOnClickListener {
            findNavController().navigate(
                WorkoutDetailsFragmentDirections
                    .actionWorkoutDetailsToAllExercises(routine.routineID)
            )
        }
    }

    fun removeExerciseAtIndex(
        routineId: String,
        index: Int,
        onComplete: (Boolean) -> Unit = {}) {
        vmRoutines.launch {
            try {
                repo.removeExerciseAtIndex(routineId, index) // suspend – כתוב למטה ב-FirebaseConnections
                // לרענן את כל הרוטינות, או לעדכן ידנית רק את הרוטינה הזו.
                reload()
                onComplete(true)
            } catch (t: Throwable) {
                onComplete(false)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // מפעיל since-started listener כדי ששינויים ייכנסו מייד
        vmRoutines.startRealtime()
    }

    override fun onStop() {
        super.onStop()
        // משחרר את ה-listener כשעוזבים את המסך
        vmRoutines.stopRealtime()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
