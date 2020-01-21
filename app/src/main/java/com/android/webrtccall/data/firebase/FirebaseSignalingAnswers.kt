package com.android.webrtccall.data.firebase

import com.android.webrtccall.app.App
import com.android.webrtccall.common.extension.rxValueEvents
import com.android.webrtccall.data.model.AnswerSessionDescription
import com.google.firebase.database.FirebaseDatabase
import com.sporty.app.helper.FirebaseManager
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.rxkotlin.toMaybe
import org.webrtc.SessionDescription
import javax.inject.Inject

class FirebaseSignalingAnswers @Inject constructor(private val firebaseDatabase: FirebaseDatabase) {

    private fun deviceAnswersPath(deviceUuid: String) = FirebaseManager.ANSWERS_PATH.plus(deviceUuid)

    fun create(recipientUuid: String, localSessionDescription: SessionDescription): Completable = Completable.create {
        val reference = firebaseDatabase.getReference(deviceAnswersPath(recipientUuid))
        reference.onDisconnect().removeValue()
        reference.setValue(AnswerSessionDescription.fromSessionDescriptionWithDefaultSenderUuid(App.vdoCallSP.userId!!, "video", System.currentTimeMillis(), localSessionDescription))
        it.onComplete()
    }

    fun listenForNewAnswers(): Flowable<SessionDescription> {
        return Single.just { firebaseDatabase.getReference(deviceAnswersPath(App.CURRENT_DEVICE_UUID)) }
                .flatMapPublisher { it().rxValueEvents(AnswerSessionDescription::class.java) }
                .flatMapMaybe { it.data.toMaybe() }
                .map { it.toSessionDescription() }
    }

}