package com.example.training_weaver.UI

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.training_weaver.ViewModels.ExerciseDatabaseViewModel
import com.example.training_weaver.ViewModels.WorkoutListViewModel
import com.example.training_weaver.utilities.RoutineExercisesAdapter
import com.example.training_weaver.utilities.FirebaseConnections

import com.training_weaver.databinding.FragmentWorkoutDetailBinding
import com.training_weaver.utilities.RoutineRow

class WorkoutDetailsFragment : Fragment() {

    // 1) קבלת ה-Args וה-ViewModels
    private val args: WorkoutDetailsFragmentArgs by navArgs()
    private val vmRoutines: WorkoutListViewModel           by activityViewModels()
    private val vmExercises: ExerciseDatabaseViewModel     by activityViewModels()
    private var _b: FragmentWorkoutDetailBinding? = null
    private val b get() = _b!!
    private val repo = FirebaseConnections()


    private lateinit var adapter: RoutineExercisesAdapter
    //private lateinit var adapter: ExerciseAdapter

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

        adapter = RoutineExercisesAdapter(object : RoutineExercisesAdapter.OnRowAction {
            override fun onClick(row: RoutineRow) {
                // אפשר לפתוח כאן Editor ל-sets/reps
            }
            override fun onRemove(row: RoutineRow) {
                // הפונקציה למטה מקבלת רק אינדקס, את ה־routineId היא שולפת מ-args
                removeExerciseAtIndex(row.indexInRoutine)
            }
        })

        b.recyclerViewWorkoutExercises.layoutManager = LinearLayoutManager(requireContext())
        b.recyclerViewWorkoutExercises.adapter = adapter

        vmExercises.exercises.observe(viewLifecycleOwner) { allExercises ->
            val rows = args.routine.exerciseIDs.mapIndexedNotNull { index, item ->
                val ex = allExercises.firstOrNull { it.exerciseID == item.exerciseID }
                    ?: return@mapIndexedNotNull null
                RoutineRow(
                    key = "${item.exerciseID}#$index",
                    exercise = ex,
                    meta = item,
                    indexInRoutine = index
                )
            }
            adapter.submitList(rows)
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

    private fun removeExerciseAtIndex(index: Int) {
        val routineId = args.routine.routineID
        repo.removeExerciseAtIndex(routineId, index) { ok ->
            if (ok) vmRoutines.reload()
            else Toast.makeText(requireContext(), "מחיקה נכשלה", Toast.LENGTH_SHORT).show()
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
