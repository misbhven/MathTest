package curtin.edu.mathtest.ui.students

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import curtin.edu.mathtest.database.Student
import curtin.edu.mathtest.databinding.StudentItemBinding

class StudentListAdapter(private val onItemClicked: (Student) -> Unit) : ListAdapter<Student, StudentListAdapter.StudentViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Student>() {
            override fun areItemsTheSame(oldItem: Student, newItem: Student): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Student, newItem: Student): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val viewHolder = StudentViewHolder(
            StudentItemBinding.inflate(
                LayoutInflater.from( parent.context),
                parent,
                false
            )
        )
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            onItemClicked(getItem(position))
        }
        return viewHolder
    }

    class StudentViewHolder(private var binding : StudentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(student : Student) {
            binding.studentItemName.text = student.firstName + " " + student.lastName
            val img : ByteArray? = student.picture
            if(img != null) {
                binding.studentItemPicture.setImageBitmap(BitmapFactory.decodeByteArray(img,0,img.size))
            }
        }
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}