package com.example.training_weaver.UI

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.training_weaver.ViewModels.WorkoutListViewModel
import com.example.training_weaver.dataclass.ExerciseInRoutine
import com.training_weaver.R
import com.training_weaver.databinding.FragmentWorkoutEditorBinding

class WorkoutEditorFragment : Fragment(R.layout.fragment_workout_editor) {

    private val args by navArgs<WorkoutEditorFragmentArgs>()
    private val vm: WorkoutListViewModel by activityViewModels()

    private var _b: FragmentWorkoutEditorBinding? = null
    private val b get() = _b!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _b = FragmentWorkoutEditorBinding.bind(view) // ← היה חסר, גרם ל-NPE

        // אם תרצה להציג את שם התרגיל שנשלח:
        // b.tvExerciseTitle.text = args.exerciseName

        b.btnDone.setOnClickListener {
            val sets = b.etSets.text?.toString()?.toIntOrNull()
            val reps = b.etReps.text?.toString()?.toIntOrNull()
            val rest = b.etRestTime.text?.toString()?.toIntOrNull()

            if (sets == null || reps == null || rest == null) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val item = ExerciseInRoutine(
                exerciseID = args.exerciseID,   // ודא שהשם תואם ל־nav_graph
                sets = sets,
                reps = reps,
                restTimeSeconds = rest
            )

            vm.addExerciseToRoutine(args.routineID, item) { ok ->
                if (ok) {
                    findNavController().popBackStack() // חזרה למסך בחירת התרגילים
                } else {
                    Toast.makeText(requireContext(), "Failed to add exercise", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
