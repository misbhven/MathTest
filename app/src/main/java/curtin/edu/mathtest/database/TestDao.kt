package curtin.edu.mathtest.database

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TestDao {
    @Query("SELECT * FROM test")
    fun getAllTests(): Flow<List<Test>>

    @Query("SELECT * FROM test WHERE student_id = :studentId")
    fun getTestsFromStudent(studentId : Int) : Flow<List<Test>>

    @Query("SELECT * FROM test WHERE id = :testId")
    fun getTest(testId : Int) : Flow<Test>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTest(test: Test)

    @Delete
    suspend fun deleteTest(test: Test)
}