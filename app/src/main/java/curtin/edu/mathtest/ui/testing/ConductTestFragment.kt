package curtin.edu.mathtest.ui.testing

import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle

import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import curtin.edu.mathtest.MathTestApplication

import curtin.edu.mathtest.database.Question

import java.io.IOException

import java.net.MalformedURLException
import java.net.URL

import java.time.LocalDateTime

import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

import android.content.Context
import android.os.CountDownTimer
import androidx.recyclerview.widget.LinearLayoutManager
import curtin.edu.mathtest.databinding.FragmentConductTestBinding

import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.lang.Integer.parseInt
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import javax.net.ssl.*
import android.R
import android.os.Handler
import androidx.navigation.fragment.findNavController
import java.util.*


class ConductTestFragment : Fragment() {
/*
    onCreate
        take date and time

    Fetch question
        do AsyncTask of fetching question
        populate question text
        check how many options
        if options == 0
            make edit text linear layout visible
        else
            make recyclerView visible and bind options

    On pass button click
        give a 0 mark
        update total
        fetch next question

    On list click / submit click
        check if answer is correct
            add 10 marks
            update total +10
        else
            subtract 5 marks
            update total +10
        fetch next question

    On finish button click
        save total elapsed time
        save total mark
        save result
        add new test object to test viewModel
 */
    //Bindings and nav args
    private var _binding : FragmentConductTestBinding? = null
    private val binding get() = _binding!!
    private val navigationArgs: ConductTestFragmentArgs by navArgs()

    //vars
    private lateinit var countDownTimer: CountDownTimer
    private var studentId = -1
    private var currentResult : Int = 0
    private var currentTotal : Int = 0
    private lateinit var currentQuestion : Question
    private var secondsLeft : Int = 1
    private var totalTimeElapsed = 0
    private var elapsedRunning : Boolean = false
    private lateinit var dateConducted : String
    private lateinit var timeConducted : String
    private val testViewModel: TestingViewModel by activityViewModels { TestingViewModelFactory((activity?.application as MathTestApplication).studentDatabase.testDao()) }


    //Views and Buttons
    private lateinit var questionView : TextView

    private lateinit var timerView : TextView
    private lateinit var elapsedView : TextView
    private lateinit var totalMarkView : TextView
    private lateinit var resultMarkView : TextView

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter : AnswerAdapter

    private lateinit var submitAnswerView : EditText
    private lateinit var submitAnswerBtn : Button

    private lateinit var passBtn : Button
    private lateinit var finishBtn : Button


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentConductTestBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dateTime = LocalDateTime.now()

        //record date and time
        studentId = navigationArgs.studentId
        dateConducted = dateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        timeConducted = dateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

        //bind views
        questionView = binding.conductTestQuestionView
        totalMarkView = binding.conductTestTotalView
        resultMarkView = binding.conductTestResultView
        finishBtn = binding.conductTestFinishBtn
        passBtn = binding.conductTestPassBtn
        recyclerView = binding.conductTestRecycler
        submitAnswerView = binding.conductTestSubmitAnswer
        submitAnswerBtn = binding.conductTestSubmitAnswerBtn
        elapsedView = binding.conductTestTimeElapsed
        timerView = binding.conductTestTimer

        binding.conductTestTimeElapsed.text = "0"
        elapsedRunning = true

        //Todo fetch first question



        adapter = AnswerAdapter(listOf()) {
            if (currentQuestion != null) {
                checkAnswer(it)
            }
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        AsyncGetQuestion().execute()
        runTimer()
        //AsyncTimeElapsedClass().execute()

        submitAnswerBtn.setOnClickListener {
            if (currentQuestion != null) {
                if (!submitAnswerView.text.toString().isNullOrEmpty()) {
                    checkAnswer(submitAnswerView.text.toString().toInt())
                }
            }
        }

        passBtn.setOnClickListener {
            if (currentQuestion != null) {
                currentTotal += 10
                totalMarkView.text =  currentTotal.toString()
                countDownTimer.cancel()
                AsyncGetQuestion().execute()
            }
        }

        finishBtn.setOnClickListener {
            //TODO
            elapsedRunning = false
            countDownTimer.cancel()
            timerView.text = "-"
            if (testViewModel.newTest(navigationArgs.studentId, currentResult, currentTotal, totalTimeElapsed, dateConducted, timeConducted)) {
                Log.d("Hello", "ADDED TEST")
            }
            else {
                Log.d("Hello", "ERROR: COULD NOT ADD TEST")
            }

            val action = ConductTestFragmentDirections.actionConductTestFragmentToNavigationTesting()
            findNavController().navigate(action)
        }
    }

    private fun checkAnswer(answer : Int) {
        if (answer == currentQuestion.answer) {
            currentResult += 10
        }
        else {
            currentResult -= 5
            if (currentResult < 0) {
                currentResult = 0
            }
        }
        currentTotal += 10
        resultMarkView.text = currentResult.toString()
        totalMarkView.text =  currentTotal.toString()
        countDownTimer.cancel()
        AsyncGetQuestion().execute()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ConductTestFragment()
    }


    inner class AsyncTimeElapsedClass() : AsyncTask<Void, Int, Void>() {
        override fun doInBackground(vararg p0: Void?): Void? {
            try {
                while (elapsedRunning) {
                    totalTimeElapsed++
                    updateProgress(totalTimeElapsed)
                    Thread.sleep(1000)
                }
            }
            catch (e : InterruptedException) {
                e.printStackTrace()
            }
            return null
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            requireActivity().runOnUiThread {
                elapsedView.text = values[0].toString()
            }
        }

        private fun updateProgress(progress : Int) {
            onProgressUpdate(progress)
        }
    }


    inner class AsyncGetQuestion() : AsyncTask<Void, Void, Question>() {
        override fun doInBackground(vararg p0: Void?): Question? {

            val jsonString = queryServer()
            if (!jsonString.isNullOrEmpty()) {
                var q = parseJson(jsonString)
                if (q != null) {
                    return q
                }
                else {
                    Log.d("Hello", "ERROR: Could not parse JSON")
                }
            }
            else {
                Log.d("Hello", "ERROR: could not fetch JSON from server")
            }
            return null
        }

        override fun onPostExecute(result: Question?) {
            super.onPostExecute(result)
            if (result != null) {
                questionView.text = result.question
                currentQuestion = result
                runTimeLeft()

                if (result.responses.size == 0) {
                    recyclerView.visibility = View.INVISIBLE
                    binding.conductTestSubmitAnswerLayout.visibility = View.VISIBLE
                }
                else {
                    recyclerView.visibility = View.VISIBLE
                    binding.conductTestSubmitAnswerLayout.visibility = View.INVISIBLE
                    adapter.updateList(currentQuestion.responses)
                }
            }
            else {
                Log.d("Hello", "ERROR: Could not bind question to views as question object is Null")
            }
        }


        fun queryServer() : String? {
            var data: String? = null
            val url = Uri.parse("https://192.168.1.104:8000/random/question/").buildUpon()
            //method thedata.getit
            //api_key = 01189998819991197253
            //format json
            url.appendQueryParameter("method", "thedata.getit")
            url.appendQueryParameter("api_key", "01189998819991197253")
            url.appendQueryParameter("format", "json")

            val urlString = url.build().toString()
            Log.d("Hello", "fetch question: $urlString")
            val connection = openConnection(urlString)
            if (connection == null) {
                Log.d("Hello","Check Internet")
            } else if (!isConnectionOkay(connection)) {
                Log.d("Hello","Could not download JSON")
            } else {
                data = downloadToString(connection)
                if (data != null) {
                    Log.d("Hello", data)
                } else {
                    Log.d("Hello", "Nothing returned")
                }
                connection.disconnect()
            }
            return data
        }

        private fun openConnection(urlString: String): HttpsURLConnection? {
            var conn: HttpsURLConnection? = null
            try {
                val url = URL(urlString)
                conn = url.openConnection() as HttpsURLConnection
                DownloadUtils().addCertificate(requireContext(), conn)
            } catch (e: MalformedURLException) {
                Log.e("Hello", "Broken URL", e)
            } catch (e: IOException) {
                Log.e("Hello", "IO Exception in opening connection", e)
            }
            catch (e :GeneralSecurityException) {
                Log.e("Hello", "Certificate error", e)
            }
            return conn
        }

        private fun isConnectionOkay(conn: HttpsURLConnection): Boolean {
            try {
                if (conn.responseCode == HttpsURLConnection.HTTP_OK) {
                    return true
                }
            } catch (e: IOException) {
                Log.e("Hello", "IO Exception in checking connection okay", e)
            }
            return false
        }

        private fun downloadToString(conn: HttpsURLConnection): String? {
            var data: String? = null
            val outputStream = ByteArrayOutputStream()
            try {
                //TODO fix this
                val inputStream = conn.inputStream
                inputStream.use {
                        input -> outputStream.use {
                        output -> input.copyTo(output)
                }
                }
                val byteData: ByteArray = outputStream.toByteArray()
                data = String(byteData, Charsets.UTF_8)
            } catch (e: IOException) {
                Log.e("Hello", "Could not download JSON from server to String", e)
            }
            return data
        }

        private fun parseJson(data: String) : Question? {
            try {
                val jBase = JSONObject(data)
                val jQuestion : String = jBase.getString("question")
                val jAnswer = jBase.getInt("result")
                val jOptions = jBase.getJSONArray("options")
                val jTime = jBase.getInt("timetosolve")
                var options = mutableListOf<Int>()
                if (jOptions.length() > 0) {
                    for (i in 0 until jOptions.length()) {
                        val option = jOptions.getInt(i)
                        options.add(option)
                    }
                }
                return Question(jQuestion, options, jAnswer, jTime)
            }
            catch (e: JSONException) {
                e.printStackTrace()
            }
            return null
        }


        inner class DownloadUtils {
            @Throws(IOException::class, GeneralSecurityException::class)
            fun addCertificate(context: Context, conn: HttpsURLConnection) {
                /**
                 * Adapted in part from https://developer.android.com/training/articles/security-ssl#java
                 */
                var cert: Certificate
                context.getResources().openRawResource(curtin.edu.mathtest.R.raw.cert).use { `is` ->
                    cert = CertificateFactory.getInstance("X.509").generateCertificate(`is`)
                }
                val keyStore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
                keyStore.load(null, null)
                keyStore.setCertificateEntry("ca", cert)
                val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm()
                )
                tmf.init(keyStore)
                val sslContext: SSLContext = SSLContext.getInstance("TLS")
                sslContext.init(null, tmf.getTrustManagers(), null)
                conn.sslSocketFactory = sslContext.getSocketFactory()
                conn.setHostnameVerifier(object : HostnameVerifier {
                    override fun verify(hostname: String?, session: SSLSession?): Boolean {
                        return true
                    }
                })
            }
        }
    }

    private fun runTimer() {
        val handler = Handler()
        handler.post(object : Runnable {
            override fun run() {
                elapsedView.text = totalTimeElapsed.toString()
                if (elapsedRunning) {
                    totalTimeElapsed++
                }

                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun runTimeLeft() {
        if (currentQuestion != null) {
            if (currentQuestion.allowedTime > 0) {
                timerView.text = currentQuestion.allowedTime.toString()
                var seconds = currentQuestion.allowedTime.toLong()
                secondsLeft = seconds.toInt()
                countDownTimer = object : CountDownTimer(seconds * 1000, 1000) {
                    override fun onTick(p0: Long) {
                        secondsLeft = ((p0 /1000) % 60).toInt()
                        binding.conductTestTimer.text = secondsLeft.toString()
                    }
                    override fun onFinish() {
                        currentTotal += 10
                        totalMarkView.text =  currentTotal.toString()
                        AsyncGetQuestion().execute()
                    }
                }.start()
            }
        }
    }
}