package com.unfair.moment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unfair.moment.database.AppSelectionDatabase
import com.unfair.moment.database.AppSelectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddMomentViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppSelectionDatabase.getDatabase(application)
    private val repository = AppSelectionRepository(
        database.appSelectionDao(),
        database.savedModeDao(),
        database.dndDao(),
        database.modeTypeDao(),
    )

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    fun saveMoment(name: String, description: String) {
        viewModelScope.launch {
            val modeType = ModeType(
                id = "custom_${System.currentTimeMillis()}",
                name = name,
                description = description,
            )
            repository.saveModeType(modeType)
            _saved.value = true
        }
    }

}
