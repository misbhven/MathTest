package curtin.edu.mathtest.ui.students

import androidx.lifecycle.*
import curtin.edu.mathtest.database.Student
import curtin.edu.mathtest.database.StudentDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


class StudentsViewModel(private val studentDao : StudentDao) : ViewModel() {

    val allStudents: LiveData<List<Student>> = studentDao.getAllStudents().asLiveData()

    private fun insertStudent(student : Student) {
        viewModelScope.launch { studentDao.insertStudent(student) }
    }

    private fun getNewStudent(firstName : String, lastName : String, emails : List<String>, phones : List<String>, image : ByteArray?) : Student {
        return Student(firstName = firstName, lastName = lastName, picture = image, emails = emails, phoneNumbers = phones)
    }

    fun isStudentValid(firstName : String, lastName : String, emails : List<String>, phones : List<String>) : Boolean {
        if (firstName.isBlank() || lastName.isBlank()) {
            return false
        }
        for (email in emails) {
            //string contains an @ symbol
            if (!email.contains("@")) {
                return false
            }
        }
        for (phone in phones) {
            //string contains only digits
            if (phone.contains("[0-9]+")) {
                return false
            }
        }
        return true
    }

    fun newStudent(firstName : String, lastName : String, emails : List<String>, phones : List<String>, image : ByteArray?) : Boolean {
        if (isStudentValid(firstName, lastName, emails, phones)) {
            insertStudent(getNewStudent(firstName, lastName, emails, phones, image))
            return true
        }
        return false
    }

    private fun updateStudent(student : Student) {
        viewModelScope.launch {
            studentDao.updateStudent(student)
        }
    }

    fun deleteStudent(student : Student) {
        viewModelScope.launch {
            studentDao.deleteStudent(student)
        }
    }

    private fun getUpdatedStudent(id : Int, firstName: String, lastName: String, emails: List<String>, phones: List<String>, img : ByteArray?) : Student {
        return Student(id, firstName, lastName, img, emails, phones)
    }

    fun updateStudent(id : Int, firstName: String, lastName: String, emails: List<String>, phones: List<String>, img : ByteArray?) {
        val updatedStudent = getUpdatedStudent(id, firstName, lastName, emails, phones, img)
        updateStudent(updatedStudent)
    }

    fun getAllStudents() : Flow<List<Student>> = studentDao.getAllStudents()

    fun getStudent(studentID : Int) : LiveData<Student> = studentDao.getStudent(studentID).asLiveData()

}

//get an instance of this view model
class StudentViewModelFactory (private val studentDao: StudentDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudentsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudentsViewModel(studentDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}