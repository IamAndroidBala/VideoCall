package com.android.webrtccall.activity.main.users

interface MakeCall {

    fun audioCall(user : UserModel)

    fun vdoCall(user: UserModel)

}