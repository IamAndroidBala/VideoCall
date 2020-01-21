package com.android.webrtccall.data.firebase

import com.android.webrtccall.app.App
import com.android.webrtccall.common.extension.ChildEventAdded
import com.android.webrtccall.common.extension.rxChildEvents
import com.android.webrtccall.data.model.RouletteDisconnectOrderFirebase
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.rxkotlin.ofType
import javax.inject.Inject

class FirebaseSignalingDisconnect @Inject constructor(private val firebaseDatabase: FirebaseDatabase) {

    companion object {
        private const val DISCONNECT_PATH = "should_disconnect/"
    }

    private fun deviceDisconnectPath(deviceUuid: String) = DISCONNECT_PATH.plus(deviceUuid)

    fun sendDisconnectOrderToOtherParty(recipientUuid: String): Completable = Completable.create {
        val reference = firebaseDatabase.getReference(deviceDisconnectPath(recipientUuid))
        reference.setValue(RouletteDisconnectOrderFirebase(App.vdoCallSP.userId!!)) {
            databaseError, _ ->
            if (databaseError != null) {
                it.onError(databaseError.toException())
            } else {
                it.onComplete()
            }
        }
    }

    fun sendDisconnectOrderToMyself(myId : String , recipientId : String): Completable = Completable.create {
        val reference = firebaseDatabase.getReference(deviceDisconnectPath(myId))
        reference.setValue(RouletteDisconnectOrderFirebase(recipientId)) {
                databaseError, _ ->
            if (databaseError != null) {
                it.onError(databaseError.toException())
            } else {
                it.onComplete()
            }
        }
    }

    fun listenForDisconnectOrders(): Flowable<ChildEventAdded<String>> {
        return firebaseDatabase.getReference(deviceDisconnectPath(App.CURRENT_DEVICE_UUID))
                .rxChildEvents()
                .ofType<ChildEventAdded<String>>()
    }

    fun cleanDisconnectOrders(): Completable = Completable.create {
        val reference = firebaseDatabase.getReference(deviceDisconnectPath(App.CURRENT_DEVICE_UUID))
        reference.onDisconnect().removeValue()
        reference.removeValue { databaseError, _ ->
            if (databaseError != null) {
                it.onError(databaseError.toException())
            } else {
                it.onComplete()
            }
        }
    }

}