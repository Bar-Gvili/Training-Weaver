/*
package com.example.training_weaver.utilities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.training_weaver.dataclass.Exercise
import com.training_weaver.databinding.ItemExerciseBinding

class ExerciseAdapter(
    private val listener: OnItemActionListener,
    private val enableContextMenu: Boolean = true
) : ListAdapter<Exercise, ExerciseAdapter.ViewHolder>(DiffCallback()) {

    private var clickListener: ((Exercise)->Unit)? = null
    fun setOnItemClickListener(l: (Exercise) -> Unit) {
        clickListener = l
    }

    interface OnItemActionListener {
        fun onEdit(exercise: Exercise)
        fun onDelete(exercise: Exercise)
    }


    inner class ViewHolder(val binding: ItemExerciseBinding)
        : RecyclerView.ViewHolder(binding.root) {
        init {
            // לחיצה רגילה
            binding.root.setOnClickListener {
                clickListener?.invoke(getItem(bindingAdapterPosition))
            }


            // לחיצה ארוכה (Popupmenu לעריכה/מחיקה)
            binding.root.setOnLongClickListener {
                PopupMenu(binding.root.context, binding.root).apply {
                    menu.add("Edit").setOnMenuItemClickListener {
                        listener.onEdit(getItem(bindingAdapterPosition))
                        true
                    }
                    menu.add("Delete").setOnMenuItemClickListener {
                        listener.onDelete(getItem(bindingAdapterPosition))
                        true
                    }
                }.show()
                true
            }
        }

        private fun showPopup(anchor: View, pos: Int) {
            val popup = PopupMenu(anchor.context, anchor)
            popup.menu.apply {
                add("Edit").setOnMenuItemClickListener {
                    // ← here we explicitly call the adapter's getItem(...)
                    listener.onEdit(this@ExerciseAdapter.getItem(pos))
                    true
                }
                add("Delete").setOnMenuItemClickListener {
                    listener.onDelete(this@ExerciseAdapter.getItem(pos))
                    true
                }
            }
            popup.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemExerciseBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.tvName.text = getItem(position).exerciseName
    }

    class DiffCallback : DiffUtil.ItemCallback<Exercise>() {
        override fun areItemsTheSame(oldItem: Exercise, newItem: Exercise) =
            oldItem.exerciseID == newItem.exerciseID

        override fun areContentsTheSame(oldItem: Exercise, newItem: Exercise) =
            oldItem == newItem
    }
}
*/

package com.example.training_weaver.utilities

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.training_weaver.dataclass.Exercise
import com.training_weaver.R
import com.training_weaver.databinding.RowExerciseBinding

class ExerciseAdapter(
    private val listener: OnItemActionListener,
    private val enableContextMenu: Boolean = true // ← חדש: תפריט לחיצה ארוכה אופציונלי
) : ListAdapter<Exercise, ExerciseAdapter.VH>(Diff) {

    interface OnItemActionListener {
        /** לחיצה רגילה על פריט (בפרויקטים קודמים קראת לזה onEdit) */
        fun onEdit(ex: Exercise)
        /** פעולה מתוך תפריט הלחיצה הארוכה */
        fun onDelete(ex: Exercise) {}
    }

    object Diff : DiffUtil.ItemCallback<Exercise>() {
        override fun areItemsTheSame(oldItem: Exercise, newItem: Exercise) =
            oldItem.exerciseID == newItem.exerciseID

        override fun areContentsTheSame(oldItem: Exercise, newItem: Exercise) =
            oldItem == newItem
    }

    inner class VH(val b: RowExerciseBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = RowExerciseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        with(holder.b) {
            // הצגה בסיסית
            title.text = item.exerciseName
            desc.text  = item.exerciseDescription

            // קליק רגיל → “onEdit” (מעבר מסך וכו’)
            root.setOnClickListener { listener.onEdit(item) }

            // לחיצה ארוכה → תפריט (רק אם ביקשנו לאפשר)
            if (enableContextMenu) {
                root.setOnLongClickListener {
                    showContextMenu(this, item)
                    true
                }
            } else {
                root.setOnLongClickListener(null)
            }
        }
    }

    private fun showContextMenu(b: RowExerciseBinding, item: Exercise) {
        val popup = PopupMenu(b.root.context, b.root)
        popup.menuInflater.inflate(R.menu.exercise_row_menu, popup.menu)
        popup.setOnMenuItemClickListener { mi ->
            when (mi.itemId) {
                R.id.menu_edit -> { listener.onEdit(item); true }
                R.id.menu_delete -> { listener.onDelete(item); true }
                else -> false
            }
        }
        popup.show()
    }
}

