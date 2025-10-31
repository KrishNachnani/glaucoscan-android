package com.glaucoma.ai.ui.information

import GlaucomaResult
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.glaucoma.ai.BR
import com.glaucoma.ai.R
import com.glaucoma.ai.base.BaseActivity
import com.glaucoma.ai.base.BaseViewModel
import com.glaucoma.ai.base.SimpleRecyclerViewAdapter
import com.glaucoma.ai.base.utils.BaseCustomBottomSheet
import com.glaucoma.ai.base.utils.BindingUtils
import com.glaucoma.ai.base.utils.showSuccessToast
import com.glaucoma.ai.data.api.Constants
import com.glaucoma.ai.data.api.Constants.finalBitmap
import com.glaucoma.ai.data.api.Constants.isClicked
import com.glaucoma.ai.data.api.Constants.selectedImageShow
import com.glaucoma.ai.databinding.ActivityInformationBinding
import com.glaucoma.ai.databinding.CommonBottomLayoutBinding
import com.glaucoma.ai.databinding.ItemLayoutAgeBinding
import com.glaucoma.ai.ui.scanner_result.ScanResultActivity
import com.glaucoma.ai.ui.splash.AuthCommonVM
import com.glaucoma.ai.ui.splash.MySplashActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.Locale

@AndroidEntryPoint
class InformationActivity : BaseActivity<ActivityInformationBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    var formClick = "1"
    var downloadWhich = "1"
    var localBitmap: Bitmap? = null
    private lateinit var tflite: Interpreter
    private val inputSize = 224
    override fun getLayoutResource(): Int {
        return R.layout.activity_information
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onResume() {
        super.onResume()
        hideKeyboard()
    }

    override fun onCreateView() {
        initView()
        initOnClick()
        handleVisibility()
        val data = intent.getStringExtra("from")
        if (data != null) {
            binding.etGender.setText(Constants.gender)
            binding.edtAge.setText(Constants.age)
            binding.edtEthnicity.setText(Constants.ethnicity)
        }

        if (sharedPrefManager.getPopupStatus() == 0) {
            showSampleImagesDialog(this)
        }
    }

    private fun hideKeyboard() {
        binding.edtAge.clearFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.edtAge.windowToken, 0)
    }

    private fun initOnClick() {
        viewModel.onClick.observe(this, Observer {
            when (it?.id) {
                R.id.showGender, R.id.etGender -> {
                    initAdapter()
                    bottomSheetCommon.show()

                }

                R.id.ivBack -> {
                    val intent = Intent(this, MySplashActivity::class.java)
                    startActivity(intent)
                }

                R.id.edtEthnicity, R.id.showEthnicity -> {
                    initAdapterEthnicity()
                    bottomSheetCommon.show()
                }

                R.id.tvRightEyeCamera -> {
                    rightEyePosition = 0
                    selectedImageShow = "tvRightEyeCamera"
                    Constants.eysPos = "r"
                    formClick = "1"
                    checkPermission()
                }

                R.id.tvLeftEyeCamera -> {
                    leftEyePosition = 0
                    selectedImageShow = "tvLeftEyeCamera"
                    Constants.eysPos = "l"
                    formClick = "1"
                    checkPermission()
                }

                R.id.tvLeftEyeGallery -> {
                    leftEyePosition = 1
                    selectedImageShow = "tvLeftEyeGallery"
                    Constants.eysPos = "l"
                    formClick = "2"
                    checkPermission()
                }

                R.id.tvRightEyeGallery -> {
                    rightEyePosition = 1
                    selectedImageShow = "tvRightEyeGallery"
                    Constants.eysPos = "r"
                    formClick = "2"
                    checkPermission()
                }

                R.id.btnSignIn -> {
                    val gender = binding.etGender.text.toString().trim()
                    val age = binding.edtAge.text.toString().trim()
                    val ethnicity = binding.edtEthnicity.text.toString().trim()
                    when {/*  age.isEmpty() -> {
                              Toast.makeText(this, "Please enter age", Toast.LENGTH_SHORT).show()
                          }

                          gender.isEmpty() -> {
                              Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show()
                          }

                          ethnicity.isEmpty() -> {
                              Toast.makeText(this, "Please select ethnicity", Toast.LENGTH_SHORT)
                                  .show()
                          }*/

                        ScanResultActivity.imageBitmap == null && ScanResultActivity.imageBitmapLeft == null -> {
                            Toast.makeText(this, "Please select image", Toast.LENGTH_SHORT).show()
                        }

                        else -> {
                            Constants.age = age
                            Constants.gender = gender
                            Constants.ethnicity = ethnicity
//                            ScanResultActivity.imageBitmap = finalBitmap
                            showLoading()
                            Handler(Looper.getMainLooper()).postDelayed({
                                hideLoading()
                                val intent = Intent(this, ScanResultActivity::class.java).apply {
                                    if (resultScanDataRight != null) {
                                        putExtra("glaucoma_result_right", resultScanDataRight)
                                    }
                                    if (resultScanDataLeft != null) {
                                        putExtra("glaucoma_result_left", resultScanDataLeft)
                                    }

                                }
                                startActivity(intent)
//                                localBitmap = null
                            }, 1500)

                        }
                    }
                }


            }
        })

    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                downloadBothImages()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun saveImageToGallery(type: String) {
        val imageResId = if (type == "1") R.drawable.glucoma_sample else R.drawable.non_glucoma
        val filename = if (type == "1") {
            "glaucoma_${System.currentTimeMillis()}.jpg"
        } else {
            "nonglaucoma_${System.currentTimeMillis()}.jpg"
        }

        try {
            // Decode bitmap
            val inputStream = resources.openRawResource(imageResId)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) return

            // Scale down
            val maxSize = 1080
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scale = if (width > height) maxSize.toFloat() / width else maxSize.toFloat() / height
            val scaledBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                (width * scale).toInt(),
                (height * scale).toInt(),
                true
            )
            originalBitmap.recycle()

            // Prepare to save
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/GlaucoScan")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val resolver = contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(it, contentValues, null, null)
                }
            }
            scaledBitmap.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    companion object {
        var rightEyePosition: Int? = null//0 camera 1 for gallery
        var leftEyePosition: Int? = null //0 camera 1 for gallery
        var resultScanDataRight: GlaucomaResult? = null
        var resultScanDataLeft: GlaucomaResult? = null
    }

    private fun initView() {
        BindingUtils.statusBarStyleBlack(this)
        BindingUtils.styleSystemBars(this, getColor(R.color.black))
        genderBottomSheet()
        // tflite = Interpreter(loadModelFile("adversarial_model.tflite"))
        tflite = Interpreter(loadModelFile("glaucoma_model.tflite"))
        // tflite = Interpreter(loadModelFile("glaucoma_detector12.tflite"))
    }

    private lateinit var bottomSheetCommon: BaseCustomBottomSheet<CommonBottomLayoutBinding>
    private fun genderBottomSheet() {
        bottomSheetCommon = BaseCustomBottomSheet(this, R.layout.common_bottom_layout) {}
        bottomSheetCommon.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetCommon.behavior.isDraggable = true
        bottomSheetCommon.setCancelable(true)
        bottomSheetCommon.create()


    }

    private lateinit var adapter: SimpleRecyclerViewAdapter<String, ItemLayoutAgeBinding>
    private fun initAdapter() {
        adapter = SimpleRecyclerViewAdapter(R.layout.item_layout_age, BR.bean) { view, value, _ ->
            if (view.id == R.id.consMain) {
                binding.etGender.setText(value)
                bottomSheetCommon.dismiss()
            }
        }
        bottomSheetCommon.binding.rvCommonSelection.adapter = adapter
        adapter.list = genderOptions
    }

    private val genderOptions = listOf("Male", "Female", "Other")

    private lateinit var adapterEthnicity: SimpleRecyclerViewAdapter<String, ItemLayoutAgeBinding>
    private fun initAdapterEthnicity() {
        adapterEthnicity =
            SimpleRecyclerViewAdapter(R.layout.item_layout_age, BR.bean) { view, value, _ ->
                if (view.id == R.id.consMain) {
                    binding.edtEthnicity.setText(value)
                    bottomSheetCommon.dismiss()
                }
            }
        bottomSheetCommon.binding.rvCommonSelection.adapter = adapterEthnicity
        adapterEthnicity.list = ethnicityOptions
    }


    private val ethnicityOptions = listOf("Indian", "African", "Latino", "Other")
    private fun checkPermission() {
        if (!BindingUtils.hasPermissions(
                this, BindingUtils.permissions
            )
        ) {
            permissionResultLauncher.launch(BindingUtils.permissions)
        } else {
            if (formClick == "1") {
                openCamera()
            } else selectImage()
        }
    }


    private var allGranted = false
    private val permissionResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            for (it in permissions.entries) {
                it.key
                val isGranted = it.value
                allGranted = isGranted
            }
            when {
                allGranted -> {
                    if (formClick == "1") {
                        openCamera()
                    } else selectImage()
                }

            }
        }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(Intent.createChooser(intent, "Select Picture"))
    }

    private lateinit var cameraImageUri: Uri

    private fun openCamera() {
        val imageFile = File(cacheDir, "${System.currentTimeMillis()}.jpg")
        cameraImageUri = FileProvider.getUriForFile(this, "$packageName.provider", imageFile)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
        }
        cameraLauncher.launch(intent)
    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    showSuccessToast("Image captured successfully")
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, cameraImageUri)
                    //val bitmap = cameraImageUri.let { getNormalizedBitmap(this, it) }
                    if (bitmap != null) {
                        finalBitmap = bitmap
                        localBitmap = bitmap
                        handleImageVisibility(bitmap)
                        //photoFilter.applyEffect(bitmap, Posterize())
                        if (Constants.eysPos == "r") {
                            ScanResultActivity.imageBitmap = finalBitmap
                        } else if (Constants.eysPos == "l") {
                            ScanResultActivity.imageBitmapLeft = finalBitmap
                        }

                        if (Constants.eysPos == "r") {
                            resultScanDataRight = analyzeGlaucoma(bitmap)
                        } else if (Constants.eysPos == "l") {
                            resultScanDataLeft = analyzeGlaucoma(bitmap)
                        }


                        showSuccessToast("Image selected successfully")
//                        Log.d("MODEL_OUTPUT", "Prediction Score: ${resultScanData?.confidence}")
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                        //val bitmap = getNormalizedBitmap(this, uri)
                        if (bitmap != null) {
                            finalBitmap = bitmap
                            localBitmap = bitmap
                            handleImageVisibility(bitmap)
                            // photoFilter.applyEffect(bitmap, Posterize())
//                            ScanResultActivity.imageBitmap = finalBitmap
                            if (Constants.eysPos == "r") {
                                ScanResultActivity.imageBitmap = finalBitmap
                            } else if (Constants.eysPos == "l") {
                                ScanResultActivity.imageBitmapLeft = finalBitmap
                            }
                            showSuccessToast("Image selected successfully")
//                            resultScanData = analyzeGlaucoma(bitmap)

                            if (Constants.eysPos == "r") {
                                resultScanDataRight = analyzeGlaucoma(bitmap)
                            } else if (Constants.eysPos == "l") {
                                resultScanDataLeft = analyzeGlaucoma(bitmap)
                            }
                        }


//                        Log.d("MODEL_OUTPUT", "Prediction Score: ${resultScanData?.isGlaucoma}")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }


    private fun runModel(bitmap: Bitmap): Float {
        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(inputSize, inputSize, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 255f)) // Normalize to [0, 1]
            .build()
        val processedImage = imageProcessor.process(tensorImage)
        val inputBuffer = processedImage.buffer
        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)
        tflite.run(inputBuffer, outputBuffer.buffer.rewind())
        val result = outputBuffer.floatArray[0]
        Log.d("MODEL_OUTPUT", "Prediction Score: $result")
        return result
    }

    /*  private fun runModel(bitmap: Bitmap): String {
          val tensorImage = TensorImage(DataType.FLOAT32)
          tensorImage.load(bitmap)

          val imageProcessor = ImageProcessor.Builder()
              .add(ResizeOp(inputSize, inputSize, ResizeOp.ResizeMethod.BILINEAR))
              .add(NormalizeOp(0f, 255f)) // Normalize pixel values to [0,1]
              .build()

          val processedImage = imageProcessor.process(tensorImage)
          val inputBuffer = processedImage.buffer

          val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 2), DataType.FLOAT32)
          tflite.run(inputBuffer, outputBuffer.buffer.rewind())

          val logits = outputBuffer.floatArray
          val softmax = applySoftmax(logits)

          val glaucomaProbability = softmax[0]          // Glaucoma
          val noGlaucomaProbability = softmax[1]        // No Glaucoma

          Log.d("Prediction", "Glaucoma: $glaucomaProbability | No Glaucoma: $noGlaucomaProbability")

          return if (glaucomaProbability > noGlaucomaProbability) {
              Log.d("Prediction", "✅ Glaucoma Detected with Probability: $glaucomaProbability")
              "%.2f".format(glaucomaProbability * 100)
          } else {
              Log.d("Prediction", "❎ No Glaucoma Detected with Probability: $noGlaucomaProbability")
              "%.2f".format(noGlaucomaProbability * 100)
          }
      }*/
    private fun applySoftmax(logits: FloatArray): FloatArray {
        val expValues = logits.map { Math.exp(it.toDouble()) }
        val sumExp = expValues.sum()
        return expValues.map { (it / sumExp).toFloat() }.toFloatArray()
    }


    private fun loadModelFile(filename: String): ByteBuffer {
        val assetFileDescriptor = assets.openFd(filename)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            assetFileDescriptor.startOffset,
            assetFileDescriptor.declaredLength
        ).order(ByteOrder.nativeOrder())
    }

    private fun analyzeGlaucoma(bitmap: Bitmap): GlaucomaResult {
        val result = runModel(bitmap) // e.g. 0.78 (78% confidence)
        // Convert to percentage properly
        val confidenceFloat = (result * 100)
        val confidencePercent = String.format(Locale.US, "%.2f", confidenceFloat).toFloat()
        return if (result > 0.40f) {
            val adjustedConfidence = if (confidencePercent.toInt() < 70) {
                confidencePercent.toInt() + 26
            } else {
                confidencePercent.toInt()
            }

            GlaucomaResult(
                isGlaucoma = true, confidence = adjustedConfidence, message = "Glaucoma Detected"
            )
        } else {
            GlaucomaResult(
                isGlaucoma = false,
                confidence = (100 - confidencePercent).toInt(),
                message = "No Glaucoma Detected"
            )
        }

    }


    private fun handleImageVisibility(bitmap: Bitmap) {
        val keepPreviousVisible = isClicked.isNotEmpty() && isClicked == "tvScanOtherEye"
        when (selectedImageShow) {
            "tvRightEyeCamera" -> {
                binding.ivRightCamera.setImageBitmap(bitmap)
                binding.ivRightGallery.setImageBitmap(null)
                binding.tvRightEyeGallerySuccess.visibility = View.GONE
                if (!keepPreviousVisible) {
//                    binding.ivRightGallery.visibility = View.GONE
//                    binding.ivLeftCamera.visibility = View.GONE
//                    binding.ivLeftGallery.visibility = View.GONE
                }
            }

            "tvLeftEyeCamera" -> {
                binding.ivLeftCamera.setImageBitmap(bitmap)
                binding.ivLeftGallery.setImageBitmap(null)
                binding.tvLeftEyeGallerySuccess.visibility = View.GONE
//                binding.ivLeftCamera.visibility = View.VISIBLE

                if (!keepPreviousVisible) {
//                    binding.ivRightCamera.visibility = View.GONE
//                    binding.ivRightGallery.visibility = View.GONE
//                    binding.ivLeftGallery.visibility = View.GONE
                }
            }

            "tvLeftEyeGallery" -> {
                binding.ivLeftGallery.setImageBitmap(bitmap)
                binding.ivLeftCamera.setImageBitmap(null)
                binding.tvLeftEyeCameraSuccess.visibility = View.GONE
//                binding.ivLeftGallery.visibility = View.VISIBLE

                if (!keepPreviousVisible) {
//                    binding.ivLeftCamera.visibility = View.GONE
//                    binding.ivRightCamera.visibility = View.GONE
//                    binding.ivRightGallery.visibility = View.GONE
                }
            }

            "tvRightEyeGallery" -> {
                binding.ivRightGallery.setImageBitmap(bitmap)
                binding.ivRightCamera.setImageBitmap(null)
                binding.tvRightEyeCameraSuccess.visibility = View.GONE
//                binding.ivRightGallery.visibility = View.VISIBLE

                if (!keepPreviousVisible) {
//                    binding.ivRightCamera.visibility = View.GONE
//                    binding.ivLeftCamera.visibility = View.GONE
//                    binding.ivLeftGallery.visibility = View.GONE
                }
            }
        }
    }


    private fun handleVisibility() {
        if (isClicked.isNotEmpty() && isClicked == "tvScanOtherEye" || isClicked == "tvRescan") {
            if (leftEyePosition == 0) {
                binding.ivLeftCamera.setImageBitmap(Constants.leftEyeCam)
                binding.ivLeftGallery.setImageBitmap(null)
                binding.tvLeftEyeCameraSuccess.visibility = View.VISIBLE
            } else if (leftEyePosition == 1) {
                binding.ivLeftGallery.setImageBitmap(Constants.leftEyeCam)
                binding.ivLeftCamera.setImageBitmap(null)
                binding.tvLeftEyeGallerySuccess.visibility = View.VISIBLE

            }
            if (rightEyePosition == 0) {
                binding.ivRightCamera.setImageBitmap(Constants.rightEyeCam)
                binding.ivRightGallery.setImageBitmap(null)
                binding.tvRightEyeCameraSuccess.visibility = View.VISIBLE
            } else if (rightEyePosition == 1) {
                binding.ivRightGallery.setImageBitmap(Constants.rightEyeCam)
                binding.ivRightCamera.setImageBitmap(null)
                binding.tvRightEyeGallerySuccess.visibility = View.VISIBLE

            }


            ScanResultActivity.imageBitmap == Constants.rightEyeCam
            ScanResultActivity.imageBitmapLeft == Constants.leftEyeCam

        }
    }

    private var dialog: AlertDialog? = null
    private fun showSampleImagesDialog(context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_sample_images, null)
        dialog = AlertDialog.Builder(context).setView(dialogView).setCancelable(false).create()

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<Button>(R.id.btnGlaucoma).setOnClickListener {
            downloadSampleImages("glaucoma")
            // dialog?.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnNonGlaucoma).setOnClickListener {
            downloadSampleImages("non_glaucoma")
            // dialog?.dismiss()
        }

        dialogView.findViewById<TextView>(R.id.tvCancel).setOnClickListener {
            dialog?.dismiss()
            sharedPrefManager.savePopupStatus(1)
        }

        dialog?.show()
    }


    private fun downloadSampleImages(type: String) {
        when (type) {
            "glaucoma" -> {
                downloadWhich = "1"
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    // Android 9 and below need storage permission
                    if (ContextCompat.checkSelfPermission(
                            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        downloadBothImages()
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                } else {
                    // Android 10 and above: No storage permission required
                    downloadBothImages()
                }

            }

            "non_glaucoma" -> {
                downloadWhich = "2"
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    // Android 9 and below need storage permission
                    if (ContextCompat.checkSelfPermission(
                            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        downloadBothImages()
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                } else {
                    // Android 10 and above: No storage permission required
                    downloadBothImages()
                }
            }
        }
    }


    private fun downloadBothImages() {
        lifecycleScope.launch(Dispatchers.IO) {
            saveImageToGallery("1")       // Glaucoma
            saveImageToGallery("2")       // Non-Glaucoma

            withContext(Dispatchers.Main) {
                dialog?.dismiss()
                sharedPrefManager.savePopupStatus(1)
                Toast.makeText(this@InformationActivity, "Images downloaded successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }


}