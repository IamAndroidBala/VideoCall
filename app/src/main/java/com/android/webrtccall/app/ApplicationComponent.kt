package com.android.webrtccall.app

import com.android.webrtccall.data.firebase.FirebaseModule
import com.android.webrtccall.activity.main.video.VideoFragmentComponent
import com.android.webrtccall.webrtchelper.service.WebRtcServiceComponent
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(ApplicationModule::class, FirebaseModule::class))
interface ApplicationComponent {

    fun videoFragmentComponent(): VideoFragmentComponent

    fun webRtcServiceComponent(): WebRtcServiceComponent

}
