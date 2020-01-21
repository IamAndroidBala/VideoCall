package com.android.webrtccall.activity.main.video

import com.android.webrtccall.common.di.FragmentScope
import dagger.Subcomponent

@FragmentScope
@Subcomponent
interface VideoFragmentComponent {
    fun inject(videoFragment: VideoCallFragment)

    fun videoFragmentPresenter(): VideoFragmentPresenter
}