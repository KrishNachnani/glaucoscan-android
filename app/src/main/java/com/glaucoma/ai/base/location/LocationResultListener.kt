package com.glaucoma.ai.base.location

import android.location.Location

interface LocationResultListener {
    fun getLocation(location: Location)
}