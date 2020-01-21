package com.android.webrtccall.data.firebase

import com.google.firebase.database.FirebaseDatabase
import com.android.webrtccall.app.App
import com.android.webrtccall.common.extension.rxValueEvents
import com.android.webrtccall.data.model.OfferSessionDescription
import com.sporty.app.helper.FirebaseManager
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.rxkotlin.toMaybe
import org.webrtc.SessionDescription
import javax.inject.Inject


class FirebaseSignalingOffers @Inject constructor(private val firebaseDatabase: FirebaseDatabase) {

    private fun deviceOffersPath(deviceUuid: String) = FirebaseManager.OFFERS_PATH.plus(deviceUuid)

    fun create(recipientUuid: String, localSessionDescription: SessionDescription): Completable = Completable.create {
        val reference = firebaseDatabase.getReference(deviceOffersPath(recipientUuid))
        reference.onDisconnect().removeValue()
        reference.setValue(OfferSessionDescription.fromSessionDescriptionWithDefaultSenderUuid(App.vdoCallSP.userId!!, App.vdoCallSP.name!!, FirebaseManager.imageUrl, "video", System.currentTimeMillis(), localSessionDescription))
        it.onComplete()
    }

    fun listenForNewOffersWithUuid(): Flowable<Pair<SessionDescription, String>> {
        return Single.just { firebaseDatabase.getReference(deviceOffersPath(App.CURRENT_DEVICE_UUID)) }
                .flatMapPublisher { it().rxValueEvents(OfferSessionDescription::class.java) }
                .flatMapMaybe { it.data.toMaybe() }
                .map { Pair(it.toSessionDescription(), it.senderUuid) }
    }

}