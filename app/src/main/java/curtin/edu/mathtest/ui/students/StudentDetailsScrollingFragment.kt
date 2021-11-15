package curtin.edu.mathtest.ui.students

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import curtin.edu.mathtest.MathTestApplication
import curtin.edu.mathtest.R
import curtin.edu.mathtest.database.Image
import curtin.edu.mathtest.database.Student
import curtin.edu.mathtest.databinding.FragmentStudentDetailsScrollingBinding

class StudentDetailsScrollingFragment : Fragment() {

    private val navigationArgs: StudentDetailsScrollingFragmentArgs by navArgs()
    lateinit var student : Student
    private var _binding: FragmentStudentDetailsScrollingBinding? = null
    private val binding get() = _binding!!
    private val studentViewModel : StudentsViewModel by activityViewModels {
        StudentViewModelFactory((activity?.application as MathTestApplication).studentDatabase.studentDao())
    }

    private fun bind(student : Student) {
        binding.apply {
            viewStudentFirstName.text = student.firstName
            viewStudentLastName.text = student.lastName
            if (student.picture != null) {
                val imgBitmap = BitmapFactory.decodeByteArray(student.picture, 0, student.picture!!.size)
                viewStudentImageView.setImageBitmap(imgBitmap)
            }

            val emailAdapter = ContactItemListAdapter(student.emails) {
                //do nothing
            }
            val phoneAdapter = ContactItemListAdapter(student.phoneNumbers) {
                //do nothing
            }
            viewContactEmailRecycler.adapter = emailAdapter
            viewContactPhoneRecycler.adapter = phoneAdapter

            viewStudentDeleteBtn.setOnClickListener {
                deleteStudent()
            }

            viewStudentEditBtn.setOnClickListener {
                editStudent()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStudentDetailsScrollingBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = navigationArgs.studentID
        studentViewModel.getStudent(id).observe(this.viewLifecycleOwner) {
            selectedStudent -> student = selectedStudent
            bind(student)
        }
    }

    private fun deleteStudent() {
        studentViewModel.deleteStudent(student)
        findNavController().navigateUp()
    }

    private fun editStudent() {
        val action = StudentDetailsScrollingFragmentDirections.actionStudentDetailsScrollingFragmentToNavigationAddContact("Edit Student",
            student.id, Image(student.picture))
        this.findNavController().navigate(action)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}