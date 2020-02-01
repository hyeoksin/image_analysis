package com.khs.imagerekognation01.api

import com.google.api.services.vision.v1.model.AnnotateImageResponse

class ConverseResponse(
    requestType:String,
    response: AnnotateImageResponse
){
    var converseInterface:ConverseResponseInterface? = null

    interface ConverseResponseInterface{
        fun converseWeb():String
        fun converseLogo():String
        fun converseFace():String
        fun converseLiteral():String
        fun converseLabel():String
        fun converseLandmark():String
    }

    fun addConverseResponseInterface(listener:ConverseResponseInterface){
        converseInterface = listener
    }
}