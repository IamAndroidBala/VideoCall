package com.android.webrtccall.data.firebase

import com.google.firebase.database.*
import com.android.webrtccall.app.App
import com.android.webrtccall.data.model.RouletteConnectionFirebase
import io.reactivex.Completable
import io.reactivex.Maybe
import timber.log.Timber
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSignalingOnline @Inject constructor(private val firebaseDatabase: FirebaseDatabase) {

    companion object {
        private const val ONLINE_DEVICES_PATH = "call/"
    }

    private fun deviceOnlinePath(deviceUuid: String) = ONLINE_DEVICES_PATH.plus(deviceUuid)

    fun setOnlineAndRetrieveRandomDevice(): Maybe<String> = Completable.create {
        val firebaseOnlineReference = firebaseDatabase.getReference(deviceOnlinePath(App.CURRENT_DEVICE_UUID))
        with(firebaseOnlineReference) {
            onDisconnect().removeValue()
            setValue(RouletteConnectionFirebase())
        }
        it.onComplete()
    }.andThen(chooseRandomDevice())

    fun disconnect(): Completable = Completable.fromAction {
        firebaseDatabase.goOffline()
    }

    fun connect(): Completable = Completable.fromAction {
        firebaseDatabase.goOnline()
    }

    private fun chooseRandomDevice(): Maybe<String> = Maybe.create {
        var lastUuid: String? = null

        firebaseDatabase.getReference(ONLINE_DEVICES_PATH).child(App.CURRENT_PARTNER_DEVICE_UUID).runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                lastUuid = null
                val genericTypeIndicator = object : GenericTypeIndicator<MutableMap<String, RouletteConnectionFirebase>>() {}
                val availableDevices = mutableData.getValue(genericTypeIndicator) ?: return Transaction.success(mutableData)

                val removedSelfValue = availableDevices.remove(App.CURRENT_DEVICE_UUID)

                if (removedSelfValue != null && !availableDevices.isEmpty()) {
                    lastUuid = deleteRandomDevice(availableDevices)
                    mutableData.value = availableDevices
                }

                return Transaction.success(mutableData)
            }

            private fun deleteRandomDevice(availableDevices: MutableMap<String, RouletteConnectionFirebase>): String {
                val devicesCount = availableDevices.count()
                val randomDevicePosition = SecureRandom().nextInt(devicesCount)
                val randomDeviceToRemoveUuid = availableDevices.keys.toList()[randomDevicePosition]
                Timber.d("Device number $randomDevicePosition from $devicesCount devices was chosen.")
                availableDevices.remove(randomDeviceToRemoveUuid)
                return randomDeviceToRemoveUuid
            }

            override fun onComplete(databaseError: DatabaseError?, completed: Boolean, p2: DataSnapshot?) {
                if (databaseError != null) {
                    it.onError(databaseError.toException())
                } else if (completed && lastUuid != null) {
                    it.onSuccess(lastUuid as String)
                }
                it.onComplete()
            }
        })
    }

}