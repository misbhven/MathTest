package curtin.edu.mathtest.ui.students

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.coroutineScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import curtin.edu.mathtest.MathTestApplication
import curtin.edu.mathtest.database.Image
import curtin.edu.mathtest.databinding.FragmentStudentsBinding

class StudentsFragment : Fragment() {

    private val studentsViewModel: StudentsViewModel by activityViewModels { StudentViewModelFactory((activity?.application as MathTestApplication).studentDatabase.studentDao()) }
    private var _binding: FragmentStudentsBinding? = null
    private lateinit var studentRecyclerView: RecyclerView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentStudentsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        studentRecyclerView = binding.studentsRecycler
        studentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val studentListAdapter = StudentListAdapter {
            val action =
                StudentsFragmentDirections.actionNavigationStudentsToStudentDetailsScrollingFragment(
                    studentID = it.id)
            view.findNavController().navigate(action)
        }

        studentRecyclerView.adapter = studentListAdapter

        //checks for data updates on student list
        studentsViewModel.allStudents.observe(this.viewLifecycleOwner) { students -> students.let {
                studentListAdapter.submitList(it)
            }
        }

        binding.addStudentBtn.setOnClickListener {
            val action = StudentsFragmentDirections.actionNavigationStudentsToNavigationAddContact("Add Contact", -1, Image(null))
            view.findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}