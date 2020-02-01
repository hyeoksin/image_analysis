package com.khs.imagerekognation01

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.media.audiofx.EnvironmentalReverb
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.FileProvider
import androidx.core.view.accessibility.AccessibilityEventCompat.setAction
import com.khs.imagerekognation01.api.DetectTask
import com.khs.imagerekognation01.model.RequestCode
import com.khs.imagerekognation01.util.PermissionUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_rekognation.*
import java.io.File

/**
 * 1) create View
 * 2) create ChooseMenuActivity
 * 3) check permission
 * */
class MainActivity : AppCompatActivity() {

    var detectTask:DetectTask?=null
    var chooseMenuActivity:ChooseMenuActivity? =null
    var chooseOptionActivity:ChooseOptionActivity?=null

    var FILE_NAME = "target_Image"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        detectTask = DetectTask(
            packageName = packageName,
            packageManager =  packageManager,
            activity = this
        )
        setOnClickListener()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            when(requestCode) {
                RequestCode().REQUEST_CODE_CAMERA ->{
                    var imageUri = FileProvider.getUriForFile(
                        this,
                        applicationContext.packageName+".provider",
                        createImageFile()
                    )
                    uploadImage(imageUri)
                }

                RequestCode().REQUEST_CODE_GALLERY->{
                    data?.let{
                        uploadImage(it.data)
                    }
                }
            }
        }
    }

    private fun uploadImage(imageUri: Uri?) {
        val bitmap:Bitmap = MediaStore.Images.Media.getBitmap(contentResolver,imageUri)
        chooseMenuActivity?.dismiss()

        chooseOptionActivity = ChooseOptionActivity().apply {
            addChooseOptionActionInterface(object : ChooseOptionActivity.ChooseOptionActionInterface {
                override fun chooseWeb() {
                    findViewById<ImageView>(R.id.target_ImageView).setImageBitmap(bitmap)
                    findViewById<TextView>(R.id.result_textView).setText("Wait! Image analysis in progress..")
                    requestCloudVisionApi(bitmap,RequestCode().WEB_DETECTION_REQUEST)
                }

                override fun chooseLogo() {
                    findViewById<ImageView>(R.id.target_ImageView).setImageBitmap(bitmap)
                    findViewById<TextView>(R.id.result_textView).setText("Wait! Image analysis in progress..")
                    requestCloudVisionApi(bitmap,RequestCode().LOGO_DETECTION_REQUEST)
                }

                override fun chooseFace() {
                    findViewById<ImageView>(R.id.target_ImageView).setImageBitmap(bitmap)
                    findViewById<TextView>(R.id.result_textView).setText("Wait! Image analysis in progress..")
                    requestCloudVisionApi(bitmap,RequestCode().FACE_DETECTION_REQUEST)
                }

                override fun chooseLiteral() {
                    findViewById<ImageView>(R.id.target_ImageView).setImageBitmap(bitmap)
                    findViewById<TextView>(R.id.result_textView).setText("Wait! Image analysis in progress..")
                    requestCloudVisionApi(bitmap,RequestCode().LITERAL_DETECTION_REQUEST)
                }

                override fun chooseLabel() {
                    findViewById<ImageView>(R.id.target_ImageView).setImageBitmap(bitmap)
                    findViewById<TextView>(R.id.result_textView).setText("Wait! Image analysis in progress..")
                    requestCloudVisionApi(bitmap,RequestCode().LABEL_DETECTION_REQUEST)
                }

                override fun chooseLandmark() {
                    findViewById<ImageView>(R.id.target_ImageView).setImageBitmap(bitmap)
                    findViewById<TextView>(R.id.result_textView).setText("Wait! Image analysis in progress..")
                    requestCloudVisionApi(bitmap,RequestCode().LANDMARK_DETECTION_REQUEST)
                }

                override fun chooseCancel() {
                    dismiss()
                }
            })
        }
        chooseOptionActivity?.show(supportFragmentManager,"")

    }

    private fun requestCloudVisionApi(bitmap: Bitmap, requestType: String) {
        detectTask?.requestCloudVisionApi(
            bitmap, object : DetectTask.DetectionInterface {
                override fun printResult(result: String) {
                    result_textView.text = result
                }
            },requestType)
    }

    private fun setOnClickListener() {
        button_uploadImage.setOnClickListener {
            chooseMenuActivity = ChooseMenuActivity().apply {
                addChooseMenuActionInterface(object : ChooseMenuActivity.ChooseMenuActionInterface {
                    override fun camera() {
                        checkCameraPermission()
                    }

                    override fun gallery() {
                        checkGalleryPermission()
                    }
                })
            }
            chooseMenuActivity?.show(supportFragmentManager,"")
        }
    }

    private fun checkGalleryPermission() {
        if(PermissionUtil().requestPermission(
                this,
                RequestCode().REQUEST_CODE_GALLERY,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )) openGallery()
    }

    private fun openGallery() {
 //       var intent=Intent().apply{
 //           setType("image/*")
  //          setAction(Intent.ACTION_GET_CONTENT)
 //       }
//       startActivityForResult(
//            Intent.createChooser(intent,"Select a photo"),
//            RequestCode().REQUEST_CODE_GALLERY
//        )

        startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).apply {
            setType("image/*")
        },RequestCode().REQUEST_CODE_GALLERY)
    }

    private fun checkCameraPermission() {
        if(PermissionUtil().requestPermission(
                this,
                RequestCode().REQUEST_CODE_CAMERA,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )) openCamera()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            RequestCode().REQUEST_CODE_CAMERA ->{
                if(PermissionUtil().requestGranted(
                        requestCode,
                        RequestCode().REQUEST_CODE_CAMERA,
                        grantResults
                    )) openCamera()
            }
            RequestCode().REQUEST_CODE_GALLERY->{
                if(PermissionUtil().requestGranted(
                        requestCode,
                        RequestCode().REQUEST_CODE_GALLERY,
                        grantResults
                    )) openGallery()
            }
        }
    }

    private fun openCamera() {
        var imageUri = FileProvider.getUriForFile(
            this,
            applicationContext.packageName+".provider",
            createImageFile()
        )

        startActivityForResult(
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply{
                Log.d("DEBUG","OnCamera")
                putExtra(MediaStore.EXTRA_OUTPUT,imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },RequestCode().REQUEST_CODE_CAMERA
        )
    }

    private fun createImageFile(): File {
        var dirPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(dirPath,FILE_NAME)
    }




}
