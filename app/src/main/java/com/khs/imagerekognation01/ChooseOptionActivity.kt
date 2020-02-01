package com.khs.imagerekognation01

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.activity_chooseoption.*


class ChooseOptionActivity: BottomSheetDialogFragment(){

    var chooseOptionActionInterface:ChooseOptionActionInterface? = null

    interface ChooseOptionActionInterface{
        fun chooseWeb()
        fun chooseLogo()
        fun chooseFace()
        fun chooseLiteral()
        fun chooseLabel()
        fun chooseLandmark()
        fun chooseCancel()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_chooseoption,container,false)
    }

    fun addChooseOptionActionInterface(listener:ChooseOptionActionInterface){
        chooseOptionActionInterface = listener
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setOnClickAction()
    }

    private fun setOnClickAction() {
        button_web.setOnClickListener {
            chooseOptionActionInterface?.chooseWeb()
            dismiss()
        }

        button_logo.setOnClickListener {
            chooseOptionActionInterface?.chooseLogo()
            dismiss()
        }
        button_face.setOnClickListener {
            chooseOptionActionInterface?.chooseFace()
            dismiss()
        }
        button_literal.setOnClickListener {
            chooseOptionActionInterface?.chooseLiteral()
            dismiss()
        }
        button_label.setOnClickListener {
            chooseOptionActionInterface?.chooseLabel()
            dismiss()
        }
        button_landmark.setOnClickListener {
            chooseOptionActionInterface?.chooseLandmark()
            dismiss()
        }
        button_cancel.setOnClickListener {
            chooseOptionActionInterface?.chooseCancel()
        }
    }

}