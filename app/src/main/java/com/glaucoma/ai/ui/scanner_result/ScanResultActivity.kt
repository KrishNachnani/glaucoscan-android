package com.glaucoma.ai.ui.scanner_result

import GlaucomaResult
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Observer
import com.glaucoma.ai.R
import com.glaucoma.ai.base.BaseActivity
import com.glaucoma.ai.base.BaseViewModel
import com.glaucoma.ai.base.utils.BindingUtils
import com.glaucoma.ai.data.api.Constants
import com.glaucoma.ai.data.api.Constants.isClicked
import com.glaucoma.ai.data.api.Constants.selectedImageShow
import com.glaucoma.ai.databinding.ActivityScanResultBinding
import com.glaucoma.ai.ui.splash.AuthCommonVM
import com.glaucoma.ai.ui.information.InformationActivity
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

@AndroidEntryPoint
class ScanResultActivity : BaseActivity<ActivityScanResultBinding>() {
    private val viewModel: AuthCommonVM by viewModels()


    override fun getLayoutResource(): Int {
        return R.layout.activity_scan_result
    }

    companion object {
        var imageBitmap: Bitmap? = null
        var imageBitmapLeft: Bitmap? = null
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
        firstTimeViewGone()
        initView()
        initOnClick()
        if (imageBitmap != null) {
            Constants.rightEyeCam = imageBitmap
            handleImageVisibility(imageBitmap!!)
            binding.ivPersonImage.setImageBitmap(imageBitmap)

        } else {
//            showToast("Image not found")
        }
        if (imageBitmapLeft != null) {
            Constants.leftEyeCam = imageBitmapLeft
            handleImageVisibility(imageBitmapLeft!!)
            binding.ivPersonImageLeft.setImageBitmap(imageBitmapLeft)

        } else {
//            showToast("Image not found")
        }
        if (imageBitmapLeft != null && imageBitmap != null) {
            binding.tvScanOtherEye.visibility = View.GONE
        } else {
            binding.tvScanOtherEye.visibility = View.VISIBLE
        }


    }

    private fun firstTimeViewGone() {
        binding.cardLeft.visibility = View.GONE
        binding.tvResultTitleLeft.visibility = View.GONE
        binding.tvConfidenceLeft.visibility = View.GONE
        binding.tvLeftEyeText.visibility = View.GONE
        binding.card.visibility = View.GONE
        binding.tvResultTitle.visibility = View.GONE
        binding.tvConfidence.visibility = View.GONE
        binding.tvRightEyeText.visibility = View.GONE
    }

    private fun handleImageVisibility(bitmap: Bitmap) {
//        when (selectedImageShow) {
//            "tvRightEyeCamera" -> {
//                Constants.rightEyeCamBitmap = bitmap
//            }
//
//            "tvLeftEyeCamera" -> {
//                Constants.leftEyeCamBitmap = bitmap
//
//            }
//
//            "tvLeftEyeGallery" -> {
//                Constants.leftEyeGalleyBitmap = bitmap
//
//            }
//
//            "tvRightEyeGallery" -> {
//                Constants.rightEyeGalleryBitmap = bitmap
//
//
//            }
//        }
    }


    private fun initOnClick() {
        viewModel.onClick.observe(this, Observer {
            when (it?.id) {
                R.id.ivBack -> {
                    onBackPressedDispatcher.onBackPressed()
                }

                R.id.tvNewProfile -> {
                    isClicked = "tvNewProfile"
                    val isGlucoma = if (binding.tvResultTitle.text.equals("Glaucoma Detected")) {
                        true
                    } else {
                        false
                    }
                    try {
                        savePredictedImage(
                            this,
                            binding.ivPersonImage.drawable.toBitmap(),
                            Constants.gender,
                            Constants.age,
                            Constants.ethnicity,
                            isGlucoma,
                            Constants.eysPos
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        nullValueFuction()
                        val intent = Intent(this, InformationActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                }

                R.id.tvRescan -> {
                    isClicked = "tvRescan"
//                    when (selectedImageShow) {
//                        "tvRightEyeCamera" -> {
//                            Constants.rightEyeCamBitmap = null
//                        }
//
//                        "tvLeftEyeCamera" -> {
//                            Constants.leftEyeCamBitmap = null
//                        }
//
//                        "tvLeftEyeGallery" -> {
//                            Constants.leftEyeGalleyBitmap = null
//                        }
//
//                        "tvRightEyeGallery" -> {
//                            Constants.rightEyeGalleryBitmap = null
//                        }
//                    }
//                    Log.i("qf23g", "savePredictedImage: " + selectedImageShow)


                    val intent = Intent(this, InformationActivity::class.java)
                    intent.putExtra("from", "rescan")
                    startActivity(intent)
                    finish()
                }

                R.id.tvScanOtherEye -> {
                    isClicked = "tvScanOtherEye"
                    val isGlucoma = if (binding.tvResultTitle.text.equals("Glaucoma Detected")) {
                        true
                    } else {
                        false
                    }
                    try {
                        savePredictedImage2(
                            this,
                            binding.ivPersonImage.drawable.toBitmap(),
                            Constants.gender,
                            Constants.age,
                            Constants.ethnicity,
                            isGlucoma,
                            Constants.eysPos
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        nullValueFuction()
                        val intent = Intent(this, InformationActivity::class.java)
                        intent.putExtra("from", "rescan")
                        startActivity(intent)
                        finish()
                    }


                }
            }
        })

    }

    private fun initView() {
        BindingUtils.statusBarStyleBlack(this)
        BindingUtils.styleSystemBars(this, getColor(R.color.black))

        val result = intent.getParcelableExtra<GlaucomaResult>("glaucoma_result_right")
        result?.let {
            binding.tvResultTitle.text = it.message
            binding.tvRightEyeText.text = "Confidence: ${it.confidence}%"
            binding.card.visibility = View.VISIBLE
            binding.tvResultTitle.visibility = View.VISIBLE
            binding.tvConfidence.visibility = View.VISIBLE
            binding.tvRightEyeText.visibility = View.VISIBLE
            Log.d(
                "ReceivedResult",
                "glaucoma_result_right: ${it.isGlaucoma}, Confidence: ${it.confidence}, Message: ${it.message}"
            )
            // Use result in your UI
        }


        val result2 = intent.getParcelableExtra<GlaucomaResult>("glaucoma_result_left")
        result2?.let {
            binding.tvResultTitleLeft.text = it.message
            binding.tvLeftEyeText.text = "Confidence: ${it.confidence}%"
            binding.cardLeft.visibility = View.VISIBLE
            binding.tvResultTitleLeft.visibility = View.VISIBLE
            binding.tvConfidenceLeft.visibility = View.VISIBLE
            binding.tvLeftEyeText.visibility = View.VISIBLE
            Log.d(
                "ReceivedResult",
                "glaucoma_result_left: ${it.isGlaucoma}, Confidence: ${it.confidence}, Message: ${it.message}"
            )
            // Use result in your UI
        }
    }
    // Save using MediaStore (Android 10+)
    private fun saveImageMediaStore(context: Context, bitmap: Bitmap, fileName: String): String {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/glaucoscan.ai")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: throw Exception("Failed to create MediaStore entry")

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
                throw Exception("Failed to compress bitmap")
            }
        } ?: throw Exception("Failed to open output stream")

        // Mark as complete
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(uri, contentValues, null, null)
        }

        Log.d("ImageSave", "Saved via MediaStore: $fileName at $uri")
        return fileName
    }

    // Save using legacy file system (Android 9 and below)
    private fun saveImageLegacy(context: Context, bitmap: Bitmap, fileName: String): String {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val folder = File(picturesDir, "glaucoscan.ai")

        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw Exception("Failed to create directory: ${folder.absolutePath}")
            }
        }

        val imageFile = File(folder, fileName)

        FileOutputStream(imageFile).use { outputStream ->
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
                throw Exception("Failed to compress bitmap")
            }
            outputStream.flush()
        }

        // Notify media scanner
        MediaScannerConnection.scanFile(
            context,
            arrayOf(imageFile.absolutePath),
            arrayOf("image/jpeg"),
            null
        )

        Log.d("ImageSave", "Saved via legacy method: ${imageFile.absolutePath}")
        return fileName
    }
    private fun savePredictedImage(
        context: Context,
        bitmap: Bitmap,
        gender: String,
        age: String,
        ethnicity: String,
        isGlaucoma: Boolean,
        isEyePos: String
    ) {
        val timeStamp = System.currentTimeMillis()
        val sdf = java.text.SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault())
        val dateTime = sdf.format(java.util.Date(timeStamp))
        val labelGlaucoma = if (isGlaucoma) "1" else "0"
        val genderLabel = if (gender.lowercase() == "male") "m" else "f"
//        val fileName = "image-$dateTime-$genderLabel-$age-$ethnicity-$labelGlaucoma-$isEyePos.jpeg"
        var fileName:String=""
        var fileName2:String=""
        if (imageBitmapLeft != null){
            fileName = "image-$dateTime-$genderLabel-$age-$ethnicity-$labelGlaucoma-l.jpeg"
        }
        if (imageBitmap  != null){
            fileName2 = "image-$dateTime-$genderLabel-$age-$ethnicity-$labelGlaucoma-r.jpeg"
        }
        //val outputDir = File(context.getExternalFilesDir(null), "glaucoscan.ai")
        // val outputDir = File(getExternalFilesDir(null), "glaucoscan.ai")

        // val folder = File(context.filesDir, "glaucoscan.ai")
        // if (!folder.exists()) folder.mkdirs()
        //if (!outputDir.exists()) outputDir.mkdirs()

        val picturesDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val folder = File(picturesDir, "glaucoscan.ai")
        if (!folder.exists()) folder.mkdirs()

        /*     val imageFile = File(folder, fileName)
             val outputStream = FileOutputStream(imageFile)
             bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
             outputStream.flush()
             outputStream.close()
     */
        var fileNameLeft=""
        var fileNameRight=""

        if (imageBitmapLeft != null){
//            val imageFile = File(folder, fileName)
//            fileNameLeft=imageFile.name
//            val outputStream = FileOutputStream(imageFile)
//            imageBitmapLeft!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
//            outputStream.flush()
//            outputStream.close()
//            val fileName = "image-$dateTime-$genderLabel-$age-$ethnicity-$labelGlaucoma-l.jpeg"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ (Use MediaStore)
                fileNameLeft = saveImageMediaStore(context, imageBitmapLeft!!, fileName)
            } else {
                // Android 9 and below (Legacy)
                fileNameLeft = saveImageLegacy(context, imageBitmapLeft!!, fileName)
            }
        }
        if (imageBitmap  != null){
//            val imageFile = File(folder, fileName2)
//            fileNameRight=imageFile.name
//            val outputStream = FileOutputStream(imageFile)
//            imageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
//            outputStream.flush()
//            outputStream.close()
//            val fileName2 = "image-$dateTime-$genderLabel-$age-$ethnicity-$labelGlaucoma-r.jpeg"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ (Use MediaStore)
                fileNameRight = saveImageMediaStore(context, imageBitmap!!, fileName2)
            } else {
                // Android 9 and below (Legacy)
                fileNameRight = saveImageLegacy(context, imageBitmap!!, fileName2)
            }
        }




        Toast.makeText(context, "Image Saved: ${fileNameLeft},${fileNameRight}", Toast.LENGTH_SHORT).show()
        when (isClicked) {
            "tvNewProfile" -> {
                imageBitmap= null
                imageBitmapLeft= null
                nullValueFuction()
                val intent = Intent(this, InformationActivity::class.java)
                startActivity(intent)
                finish()
            }

            "tvRescan" -> {
                nullValueFuction()
                val intent = Intent(this, InformationActivity::class.java)
                intent.putExtra("from", "rescan")
                startActivity(intent)

                finish()
            }

            "tvScanOtherEye" -> {
                nullValueFuction()
                val intent = Intent(this, InformationActivity::class.java)
                intent.putExtra("from", "rescan")
                startActivity(intent)
                finish()
            }
        }


    }
    private fun savePredictedImage2(
        context: Context,
        bitmap: Bitmap,
        gender: String,
        age: String,
        ethnicity: String,
        isGlaucoma: Boolean,
        isEyePos: String
    ) {
        val timeStamp = System.currentTimeMillis()
        val sdf = java.text.SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault())
        val dateTime = sdf.format(java.util.Date(timeStamp))

        val labelGlaucoma = if (isGlaucoma) "1" else "0"
        val genderLabel = if (gender.lowercase() == "male") "m" else "f"
//        val fileName = "image-$dateTime-$genderLabel-$age-$ethnicity-$labelGlaucoma-$isEyePos.jpeg"
        var fileName:String=""
        var fileName2:String=""
        if (imageBitmapLeft != null){
            fileName = "image-$dateTime-$genderLabel-$age-$ethnicity-$labelGlaucoma-l.jpeg"
        }
        if (imageBitmap  != null){
            fileName2 = "image-$dateTime-$genderLabel-$age-$ethnicity-$labelGlaucoma-r.jpeg"
        }
        //val outputDir = File(context.getExternalFilesDir(null), "glaucoscan.ai")
        // val outputDir = File(getExternalFilesDir(null), "glaucoscan.ai")

        // val folder = File(context.filesDir, "glaucoscan.ai")
        // if (!folder.exists()) folder.mkdirs()
        //if (!outputDir.exists()) outputDir.mkdirs()

        val picturesDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val folder = File(picturesDir, "glaucoscan.ai")
        if (!folder.exists()) folder.mkdirs()

   /*     val imageFile = File(folder, fileName)
        val outputStream = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
*/
var fileNameLeft=""
var fileNameRight=""

        if (imageBitmapLeft != null){
//            val imageFile = File(folder, fileName)
//            fileNameLeft=imageFile.name
//            val outputStream = FileOutputStream(imageFile)
//            imageBitmapLeft!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
//            outputStream.flush()
//            outputStream.close()
//            val fileName = "image-$dateTime-$genderLabel-$age-$ethnicity-$labelGlaucoma-l.jpeg"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ (Use MediaStore)
                fileNameLeft = saveImageMediaStore(context, imageBitmapLeft!!, fileName)
            } else {
                // Android 9 and below (Legacy)
                fileNameLeft = saveImageLegacy(context, imageBitmapLeft!!, fileName)
            }
        }
        if (imageBitmap  != null){
//            val imageFile = File(folder, fileName2)
//            fileNameRight=imageFile.name

//            val fileName2 = "image-$dateTime-$genderLabel-$age-$ethnicity-$labelGlaucoma-r.jpeg"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ (Use MediaStore)
                fileNameRight = saveImageMediaStore(context, imageBitmap!!, fileName2)
            } else {
                // Android 9 and below (Legacy)
                fileNameRight = saveImageLegacy(context, imageBitmap!!, fileName2)
            }
//            val outputStream = FileOutputStream(imageFile)
//            imageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
//            outputStream.flush()
//            outputStream.close()
        }




        Toast.makeText(context, "Image Saved: ${fileNameLeft},${fileNameRight}", Toast.LENGTH_SHORT).show()
        when (isClicked) {
            "tvNewProfile" -> {

                val intent = Intent(this, InformationActivity::class.java)
                startActivity(intent)
                finish()
            }

            "tvRescan" -> {

                val intent = Intent(this, InformationActivity::class.java)
                intent.putExtra("from", "rescan")
                startActivity(intent)

                finish()
            }

            "tvScanOtherEye" -> {

                val intent = Intent(this, InformationActivity::class.java)
                intent.putExtra("from", "rescan")
                startActivity(intent)
                finish()
            }
        }


    }

    private fun nullValueFuction() {

        InformationActivity.rightEyePosition = null//0 camera 1 for gallery
        InformationActivity.leftEyePosition = null //0 camera 1 for gallery
        InformationActivity.resultScanDataRight = null
        InformationActivity.resultScanDataLeft = null
//        imageBitmap= null
//        imageBitmapLeft= null

        Constants.age = ""
        Constants.gender = ""
        Constants.ethnicity = ""
        Constants.eysPos = "l"
        Constants.selectedImageShow = ""

        Constants.isClicked = ""
        Constants.finalBitmap = null

        Constants.leftEyeCam = null
        Constants.rightEyeCam = null
    }

}