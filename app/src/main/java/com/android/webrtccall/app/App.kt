package com.android.webrtccall.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp
import com.android.webrtccall.BuildConfig
import com.android.webrtccall.R
import com.android.webrtccall.data.firebase.FirebaseModule
import com.android.webrtccall.utils.VdoCallSP
import com.android.webrtccall.webrtchelper.disableWebRtcLogs
import com.android.webrtccall.webrtchelper.enableInternalWebRtclogs
import com.android.webrtccall.webrtchelper.enableWebRtcLogs
import com.sporty.app.helper.Firebaseuser
import org.webrtc.Logging
import timber.log.Timber

class App : Application() {

    companion object Factory {

        val BACKGROUND_WORK_NOTIFICATIONS_CHANNEL_ID = "background_channel"

        var CURRENT_DEVICE_UUID         : String = ""
        var CURRENT_PARTNER_DEVICE_UUID : String = ""

        var answerSessonDescription     : Any? = null

        fun get(context: Context): App = context.applicationContext as App

        fun getApplicationComponent(context: Context): ApplicationComponent = (context.applicationContext as App).applicationComponent

        lateinit var vdoCallSP : VdoCallSP
            private set

    }

    val applicationComponent: ApplicationComponent by lazy {
        DaggerApplicationComponent.builder()
                .applicationModule(ApplicationModule(this))
                .firebaseModule(FirebaseModule())
                .build()
    }

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(applicationContext)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            enableWebRtcLogs(true)
            enableInternalWebRtclogs(Logging.Severity.LS_INFO)
        } else {
            disableWebRtcLogs()
        }

        createNotificationChannels()

        vdoCallSP = VdoCallSP(this)

        if(vdoCallSP.loggedIn) {
            getCurrentUserId()
        }

    }

    fun getCurrentUserId() : String {
        CURRENT_DEVICE_UUID = Firebaseuser.getUserId()
        AppLog.d("UUUID " + CURRENT_DEVICE_UUID)
        return CURRENT_DEVICE_UUID
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(BACKGROUND_WORK_NOTIFICATIONS_CHANNEL_ID, getString(R.string.background_work_notifications_channel),
                    NotificationManager.IMPORTANCE_HIGH).apply {
                        description = getString(R.string.background_work_notification_channel_description)
                    }
            notificationManager.createNotificationChannel(channel)
        }
    }
}