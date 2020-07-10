package com.izho.saveentry.viewmodel

import android.app.Application
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.izho.saveentry.data.Location
import com.izho.saveentry.data.getAppDatabase
import kotlinx.coroutines.launch

class FavoritesViewModel(app: Application) : PlacesRelatedViewModel(app) {
    val favoriteLocations = database.dao.getAllFavoriteLocations()

    fun removeFromFavorite(location: Location) {
        viewModelScope.launch {
            location.favorite = false
            database.dao.updateLocation(location)
        }
    }
}