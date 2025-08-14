package com.example.training_weaver.UI

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.training_weaver.dataclass.Exercise
import com.training_weaver.R
import com.training_weaver.databinding.FragmentAddExerciseBinding

//fragment with form to add exercises to database of the user
class addExerciseFragment : Fragment() {

    private var _binding: FragmentAddExerciseBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //navigate to exerciseDatabaseFragment with the new exercise
        binding.btnAddExercise.setOnClickListener {
            val exercise = getExerciseFromFrom() ?: return@setOnClickListener
            val action = addExerciseFragmentDirections.actionAddExerciseFragmentToExerciseDatabaseFragment(exercise)
            findNavController().navigate(action)
        }
    }

    private fun getExerciseFromFrom(): Exercise?{
        val name=  binding.etExerciseName.text.toString()
        val description=  binding.etExerciseDescription.text.toString()
        val url=  binding.etExerciseUrl.text.toString()
        //check if all fields are filled
        if (name.isEmpty() || url.isEmpty()) {
            Toast.makeText(requireContext(), "please fill all non optional fields", Toast.LENGTH_SHORT).show()
            return null
        }

        return Exercise(exerciseName = name, exerciseDescription = description, url = url)

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

