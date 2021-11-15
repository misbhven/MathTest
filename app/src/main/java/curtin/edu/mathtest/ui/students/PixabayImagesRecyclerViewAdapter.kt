package curtin.edu.mathtest.ui.students

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import curtin.edu.mathtest.databinding.FragmentPixabayBinding
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class PixabayImagesRecyclerViewAdapter(
    private var pictures: List<String?>, private val activity: Activity,
    private val onPictureClicked: (ByteArray) -> Unit
    ) : RecyclerView.Adapter<PixabayImagesRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val viewHolder = ViewHolder(
            FragmentPixabayBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        //on item clicked, return a byte array of the image selected.
        viewHolder.itemView.setOnClickListener {
            val pos = viewHolder.adapterPosition
            val url : String? = pictures[pos]
            if (!url.isNullOrEmpty()) {
                val bitmap = viewHolder.Async().getImageFromUrl(url)

                if (bitmap != null) {
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    val byteArray : ByteArray = stream.toByteArray()

                    onPictureClicked(byteArray)
                }
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUrl = pictures[position]
        holder.bind(imageUrl)
    }

    override fun getItemCount(): Int = pictures.size

    inner class ViewHolder(private var binding: FragmentPixabayBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun bind(imageUrl : String?) {
                if (!imageUrl.isNullOrEmpty() ) {
                    Async().execute(imageUrl)
                }
            }

        inner class Async() : AsyncTask<String, Int, Bitmap?>() {
            override fun doInBackground(vararg p0: String?): Bitmap? {
                if (!p0.isNullOrEmpty()) {
                    if (!p0[0].isNullOrEmpty()) {
                        return getImageFromUrl(p0[0]!!)!!
                    }
                }
                return null
            }

            override fun onPostExecute(result: Bitmap?) {
                super.onPostExecute(result)
                if (result != null) {
                    activity.runOnUiThread {
                        binding.progressBar.visibility = (View.INVISIBLE)
                        binding.pixabayImage.setImageBitmap(result)
                        binding.pixabayImage.visibility = (View.VISIBLE)
                    }
                }
            }

            override fun onProgressUpdate(vararg values: Int?) {
                super.onProgressUpdate(*values)
                activity.runOnUiThread {
                    binding.progressBar.visibility = (View.VISIBLE)
                    binding.progressBar.progress = (values[0]!!)
                }
            }

            fun getImageFromUrl(imageUrl: String): Bitmap? {
                var image: Bitmap? = null
                val url = Uri.parse(imageUrl).buildUpon()
                val urlString = url.build().toString()
                Log.d("Hello", "ImageUrl: $urlString")
                val connection = openConnection(urlString)
                if (connection == null) {
                        Log.d("Hello", "Check Internet")
                } else if (isConnectionOkay(connection) == false) {
                    Log.d("Hello", "Problem Downloading image")
                } else {
                    //download bitmap from URL connection
                    image = downloadToBitmap(connection)
                    if (image != null) {
                        // Log.d("Hello", image.toString());
                    } else {
                        Log.d("Hello", "Nothing returned")
                    }
                    connection.disconnect()
                }
                return image
            }

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

            private fun downloadToBitmap(conn: HttpURLConnection): Bitmap? {
                // Log.d("Hello", String.valueOf(conn.getContentLength()));
                setProgressBar(conn.contentLength)
                var data: Bitmap? = null
                try {
                    val inputStream = conn.inputStream
                    val byteData = getByteArrayFromInputStream(inputStream)
                    Log.d("Hello", byteData.size.toString())
                    data = BitmapFactory.decodeByteArray(byteData, 0, byteData.size)
                    data = Bitmap.createScaledBitmap(data, 128,128, true)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                return data
            }

            //adapter
            @Throws(IOException::class)
            private fun getByteArrayFromInputStream(inputStream: InputStream): ByteArray {
                val buffer = ByteArrayOutputStream()
                var nRead: Int
                val data = ByteArray(4096)
                var progress = 0
                while (inputStream.read(data, 0, data.size).also { nRead = it } != -1) {
                    buffer.write(data, 0, nRead)
                    progress += nRead
                    onProgressUpdate(progress)
                }
                return buffer.toByteArray()
            }

            private fun setProgressBar(max: Int) {
                activity.runOnUiThread {
                    binding.progressBar.setMin(0)
                    binding.progressBar.setMax(max)
                }

            }
        }
    }

    fun updateList(newList : List<String?>) {
        pictures = newList
        notifyDataSetChanged()
    }
}