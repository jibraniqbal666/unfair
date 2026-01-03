package com.unfair.moment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unfair.moment.database.AppSelectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ModePreferencesViewModel @Inject constructor(
    val repository: AppSelectionRepository,
) : ViewModel() {

    private var _modeType = MutableStateFlow<ModeType?>(null)
    val modeType: StateFlow<ModeType?> = _modeType.asStateFlow()


    private val _dndEnabled = MutableStateFlow(false)
    val dndEnabled: StateFlow<Boolean> = _dndEnabled.asStateFlow()

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

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

    fun deleteMoment() {
        viewModelScope.launch {
            _modeType.value?.let { modeType ->
                repository.deleteModeType(modeType.id)
                _deleted.value = true
            }
        }
    }

}
