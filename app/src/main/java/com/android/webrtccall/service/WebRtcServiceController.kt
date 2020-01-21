package com.android.webrtccall.webrtchelper.service

import android.os.Handler
import android.os.Looper
import com.android.webrtccall.common.extension.ChildEventAdded
import com.android.webrtccall.common.util.RxUtils
import com.android.webrtccall.data.firebase.FirebaseIceCandidates
import com.android.webrtccall.data.firebase.FirebaseIceServers
import com.android.webrtccall.data.firebase.FirebaseSignalingAnswers
import com.android.webrtccall.data.firebase.FirebaseSignalingOffers
import com.android.webrtccall.activity.base.service.BaseServiceController
import com.android.webrtccall.app.AppLog
import com.android.webrtccall.webrtchelper.PeerConnectionListener
import com.android.webrtccall.webrtchelper.WebRtcAnsweringPartyListener
import com.android.webrtccall.webrtchelper.WebRtcClient
import com.android.webrtccall.webrtchelper.WebRtcOfferingActionListener
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import timber.log.Timber
import javax.inject.Inject

class WebRtcServiceController @Inject constructor(
        private val webRtcClient             : WebRtcClient,
        private val firebaseSignalingAnswers : FirebaseSignalingAnswers,
        private val firebaseSignalingOffers  : FirebaseSignalingOffers,
        private val firebaseIceCandidates    : FirebaseIceCandidates,
        private val firebaseIceServers       : FirebaseIceServers) : BaseServiceController<WebRtcServiceFacade>() {

    var serviceListener     : WebRtcServiceListener? = null
    var remoteUuid          : String? = null
    val mainThreadHandler   = Handler(Looper.getMainLooper())
    private val disposables = CompositeDisposable()

    private var isOfferingParty = false
    private var shouldCreateOffer = false
    private var finishedInitializing = false

    override fun attachService(service: WebRtcServiceFacade) {
        super.attachService(service)
        AppLog.d("VDOO controller ")
        loadIceServers()
    }

    override fun detachService() {
        super.detachService()
        disposables.dispose()
        webRtcClient.detachViews()
        webRtcClient.dispose()
        AppLog.d("VDOO controller destroy vieww ")
    }

    fun offerDevice(deviceUuid: String) {
        isOfferingParty = true
        this.remoteUuid = deviceUuid
        listenForIceCandidates(deviceUuid)
        if (finishedInitializing) webRtcClient.createOffer() else shouldCreateOffer = true
    }

    fun attachRemoteView(remoteView: SurfaceViewRenderer) {
        webRtcClient.attachRemoteView(remoteView)
    }

    fun attachLocalView(localView: SurfaceViewRenderer) {
        webRtcClient.attachLocalView(localView)
    }

    fun detachViews() {
        webRtcClient.detachViews()
    }

    fun switchCamera() = webRtcClient.switchCamera()

    fun enableCamera(isEnabled: Boolean) {
        webRtcClient.cameraEnabled = isEnabled
    }

    fun isCameraEnabled() = webRtcClient.cameraEnabled

    fun enableMicrophone(enabled: Boolean) {
        webRtcClient.microphoneEnabled = enabled
    }

    fun isMicrophoneEnabled() = webRtcClient.microphoneEnabled

    private fun loadIceServers() {
        disposables += firebaseIceServers.getIceServers()
                .subscribeBy(
                        onSuccess = {
                            AppLog.d("VDOO controller ${it.get(0)} ")
                            listenForOffers()
                            initializeWebRtc(it)
                        },
                        onError = {
                            handleCriticalException(it)
                        }
                )
    }

    private fun initializeWebRtc(iceServers: List<PeerConnection.IceServer>) {

        webRtcClient.initializePeerConnection(iceServers,
                peerConnectionListener       = object : PeerConnectionListener {
                    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
                        if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED && isOfferingParty) {
                            webRtcClient.restart()
                        }
                        mainThreadHandler.post {
                            serviceListener?.connectionStateChange(iceConnectionState)
                        }

                    }

                    override fun onIceCandidate(iceCandidate: IceCandidate) {
                        sendIceCandidate(iceCandidate)
                    }

                    override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {
                        removeIceCandidates(iceCandidates)
                    }

                },
                webRtcOfferingActionListener = object : WebRtcOfferingActionListener {
                    override fun onError(error: String) {
                        Timber.e("Error in offering party: $error")
                    }

                    override fun onOfferRemoteDescription(localSessionDescription: SessionDescription) {
                        listenForAnswers()
                        sendOffer(localSessionDescription)
                    }

                },
                webRtcAnsweringPartyListener = object : WebRtcAnsweringPartyListener {
                    override fun onError(error: String) {
                        Timber.e("Error in answering party: $error")
                    }

                    override fun onSuccess(localSessionDescription: SessionDescription) {
                        sendAnswer(localSessionDescription)
                        AppLog.d("VDOO controller answer ")
//                        App.answerSessonDescription = localSessionDescription
                    }
                })

        if (shouldCreateOffer) webRtcClient.createOffer()
        finishedInitializing = true

    }

    private fun listenForIceCandidates(remoteUuid: String) {
        disposables += firebaseIceCandidates.get(remoteUuid)
                .compose(RxUtils.applyFlowableIoSchedulers())
                .subscribeBy(
                        onNext = {
                            if (it is ChildEventAdded) {
                                webRtcClient.addRemoteIceCandidate(it.data)
                            } else {
                                webRtcClient.removeRemoteIceCandidate(arrayOf(it.data))
                            }
                        },
                        onError = {
                            handleCriticalException(it)
                        }
                )
    }

    private fun sendIceCandidate(iceCandidate: IceCandidate) {
        disposables += firebaseIceCandidates.send(iceCandidate)
                .compose(RxUtils.applyCompletableIoSchedulers())
                .subscribeBy(
                        onComplete = {
                            Timber.d("Ice message sent")
                        },
                        onError = {
                            Timber.e(it, "Error while sending message")
                        }
                )
    }

    private fun removeIceCandidates(iceCandidates: Array<IceCandidate>) {
        disposables += firebaseIceCandidates.remove(iceCandidates)
                .compose(RxUtils.applyCompletableIoSchedulers())
                .subscribeBy(
                        onComplete = {
                            Timber.d("Ice candidates successfully removed")
                        },
                        onError = {
                            Timber.e(it, "Error while removing ice candidates")
                        }
                )
    }

    private fun sendOffer(localDescription: SessionDescription) {
        disposables += firebaseSignalingOffers.create(
                recipientUuid = remoteUuid ?: throw IllegalArgumentException("Remote uuid should be set first"),
                localSessionDescription = localDescription)
                .compose(RxUtils.applyCompletableIoSchedulers())
                .subscribeBy(
                        onComplete = {
                            Timber.d("description set")
                        },
                        onError = {
                            handleCriticalException(it)
                        }
                )
    }

    private fun listenForOffers() {
        disposables += firebaseSignalingOffers.listenForNewOffersWithUuid()
                .compose(RxUtils.applyFlowableIoSchedulers())
                .subscribeBy(
                        onNext = { (sessionDescription, remoteUuid) ->
                            this.remoteUuid = remoteUuid
                            listenForIceCandidates(remoteUuid)
                            webRtcClient.handleRemoteOffer(sessionDescription)
                        },
                        onError = {
                            handleCriticalException(it)
                        }
                )
    }

    fun sendAnswer(localDescription: SessionDescription) {
        disposables += firebaseSignalingAnswers.create(
                recipientUuid = remoteUuid ?: throw IllegalArgumentException("Remote uuid should be set first"),
                localSessionDescription = localDescription)
                .compose(RxUtils.applyCompletableIoSchedulers())
                .subscribeBy(
                        onError = {
                            handleCriticalException(it)
                        }
                )
    }

    private fun listenForAnswers() {
        disposables += firebaseSignalingAnswers.listenForNewAnswers()
                .compose(RxUtils.applyFlowableIoSchedulers())
                .subscribeBy(
                        onNext = {
                            Timber.d("Next answer $it")
                            webRtcClient.handleRemoteAnswer(it)
                        },
                        onError = {
                            handleCriticalException(it)
                        }
                )
    }

    private fun handleCriticalException(throwable: Throwable) {
        serviceListener?.criticalWebRTCServiceException(throwable)
        getService()?.stop()
        AppLog.d("kopsjdncmakdjfh " + throwable.localizedMessage)
    }

}
