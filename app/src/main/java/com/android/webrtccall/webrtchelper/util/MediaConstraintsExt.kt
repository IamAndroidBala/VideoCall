package com.android.webrtccall.webrtchelper.util

import com.android.webrtccall.webrtchelper.constraints.WebRtcConstraints
import org.webrtc.MediaConstraints


internal fun MediaConstraints.addConstraints(constraints: WebRtcConstraints<*, *>) {
    mandatory.addAll(constraints.mandatoryKeyValuePairs)
    optional.addAll(constraints.optionalKeyValuePairs)
}

internal fun MediaConstraints.addConstraints(vararg constraints: WebRtcConstraints<*, *>) {
    constraints.forEach {
        addConstraints(it)
    }
}