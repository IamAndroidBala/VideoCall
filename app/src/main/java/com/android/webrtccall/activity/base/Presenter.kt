package com.android.webrtccall.activity.base


interface Presenter<T : MvpView> {

    fun attachView(mvpView: T)

    fun detachView()

    fun getView(): T?

}