package com.android.webrtccall.webrtchelper.constraints

import org.webrtc.MediaConstraints

interface WebRtcConstraint<in T> {

    val constraintString: String

    fun toKeyValuePair(value: T) = MediaConstraints.KeyValuePair(constraintString, value.toString())
}