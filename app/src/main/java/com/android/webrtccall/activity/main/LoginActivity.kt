package com.android.webrtccall.activity.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.app.ActivityCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.kaopiz.kprogresshud.KProgressHUD
import com.android.webrtccall.R
import com.android.webrtccall.app.App
import com.android.webrtccall.data.firebase.FirebaseTokenUpdate
import com.android.webrtccall.activity.base.BaseActivity
import com.android.webrtccall.utils.CommonMethods
import com.sporty.app.helper.FirebaseManager
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : BaseActivity() , View.OnClickListener{

    lateinit var kProgressHUD : KProgressHUD
    val fbAuth = FirebaseManager.getFirebaseAuth()

    val CAMERA_AUDIO_PERMISSION = 126

    override fun getLayoutId() = R.layout.activity_login

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btn_login.setOnClickListener(this)

        ed_user_password.setOnEditorActionListener() { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                loginIntoAccount()
                true
            } else {
                false
            }
        }

        val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        ActivityCompat.requestPermissions(this, permissions,CAMERA_AUDIO_PERMISSION)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_AUDIO_PERMISSION -> {

                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                } else {

                }
                return
            }

            else -> {

            }

        }
    }


    override fun onClick(p0: View?) {
        when(p0?.id){
            com.android.webrtccall.R.id.btn_login -> {
                loginIntoAccount()
            }
        }
    }

    private fun loginIntoAccount () {

        CommonMethods.closeKeyboard(applicationContext, ed_user_password)

        if(!CommonMethods.isNetworkAvailable(applicationContext)){
            CommonMethods.showToast( resources.getString(com.android.webrtccall.R.string.no_net), this@LoginActivity)
            return
        }

        if(TextUtils.isEmpty(ed_user_email.text.toString())){
            CommonMethods.showToast( resources.getString(com.android.webrtccall.R.string.password_empty), this@LoginActivity)
            return
        }

        if(!CommonMethods.isValidPassword(ed_user_password.text.toString())){
            CommonMethods.showToast( resources.getString(com.android.webrtccall.R.string.invalid_password), this@LoginActivity)
            return
        }

        kProgressHUD = CommonMethods.createHUD(this@LoginActivity)

        loginWithEmailAndPassword(ed_user_email.text.toString().trim(), ed_user_password.text.toString().trim())

    }

    private fun loginWithEmailAndPassword(userName:String, password:String){

        try {
            fbAuth.signInWithEmailAndPassword(userName, password).addOnCompleteListener(this, OnCompleteListener<AuthResult> { task ->
                if (task.isSuccessful) {

                    if(fbAuth.currentUser == null){
                        return@OnCompleteListener
                    }

                    if(!fbAuth.currentUser!!.isEmailVerified) {
                        closeProgress()

                        App.vdoCallSP.email    = fbAuth.currentUser?.email
                        App.vdoCallSP.userId   = fbAuth.currentUser?.uid
                        App.vdoCallSP.name     = userName.split("@")[0]
                        App().getCurrentUserId()
                        FirebaseTokenUpdate().add(this@LoginActivity)

                        startActivity(Intent(this@LoginActivity , MainActivity::class.java))
                        finish()
                    }

                } else {
                    closeProgress()
                    CommonMethods.showToast("${task.exception?.message}", applicationContext)
                }
            })
        }catch (e:Exception){
            closeProgress()
        }
    }

    private fun closeProgress() {
        if(::kProgressHUD.isInitialized){
            CommonMethods.cancelHUD(kProgressHUD)
        }
    }

}