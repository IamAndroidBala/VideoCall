package com.sporty.app.helper

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FirebaseManager {

    companion object {

        val firebaseDB = FirebaseDatabase.getInstance()

        fun getFirebaseAuth() : FirebaseAuth {
            return FirebaseAuth.getInstance()
        }

        const val OFFERS_PATH  = "offers/"
        const val ANSWERS_PATH = "answers/"

        const val imageUrl    = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRmX4ruwxjF_DgR4KwfDvf6rQ2PvMkWq-Y0cpclxvr10964_NEAVA"

    }

}