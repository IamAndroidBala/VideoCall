package com.android.webrtccall.activity.main.video

import com.android.webrtccall.common.util.RxUtils
import com.android.webrtccall.data.firebase.FirebaseSignalingDisconnect
import com.android.webrtccall.data.firebase.FirebaseSignalingOnline
import com.android.webrtccall.activity.base.BasePresenter
import com.android.webrtccall.app.App
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.webrtc.PeerConnection
import timber.log.Timber
import javax.inject.Inject

class VideoFragmentPresenter @Inject constructor(private val firebaseSignalingOnline: FirebaseSignalingOnline, private val firebaseSignalingDisconnect: FirebaseSignalingDisconnect) : BasePresenter<VideoFragmentView>() {

    private val disposables = CompositeDisposable()
    private var disconnectOrdersSubscription: Disposable = Disposables.disposed()

    override fun detachView() {
        super.detachView()
        disposables.dispose()
        disconnectOrdersSubscription.dispose()
    }

    fun startRoulette() {

//        disposables += firebaseSignalingOnline.connect()
//            .andThen(firebaseSignalingDisconnect.cleanDisconnectOrders())
//                .doOnComplete { listenForDisconnectOrders() }
//                .andThen(firebaseSignalingOnline.setOnlineAndRetrieveRandomDevice())
//                .compose(RxUtils.applyMaybeIoSchedulers())
//                .subscribeBy(
//                        onSuccess = {
//                            Timber.d("Next $it")
//                            getView()?.showCamViews()
//                            getView()?.connectTo(it)
//                        },
//                        onError = {
//                            Timber.e(it, "Error while choosing random")
//                            getView()?.showErrorWhileChoosingRandom()
//                        },
//                        onComplete = {
//                            Timber.d("Done")
//                            getView()?.showCamViews()
//                            getView()?.connectTo(App.CURRENT_PARTNER_DEVICE_UUID)
//                            getView()?.showNoOneAvailable()
//                        }
//                )

//        disposables +=
//            firebaseSignalingDisconnect.cleanDisconnectOrders()
//            .doOnComplete { listenForDisconnectOrders() }
//            .andThen(firebaseSignalingOnline.setOnlineAndRetrieveRandomDevice())
//            .compose(RxUtils.applyMaybeIoSchedulers())
//            .subscribeBy(
//                onSuccess = {
//                    Timber.d("Next $it")
//                    getView()?.showCamViews()
//                    getView()?.connectTo(it)
//                },
//                onError = {
//                    Timber.e(it, "Error while choosing random")
//                    getView()?.showErrorWhileChoosingRandom()
//                },
//                onComplete = {
//                    Timber.d("Done")
//                    getView()?.showCamViews()
//                    getView()?.connectTo(App.CURRENT_PARTNER_DEVICE_UUID)
//                    getView()?.showNoOneAvailable()
//                }
//            )


        getView()?.showCamViews()

        if(getView()?.isMe()!!) {
            getView()?.connectTo(App.CURRENT_PARTNER_DEVICE_UUID)
        }

        getView()?.showNoOneAvailable()

    }

    fun listenForDisconnectOrders() {
        disconnectOrdersSubscription = firebaseSignalingDisconnect.cleanDisconnectOrders()
                .andThen(firebaseSignalingDisconnect.listenForDisconnectOrders())
                .compose(RxUtils.applyFlowableIoSchedulers())
                .subscribeBy(
                        onNext = {
                            Timber.d("Disconnect order")
                            getView()?.showOtherPartyFinished()
                            disconnect()
                        }
                )
    }

    private fun disconnect() {
        disposables += firebaseSignalingOnline.disconnect()
                .compose(RxUtils.applyCompletableIoSchedulers())
                .subscribeBy(
                        onError = {
                            Timber.d(it)
                        },
                        onComplete = {
                            disconnectOrdersSubscription.dispose()
                            getView()?.disconnect()
                            getView()?.showStartRouletteView()
                        }
                )

    }

    fun connect() = getView()?.run {
        attachService()
        showLookingForPartnerMessage()
    }

    fun disconnectByUser() {

        val remoteUuid  = getView()?.remoteUuid
        if (remoteUuid != null) {
            disposables += firebaseSignalingDisconnect.sendDisconnectOrderToOtherParty(App.CURRENT_PARTNER_DEVICE_UUID)
                    .compose(RxUtils.applyCompletableIoSchedulers())
                    .subscribeBy(
                            onComplete = {
                                //firebaseSignalingDisconnect.cleanDisconnectOrders()
                                disconnect()
                            }
                    )
        } else {
            disconnect()
        }

    }

    fun disconnectAutomatically(myId : String , recipientId : String) {

        if ( myId != null && recipientId != null ){
            disposables += firebaseSignalingDisconnect.sendDisconnectOrderToMyself(myId, recipientId)
                .compose(RxUtils.applyCompletableIoSchedulers())
                .subscribeBy(
                    onComplete = {
                        disconnect()
                    }
                )
        } else {
            disconnect()
        }

    }


    fun connectionStateChange(iceConnectionState: PeerConnection.IceConnectionState) {
        Timber.d("Ice connection state changed: $iceConnectionState")
        when (iceConnectionState) {
            PeerConnection.IceConnectionState.CONNECTED -> {
                getView()?.showConnectedMsg()
            }
            PeerConnection.IceConnectionState.DISCONNECTED -> {
                getView()?.showWillTryToRestartMsg()
            }
            else -> {
                //no-op for now - could show or hide progress bars or messages on given event
            }
        }
    }

}