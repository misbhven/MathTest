package curtin.edu.mathtest.ui.results


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import curtin.edu.mathtest.database.Test
import curtin.edu.mathtest.databinding.TestItemBinding
import curtin.edu.mathtest.ui.students.StudentsViewModel


class TestItemAdapter(private val studentsViewModel: StudentsViewModel, private val frag: Fragment) : ListAdapter<Test, TestItemAdapter.TestItemViewHolder>(
    DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Test>() {
            override fun areItemsTheSame(oldItem: Test, newItem: Test): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Test, newItem: Test): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class TestItemViewHolder(private var binding : TestItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(test : Test) {
            //TODO

            var studentName : String = "Jane Doe"
            studentsViewModel.getStudent(test.studentId).observe(frag.viewLifecycleOwner) { student ->
                binding.testItemName.text = student.firstName + " " + student.lastName
            }
            binding.testItemDate.text = test.date
            binding.testItemTime.text = test.time
            val score = "${test.result}/${test.total}"
            binding.testItemScore.text = score
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestItemAdapter.TestItemViewHolder {
        val viewHolder = TestItemViewHolder(
            TestItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        return viewHolder
    }

    override fun onBindViewHolder(holder: TestItemAdapter.TestItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    private fun sortList() : MutableList<Test> {
        var sortedList : MutableList<Test> = currentList as MutableList<Test>
        sortedList.sortBy { it.result }
        return sortedList
    }

    override fun submitList(list: List<Test>?) {
        super.submitList(list?.let { ArrayList(it) })
    }
}