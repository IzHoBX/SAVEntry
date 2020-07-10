package com.izho.saveentry.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.izho.saveentry.data.Visit
import com.izho.saveentry.data.getAppDatabase
import com.izho.saveentry.SAVEntryApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ActiveViewModel(app: Application) : PlacesRelatedViewModel(app) {
    val activeVisits = database.dao.getAllActiveVisitWithLocation()

    fun deleteActiveVisit(visit: Visit) {
        viewModelScope.launch {
            // Remove stored pass image file
            visit.passImagePath?.let {
                withContext(Dispatchers.IO) {
                    val app = getApplication<SAVEntryApplication>()
                    val file = File(app.applicationContext.filesDir, it)
                    if (file.exists()) {
                        file.delete()
                    }
                }
            }

            database.dao.deleteVisit(visit)
        }
    }
}