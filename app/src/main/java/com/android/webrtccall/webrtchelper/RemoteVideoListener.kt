package com.android.webrtccall.webrtchelper

import org.webrtc.VideoTrack


internal interface RemoteVideoListener {

    fun onAddRemoteVideoStream(remoteVideoTrack: VideoTrack)

    fun removeVideoStream()
}