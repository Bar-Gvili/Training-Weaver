package com.example.training_weaver.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.training_weaver.databinding.FragmentEditExerciseInRoutineBinding
import com.example.training_weaver.dataclass.ExerciseInRoutine
import com.example.training_weaver.utilities.FirebaseConnections

class EditExerciseInRoutineFragment : Fragment() {

    private val args: EditExerciseInRoutineFragmentArgs by navArgs()
    private var _b: FragmentEditExerciseInRoutineBinding? = null
    private val b get() = _b!!
    private val repo = FirebaseConnections()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _b = FragmentEditExerciseInRoutineBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ממלא ערכים נוכחיים לטופס
        val m = args.meta
        b.etSets.setText(m.sets.toString())
        b.etReps.setText(m.reps.toString())
        b.etRestTime.setText(m.restTimeSeconds.toString())

        // שמירה
        b.btnEdit.setOnClickListener {
            val sets = b.etSets.text.toString().toIntOrNull()
            val reps = b.etReps.text.toString().toIntOrNull()
            val rest = b.etRestTime.text.toString().toIntOrNull()

            if (sets == null || reps == null || rest == null) {
                Toast.makeText(requireContext(), "נא למלא מספרים תקינים", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updated = ExerciseInRoutine(
                exerciseID = m.exerciseID, // לא משנים את התרגיל עצמו, רק את המטא
                sets = sets,
                reps = reps,
                restTimeSeconds = rest
            )

            repo.updateExerciseAtIndex(
                routineId = args.routineID,
                index = args.indexInRoutine,
                newMeta = updated
            ) { ok ->
                if (ok) {
                    Toast.makeText(requireContext(), "עודכן", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(requireContext(), "עדכון נכשל", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
