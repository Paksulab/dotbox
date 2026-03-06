package com.dotbox.app

import android.app.Application
import com.dotbox.app.data.local.AppDatabase
import com.dotbox.app.data.repository.ToolsRepository

class DotBoxApplication : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val toolsRepository by lazy { ToolsRepository(database.favoriteDao(), this) }
}
