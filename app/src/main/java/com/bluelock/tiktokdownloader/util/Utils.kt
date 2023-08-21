package com.bluelock.tiktokdownloader.util

import android.content.Context
import android.widget.Toast

class Utils(private val context: Context) {
    companion object {
        fun setToast(mContext: Context?, str: String?) {
            val toast = Toast.makeText(mContext, str, Toast.LENGTH_SHORT)
            toast.show()
        }
    }
}