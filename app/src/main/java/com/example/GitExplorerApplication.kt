package com.example

import android.app.Application
import com.example.core.common.AppContainer
import com.example.core.common.AppContainerImpl

class GitExplorerApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this)
    }
}
