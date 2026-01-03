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
class AddMomentViewModel @Inject constructor(
    val repository: AppSelectionRepository,
) : ViewModel() {

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
