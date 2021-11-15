package curtin.edu.mathtest.ui.results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import curtin.edu.mathtest.MathTestApplication
import curtin.edu.mathtest.database.Test
import curtin.edu.mathtest.databinding.FragmentResultsBinding
import curtin.edu.mathtest.ui.students.StudentViewModelFactory
import curtin.edu.mathtest.ui.students.StudentsViewModel
import curtin.edu.mathtest.ui.testing.TestingViewModel
import curtin.edu.mathtest.ui.testing.TestingViewModelFactory

class ResultsFragment : Fragment() {

    private val studentsViewModel: StudentsViewModel by activityViewModels { StudentViewModelFactory((activity?.application as MathTestApplication).studentDatabase.studentDao()) }
    private var _binding: FragmentResultsBinding? = null
    private val testingViewModel: TestingViewModel by activityViewModels { TestingViewModelFactory((activity?.application as MathTestApplication).studentDatabase.testDao()) }

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var adapter = TestItemAdapter(studentsViewModel, this)
        testingViewModel.getAllTests().observe(this.viewLifecycleOwner) { tests -> tests.let {

                adapter.submitList(it.sortedByDescending{ it.result })
            }
        }
        binding.resultsRecycler.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}