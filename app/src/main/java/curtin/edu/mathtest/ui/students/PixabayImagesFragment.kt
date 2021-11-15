package curtin.edu.mathtest.ui.students

import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import curtin.edu.mathtest.database.Image
import curtin.edu.mathtest.databinding.FragmentImageListBinding
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import android.os.StrictMode
import com.google.android.material.snackbar.Snackbar

class PixabayImagesFragment : Fragment() {
    private val navigationArgs: PixabayImagesFragmentArgs by navArgs()
    private var columnCount = 3
    private var _binding : FragmentImageListBinding? = null
    private val binding get() = _binding!!
    private lateinit var searchKey : EditText
    private var picturesList : List<String?> = listOf()
    private lateinit var pixabayAdapter : PixabayImagesRecyclerViewAdapter

    companion object {
        const val ARG_COLUMN_COUNT = "column-count"
        @JvmStatic
        fun newInstance(columnCount: Int) =
            PixabayImagesFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentImageListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //needed to use threads
        val SDK_INT = Build.VERSION.SDK_INT
        if (SDK_INT > 8) {
            val policy = StrictMode.ThreadPolicy.Builder()
                .permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }
        searchKey = binding.pixabaySearchText

        pixabayAdapter  = PixabayImagesRecyclerViewAdapter(picturesList, requireActivity()) {
            val action = PixabayImagesFragmentDirections.actionPixabayImagesFragmentToSelectImageSourceFragment(navigationArgs.studentId, Image(it))
            findNavController().navigate(action)
        }
        binding.pixabayList.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.pixabayList.adapter = pixabayAdapter
        binding.pixabaySearchBtn.setOnClickListener {
            val searchTerm = searchKey.text.toString()
            if (searchTerm.isNotEmpty()) {
                GetJSONAsynch().execute(searchTerm)
            }
        }

    }

    private fun searchRemoteAPI(searchKey: String): String? {
        var data: String? = null
        val url = Uri.parse("https://pixabay.com/api/").buildUpon()
        url.appendQueryParameter("key", "23948721-c41eefd03aa54b509034ae709")
        url.appendQueryParameter("q", searchKey)
        url.appendQueryParameter("per_page", 50.toString())
        val urlString = url.build().toString()
        Log.d("Hello", "pictureRetrievalTask: $urlString")
        val connection = openConnection(urlString)
        if (connection == null) {
            displaySnackbar("Check Internet")
        } else if (!isConnectionOkay(connection)) {
            displaySnackbar("Could not download JSON")
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

    private fun pictureRetrievalTask(searchKey: String) : Boolean {
        val data = searchRemoteAPI(searchKey)
        if (data != null) {
            //TODO make imageUrls a list
            val imageUrls = getImageLargeUrl(data)

            //feed list to adapter, contents of if block occur in adapter
            if (!imageUrls.isNullOrEmpty()) {
                picturesList = imageUrls
                return true
            } else {
                displaySnackbar("No search Results")
            }
        }
        return false
    }

    //converts json to image URLs
    //frag
    private fun getImageLargeUrl(data: String): List<String?> {
        try {
            val jBase = JSONObject(data)
            val jHits = jBase.getJSONArray("hits")
            if (jHits.length() > 0) {
                var tempList = mutableListOf<String?>()
                if (jHits.length() > 50) {
                    for (i in 0..50) {
                        val jHitsItem = jHits.getJSONObject(i)
                        tempList.add(jHitsItem.getString("largeImageURL"))
                    }
                }
                else {
                    for (i in 0 until jHits.length()) {
                        val jHitsItem = jHits.getJSONObject(i)
                        tempList.add(jHitsItem.getString("largeImageURL"))
                    }
                }
                return tempList
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return listOf<String>()
    }

    //both
    private fun openConnection(urlString: String): HttpURLConnection? {
        var conn: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            conn = url.openConnection() as HttpURLConnection
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return conn
    }

    private fun isConnectionOkay(conn: HttpURLConnection): Boolean {
        try {
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                return true
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    //frag
    private fun downloadToString(conn: HttpURLConnection): String? {
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
            e.printStackTrace()
        }
        return data
    }

    inner class GetJSONAsynch() : AsyncTask<String, Void, Boolean>() {
        override fun doInBackground(vararg p0: String?): Boolean {
            if (!p0.isNullOrEmpty()) {
                if (!p0[0].isNullOrEmpty()) {
                    pictureRetrievalTask(p0[0]!!)
                    return true
                }
            }
            return false
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)
            if (result) {
                pixabayAdapter.updateList(picturesList)
            }
        }
    }

    private fun displaySnackbar(message : String) {
        Snackbar.make(
            binding.pixabayFrag,
            message,
            Snackbar.LENGTH_SHORT).show()
    }
}