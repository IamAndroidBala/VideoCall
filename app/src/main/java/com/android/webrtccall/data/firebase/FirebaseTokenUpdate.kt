package com.android.webrtccall.data.firebase

import android.app.Activity
import com.sporty.app.helper.Firebaseuser
import com.google.firebase.iid.InstanceIdResult
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.iid.FirebaseInstanceId
import com.android.webrtccall.app.App


class FirebaseTokenUpdate {

    companion object {
        private const val TOKENS_PATH = "tokens/"
    }

    private fun deviceTokensPath() = TOKENS_PATH.plus(Firebaseuser.getUserId() + "/")

    fun add(activity: Activity) {
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(activity, OnSuccessListener<InstanceIdResult> { instanceIdResult ->
            val newToken = instanceIdResult.token
            App.vdoCallSP.fcm =  newToken
            //val ref = FirebaseModule().provideFirebaseDatabase().reference.child(deviceTokensPath()).child("token").setValue(newToken)
        })
    }

}