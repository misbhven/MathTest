package curtin.edu.mathtest.ui.testing

import androidx.lifecycle.*
import curtin.edu.mathtest.database.StudentDao
import curtin.edu.mathtest.database.Test
import curtin.edu.mathtest.database.TestDao
import curtin.edu.mathtest.ui.students.StudentsViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TestingViewModel(private val testDao: TestDao) : ViewModel() {

    private val allTests : LiveData<List<Test>> = testDao.getAllTests().asLiveData()

    fun getAllTests(): LiveData<List<Test>> {
        return testDao.getAllTests().asLiveData()
    }

    fun getStudentTests(student_id: Int): Flow<List<Test>> {
        return testDao.getTestsFromStudent(student_id)
    }

    fun getTest(testId : Int): Flow<Test> {
        return testDao.getTest(testId)
    }

    fun deleteTest(test : Test) {
        viewModelScope.launch { testDao.deleteTest(test) }
    }

    fun newTest(student_id : Int, result : Int, total : Int, elapsed : Int, date : String, time : String) : Boolean {
        if (isTestValid(student_id, result, total, elapsed, date, time)) {
            val test = createTest(student_id, result, total, elapsed, date, time)
            insertTest(test)
            return true
        }
        return false
    }

    private fun insertTest(test : Test) {
        viewModelScope.launch { testDao.insertTest(test) }
    }

    private fun createTest(student_id : Int, result : Int, total : Int, elapsed : Int, date : String, time : String) : Test {
        return Test(studentId = student_id, result = result, total = total, elapsedTime = elapsed, date = date, time = time)
    }

    private fun isTestValid(student_id : Int, result : Int, total : Int, elapsed : Int, date : String, time : String) : Boolean {
        if (student_id >= 0 && result >= 0 && total >= 0 && elapsed >= 0 && !date.isNullOrEmpty() && !time.isNullOrEmpty()) {
            return true
        }
        return false
    }
}

//get an instance of this view model
class TestingViewModelFactory (private val testDao: TestDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TestingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TestingViewModel(testDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}