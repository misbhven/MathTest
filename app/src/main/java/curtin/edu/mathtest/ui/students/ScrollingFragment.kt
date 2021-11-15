package curtin.edu.mathtest.ui.students
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import curtin.edu.mathtest.MathTestApplication
import curtin.edu.mathtest.database.Student
import curtin.edu.mathtest.databinding.FragmentScrollingBinding
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import curtin.edu.mathtest.database.Image
import java.io.ByteArrayOutputStream

class ScrollingFragment : Fragment() {

    //contains the student ID (if being edited) and img Byte Array
    private val navigationArgs: ScrollingFragmentArgs by navArgs()

    private var _binding : FragmentScrollingBinding? = null
    private val binding get() = _binding!!
    private val viewModel : StudentsViewModel by activityViewModels {StudentViewModelFactory(
            (activity?.application as MathTestApplication).studentDatabase.studentDao()
    )}

    private lateinit var tempEmails : MutableList<String>
    private lateinit var tempPhones : MutableList<String>
    lateinit var student : Student
    private var currentImg : ByteArray? = null


    //Logic for editing a student
    private fun bind(student : Student) {
        binding.apply {
            addContactFirstNameEditText.setText(student.firstName, TextView.BufferType.SPANNABLE)
            addContactLastNameEditText.setText(student.lastName, TextView.BufferType.SPANNABLE)
            if (navigationArgs.img.getImg() != null) {
                addContactImageView.setImageBitmap(getBitmapFromByteArray(navigationArgs.img.getImg()!!))
            }

            //set up recyclers
            addContactEmailRecycler.layoutManager = LinearLayoutManager(requireContext())
            addContactPhoneRecycler.layoutManager = LinearLayoutManager(requireContext())
            val emailAdapter = ContactItemListAdapter(tempEmails) {
                tempEmails.remove(it)
            }
            val phoneAdapter = ContactItemListAdapter(tempPhones) {
                tempPhones.remove(it)
            }
            addContactEmailRecycler.adapter = emailAdapter
            addContactPhoneRecycler.adapter = phoneAdapter


            //Click listeners
            addContactAddPhoneBtn.setOnClickListener {
                var phone = binding.addStudentPhoneEditText.text.toString()
                if (!phone.isNullOrEmpty() && tempPhones.size < 10 && !tempPhones.contains(phone)) {
                    phoneAdapter.addContent(phone)
                    displaySnackbar("Added $phone")
                }
            }
            addContactAddEmailBtn.setOnClickListener {
                var email = addContactEmailEditText.text.toString()
                if (!email.isNullOrEmpty() && tempEmails.size < 10 && !tempEmails.contains(email) && "@" in email) {
                    emailAdapter.addContent(email)
                    displaySnackbar("Added $email")
                }
            }
            addContactDoneBtn.setOnClickListener {
                updateStudent()
            }
            addContactImageView.setOnClickListener {

                val action = ScrollingFragmentDirections.actionNavigationAddContactToSelectImageSourceFragment(student.id, Image(getImageViewByteArray()))
                findNavController().navigate(action)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScrollingBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = navigationArgs.studentId
        if (navigationArgs.img.getImg() != null ) {
            binding.addContactImageView.tag = "not bg"
        }

        //if student is being edited
        if (id > 0) {
            viewModel.getStudent(id).observe(this.viewLifecycleOwner) { selectedStudent ->
                student = selectedStudent
                if (student.picture != null) {
                    binding.addContactImageView.tag = "not"
                }
                tempEmails = student.emails as MutableList<String>
                tempPhones = student.phoneNumbers as MutableList<String>
                bind(student)
            }

        }
        //if student is being added
        else {
            tempEmails = mutableListOf()
            tempPhones = mutableListOf()

            //set up recyclers
            binding.addContactEmailRecycler.layoutManager = LinearLayoutManager(requireContext())
            binding.addContactPhoneRecycler.layoutManager = LinearLayoutManager(requireContext())
            val emailAdapter = ContactItemListAdapter(tempEmails) {
                tempEmails.remove(it)
            }
            val phoneAdapter = ContactItemListAdapter(tempPhones) {
                tempPhones.remove(it)
            }
            binding.addContactEmailRecycler.adapter = emailAdapter
            binding.addContactPhoneRecycler.adapter = phoneAdapter
            if (navigationArgs.img.getImg() != null) {
                binding.addContactImageView.setImageBitmap(getBitmapFromByteArray(navigationArgs.img.getImg()!!))
                binding.addContactImageView.setTag("not")
                currentImg = navigationArgs.img.getImg()!!
            }

            //Click Listeners
            binding.addContactAddPhoneBtn.setOnClickListener {

                var phone = binding.addStudentPhoneEditText.text.toString()
                if (!phone.isNullOrEmpty() && !tempPhones.contains(phone) && tempPhones.size <= 10) {
                    phoneAdapter.addContent(phone)
                    displaySnackbar("Added ${phone}")
                }
                else {
                    displaySnackbar("ERROR: Could not add Phone")
                }
            }
            binding.addContactAddEmailBtn.setOnClickListener {
                var email = binding.addContactEmailEditText.text.toString()
                if (!email.isNullOrEmpty() && !tempEmails.contains(email) && tempEmails.size <= 10 && "@" in email) {
                    emailAdapter.addContent(email)
                    displaySnackbar("Added ${email}")
                }
                else {
                    displaySnackbar("ERROR: Could not add Email")
                }
            }
            binding.addContactDoneBtn.setOnClickListener {
                addStudent()
            }
            binding.addContactImageView.setOnClickListener {
                val action = ScrollingFragmentDirections.actionNavigationAddContactToSelectImageSourceFragment(-1, Image(getImageViewByteArray()))
                findNavController().navigate(action)
            }
        }
    }

    fun addStudent() {
        if (!binding.addContactFirstNameEditText.text.toString().isNullOrEmpty()) {
            if (!binding.addContactLastNameEditText.text.toString().isNullOrEmpty()) {
                if (tempEmails.size in 1..10) {
                    if (tempPhones.size in 1..10) {
                        if (viewModel.newStudent(
                                binding.addContactFirstNameEditText.text.toString(),
                                binding.addContactLastNameEditText.text.toString(),
                                tempEmails,
                                tempPhones,
                                getImageViewByteArray())) {
                            val action = ScrollingFragmentDirections.actionNavigationAddContactToNavigationStudents()
                            findNavController().navigate(action)
                        }
                        else {
                            displaySnackbar("ERROR: Could not add student, invalid fields")
                        }
                    }
                    else {
                        displaySnackbar("ERROR: Could not add student, invalid phones")
                    }
                }
                else {
                    displaySnackbar("ERROR: Could not add student, invalid emails")
                }
            }
            else {
                displaySnackbar("ERROR: Could not add student, invalid last name")
            }
        }
        else {
            displaySnackbar("ERROR: Could not add student, invalid first name")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun displaySnackbar(message : String) {
        Snackbar.make(
            binding.scrollingFragment,
            message,
            Snackbar.LENGTH_SHORT).show()
    }

    //Converts current image to ByteArray
    private fun getImageViewByteArray() : ByteArray? {

        if (binding.addContactImageView.tag != "bg") {
            var bitmap : Bitmap
            bitmap = (binding.addContactImageView.drawable as BitmapDrawable).bitmap
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            return stream.toByteArray()
        }
        return null
    }

    private fun getBitmapFromByteArray(bArray : ByteArray) : Bitmap {
        return BitmapFactory.decodeByteArray(bArray, 0, bArray.size)
    }

    private fun updateStudent() {
        if (viewModel.isStudentValid(binding.addContactFirstNameEditText.text.toString(), binding.addContactLastNameEditText.text.toString(), tempEmails, tempPhones)) {
            viewModel.updateStudent(navigationArgs.studentId, binding.addContactFirstNameEditText.text.toString(), binding.addContactLastNameEditText.text.toString(), tempEmails, tempPhones, getImageViewByteArray())
            val action = ScrollingFragmentDirections.actionNavigationAddContactToNavigationStudents()
            findNavController().navigate(action)
        }
        else {
            displaySnackbar("ERROR: Could not edit student: Invalid fields")
        }
    }
}