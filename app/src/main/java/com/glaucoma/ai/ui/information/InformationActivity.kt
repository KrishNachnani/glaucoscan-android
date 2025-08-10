package com.glaucoma.ai.ui.information

import GlaucomaResult
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
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
    var localBitmap: Bitmap? = null
    private lateinit var tflite: Interpreter

    /*   private lateinit var photoFilter: PhotoFilter*/
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
    }

    fun hideKeyboard() {
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

                    when {
                        /*  age.isEmpty() -> {
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
      //  tflite = Interpreter(loadModelFile("adversarial_model.tflite"))
        tflite = Interpreter(loadModelFile("glaucoma_model.tflite"))

        /*  photoFilter = PhotoFilter(binding.effectView, object : OnProcessingCompletionListener {
              override fun onProcessingComplete(bitmap: Bitmap) {
                  runOnUiThread {
                      finalBitmap = bitmap
                      //  bottomSheetCommon.binding.ivPersonImage.setImageBitmap(bitmap)
                      // bottomSheetCommon.show()
                  }

                  // Do anything with the bitmap save it or add another effect to it
              }
          })*/

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

    val ethnicityOptions = listOf("Indian", "African", "Latino", "Other")


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
        val result = runModel(bitmap) // Your TFLite model output
        val confidenceFloat = String.format(Locale.US, "%.2f", result).toFloat()
        val confidencePercent = (confidenceFloat * 100).toInt()

        return if (confidenceFloat > 0.40f) {
            GlaucomaResult(
                isGlaucoma = true, confidence = confidencePercent, message = "Glaucoma Detected"
            )
        } else {
            GlaucomaResult(
                isGlaucoma = false,
                confidence = 100 - confidencePercent,
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
            } else  if (leftEyePosition == 1){
                binding.ivLeftGallery.setImageBitmap(Constants.leftEyeCam)
                binding.ivLeftCamera.setImageBitmap(null)
                binding.tvLeftEyeGallerySuccess.visibility = View.VISIBLE

            }
            if (rightEyePosition == 0) {
                binding.ivRightCamera.setImageBitmap(Constants.rightEyeCam)
                binding.ivRightGallery.setImageBitmap(null)
                binding.tvRightEyeCameraSuccess.visibility = View.VISIBLE
            } else   if (rightEyePosition == 1) {
                binding.ivRightGallery.setImageBitmap(Constants.rightEyeCam)
                binding.ivRightCamera.setImageBitmap(null)
                binding.tvRightEyeGallerySuccess.visibility = View.VISIBLE

            }


            ScanResultActivity.imageBitmap == Constants.rightEyeCam
            ScanResultActivity.imageBitmapLeft == Constants.leftEyeCam
//            if (Constants.leftEyeCamBitmap != null) {
//                binding.ivLeftCamera.setImageBitmap(Constants.leftEyeCamBitmap)
////                binding.ivLeftCamera.visibility = View.VISIBLE
////                binding.tvLeftEyeCameraSuccess.visibility = View.VISIBLE
//
//            } else {
////                binding.ivLeftCamera.visibility = View.GONE
////                binding.tvLeftEyeCameraSuccess.visibility = View.GONE
//            }
//            if (Constants.rightEyeCamBitmap != null) {
//                binding.ivRightCamera.setImageBitmap(Constants.rightEyeCamBitmap)
////                binding.ivRightCamera.visibility = View.VISIBLE
////                binding.tvRightEyeCameraSuccess.visibility = View.VISIBLE
//            } else {
////                binding.ivRightCamera.visibility = View.GONE
////                binding.tvRightEyeCameraSuccess.visibility = View.GONE
//            }
//            if (Constants.leftEyeGalleyBitmap != null) {
//                binding.ivLeftGallery.setImageBitmap(Constants.leftEyeGalleyBitmap)
////                binding.ivLeftGallery.visibility = View.VISIBLE
////                binding.tvLeftEyeGallerySuccess.visibility = View.VISIBLE
//            } else {
////                binding.ivLeftGallery.visibility = View.GONE
////                binding.tvLeftEyeGallerySuccess.visibility = View.GONE
//            }
//            if (Constants.rightEyeGalleryBitmap != null) {
//                binding.ivRightGallery.setImageBitmap(Constants.rightEyeGalleryBitmap)
////                binding.ivRightGallery.visibility = View.VISIBLE
////                binding.tvRightEyeGallerySuccess.visibility = View.VISIBLE
//            } else {
////                binding.ivRightGallery.visibility = View.GONE
////                binding.tvRightEyeGallerySuccess.visibility = View.GONE
//            }
//            //  finalBitmap?.let { handleImageVisibility(it) }

        }
    }
}