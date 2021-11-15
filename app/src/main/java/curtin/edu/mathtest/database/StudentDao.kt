package curtin.edu.mathtest.database
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM student")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM student WHERE id = :studentID")
    fun getStudent(studentID: Int): Flow<Student>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertStudent(student: Student)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

}