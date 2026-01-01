package com.unfair.moment

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unfair.moment.database.AppSelectionDatabase
import com.unfair.moment.database.AppSelectionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class UnfairViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppSelectionDatabase.getDatabase(application)
    private val repository = AppSelectionRepository(
        database.appSelectionDao(),
        database.savedModeDao(),
        database.dndDao(),
        database.modeTypeDao(),
    )

    private val _currentMode = MutableStateFlow<Mode?>(null)
    val currentMode: StateFlow<Mode?> = _currentMode.asStateFlow()

    init {
        launchMode()
    }

    fun launchMode() {
        viewModelScope.launch {
            // Load saved app selections for this mode
            repository.getActiveModeFlow().flowOn(Dispatchers.IO).collect {
                val modeType = it ?: return@collect
                val savedMode = repository.loadSavedMode(modeType.modeTypeId) ?: return@collect
                val selectedApps = savedMode.selectedApps

                // Update the selected apps with icons from package manager
                val appsWithIcons = selectedApps.map { appInfo ->
                    try {
                        val pm = getApplication<Application>().packageManager
                        val icon = pm.getApplicationIcon(appInfo.packageName)
                        AppInfo(
                            packageName = appInfo.packageName,
                            name = appInfo.name,
                            icon = icon,
                        )
                    } catch (e: Exception) {
                        appInfo
                    }
                }

                val dndActive = repository.isDNDEnabled(modeType.modeTypeId)

                _currentMode.value = Mode(
                    type = savedMode.type,
                    selectedApps = appsWithIcons,
                    isDNDActive = dndActive,
                )
            }
        }
    }

    fun launchApp(app: AppInfo, context: Context) {
        val pm = getApplication<Application>().packageManager
        val intent = pm.getLaunchIntentForPackage(app.packageName)
        context.startActivity(intent)
    }
}


