package com.android.webrtccall.data.model

import com.android.webrtccall.app.App
import org.webrtc.SessionDescription


data class AnswerSessionDescription(val senderUuid: String = App.CURRENT_DEVICE_UUID,
                                    val callType     : String = "video",
                                    val responseAt   : Long   = System.currentTimeMillis(),
                                    val type: SessionDescription.Type? = null,
                                    val description: String? = null) {

    companion object {
        fun fromSessionDescriptionWithDefaultSenderUuid(senderUuid: String,  callType: String, responseAt: Long, sessionDescription: SessionDescription): AnswerSessionDescription = AnswerSessionDescription(senderUuid,callType, responseAt,type = sessionDescription.type, description = sessionDescription.description)
    }

    fun toSessionDescription() = SessionDescription(type, description)

}