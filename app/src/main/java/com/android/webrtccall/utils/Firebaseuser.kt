package com.sporty.app.helper

import com.google.firebase.auth.FirebaseAuth

class Firebaseuser {
    companion object {
        fun getUserId(): String {
            return FirebaseAuth.getInstance().currentUser?.uid!!
        }
    }
}