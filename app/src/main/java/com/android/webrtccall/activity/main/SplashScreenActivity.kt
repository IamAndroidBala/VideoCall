package com.android.webrtccall.activity.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.android.webrtccall.R
import com.android.webrtccall.app.App
import com.android.webrtccall.activity.base.BaseActivity

class SplashScreenActivity  : BaseActivity() {


    override fun getLayoutId(): Int {
        return R.layout.activity_splash
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Handler().postDelayed({
            if (App.vdoCallSP.loggedIn){
                startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
            }else {
                startActivity(Intent(this@SplashScreenActivity, LoginActivity::class.java))
            }
            finish()
        },1000)

    }

}