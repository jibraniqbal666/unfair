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

class ModePreferencesViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppSelectionDatabase.getDatabase(application)
    private val repository = AppSelectionRepository(
        database.appSelectionDao(),
        database.savedModeDao(),
        database.dndDao(),
        database.modeTypeDao(),
    )

    private var _modeType = MutableStateFlow<ModeType?>(null)
    val modeType: StateFlow<ModeType?> = _modeType.asStateFlow()


    private val _dndEnabled = MutableStateFlow(false)
    val dndEnabled: StateFlow<Boolean> = _dndEnabled.asStateFlow()

    fun setMode(modeTypeId: String) {
        viewModelScope.launch {
            _modeType.value = repository.getModeType(modeTypeId)
            _dndEnabled.value = repository.isDNDEnabled(modeTypeId)
        }
    }

    fun toggleDND() {
        viewModelScope.launch {
            _modeType.value?.let {
                _dndEnabled.value = repository.isDNDEnabled(it.id).not()
                repository.toggleDND(it.id)
            }
        }
    }

}
