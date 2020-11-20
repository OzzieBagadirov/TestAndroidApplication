package com.adigium.androidrfb.rfb.test

object RequestType {
    const val EOF = -1
    const val SET_PIXEL_FORMAT = 0
    const val SET_ENCODINGS = 2
    const val FRAMEBUFFER_UPDATE_REQUEST = 3
    const val KEY_EVENT = 4
    const val POINTER_EVENT = 5
    const val CLIENT_CUT_TEXT = 6
}