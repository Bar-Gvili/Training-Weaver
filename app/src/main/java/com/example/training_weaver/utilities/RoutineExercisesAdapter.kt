package com.example.training_weaver.utilities

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.training_weaver.UI.WorkoutDetailsFragmentDirections
import com.training_weaver.R
import com.training_weaver.utilities.RoutineRow
import com.training_weaver.databinding.ItemRoutineRowBinding


class RoutineExercisesAdapter(
    private val onRowAction: OnRowAction
) : ListAdapter<RoutineRow, RoutineExercisesAdapter.VH>(DIFF) {

    interface OnRowAction {
        fun onRemove(row: RoutineRow)
        fun onClick(row: RoutineRow)
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<RoutineRow>() {
            override fun areItemsTheSame(oldItem: RoutineRow, newItem: RoutineRow) =
                oldItem.key == newItem.key
            override fun areContentsTheSame(oldItem: RoutineRow, newItem: RoutineRow) =
                oldItem == newItem
        }
    }

    inner class VH(private val b: ItemRoutineRowBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(row: RoutineRow) {
            b.title.text = row.exercise.exerciseName
            b.subtitle.text = "${row.meta.sets} x ${row.meta.reps}  •  rest ${row.meta.restTimeSeconds}s"

            // לחיצה רגילה – כניסה/עריכה
            b.root.setOnClickListener {
                val action = WorkoutDetailsFragmentDirections
                    .actionWorkoutDetailsFragmentToExercisePlayerFragment(
                        row.exercise,
                        row.meta
                    )
                it.findNavController().navigate(action)
            }
            // לחיצה ארוכה – מחיקה (בלי כפתור)
            b.root.setOnLongClickListener {
                val popup = PopupMenu(b.root.context, b.root)
                popup.menuInflater.inflate(R.menu.exercise_row_menu, popup.menu)
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_edit -> {
                            onRowAction.onClick(row)   // נשתמש ב־onClick כ"עריכה"
                            true
                        }
                        R.id.menu_delete -> {
                            onRowAction.onRemove(row)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inf = LayoutInflater.from(parent.context)
        val binding = ItemRoutineRowBinding.inflate(inf, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}