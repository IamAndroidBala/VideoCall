package com.android.webrtccall.activity.main.video

import android.view.Gravity
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout


class MoveUpBehavior : CoordinatorLayout.Behavior<View>() {

    override fun onAttachedToLayoutParams(lp: CoordinatorLayout.LayoutParams) {
        if (lp.dodgeInsetEdges == Gravity.NO_GRAVITY) {
            lp.dodgeInsetEdges = Gravity.BOTTOM
        }
    }
}