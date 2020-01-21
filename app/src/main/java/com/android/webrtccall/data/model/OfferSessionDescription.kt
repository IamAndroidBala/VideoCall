package com.android.webrtccall.data.model

import com.android.webrtccall.app.App
import org.webrtc.SessionDescription


data class OfferSessionDescription(val senderUuid   : String = App.CURRENT_DEVICE_UUID,
                                   val senderName   : String = App.vdoCallSP.name!!,
                                   val senderImage  : String = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRmX4ruwxjF_DgR4KwfDvf6rQ2PvMkWq-Y0cpclxvr10964_NEAVA",
                                   val callType     : String = "video",
                                   val requestAt    : Long  = System.currentTimeMillis(),
                                   val type         : SessionDescription.Type? = null,
                                   val description  : String? = null) {

    companion object {
        fun fromSessionDescriptionWithDefaultSenderUuid(senderUuid: String,
                                                        senderName: String,
                                                        senderImage: String,
                                                        callType: String,
                                                        requestAt: Long,
                                                        sessionDescription: SessionDescription
                                                        ): OfferSessionDescription = OfferSessionDescription(
                                                                                                                senderUuid ,
                                                                                                                senderName,
                                                                                                                senderImage,
                                                                                                                callType,requestAt,
                                                                                                                type = sessionDescription.type,
                                                                                                                description = sessionDescription.description)
    }

    fun toSessionDescription() = SessionDescription(type, description)

}