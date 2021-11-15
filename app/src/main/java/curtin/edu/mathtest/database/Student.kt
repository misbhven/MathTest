package curtin.edu.mathtest.database

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable

@Entity
data class Student(
    @PrimaryKey(autoGenerate = true) val id : Int = 0,
    @NonNull @ColumnInfo(name = "first_name") val firstName : String,
    @NonNull @ColumnInfo(name = "last_name") val lastName : String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB, name = "picture") val picture : ByteArray?,
    @NonNull @ColumnInfo(name = "emails") val emails : List<String>,
    @NonNull @ColumnInfo(name = "phone_numbers") val phoneNumbers : List<String>
) : Serializable

class Converters {
    @TypeConverter
    fun fromString(value : String?) : List<String> {

        val listType = object : TypeToken<List<String>>(){}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list : List<String?>) : String {
        return Gson().toJson(list)
    }

}

