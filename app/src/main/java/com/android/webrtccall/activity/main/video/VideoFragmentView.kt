package com.android.webrtccall.activity.main.video

import com.android.webrtccall.activity.base.MvpView

interface VideoFragmentView : MvpView {

    val remoteUuid: String?

    fun connectTo(uuid: String)
    fun showCamViews()
    fun showStartRouletteView()
    fun disconnect()
    fun attachService()
    fun showErrorWhileChoosingRandom()
    fun showNoOneAvailable()
    fun showLookingForPartnerMessage()
    fun showOtherPartyFinished()
    fun showConnectedMsg()
    fun showWillTryToRestartMsg()
    fun hideConnectButtonWithAnimation()
    fun isMe() : Boolean

}