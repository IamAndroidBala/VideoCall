package com.android.webrtccall.activity.base.service


abstract class BaseServiceController<T : ServiceFacade> : ServiceController<T> {
    private var serviceFacade: T? = null

    override fun attachService(service: T) {
        serviceFacade = service
    }

    override fun detachService() {
        serviceFacade = null
    }

    override fun getService() = serviceFacade
}