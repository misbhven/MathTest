package curtin.edu.mathtest.ui.students

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import curtin.edu.mathtest.database.Student
import curtin.edu.mathtest.databinding.ContactItemBinding
import curtin.edu.mathtest.databinding.StudentItemBinding

class ContactItemListAdapter(private val myList: List<String>,private val onDeleteClicked: (String) -> Unit ) : RecyclerView.Adapter<ContactItemListAdapter.ContactItemViewHolder>() {


    /*
    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
        }
    }

     */
    private var listData : MutableList<String> = myList as MutableList<String>

    fun addContent(newContent : String) : Boolean {
        if (listData.size < 10 && !listData.contains(newContent)) {
            listData.add(newContent)
            notifyItemInserted(listData.size)
            return true
        }
        return false
    }

    fun deleteContent(content : String) {
        listData.remove(content)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactItemListAdapter.ContactItemViewHolder {
        val viewHolder = ContactItemViewHolder(
            ContactItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        return viewHolder
    }

    inner class ContactItemViewHolder(private var binding : ContactItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(content : String) {
            binding.contactItemTextView.text = content
            binding.contactItemDeleteBtn.setOnClickListener {
                onDeleteClicked(content)
                deleteContent(content)
            }
        }
    }

    override fun onBindViewHolder(holder: ContactItemListAdapter.ContactItemViewHolder, position: Int) {
        holder.bind(listData[position])
    }

    override fun getItemCount(): Int {
        return listData.size
    }
}