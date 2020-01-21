package com.android.webrtccall.activity.main.video

import android.Manifest
import android.content.*
import android.content.Context.AUDIO_SERVICE
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.android.webrtccall.R
import com.android.webrtccall.activity.base.BaseMvpFragment
import com.android.webrtccall.activity.main.users.MakeCall
import com.android.webrtccall.activity.main.users.UserAdapter
import com.android.webrtccall.activity.main.users.UserModel
import com.android.webrtccall.app.App
import com.android.webrtccall.app.AppLog
import com.android.webrtccall.common.extension.areAllPermissionsGranted
import com.android.webrtccall.common.extension.startAppSettings
import com.android.webrtccall.utils.SwipeTouchListener
import com.android.webrtccall.webrtchelper.service.WebRtcService
import com.android.webrtccall.webrtchelper.service.WebRtcServiceListener
import com.sporty.app.helper.Firebaseuser
import kotlinx.android.synthetic.main.fragment_video.*
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.PeerConnection
import timber.log.Timber
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.Map
import kotlin.collections.all
import kotlin.collections.isNotEmpty
import kotlin.collections.set

class VideoCallFragment         : BaseMvpFragment<VideoFragmentView, VideoFragmentPresenter>(), MakeCall,VideoFragmentView, WebRtcServiceListener , Animation.AnimationListener{

    internal var  vibrator      : Vibrator? = null
    internal var appPlayer      : MediaPlayer? = null
    private var mRequestQue     : RequestQueue? = null
    var service                 : WebRtcService? = null

    private val URL             = "https://fcm.googleapis.com/fcm/send"

    lateinit var userAdapter    : UserAdapter
    var userList                = ArrayList<UserModel>()

    private lateinit var serviceConnection: ServiceConnection

    var isCancelled     = false
    var enableCamera    = false
    var isAnswwerred    = false

    companion object {

        val TAG: String   = VideoCallFragment::class.java.name
        fun newInstance() = VideoCallFragment()

        private const val KEY_IN_CHAT     = "key:in_chat"
        private val NECESSARY_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        private const val CONNECT_BUTTON_ANIMATION_DURATION_MS       = 500L
        private const val CHECK_PERMISSIONS_AND_CONNECT_REQUEST_CODE = 1

    }

    override fun audioCall(user: UserModel) {

    }

    override fun vdoCall(user: UserModel) {

        enableCamera = true
        App.CURRENT_PARTNER_DEVICE_UUID = user.user_id
        checkPermissionsAndConnect()
        sendCallNotification(user)

    }


    override fun getLayoutId() = R.layout.fragment_video

    override fun retrievePresenter() = App.getApplicationComponent(context!!).videoFragmentComponent().videoFragmentPresenter()

    override val remoteUuid
        get() = service?.getRemoteUuid()

    fun timer(check: Boolean) {
        if (check) {
            chronometer.base = SystemClock.elapsedRealtime()
            chronometer.start()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mRequestQue =  Volley.newRequestQueue(activity?.applicationContext)


        recycler_user.layoutManager = LinearLayoutManager(activity?.applicationContext)
        userAdapter                 = UserAdapter(userList, this)
        recycler_user.adapter       = userAdapter

        (buttonPanel.layoutParams as CoordinatorLayout.LayoutParams).behavior = MoveUpBehavior()
        (localVideoView.layoutParams as CoordinatorLayout.LayoutParams).behavior = MoveUpBehavior()


        if(activity?.intent?.extras!=null && activity?.intent?.extras?.getString("UUID")!=null){

            playMusic()
            rl_incoming_call.visibility = View.VISIBLE

            tv_user_name.text = if(activity?.intent?.extras?.getString("CallFrom")!=null) activity?.intent?.extras?.getString("CallFrom") else "User"

            pickCallButton.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.anim_call_pick_button))

            attachService()
            //callTo(App.CURRENT_DEVICE_UUID, App.CURRENT_PARTNER_DEVICE_UUID)

        }

        pickCallButton.setOnTouchListener(object : SwipeTouchListener(activity){

            override fun onSwipeTop() {}

            override fun onSwipeBottom() {}

            override fun onSwipeRight() {

                cancelRinging()
                val animation = AnimationUtils.loadAnimation(activity, R.anim.slide_right)
                animation.setAnimationListener(this@VideoCallFragment)
                pickCallButton.startAnimation(animation)

                enableCamera = true
                isAnswwerred = true
                if(activity?.intent?.extras?.getString("CallType").equals("audio")) setAudioCallSpeaker() else setVdoCallSpeaker()

                initAlreadyRunningConnection()
                getPresenter().startRoulette()

               // service?.sendAnswer()

            }

            override fun onSwipeLeft() {

                isCancelled = true
                cancelRinging()
                val animation = AnimationUtils.loadAnimation(activity, R.anim.slide_left)
                animation.setAnimationListener(this@VideoCallFragment)
                pickCallButton.startAnimation(animation)

                getPresenter().disconnectByUser()
                unbindService()

            }

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                return gestureDetector.onTouchEvent(event)
            }

        })

        if (savedInstanceState?.getBoolean(KEY_IN_CHAT) == true) {
            initAlreadyRunningConnection()
        }

        disconnectButton.setOnClickListener {
            getPresenter().disconnectByUser()
            unbindService()
        }

        switchCameraButton.setOnClickListener {
            service?.switchCamera()
        }

        cameraEnabledToggle.setOnCheckedChangeListener { _, enabled ->
            service?.enableCamera(enabled)
        }

        microphoneEnabledToggle.setOnCheckedChangeListener { _, enabled ->
            service?.enableMicrophone(enabled)
        }

    }

    private fun callTo(myId : String, recipientId : String) {

        val timer = object: CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                if(!isAnswwerred) {
                    cancelRinging()
                    getPresenter().disconnectAutomatically(myId,recipientId)
                    AppLog.d("heloosuiojhf ")
                }
            }
        }
        timer.start()

    }


    private fun setAudioCallSpeaker() {
//        activity?.volumeControlStream = AudioManager.STREAM_VOICE_CALL
//        service?.enableCamera(false)
//        enableCamera = false
//        attachCamera()
    }

    private fun setVdoCallSpeaker() {
        val audioManager  = activity?.applicationContext?.getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_CALL
        audioManager.isSpeakerphoneOn = true
    }

    private fun startCall() {
        pickCallButton.clearAnimation()
        checkPermissionsAndConnect()
        rl_incoming_call.visibility = View.GONE
    }

    override fun onAnimationEnd(anim: Animation?) {
        if(!isCancelled) {
            startCall()
        }else{
            isCancelled = false
            activity?.finish()
        }
    }

    override fun onAnimationRepeat(p0: Animation?) {

    }

    override fun onAnimationStart(p0: Animation?) {

    }

    fun playMusic() {

        try {

            if (appPlayer != null) {
                appPlayer?.stop()
                appPlayer?.release()
                appPlayer = null
            }

            val audioManager = activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            if (audioManager.getStreamVolume(AudioManager.STREAM_RING) != 0) {

                val des    = activity!!.applicationContext.assets.openFd("raw/" + "sample.mp3")
                appPlayer = MediaPlayer()
                appPlayer?.setDataSource(des.getFileDescriptor(), des.getStartOffset(), des.getLength())

                appPlayer?.setAudioStreamType(AudioManager.STREAM_RING)
                appPlayer?.setLooping(true)
                appPlayer?.prepare()
                appPlayer?.start()

            } else {

                vibrator    = activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                val pattern = longArrayOf(1000,1000)
                vibrator?.vibrate(pattern, 0)

            }

        } catch (e: Exception) {}

    }

    private fun cancelRinging() {

        if (appPlayer != null) {

            appPlayer?.stop()
            appPlayer?.release()
            appPlayer = null

        } else if(vibrator != null){

            vibrator?.cancel()

        }

    }

    private fun getData(){

        FirebaseDatabase.getInstance().getReference("User").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                for(user in p0.children){
                    if(user.getValue(UserModel::class.java)!=null && user.getValue(UserModel::class.java)!!.user_id!= Firebaseuser.getUserId()) {
                        if(!userList.contains(user.getValue(UserModel::class.java)!!)) {
                            userList.add(user.getValue(UserModel::class.java)!!)
                        }
                    }
                }
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }

    override fun onResume() {
        super.onResume()
        userList.clear()
        getData()

        if(!App.vdoCallSP.loggedIn) {
            val user        = UserModel()

            user.user_name  = App.vdoCallSP.email!!.split("@")[0]
            user.user_email = App.vdoCallSP.email!!
            user.user_id    = Firebaseuser.getUserId()
            user.user_fcm   = App.vdoCallSP.fcm!!

            FirebaseDatabase.getInstance().getReference("User").child(user.user_id + "/").setValue(user)
                .addOnSuccessListener {
                    App.vdoCallSP.loggedIn = true
                }
        }

    }

    override fun onStart() {
        super.onStart()
        service?.hideBackgroundWorkWarning()
    }

    override fun onStop() {
        super.onStop()
        if (!activity!!.isChangingConfigurations) {
            service?.showBackgroundWorkWarning()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        service?.let {
            it.detachViews()
            unbindService()
        }
    }

    private fun stopTimer() {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) {
            if (chronometer.isCountDown) {
                chronometer.stop()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (remoteVideoView.visibility == View.VISIBLE) {
            outState.putBoolean(KEY_IN_CHAT, true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!activity!!.isChangingConfigurations) disconnect()
    }

    private fun checkPermissionsAndConnect() {
        if (context!!.areAllPermissionsGranted(*NECESSARY_PERMISSIONS)) {
            getPresenter().connect()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), CHECK_PERMISSIONS_AND_CONNECT_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) = when (requestCode) {
        CHECK_PERMISSIONS_AND_CONNECT_REQUEST_CODE -> {
            val grantResult = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (grantResult) {
                checkPermissionsAndConnect()
            } else {
                showNoPermissionsSnackbar()
            }
        }
        else -> {
            error("Unknown permission request code $requestCode")
        }
    }

    override fun criticalWebRTCServiceException(throwable: Throwable) {
        unbindService()
        showSnackbarMessage(R.string.error_web_rtc_error, BaseTransientBottomBar.LENGTH_LONG)
        Timber.e(throwable, "Critical WebRTC service error")
    }

    override fun connectionStateChange(iceConnectionState: PeerConnection.IceConnectionState) {
        getPresenter().connectionStateChange(iceConnectionState)
    }

    override fun connectTo(uuid: String) {
        if(!isAnswwerred) {
            service?.offerDevice(uuid)
        }
    }

    override fun disconnect() {
        service?.let {
            it.stopSelf()
            unbindService()
        }
    }

    private fun unbindService() {
        AppLog.d("VDOO unbind service ")
        service?.let {
            stopTimer()
            it.detachServiceActionsListener()
            it.detachViews()
            context!!.unbindService(serviceConnection)
            service = null
            AppLog.d("VDOO unbind done ")
        }
    }

    override fun showCamViews() {
        buttonPanel.visibility      = View.VISIBLE
        remoteVideoView.visibility  = View.VISIBLE
        localVideoView.visibility   = View.VISIBLE
        chronometer.visibility      = View.VISIBLE
        recycler_user.visibility    = View.GONE
        rl_incoming_call.visibility = View.GONE
    }

    override fun showStartRouletteView() {
        buttonPanel.visibility      = View.GONE
        remoteVideoView.visibility  = View.GONE
        localVideoView.visibility   = View.GONE
        rl_incoming_call.visibility = View.GONE
        chronometer.visibility      = View.GONE
        recycler_user.visibility    = View.VISIBLE
    }

    override fun hideConnectButtonWithAnimation() {
        pickCallButton.animate().scaleX(0f).scaleY(0f)
                .setInterpolator(OvershootInterpolator())
                .setDuration(CONNECT_BUTTON_ANIMATION_DURATION_MS)
                .withStartAction { pickCallButton.isClickable = false }
                .withEndAction {
                    pickCallButton.isClickable = true
                    pickCallButton.visibility = View.GONE
                    pickCallButton.scaleX = 1f
                    pickCallButton.scaleY = 1f
                }
                .start()
    }

    override fun showErrorWhileChoosingRandom() {
        showSnackbarMessage(R.string.error_choosing_random_partner, BaseTransientBottomBar.LENGTH_LONG)
    }

    override fun showNoOneAvailable() {
        showSnackbarMessage(R.string.msg_no_one_available, BaseTransientBottomBar.LENGTH_LONG)
    }

    override fun showLookingForPartnerMessage() {
        showSnackbarMessage(R.string.msg_looking_for_partner, BaseTransientBottomBar.LENGTH_SHORT)
    }

    override fun showOtherPartyFinished() {
        showSnackbarMessage(R.string.msg_other_party_finished, BaseTransientBottomBar.LENGTH_SHORT)
    }

    override fun showConnectedMsg() {
        showSnackbarMessage(R.string.msg_connected_to_other_party, BaseTransientBottomBar.LENGTH_LONG)
        timer(true)
    }

    override fun showWillTryToRestartMsg() {
        showSnackbarMessage(R.string.msg_will_try_to_restart_msg, BaseTransientBottomBar.LENGTH_LONG)
    }

    override fun attachService() {
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
                onWebRtcServiceConnected((iBinder as (WebRtcService.LocalBinder)).service)
                if(enableCamera) { getPresenter().startRoulette() }
            }
            override fun onServiceDisconnected(componentName: ComponentName) {
                onWebRtcServiceDisconnected()
            }
        }
        startAndBindWebRTCService(serviceConnection)
    }

    private fun initAlreadyRunningConnection() {
        showCamViews()
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
                onWebRtcServiceConnected((iBinder as (WebRtcService.LocalBinder)).service)
                getPresenter().listenForDisconnectOrders()
            }

            override fun onServiceDisconnected(componentName: ComponentName) {
                onWebRtcServiceDisconnected()
            }
        }
        startAndBindWebRTCService(serviceConnection)
    }

    private fun startAndBindWebRTCService(serviceConnection: ServiceConnection) {
        WebRtcService.startService(context!!)
        WebRtcService.bindService(context!!, serviceConnection)
    }

    private fun showNoPermissionsSnackbar() {
        view?.let {
            Snackbar.make(it,R.string.msg_permissions, Snackbar.LENGTH_LONG)
                    .setAction(R.string.action_settings) {
                        try {
                            context!!.startAppSettings()
                        } catch (e: ActivityNotFoundException) {
                            showSnackbarMessage(R.string.error_permissions_couldnt_start_settings, BaseTransientBottomBar.LENGTH_LONG)
                        }
                    }
                    .show()
        }
    }

    private fun onWebRtcServiceConnected(service: WebRtcService) {
        Timber.d("Service connected")
        this.service = service
        if (enableCamera) { attachCamera() }
        syncButtonsState(service)
        service.attachServiceActionsListener(webRtcServiceListener = this)
    }

    private fun attachCamera() {
        service?.attachLocalView(localVideoView)
        service?.attachRemoteView(remoteVideoView)
    }

    private fun syncButtonsState(service: WebRtcService) {
        cameraEnabledToggle.isChecked = service.isCameraEnabled()
        microphoneEnabledToggle.isChecked = service.isMicrophoneEnabled()
    }

    private fun onWebRtcServiceDisconnected() {
        Timber.d("Service disconnected")
    }

    override fun isMe() :Boolean{
        return enableCamera
    }

    private fun sendCallNotification(user: UserModel) {

        val json            = JSONObject()
        val notificationObj = JSONObject()

        try {

            val title = "${user.user_name} is calling ..."

            json.put("to",                      user.user_fcm)

            notificationObj.put("title",        title)
            notificationObj.put("body",     "Tap to respond.")
            notificationObj.put("callTo",       user.user_name )
            notificationObj.put("callFrom",     App.vdoCallSP.name)
            notificationObj.put("meetingId",    Firebaseuser.getUserId())
            notificationObj.put("callType", "video")

            json.put("data",                    notificationObj)
            json.put("priority",            "high")

            val request = object : JsonObjectRequest(
                Request.Method.POST, URL,
                json, com.android.volley.Response.Listener { Log.d("MUR", "onResponse: ") },
                com.android.volley.Response.ErrorListener { Log.d("MUR", "onError: ") }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val header = HashMap<String, String>()
                    header["content-type"]  = "application/json"
                    header["authorization"] = "key=AIzaSyCHFllmEXF1d1NKBi6URKPG1xXfNo2HwOo"
                    return header
                }
            }

            mRequestQue?.add(request)

        } catch (e: JSONException) { AppLog.d("jiepsddkkdi $e") }

    }


}