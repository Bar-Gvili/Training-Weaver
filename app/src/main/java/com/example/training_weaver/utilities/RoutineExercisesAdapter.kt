package com.example.training_weaver.utilities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.training_weaver.databinding.RowExerciseBinding
import com.training_weaver.utilities.RoutineRow

class RoutineExercisesAdapter(
    private val listener: OnRowAction
) : ListAdapter<RoutineRow, RoutineExercisesAdapter.VH>(DIFF) {

    interface OnRowAction {
        fun onRemove(row: RoutineRow)
    }

    inner class VH(private val b: RowExerciseBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(row: RoutineRow) {
            // כותרת ושורת משנה – עדכן לשמות ה-View אצלך ב-binding (title/subtitle/...)
            b.title.text = row.exercise.exerciseName
            b.subtitles.text = "Sets: ${row.meta.sets} • Reps: ${row.meta.reps} • Rest: ${row.meta.restTimeSeconds}s"

            // לחיצה ארוכה → הסרת המופע מהרוטינה (אפשר לפתוח תפריט אם תרצה; פה זה ישר מסיר)
            b.root.setOnLongClickListener {
                listener.onRemove(row)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RowExerciseBinding.inflate(inflater, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<RoutineRow>() {
            override fun areItemsTheSame(oldItem: RoutineRow, newItem: RoutineRow): Boolean =
                oldItem.key == newItem.key

            override fun areContentsTheSame(oldItem: RoutineRow, newItem: RoutineRow): Boolean =
                oldItem == newItem
        }
    }
}
