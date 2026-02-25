package com.altercard

import android.app.Application

class AltercardApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { CardRepository(database.cardDao()) }
    val driveAuthManager by lazy { DriveAuthManager(this) }

    fun buildSyncManager(): SyncManager? {
        val credential = driveAuthManager.getCredential() ?: return null
        val driveRepo = DriveRepository(credential)
        val sm = SyncManager(repository, driveRepo)
        repository.syncManager = sm
        return sm
    }

    override fun onCreate() {
        super.onCreate()
        buildSyncManager()
    }
}
