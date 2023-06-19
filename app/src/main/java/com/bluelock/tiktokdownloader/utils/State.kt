package com.bluelock.tiktokdownloader.utils

open class State {
    data class COMPLETE(val path: String = "") : State()
    object FAILED : State()
}