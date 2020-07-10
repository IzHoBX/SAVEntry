package com.izho.saveentry.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.izho.saveentry.data.Location
import com.izho.saveentry.data.Visit
import com.izho.saveentry.data.getAppDatabase
import kotlinx.coroutines.launch

open class PlacesRelatedViewModel(app:Application) :AndroidViewModel(app) {
    protected val database = getAppDatabase(app.applicationContext)

    fun updateLocationUserDefinedName(location:Location, userDefinedName:String?) {
        viewModelScope.launch {
            location.userDefinedName = userDefinedName
            database.dao.updateLocation(location)
        }
    }
}

class HistoryViewModel(app: Application) : PlacesRelatedViewModel(app) {
    val visits = database.dao.getAllCompletedVisitWithLocation()

    fun deleteVisit(visit: Visit) {
        viewModelScope.launch {
            database.dao.deleteVisit(visit)
        }
    }

    fun addToFavorite(location: Location) {
        viewModelScope.launch {
            location.favorite = true
            database.dao.updateLocation(location)
        }
    }
}