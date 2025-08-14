package com.example.training_weaver.utilities

import android.view.LayoutInflater
import android.view.Menu
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.training_weaver.dataclass.Routine
import com.training_weaver.databinding.ItemRoutineBinding

class WorkoutListAdapter(
    private val listener: OnItemActionListener
) : ListAdapter<Routine, WorkoutListAdapter.VH>(DIFF) {

    interface OnItemActionListener {
        fun onEdit(r: Routine)
        fun onDelete(r: Routine)
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Routine>() {
            override fun areItemsTheSame(a: Routine, b: Routine) = a.routineID == b.routineID
            override fun areContentsTheSame(a: Routine, b: Routine) = a == b
        }
        private const val MENU_EDIT = 1
        private const val MENU_DELETE = 2
    }

    inner class VH(val binding: ItemRoutineBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            // לחיצה קצרה — עריכה (ניווט לפרטי הרשימה)
            binding.root.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onEdit(getItem(pos))
                }
            }

            // לחיצה ארוכה — מציג תפריט עם עריכה/מחיקה
            binding.root.setOnLongClickListener {
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnLongClickListener true
                val item = getItem(pos)

                val popup = PopupMenu(binding.root.context, binding.root)
                popup.menu.add(Menu.NONE, MENU_EDIT, 0, "Edit")
                popup.menu.add(Menu.NONE, MENU_DELETE, 1, "Delete")
                popup.setOnMenuItemClickListener { mi ->
                    when (mi.itemId) {
                        MENU_EDIT -> { listener.onEdit(item); true }
                        MENU_DELETE -> { listener.onDelete(item); true }
                        else -> false
                    }
                }
                popup.show()
                true
            }
        }

        fun bind(r: Routine) {
            binding.tvRoutineName.text = r.routineName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemRoutineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}
