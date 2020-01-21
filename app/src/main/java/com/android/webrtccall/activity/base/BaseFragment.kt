package com.android.webrtccall.activity.base

import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.android.webrtccall.R
import com.android.webrtccall.common.extension.getColorCompat

abstract class BaseFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = inflater.inflate(getLayoutId(), container, false)

    @LayoutRes
    abstract fun getLayoutId(): Int

    fun showSnackbarMessage(@StringRes resId: Int, @BaseTransientBottomBar.Duration duration: Int) {
        view?.let {
            val snackbar = Snackbar.make(it, resId, duration)
            val layout = snackbar.view as Snackbar.SnackbarLayout
            layout.setBackgroundColor(context!!.getColorCompat(R.color.transparent_black))
            snackbar.show()
        }
    }
}