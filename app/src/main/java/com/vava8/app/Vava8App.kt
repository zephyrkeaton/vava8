package com.vava8.app

import android.app.Application
import com.vava8.app.data.api.ApiFactory
import com.vava8.app.data.api.PersistentCookieJar
import com.vava8.app.data.prefs.AppPreferences
import com.vava8.app.data.repository.Vava8Repository

class Vava8App : Application() {
    lateinit var repository: Vava8Repository
        private set
    lateinit var preferences: AppPreferences
        private set
    lateinit var cookieJar: PersistentCookieJar
        private set

    override fun onCreate() {
        super.onCreate()
        preferences = AppPreferences(this)
        cookieJar = PersistentCookieJar(this)
        val api = ApiFactory.create(cookieJar)
        repository = Vava8Repository(api, cookieJar, preferences)
        instance = this
    }

    companion object {
        lateinit var instance: Vava8App
            private set
    }
}
