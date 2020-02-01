package com.khs.imagerekognation01.util

import android.content.pm.PackageManager
import android.content.pm.Signature
import com.google.common.io.BaseEncoding
import java.lang.Exception
import java.security.MessageDigest

class PackageManagerUtil {

    fun getSignature(pm: PackageManager,packageName:String):String?{
        try{
            val packageInfo = pm.getPackageInfo(packageName,PackageManager.GET_SIGNATURES)
            return if(packageInfo == null
                || packageInfo.signatures == null
                || packageInfo.signatures.size==0
                || packageInfo.signatures[0]==null){
                null
            }else{
                sigNatureDigest(packageInfo.signatures[0])
            }
        } catch (e:Exception){
            e.printStackTrace()
            return null
        }
    }

    private fun sigNatureDigest(signature: Signature?): String? {
        val sig=signature?.toByteArray()
        try{
            val md = MessageDigest.getInstance("SHA1")
            val digest = md.digest(sig)
            return BaseEncoding.base16().lowerCase().encode(digest)
        } catch (e:Exception){
            e.printStackTrace()
            return null
        }
    }
}