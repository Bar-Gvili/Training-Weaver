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
import com.google.android.material.snackbar.Snackbar
import com.training_weaver.databinding.FragmentExerciseDatabaseBinding

class ExerciseDatabaseFragment : Fragment() {

    private val vm: ExerciseDatabaseViewModel by activityViewModels()
    private val args: ExerciseDatabaseFragmentArgs by navArgs()

    private var _binding: FragmentExerciseDatabaseBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ExerciseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseDatabaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null)
        {
            findNavController().navigate(ExerciseDatabaseFragmentDirections.actionExerciseDatabaseFragmentToLoginFragment())
            Snackbar.make(view, "Login needed to add exercises", Snackbar.LENGTH_LONG).show()
        }
        // אם הגיע exercise מהמסך Add – צור אותו בבסיס הנתונים
        args.exercise?.let { ex ->
            vm.create(ex) { ok ->
                if (ok) {
                    // השמירה הצליחה – נטען מחדש כדי לוודא רענון מיידי (בנוסף לליסנר)
                    vm.load()
                } else {
                    // תן פידבק אם נכשל (למשל אם לא מחובר)
                    Snackbar.make(binding.root, "Failed to save exercise (are you logged in?)", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        // אתחול RecyclerView + מאזינים
        adapter = ExerciseAdapter(object : ExerciseAdapter.OnItemActionListener {
            override fun onEdit(exercise: Exercise) {
                // נווט למסך העריכה (אם יש לך action כזה בגרף)
                 findNavController().navigate(
                     ExerciseDatabaseFragmentDirections.actionExerciseDatabaseFragmentToEditExerciseFragment(exercise)
                 )
            }

            override fun onDelete(exercise: Exercise) {
                vm.delete(exercise.exerciseID)   // ← במקום removeExercise
            }
        })
        binding.recyclerViewExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewExercises.adapter = adapter

        // הקשבה לעדכונים מהרשימה – מציינים טיפוס מפורש ללמבדה
        vm.exercises.observe(viewLifecycleOwner) { list: List<Exercise> ->
            adapter.submitList(list.toList())
        }

        // טעינה ראשונית (אם עוד לא נטען)
        if (vm.exercises.value == null) vm.load()

        // FAB – למסך הוספת תרגיל
        binding.addExercise.setOnClickListener {
            val action =
                ExerciseDatabaseFragmentDirections.actionExerciseDatabaseFragmentToAddExerciseFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        vm.load()
    }

    override fun onStop() {
        vm.stopListening()    // מפסיק כשהמסך יוצא מהחזית
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        vm.startListening()   // מתחיל להקשיב ל-Firestore
    }

}
