package com.android.webrtccall.activity.main

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import com.android.webrtccall.activity.base.BaseActivity
import com.android.webrtccall.activity.main.video.VideoCallFragment


class MainActivity : BaseActivity() {

    private val videoFragment = VideoCallFragment.newInstance()

    override fun getLayoutId() = com.android.webrtccall.R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            getReplaceFragmentTransaction(com.android.webrtccall.R.id.fragmentContainer, videoFragment, VideoCallFragment.TAG).commit()
        }

        if(intent?.extras?.getString("UUID")!=null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
                val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                keyguardManager.requestDismissKeyguard(this, null)
            } else {
                this.window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            }

        }

    }

}