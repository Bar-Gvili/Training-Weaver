package com.example.training_weaver.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.training_weaver.ViewModels.ExerciseDatabaseViewModel
import com.example.training_weaver.dataclass.Exercise
import com.example.training_weaver.utilities.ExerciseAdapter
import com.training_weaver.databinding.FragmentAllExercisesToAddToListBinding

class AllExercisesToAddToListFragment : Fragment() {

    private var _binding: FragmentAllExercisesToAddToListBinding? = null
    private val binding get() = _binding!!

    private val args: AllExercisesToAddToListFragmentArgs by navArgs()
    private val vmExercises: ExerciseDatabaseViewModel by activityViewModels()

    private lateinit var adapter: ExerciseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllExercisesToAddToListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // כותרת
        binding.header.text = "All Exercises"

        // אדפטר: לחיצה רגילה על פריט => מעבר למסך העורך
        adapter = ExerciseAdapter(object : ExerciseAdapter.OnItemActionListener {
            override fun onEdit(ex: Exercise) {
                val action =
                    AllExercisesToAddToListFragmentDirections
                        .actionAllExercisesToEditor(
                            routineID = args.routineID,      // השם לפי ה-args שלך
                            exerciseID = ex.exerciseID,      // מזהה התרגיל
                            exerciseName = ex.exerciseName   // להצגה במסך הבא (אם צריך)
                        )
                findNavController().navigate(action)
            }

            override fun onDelete(ex: Exercise) {
                // במסך הזה לא מוחקים כלום, אין פעולה
            }
        },enableContextMenu = false)

        binding.recyclerAllExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerAllExercises.adapter = adapter

        // מאזינים לרשימת התרגילים
        vmExercises.exercises.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list.toList())
        }

        // טעינה ראשונית
        vmExercises.load()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
