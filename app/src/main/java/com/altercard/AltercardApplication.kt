package com.altercard

import android.app.Application

class AltercardApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { CardRepository(database.cardDao()) }
}
