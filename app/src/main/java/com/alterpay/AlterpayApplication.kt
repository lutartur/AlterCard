package com.alterpay

import android.app.Application

class AlterpayApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { CardRepository(database.cardDao()) }
}
