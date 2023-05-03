package com.nonoka.nhentai

import android.app.Application
import timber.log.Timber

class nHentaiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}