package curtin.edu.mathtest.ui.students

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import curtin.edu.mathtest.R

import curtin.edu.mathtest.database.Image

import curtin.edu.mathtest.databinding.FragmentSelectImageSourceBinding
import java.io.ByteArrayOutputStream

class SelectImageSourceFragment : Fragment() {

    val CAM_REQUEST_CODE = 1998
    val SELECT_REQ_CODE = 1898
    private val navigationArgs: SelectImageSourceFragmentArgs by navArgs()
    private var _binding : FragmentSelectImageSourceBinding? = null
    private val binding get() = _binding!!
    private var currentImg : ByteArray? = null

    private val requestCameraPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                takePhoto()
            } else {
                displaySnackbar("Access denied")
            }
        }
    private val requestSelectPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                selectPhoto()
            } else {
                displaySnackbar("Access denied")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSelectImageSourceBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (navigationArgs.img.getImg() != null) {
            currentImg = navigationArgs.img.getImg()!!
            binding.imageSelectImageView.setImageBitmap(getBitmapFromByteArray(currentImg!!))
            binding.imageSelectImageView.tag = "not bg"
        }

        var id = navigationArgs.studentId
        var title = "Edit Student"
        if (id < 1) {
            title = "Add Student"
        }

        binding.selectImageCapture.setOnClickListener {
            prepTakePhoto()
        }

        binding.selectImageSource.setOnClickListener {
            prepSelectPhoto()
        }

        //TODO select from pixabay
        binding.selectImagePixabay.setOnClickListener {
            val action = SelectImageSourceFragmentDirections.actionSelectImageSourceFragmentToPixabayImagesFragment(id, Image(null))
            findNavController().navigate(action)
        }

        binding.selectImageDoneBtn.setOnClickListener {
            currentImg = getImageViewByteArray()
            val action = SelectImageSourceFragmentDirections.actionSelectImageSourceFragmentToNavigationAddContact(title, id, Image(currentImg))
            findNavController().navigate(action)
        }
    }

    private fun getBitmapFromByteArray(bArray : ByteArray) : Bitmap {
        return BitmapFactory.decodeByteArray(bArray, 0, bArray.size)
    }

    private fun prepTakePhoto() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
                takePhoto()
        } else requestCameraPermission()
    }

    private fun prepSelectPhoto() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {
            selectPhoto()
        } else requestSelectPermission()
    }

    private fun requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun requestSelectPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            requestSelectPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            requestSelectPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }


    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAM_REQUEST_CODE)
    }

    private fun selectPhoto() {
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.INTERNAL_CONTENT_URI
        )
        intent.type = "image/*"
        startActivityForResult(intent, SELECT_REQ_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK) {
            if (requestCode == CAM_REQUEST_CODE) {
                var bitmap = data!!.extras!!.get("data") as Bitmap
                bitmap = Bitmap.createScaledBitmap(bitmap, 128, 128, true)
                binding.imageSelectImageView.setImageBitmap(bitmap)
                binding.imageSelectImageView.tag = "not bg"
            }
            //source
            if (requestCode == SELECT_REQ_CODE) {
                val uri = data?.data
                var bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                bitmap = Bitmap.createScaledBitmap(bitmap, 128, 128, true)
                binding.imageSelectImageView.setImageBitmap(bitmap)
                binding.imageSelectImageView.tag = "not bg"
            }
        }
    }




    private fun getImageViewByteArray() : ByteArray? {
        if (binding.imageSelectImageView.tag != "bg") {
            val bitmap = (binding.imageSelectImageView.drawable as BitmapDrawable).bitmap
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            var imgByteArray = stream.toByteArray()
            return imgByteArray
        }
        return null
    }

    private fun displaySnackbar(message : String) {
        Snackbar.make(
            binding.imageSelectFrag,
            message,
            Snackbar.LENGTH_SHORT).show()
    }
}