package com.example.training_weaver.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels           // CHANGED: for sharing VM
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs            // CHANGED: for SafeArgs
import androidx.room.util.copy
import com.example.training_weaver.ViewModels.ExerciseDatabaseViewModel
import com.example.training_weaver.dataclass.Exercise
import com.training_weaver.databinding.FragmentEditExerciseBinding

class EditExerciseFragment : Fragment() {

    // CHANGED: view-binding setup
    private var _binding: FragmentEditExerciseBinding? = null
    private val binding get() = _binding!!

    // CHANGED: pull in the arg named “exercise”
    private val args: EditExerciseFragmentArgs by navArgs()

    // CHANGED: share the same VM so we can update
    private val vm: ExerciseDatabaseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // CHANGED: get the passed Exercise and pre-fill the form
        val exercise: Exercise = args.exercise
        binding.etExerciseName.setText(exercise.exerciseName)
        binding.etExerciseUrl.setText(exercise.url)
        binding.etExerciseDescription.setText(exercise.exerciseDescription)

        // CHANGED: Save button logic
        binding.btnEditExercise.setOnClickListener {
            val name = binding.etExerciseName.text.toString().trim()
            val url  = binding.etExerciseUrl.text.toString().trim()
            val desc = binding.etExerciseDescription.text.toString().trim()

            if (name.isEmpty() || url.isEmpty()) {
                Toast.makeText(requireContext(),
                    "Please fill in name & URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create a copy with the same ID, new values
            val updated = exercise.copy(
                exerciseName = name,
                exerciseDescription = desc,
                url = url
            )

            // Update in the ViewModel
            vm.updateExercise(updated)

            // Go back
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
