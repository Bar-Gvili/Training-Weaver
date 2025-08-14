package com.example.training_weaver.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.training_weaver.ViewModels.WorkoutListViewModel
import com.training_weaver.R
import com.training_weaver.databinding.FragmentWorkoutNameBinding

class WorkoutNameFragment : Fragment() {

    private var _binding: FragmentWorkoutNameBinding? = null
    private val binding get() = _binding!!

    private val vm: WorkoutListViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutNameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnDone.setOnClickListener {
            val name = binding.etWorkoutName.text.toString().trim()
            if (name.isEmpty()) {
                binding.etWorkoutName.error = "Please enter a name"
                return@setOnClickListener
            }

            vm.createRoutine(name) { ok ->
                binding.btnDone.isEnabled = true
                if (ok) {
                    // חזרה למסך הקודם:
                    findNavController().navigate(WorkoutNameFragmentDirections.actionWorkoutNameFragmentToWorkoutListFragment())

                //findNavController().navigateUp()

                } else {
                    Toast.makeText(requireContext(), "Create failed, try again", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
