package com.android.webrtccall.webrtchelper

import org.webrtc.SdpObserver

internal interface SdpCreateObserver : SdpObserver {

    override fun onSetFailure(error: String) = throw IllegalStateException("onSetFailure called in set listener")

    override fun onSetSuccess() = throw IllegalStateException("onSetSuccess called in set listener")

}