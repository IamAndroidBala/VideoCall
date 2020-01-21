package com.android.webrtccall.utils

import android.app.Activity
import android.content.Context
import android.graphics.PorterDuff
import android.net.ConnectivityManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import com.kaopiz.kprogresshud.KProgressHUD
import com.android.webrtccall.R
import com.android.webrtccall.activity.main.users.UserModel
import java.util.regex.Matcher
import java.util.regex.Pattern

class CommonMethods {

    companion object {


        fun isValidMail(email: String): Boolean {
            val check: Boolean
            val p: Pattern
            val m: Matcher

            val EMAIL_STRING =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"

            p = Pattern.compile(EMAIL_STRING)

            m = p.matcher(email)
            check = m.matches()

            return check
        }

        fun showToast(string: String, context: Context) {
            if (!string.isNullOrBlank() && context != null) {
                //Toast.makeText(context, string, Toast.LENGTH_SHORT).show()

                val toast = Toast.makeText(context, string, Toast.LENGTH_SHORT)
                //toast.setGravity(Gravity.CENTER| Gravity.CENTER_HORIZONTAL, 0, 0);

                val view = toast.getView()
                view.getBackground().setColorFilter(
                    context.resources.getColor(R.color.primary),
                    PorterDuff.Mode.SRC_IN
                )

                val text = view.findViewById(android.R.id.message) as TextView
                text.setTextColor(context.resources.getColor(R.color.white))

                toast.show()

            }
        }

        fun isNetworkAvailable(context: Context): Boolean {
            try {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                return if (cm.activeNetworkInfo != null && cm.activeNetworkInfo.isFailover)
                    false
                else if (cm.activeNetworkInfo != null && cm.activeNetworkInfo.isAvailable && cm.activeNetworkInfo.isConnected)
                    true
                else
                    false
            } catch (e: Exception) {
            }

            return true
        }

        fun createHUD(activity: Activity): KProgressHUD {

            return KProgressHUD.create(activity)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show()
        }

        fun cancelHUD(kProgressHUD: KProgressHUD?) {
            if (kProgressHUD != null && kProgressHUD.isShowing) {
                kProgressHUD.dismiss()
            }
        }

        fun closeKeyboard(context: Context, view: View?) {
            if (view != null) {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        fun isValidPassword(password: String): Boolean {
            var isValid = false

            if (password.length >= 6) {
                isValid = true
            }

            return isValid
        }

    }

    fun getName(userModel : UserModel ) = if(userModel!=null) userModel.user_name else "User"

}