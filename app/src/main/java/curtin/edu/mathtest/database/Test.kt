package curtin.edu.mathtest.database

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey


@Entity( foreignKeys = arrayOf(ForeignKey(entity = Student::class, parentColumns = ["id"], childColumns = ["student_id"], onDelete = CASCADE)))
data class Test(
    @PrimaryKey(autoGenerate = true) val id : Int = 0,

    //foreign key
    @NonNull @ColumnInfo(name = "student_id") val studentId : Int,
    @NonNull @ColumnInfo(name = "test_result") val result : Int,
    @NonNull @ColumnInfo(name = "test_total") val total : Int,
    @NonNull @ColumnInfo(name = "test_time_elapsed") val elapsedTime : Int,
    @NonNull @ColumnInfo(name = "test_date") val date : String,
    @NonNull @ColumnInfo(name = "test_time") val time : String
)
