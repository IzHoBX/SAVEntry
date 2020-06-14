package com.izho.saveentry.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.izho.saveentry.data.getAppDatabase

class ChooseLocationForWidgetViewModel(app:Application) : AndroidViewModel(app) {
    private val database = getAppDatabase(app.applicationContext)
    val allLocations = database.dao.getAllLocations()
}