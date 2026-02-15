package com.fahh

import android.app.Application
import com.fahh.utils.AdManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FahhApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AdManager.initialize(this)
    }
}
