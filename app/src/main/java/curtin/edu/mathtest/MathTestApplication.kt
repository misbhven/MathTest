package curtin.edu.mathtest

import android.app.Application
import curtin.edu.mathtest.database.AppDatabase

class MathTestApplication : Application() {
    val studentDatabase : AppDatabase by lazy {AppDatabase.getStudentsDatabase(this)}
}