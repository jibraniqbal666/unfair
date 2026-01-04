package com.unfair.moment.database

import com.unfair.moment.AppInfo
import com.unfair.moment.Mode
import com.unfair.moment.ModeType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppSelectionRepository(
    private val appSelectionDao: AppSelectionDao,
    private val savedModeDao: SavedModeDao,
    private val dndDao: DNDDao,
    private val modeTypeDao: ModeTypeDao,
) {

    suspend fun saveAppSelectionsForMode(modeTypeId: String, appInfos: List<AppInfo>) {
        val entities = appInfos.mapIndexed { index, appInfo ->
            AppSelectionEntity(
                modeTypeId = modeTypeId,
                packageName = appInfo.packageName,
                appName = appInfo.name,
                selectionOrder = index,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
        }
        appSelectionDao.saveAppSelectionsForMode(modeTypeId, entities)
    }

    suspend fun clearAppSelectionsForMode(modeTypeId: String) {
        appSelectionDao.clearAppSelectionsForMode(modeTypeId)
    }

    suspend fun setActiveMode(modeTypeId: String) {
        savedModeDao.setActiveMode(modeTypeId)
    }

    fun getActiveModeFlow(): Flow<SavedModeEntity?> {
        return savedModeDao.getActiveModeFlow()
    }

    suspend fun loadSavedMode(modeTypeId: String): Mode? {
        val appSelections = appSelectionDao.getAppSelectionsForModeSync(modeTypeId)
        val modeType = getModeType(modeTypeId) ?: return null
        val appInfos = appSelections.map { entity ->
            AppInfo(
                packageName = entity.packageName,
                name = entity.appName,
                icon = null,
            )
        }
        return Mode(type = modeType, selectedApps = appInfos)
    }

    suspend fun toggleDND(modeTypeId: String) {
        val dndSetting = dndDao.getDNDSetting(modeTypeId)
        if (dndSetting != null) {
            dndDao.updateDNDSetting(dndSetting.copy(isEnabled = !dndSetting.isEnabled))
        } else {
            dndDao.insertSavedMode(DNDSettingEntity(modeTypeId = modeTypeId, isEnabled = true))
        }
    }

    suspend fun isDNDEnabled(modeTypeId: String): Boolean {
        val dndSetting = dndDao.getDNDSetting(modeTypeId)
        return dndSetting?.isEnabled ?: false
    }

    suspend fun saveModeType(modeType: ModeType) {
        modeTypeDao.insertModeType(
            ModeTypeEntity(
                modeTypeId = modeType.id,
                name = modeType.name,
                description = modeType.description,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    fun getSavedModeTypes(): Flow<List<ModeType>> {
        return modeTypeDao.getAllModeTypes().map {
            it.map { entity ->
                ModeType(
                    id = entity.modeTypeId,
                    name = entity.name,
                    description = entity.description,
                    isCustom = true, // All saved mode types are custom
                )
            }
        }
    }

    suspend fun getModeType(modeTypeId: String): ModeType? {
        val modeType = ModeType.MODES.find { it.id == modeTypeId }
        if (modeType == null) {
            modeTypeDao.getModeType(modeTypeId)?.let {
                return ModeType(
                    id = it.modeTypeId,
                    name = it.name,
                    description = it.description,
                    isCustom = true, // Custom since it's in the database
                )
            }
            return null
        }
        return modeType // Default modes are not custom
    }

    suspend fun updateModeType(modeTypeId: String, name: String, description: String) {
        modeTypeDao.getModeType(modeTypeId)?.let { existingModeType ->
            modeTypeDao.updateModeType(
                existingModeType.copy(
                    name = name,
                    description = description,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun deleteModeType(modeTypeId: String) {
        // First delete all related data
        clearAppSelectionsForMode(modeTypeId)

        // Delete saved mode entry
        savedModeDao.getSavedMode(modeTypeId)?.let { savedMode ->
            savedModeDao.deleteSavedMode(savedMode)
        }

        // Delete DND settings
        dndDao.getDNDSetting(modeTypeId)?.let { dndSetting ->
            dndDao.deleteDNDSetting(dndSetting)
        }

        // Finally delete the mode type
        modeTypeDao.getModeType(modeTypeId)?.let { modeType ->
            modeTypeDao.deleteModeType(modeType)
        }
    }
}
