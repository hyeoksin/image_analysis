package com.khs.imagerekognation01

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.activity_choosemenu.*

class ChooseMenuActivity : BottomSheetDialogFragment(){

    var chooseMenuActionInterface:ChooseMenuActionInterface?=null

    interface ChooseMenuActionInterface{
        fun camera()
        fun gallery()
    }

    fun addChooseMenuActionInterface(listener:ChooseMenuActionInterface){
        chooseMenuActionInterface = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_choosemenu,container,false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUpBtnClickListener()
    }

    private fun setUpBtnClickListener() {
        button_camera.setOnClickListener {
            chooseMenuActionInterface?.camera()
        }

        button_gallery.setOnClickListener {
            chooseMenuActionInterface?.gallery()
        }
    }
}