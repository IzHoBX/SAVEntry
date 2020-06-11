package com.izho.saveentry.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.izho.saveentry.data.Location
import com.izho.saveentry.data.getAppDatabase
import kotlinx.coroutines.launch

class FavoritesViewModel(app: Application) : AndroidViewModel(app) {
    private val database = getAppDatabase(app.applicationContext)
    val favoriteLocations = database.dao.getAllFavoriteLocations()

    fun removeFromFavorite(location: Location) {
        viewModelScope.launch {
            location.favorite = false
            database.dao.updateLocation(location)
        }
    }
}