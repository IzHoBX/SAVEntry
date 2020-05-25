package com.lamkeewei.android.safeentrylogger.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lamkeewei.android.safeentrylogger.data.Visit
import com.lamkeewei.android.safeentrylogger.data.getAppDatabase
import com.lamkeewei.android.safeentrylogger.SaveEntryLoggerApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ActiveViewModel(app: Application) : AndroidViewModel(app) {
    private val database = getAppDatabase(app.applicationContext, resetDb = false)
    val activeVisits = database.dao.getAllActiveVisitWithLocation()

    fun deleteActiveVisit(visit: Visit) {
        viewModelScope.launch {
            // Remove stored pass image file
            visit.passImagePath?.let {
                withContext(Dispatchers.IO) {
                    val app = getApplication<SaveEntryLoggerApplication>()
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