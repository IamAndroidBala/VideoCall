package com.android.webrtccall.data.firebase

import com.android.webrtccall.app.App
import com.android.webrtccall.common.extension.ChildEvent
import com.android.webrtccall.common.extension.ChildEventAdded
import com.android.webrtccall.common.extension.ChildEventRemoved
import com.android.webrtccall.common.extension.rxChildEvents
import com.android.webrtccall.data.model.IceCandidateFirebase
import com.google.firebase.database.*
import io.reactivex.Completable
import io.reactivex.Flowable
import org.webrtc.IceCandidate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseIceCandidates @Inject constructor(private val firebaseDatabase: FirebaseDatabase) {

    companion object {
        private const val ICE_CANDIDATES_PATH = "ice_candidates/"
    }

    private fun deviceIceCandidatesPath(uuid: String) = ICE_CANDIDATES_PATH.plus(uuid)

    fun send(iceCandidate: IceCandidate): Completable = Completable.create {
        val reference = firebaseDatabase.getReference(deviceIceCandidatesPath(App.CURRENT_DEVICE_UUID))
        with(reference) {
            onDisconnect().removeValue()
            push().setValue(IceCandidateFirebase.createFromIceCandidate(iceCandidate))
        }
        it.onComplete()
    }

    fun remove(iceCandidatesToRemove: Array<IceCandidate>): Completable = Completable.create {
        val iceCandidatesToRemoveList = iceCandidatesToRemove
                .map { IceCandidateFirebase.createFromIceCandidate(it) }
                .toMutableList()
        val reference = firebaseDatabase.getReference(deviceIceCandidatesPath(App.CURRENT_DEVICE_UUID))

        reference.runTransaction(object : Transaction.Handler {

            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val typeIndicator = object : GenericTypeIndicator<MutableMap<String, IceCandidateFirebase>>() {}
                val currentIceCandidatesInFirebaseMap = mutableData.getValue(typeIndicator) ?:
                        return Transaction.success(mutableData)


                for ((key, value) in currentIceCandidatesInFirebaseMap) {
                    if (iceCandidatesToRemoveList.remove(value)) {
                        currentIceCandidatesInFirebaseMap.remove(key)
                    }
                }
                mutableData.value = currentIceCandidatesInFirebaseMap
                return Transaction.success(mutableData)
            }

//            override fun onComplete(@Nullable databaseError: DatabaseError, committed: Boolean, @Nullable p2: DataSnapshot?) {
//                if (committed) {
//                    it.onComplete()
//                } else {
//                    it.onError(databaseError.toException())
//                }
//            }

            override fun onComplete(p0: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {
                if (p1) {
                    it.onComplete()
                } else {
                    it.onError(p0!!.toException())
                }
            }

        })
    }

    fun get(remoteUuid: String): Flowable<ChildEvent<IceCandidate>> {
        return firebaseDatabase.getReference(deviceIceCandidatesPath(remoteUuid)).rxChildEvents()
                .filter { it is ChildEventAdded || it is ChildEventRemoved }
                .map {
                    val iceCandidateFirebase: IceCandidateFirebase = it.data.getValue(IceCandidateFirebase::class.java) as IceCandidateFirebase
                    val iceCandidate = iceCandidateFirebase.toIceCandidate()
                    if (it is ChildEventAdded) {
                        ChildEventAdded(iceCandidate, it.previousChildName)
                    } else {
                        ChildEventRemoved(iceCandidate)
                    }
                }
    }

}
