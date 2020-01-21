package com.android.webrtccall.data.firebase

import android.content.Intent
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.android.webrtccall.activity.main.MainActivity
import com.android.webrtccall.app.AppLog

class FirebaseTokenService : FirebaseMessagingService() {

    override fun onNewToken(s: String?) {
        super.onNewToken(s)

        try {
            val refreshedToken = FirebaseInstanceId.getInstance().token
            AppLog.d("hdidhddhdiddhdii ${refreshedToken}")
        } catch (e: Exception) { }

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)

        if(remoteMessage!=null) {
            val callingTo   = remoteMessage.getData().get("callTo")
            val callingFrom = remoteMessage.getData().get("callFrom")
            val meetingId   = remoteMessage.getData().get("meetingId")
            val callType    = remoteMessage.getData().get("callType")

            Log.e(
                "FCMMMM",
                "onMessageReceived: called with from: $callingFrom CallingTo: $callingTo meeting id $meetingId"
            )

            val myNewActivity   = Intent(this, MainActivity::class.java)
            myNewActivity.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            myNewActivity.putExtra("UUID", meetingId)
            myNewActivity.putExtra("CallFrom", callingFrom )
            myNewActivity.putExtra("CallType", callType)
            startActivity(myNewActivity)

        }

    }

}