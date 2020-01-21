package com.android.webrtccall.activity.main.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.webrtccall.R
import kotlinx.android.synthetic.main.item_user.view.*
import java.util.*

class UserAdapter(val thisList : ArrayList<UserModel>, val makeCall: MakeCall) : androidx.recyclerview.widget.RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    override fun onBindViewHolder(itemView: ViewHolder, position: Int) {
        itemView.bindItems(thisList[position])
    }

    override fun getItemCount(): Int {
        return thisList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        fun bindItems(user: UserModel) {

            try {
                itemView.tv_user_name.text = user.user_name
                
                itemView.img_vdo_call.setOnClickListener { 
                    makeCall.vdoCall(user)
                }
            } catch (e: Exception) { }

        }
    }
}