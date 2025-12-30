package com.unfair.moment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unfair.moment.database.AppSelectionDatabase
import com.unfair.moment.database.AppSelectionRepository
import kotlinx.coroutines.launch

class ModeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppSelectionDatabase.getDatabase(application)
    private val repository = AppSelectionRepository(
        database.appSelectionDao(),
        database.savedModeDao(),
    )

    fun setMode(modeType: ModeType) {
        viewModelScope.launch {
            repository.setActiveMode(modeType.id)
        }
    }
}


