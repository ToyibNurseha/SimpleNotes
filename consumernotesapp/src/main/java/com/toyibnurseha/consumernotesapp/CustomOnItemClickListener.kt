package com.toyibnurseha.consumernotesapp

import android.view.View

class CustomOnItemClickListener(private val position: Int, private val onItemClickCallback: OnItemCallback) : View.OnClickListener {
    override fun onClick(v: View?) {
        onItemClickCallback.onItemClicked(v, position)
    }

    interface OnItemCallback {
        fun onItemClicked(v: View?, position: Int)
    }

}