package com.khs.imagerekognation01.api

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.AsyncTask
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.vision.v1.Vision
import com.google.api.services.vision.v1.VisionRequest
import com.google.api.services.vision.v1.VisionRequestInitializer
import com.google.api.services.vision.v1.model.*
import com.khs.imagerekognation01.MainActivity
import com.khs.imagerekognation01.model.RequestCode
import com.khs.imagerekognation01.util.PackageManagerUtil
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.lang.StringBuilder
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList

class DetectTask (
    private val packageName:String,
    private val packageManager:PackageManager,
    private val activity:MainActivity
){
    private val WEB_DETECTION ="WEB_DETECTION"
    private val LOGO_DETECTION ="LOGO_DETECTION"
    private val FACE_DETECTION ="FACE_DETECTION"
    private val LITERAL_DETECTION ="TEXT_DETECTION"
    private val LABEL_DETECTION ="LABEL_DETECTION"
    private val LANDMARK_DETECTION="LANDMARK_DETECTION"


    private val CLOUD_VISION_API_KEY = "AIzaSyBW2QK1qAux8brhcw6xG8cJuyz_GkYMTBw"
    private val ANDROID_PACKAGE_HEADER = "X-Android-Package"
    private val ANDROID_CERT_HEADER = "X-Android-Cert"
    private val MAX_LABEL_RESULTS = 10
    private var requestType: String? = null

    private var detectionInterface:DetectionInterface? = null
    private var converseResponse:ConverseResponse?=null

    interface DetectionInterface {
        fun printResult(result:String)
    }

    fun addDetectionInterface(listener:DetectionInterface){
        detectionInterface = listener
    }

    fun requestCloudVisionApi(
        bitmap:Bitmap,
        detectionInterface: DetectionInterface,
        requestType:String?
    ){
        this.requestType = requestType
        this.detectionInterface = detectionInterface
        val visionTask = ImageRequestTask(prepareImageRequestTask(bitmap))
        visionTask.execute()
    }

    private fun prepareImageRequestTask(bitmap: Bitmap): Vision.Images.Annotate {
        var httpTransport= AndroidHttp.newCompatibleTransport()
        var jsonFactory = GsonFactory.getDefaultInstance()

        val requestInitializer = object : VisionRequestInitializer(CLOUD_VISION_API_KEY) {
            override fun initializeVisionRequest(request: VisionRequest<*>?) {
                super.initializeVisionRequest(request)
                request?.requestHeaders?.set(ANDROID_PACKAGE_HEADER,packageName)
                val sig= PackageManagerUtil().getSignature(packageManager,packageName)
                request?.requestHeaders?.set(ANDROID_CERT_HEADER,sig)
            }
        }

        val builder = Vision.Builder(httpTransport,jsonFactory,null)
        builder.setVisionRequestInitializer(requestInitializer)
        val vision =builder.build()

        val batchAnnotationImagesRequest = BatchAnnotateImagesRequest()
        batchAnnotationImagesRequest.requests = object : ArrayList<AnnotateImageRequest>() {
            init{
                val annotateImageRequest = AnnotateImageRequest()
                val base64EncodedImage = Image()
                val byteArrayOutputStream = ByteArrayOutputStream()

                bitmap.compress(Bitmap.CompressFormat.JPEG,90,byteArrayOutputStream)
                val imageBytes = byteArrayOutputStream.toByteArray()

                base64EncodedImage.encodeContent(imageBytes)
                annotateImageRequest.image = base64EncodedImage

                annotateImageRequest.features = object : ArrayList<Feature>() {
                    init{
                        val detectTargetOptions = Feature()
                        when(requestType){
                            RequestCode().WEB_DETECTION_REQUEST         -> detectTargetOptions.type = WEB_DETECTION
                            RequestCode().LOGO_DETECTION_REQUEST        -> detectTargetOptions.type = LOGO_DETECTION
                            RequestCode().FACE_DETECTION_REQUEST        -> detectTargetOptions.type = FACE_DETECTION
                            RequestCode().LITERAL_DETECTION_REQUEST     -> detectTargetOptions.type = LITERAL_DETECTION
                            RequestCode().LABEL_DETECTION_REQUEST       -> detectTargetOptions.type = LABEL_DETECTION
                            RequestCode().LANDMARK_DETECTION_REQUEST    -> detectTargetOptions.type = LANDMARK_DETECTION
                        }
                        detectTargetOptions.maxResults = MAX_LABEL_RESULTS
                        add(detectTargetOptions)
                    }
                }
                add(annotateImageRequest)
            }
        }

        val annotateRequest = vision.images().annotate(batchAnnotationImagesRequest)
        annotateRequest.setDisableGZipContent(true)
        return annotateRequest
    }

    inner class ImageRequestTask constructor(
        val request:Vision.Images.Annotate
    ):AsyncTask<Any,Void,String>(){
        private val weakReference:WeakReference<MainActivity>

        init{
            weakReference = WeakReference(activity)
        }

        override fun onPostExecute(result: String?) {
            val activity = weakReference.get()
            if(activity!=null && !activity.isFinishing){
                result?.let{
                    detectionInterface?.printResult(it)
                }
            }
        }

        override fun doInBackground(vararg params: Any?): String {
           try{
               val response = request.execute()
               return findProperResponseType(response)
           }catch(e:Exception){
               e.printStackTrace()
           }
           return "REKOGNITION FAIL"
        }

    }

    private fun findProperResponseType(response: BatchAnnotateImagesResponse): String {
        converseResponse = ConverseResponse(requestType!!,response.responses[0]).apply {
            addConverseResponseInterface(object : ConverseResponse.ConverseResponseInterface {
                override fun converseWeb():String {
                    var webDetection:WebDetection = response.responses[0].webDetection
                    val message = StringBuilder("[ Rekognition Result ]\n")
                    webDetection?.webEntities?.forEach {
                        message.append(String.format(Locale.US,"%.3f: %s\n",it.score,it.description))
                    }

                    webDetection?.bestGuessLabels?.forEach {
                        message.append(String.format(Locale.US,"Best guess label: %s\n",it.label))
                    }

                    webDetection?.pagesWithMatchingImages?.forEach {
                        message.append(String.format(Locale.US,"%.3f: %s\n",it.score,it.url))
                    }
                    webDetection?.fullMatchingImages?.forEach {
                        message.append(String.format(Locale.US,"%.3f %s\n",it.score,it.url))
                    }

                    webDetection?.visuallySimilarImages?.forEach {
                        message.append(String.format(Locale.US,"%.3f %s\n",it.score,it.url))
                    }
                    return message.toString()
                }

                override fun converseLogo():String {
                    var logoDetection:List<EntityAnnotation> = response.responses[0].logoAnnotations
                    val message = StringBuilder("[ Rekognition Result ]\n")
                    logoDetection.forEach {
                        message.append(String.format(Locale.US,"%.3f: %s\n",it.score,it.description))
                    }
                    return message.toString()
                }

                override fun converseFace():String {
                    var faceDetection:List<FaceAnnotation> = response.responses[0].faceAnnotations
                    val message = StringBuilder("[ Rekognition Result ]\n")
                    faceDetection.forEach {
                        message.append(String.format(Locale.US,"anger: %s\nenjoy: %s\nsurprise: %s\n",
                            it.angerLikelihood,it.joyLikelihood,it.surpriseLikelihood))
                    }
                    return message.toString()

                }

                override fun converseLiteral():String {
                    var textDetection:List<EntityAnnotation> = response.responses[0].textAnnotations
                    val message = StringBuilder("[ Rekognition Result ]\n")
                    textDetection.forEach {
                        message.append(String.format(Locale.US,"Text: %s\n",it.description))
            //          message.append(String.format(Locale.US,"Position: %s\n",it.boundingPoly))
                    }
                    return message.toString()
                }

                override fun converseLabel():String {
                    var labelDetection:List<EntityAnnotation> = response.responses[0].labelAnnotations
                    val message = StringBuilder("[ Rekognition Result ]\n")
                    labelDetection.forEach {
                        message.append(String.format(Locale.US,"%.3f: %s\n",it.score,it.description))
                    }
                    return message.toString()
                }

                override fun converseLandmark():String {
                    var landmarkDetection:List<EntityAnnotation> = response.responses[0].landmarkAnnotations
                    val message = StringBuilder("[ Rekognition Result ]\n")
                    landmarkDetection.forEach {
                        message.append(String.format(Locale.US,"%.3f: %s\n",it.score,it.description))
                    }
                    return message.toString()
                }
            })
        }
        when(requestType){
            RequestCode().WEB_DETECTION_REQUEST          -> return converseResponse?.converseInterface!!.converseWeb()
            RequestCode().LOGO_DETECTION_REQUEST          -> return converseResponse?.converseInterface!!.converseLogo()
            RequestCode().FACE_DETECTION_REQUEST          -> return converseResponse?.converseInterface!!.converseFace()
            RequestCode().LITERAL_DETECTION_REQUEST       -> return converseResponse?.converseInterface!!.converseLiteral()
            RequestCode().LABEL_DETECTION_REQUEST         -> return converseResponse?.converseInterface!!.converseLabel()
            RequestCode().LANDMARK_DETECTION_REQUEST      -> return converseResponse?.converseInterface!!.converseLandmark()
        }
        return "CONVERSE RESULT FAIL"
    }

}