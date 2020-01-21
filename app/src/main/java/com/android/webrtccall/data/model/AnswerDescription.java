package com.android.webrtccall.data.model;

import org.webrtc.SessionDescription;

public class AnswerDescription {

    private SessionDescription mSessionDescription;

    public AnswerDescription(SessionDescription sessionDescription) {
        this.mSessionDescription = sessionDescription;
    }

    public SessionDescription getmSessionDescription() {
        return mSessionDescription;
    }

    public void setmSessionDescription(SessionDescription mSessionDescription) {
        this.mSessionDescription = mSessionDescription;
    }

}
