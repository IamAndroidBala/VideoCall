package com.android.webrtccall.webrtchelper.service

import com.android.webrtccall.common.di.ServiceScope
import dagger.Subcomponent

@ServiceScope
@Subcomponent
interface WebRtcServiceComponent {

    fun inject(webRtcService: WebRtcService)

}