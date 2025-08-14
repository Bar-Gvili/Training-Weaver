package com.example.training_weaver.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.training_weaver.ViewModels.WorkoutListViewModel
import com.example.training_weaver.dataclass.Routine
import com.example.training_weaver.utilities.WorkoutListAdapter
import com.training_weaver.R
import com.training_weaver.databinding.FragmentWorkoutListBinding


class WorkoutListFragment : Fragment() {
    private var _b: FragmentWorkoutListBinding? = null
    private val b get() = _b!!
    private val vm: WorkoutListViewModel by activityViewModels()
    private lateinit var adapter: WorkoutListAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentWorkoutListBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.startRealtime()

        // אדפטר עם פעולות עריכה/מחיקה
        adapter = WorkoutListAdapter(object : WorkoutListAdapter.OnItemActionListener {
            override fun onEdit(r: Routine) = openRoutineDetails(r)
            override fun onDelete(r: Routine) = confirmAndDelete(r)
        })

        // RecyclerView
        b.recyclerViewRoutines.layoutManager = LinearLayoutManager(requireContext())
        b.recyclerViewRoutines.adapter = adapter

        // FAB – מעבר למסך בחירת שם לרשימה חדשה
        b.newRoutine.setOnClickListener {
            findNavController().navigate(R.id.action_workoutListFragment_to_workoutNameFragment)
        }

        // האזנה לשינויים ברשימות האימונים
        vm.routines.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list.toList()) // יוצרים עותק כדי לעדכן DiffUtil
        }

        // טעינה ראשונית
        vm.reload()
    }

    private fun openRoutineDetails(r: Routine) {
        // ניווט למסך פירוט הרשימה
        findNavController().navigate(
            WorkoutListFragmentDirections
                .actionWorkoutListFragmentToWorkoutDetailsFragment(r)
        )
    }

    private fun confirmAndDelete(r: Routine) {
                vm.deleteRoutine(r) // זה כבר מעדכן דרך ה-ViewModel
    }

    override fun onDestroyView() {
        super.onDestroyView()
        vm.stopRealtime()
    }
}
