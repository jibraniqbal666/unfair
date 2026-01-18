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
class ModeViewModel @Inject constructor(
    val repository: AppSelectionRepository,
) : ViewModel() {

    private val _allModes = MutableStateFlow<List<ModeType>>(emptyList())
    val allModes: StateFlow<List<ModeType>> = _allModes.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getSavedModeTypes().collect {
                _allModes.value = ModeType.DEFAULT_MODES + it
            }
        }
    }


    fun setMode(modeType: ModeType) {
        viewModelScope.launch {
            repository.setActiveMode(modeType.id)
        }
    }
}


