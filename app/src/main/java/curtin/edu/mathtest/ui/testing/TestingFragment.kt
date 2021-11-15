package curtin.edu.mathtest.ui.testing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import curtin.edu.mathtest.MathTestApplication
import curtin.edu.mathtest.database.Image
import curtin.edu.mathtest.databinding.FragmentTestingBinding
import curtin.edu.mathtest.ui.students.StudentListAdapter
import curtin.edu.mathtest.ui.students.StudentViewModelFactory
import curtin.edu.mathtest.ui.students.StudentsFragmentDirections
import curtin.edu.mathtest.ui.students.StudentsViewModel

class TestingFragment : Fragment() {

    //get instances
    private val studentsViewModel: StudentsViewModel by activityViewModels { StudentViewModelFactory((activity?.application as MathTestApplication).studentDatabase.studentDao()) }

    private lateinit var studentRecyclerView: RecyclerView
    private var _binding: FragmentTestingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentTestingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        studentRecyclerView = binding.studentsTestingRecycler
        studentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val studentListAdapter = StudentListAdapter {
            val action =
                TestingFragmentDirections.actionNavigationTestingToConductTestFragment(it.id)
            view.findNavController().navigate(action)
        }

        studentRecyclerView.adapter = studentListAdapter

        //checks for data updates on student list
        studentsViewModel.allStudents.observe(this.viewLifecycleOwner) { students -> students.let {
            studentListAdapter.submitList(it)
        }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}