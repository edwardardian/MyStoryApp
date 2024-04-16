package com.example.mystoryapp

import android.Manifest
import com.example.mystoryapp.utils.Result
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagingConfig
import com.example.mystoryapp.MainActivity.Companion.KEY_TOKEN
import com.example.mystoryapp.data.response.StoryPagingSource
import com.example.mystoryapp.data.retrofit.ApiConfig
import com.example.mystoryapp.database.StoryRepository
import com.example.mystoryapp.databinding.ActivityUploadBinding
import com.example.mystoryapp.model.UploadViewModel
import com.example.mystoryapp.model.UploadViewModelFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class UploadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadBinding

    private lateinit var uploadViewModel: UploadViewModel

    private var pathImg: String = ""

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val image = result.data?.data as Uri
            image.let { uri ->
                val file = reduceFile(fileUri(uri))
                uploadViewModel.imageFile.postValue(file)
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val file = File(pathImg)
            file.let { image ->
                val bitmap = BitmapFactory.decodeFile(image.path)
                rotateImage(bitmap, pathImg).compress(
                    Bitmap.CompressFormat.JPEG,
                    100,
                    FileOutputStream(image)
                )
                uploadViewModel.imageFile.postValue(reduceFile(image))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val token = intent.getStringExtra(KEY_TOKEN)
        val storyRepository = StoryRepository(ApiConfig.getApiService(token!!), PagingConfig(StoryPagingSource.INITIAL_PAGE_INDEX))

        val viewModelFactory = UploadViewModelFactory(
            storyRepository
        )

        uploadViewModel = ViewModelProvider(this, viewModelFactory).get(UploadViewModel::class.java)

        observeViewModel()

        binding.apply {

            btnCamera.setOnClickListener {
                if (!checkImagePermission()) {
                    ActivityCompat.requestPermissions(
                        this@UploadActivity,
                        REQUIRED_CAMERA_PERMISS,
                        REQUEST_CODE_PERMISS
                    )

                    if (checkImagePermission()) {
                        startCamera()
                    }
                } else {
                    startCamera()
                }
            }

            btnGallery.setOnClickListener {
                val intent = Intent()
                intent.action = Intent.ACTION_GET_CONTENT
                intent.type = "image/*"
                val chooser = Intent.createChooser(intent, "Pilih Gambar")
                galleryLauncher.launch(chooser)
            }

            btnUpload.setOnClickListener {
                if (uploadViewModel.imageFile.value == null) {
                    showToast("Gambar tidak boleh kosong!")
                } else if (edDesc.text.toString().isEmpty()) {
                    showToast("Deskripsi tidak boleh kosong!")
                } else {
                    uploadViewModel.upload(edDesc.text.toString())
                        .observe(this@UploadActivity) { result ->
                            when (result) {
                                is Result.Loading -> {
                                    uploadViewModel.isLoading.postValue(true)
                                }

                                is Result.Success -> {
                                    uploadViewModel.isLoading.postValue(false)
                                    showToast(result.data.message.toString())

                                    val intent = Intent()
                                    setResult(RESULT_OK, intent)
                                    finish()
                                }

                                is Result.Error -> {
                                    uploadViewModel.isLoading.postValue(false)
                                    uploadViewModel.errorText.postValue(result.error)
                                }
                            }
                        }
                }
            }
        }
    }

    private fun observeViewModel() {
        uploadViewModel.apply {
            imageFile.observe(this@UploadActivity) {
                binding.ivStoryImage.setImageBitmap(BitmapFactory.decodeFile(it.path))
            }

            isLoading.observe(this@UploadActivity) {
                showLoading(it)
            }

            errorText.observe(this@UploadActivity) {
                showToast(it)
            }
        }
    }

    private fun startCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val customTempFile = File.createTempFile(
            SimpleDateFormat(
                "dd-MMM-yyyy",
                Locale.US
            ).format(System.currentTimeMillis()), ".jpg", storageDir
        )
        customTempFile.also {
            pathImg = it.absolutePath
            intent.putExtra(
                MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(
                    this@UploadActivity,
                    "com.example.mystoryapp",
                    it
                )
            )
            cameraLauncher.launch(intent)
        }
    }

    private fun rotateImage(bitmap: Bitmap, path: String): Bitmap {
        val orientation = ExifInterface(path).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
        }

        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    private fun fileUri(uri: Uri): File {
        val myFile = File.createTempFile(
            SimpleDateFormat(
                "dd-MMM-yyyy",
                Locale.US
            ).format(System.currentTimeMillis()),
            ".jpg",
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )

        val inputStream = contentResolver.openInputStream(uri) as InputStream
        val outputStream: OutputStream = FileOutputStream(myFile)
        val buf = ByteArray(1024)
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
        outputStream.close()
        inputStream.close()

        return myFile
    }

    private fun checkImagePermission() = REQUIRED_CAMERA_PERMISS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun reduceFile(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)
        var compressQuality = 100
        var streamLength: Int

        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
        } while (streamLength > 1000000)

        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
        return file
    }

    private fun showLoading(isLoading: Boolean) {
        with(binding) {
            progressbar.isVisible = isLoading
            btnUpload.isVisible = !isLoading
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private val REQUIRED_CAMERA_PERMISS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISS = 101
    }
}