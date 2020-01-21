package com.android.webrtccall.app

import android.app.Application
import android.content.Context
import android.content.res.Resources
import com.android.webrtccall.data.firebase.FirebaseIceCandidates
import com.android.webrtccall.data.firebase.FirebaseIceServers
import com.android.webrtccall.data.firebase.FirebaseSignalingAnswers
import com.android.webrtccall.data.firebase.FirebaseSignalingOffers
import com.android.webrtccall.webrtchelper.WebRtcClient
import com.android.webrtccall.webrtchelper.service.WebRtcServiceController
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApplicationModule(private val application: Application) {

    @Provides
    @Singleton
    fun provideContext(): Context = application.applicationContext

    @Provides
    @Singleton
    fun provideApplication() = application

    @Provides
    @Singleton
    fun provideResources(): Resources = application.resources

    @Provides
    fun provideWebRtcClient(context: Context) = WebRtcClient(context)

    @Provides
    fun provideWebRtcServiceController(webRtcClient: WebRtcClient, firebaseSignalingAnswers: FirebaseSignalingAnswers, firebaseSignalingOffers: FirebaseSignalingOffers,
                                       firebaseIceCandidates: FirebaseIceCandidates, firebaseIceServers: FirebaseIceServers): WebRtcServiceController {

        return WebRtcServiceController(webRtcClient, firebaseSignalingAnswers, firebaseSignalingOffers, firebaseIceCandidates, firebaseIceServers)

    }

}