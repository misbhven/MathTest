package curtin.edu.mathtest.database

import android.content.Context
import androidx.room.*

@Database(entities = arrayOf(Student::class, Test::class), version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun testDao() : TestDao

    companion object {
        @Volatile
        private var INSTANCE : AppDatabase? = null

        fun getStudentsDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context, AppDatabase::class.java, "app_database").fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}