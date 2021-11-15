package curtin.edu.mathtest.ui.testing

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import curtin.edu.mathtest.databinding.AnswerItemBinding

class AnswerAdapter(private var myList: List<Int>,private val onItemClicked: (Int) -> Unit ) : RecyclerView.Adapter<AnswerAdapter.AnswerViewHolder>() {


    inner class AnswerViewHolder(private var binding : AnswerItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(answer : Int, pos : Int) {
            val text : String = "${pos + 1}) ${answer}"
            binding.answerItemText.text = text
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerAdapter.AnswerViewHolder {
        val viewHolder = AnswerViewHolder(
            AnswerItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            onItemClicked(myList[position])
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: AnswerAdapter.AnswerViewHolder, position: Int) {
        holder.bind(myList[position], position)
    }

    override fun getItemCount(): Int {
        return myList.size
    }

    fun updateList(newList : List<Int>) {
        myList = newList
        notifyDataSetChanged()
    }
}