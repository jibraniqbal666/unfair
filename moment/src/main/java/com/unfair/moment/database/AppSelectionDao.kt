package com.unfair.moment.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSelectionDao {

    @Query("SELECT * FROM app_selections WHERE mode_type_id = :modeTypeId ORDER BY selection_order ASC")
    fun getAppSelectionsForMode(modeTypeId: String): Flow<List<AppSelectionEntity>>

    @Query("SELECT * FROM app_selections WHERE mode_type_id = :modeTypeId ORDER BY selection_order ASC")
    suspend fun getAppSelectionsForModeSync(modeTypeId: String): List<AppSelectionEntity>

    @Query("SELECT * FROM app_selections WHERE mode_type_id = :modeTypeId ORDER BY selection_order ASC")
    fun getAppSelectionsForModeSyncFlow(modeTypeId: String): Flow<List<AppSelectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppSelection(appSelection: AppSelectionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppSelections(appSelections: List<AppSelectionEntity>)

    @Delete
    suspend fun deleteAppSelection(appSelection: AppSelectionEntity)

    @Query("DELETE FROM app_selections WHERE mode_type_id = :modeTypeId")
    suspend fun clearAppSelectionsForMode(modeTypeId: String)

    @Query("DELETE FROM app_selections WHERE mode_type_id = :modeTypeId AND package_name = :packageName")
    suspend fun removeAppFromMode(modeTypeId: String, packageName: String)

    @Transaction
    suspend fun saveAppSelectionsForMode(
        modeTypeId: String,
        appSelections: List<AppSelectionEntity>,
    ) {
        clearAppSelectionsForMode(modeTypeId)
        insertAppSelections(appSelections)
    }
}

@Dao
interface SavedModeDao {

    @Query("SELECT * FROM saved_modes")
    fun getAllSavedModes(): Flow<List<SavedModeEntity>>

    @Query("SELECT * FROM saved_modes WHERE mode_type_id = :modeTypeId")
    suspend fun getSavedMode(modeTypeId: String): SavedModeEntity?

    @Query("SELECT * FROM saved_modes WHERE is_active = 1 LIMIT 1")
    suspend fun getActiveMode(): SavedModeEntity?

    @Query("SELECT * FROM saved_modes WHERE is_active = 1 LIMIT 1")
    fun getActiveModeFlow(): Flow<SavedModeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedMode(savedMode: SavedModeEntity)

    @Update
    suspend fun updateSavedMode(savedMode: SavedModeEntity)

    @Query("UPDATE saved_modes SET is_active = 0")
    suspend fun deactivateAllModes()

    @Transaction
    suspend fun setActiveMode(modeTypeId: String) {
        deactivateAllModes()
        val existingMode = getSavedMode(modeTypeId)
        if (existingMode != null) {
            updateSavedMode(
                existingMode.copy(
                    isActive = true,
                    lastUsedAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                ),
            )
        } else {
            insertSavedMode(
                SavedModeEntity(
                    modeTypeId = modeTypeId,
                    isActive = true,
                    lastUsedAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    @Delete
    suspend fun deleteSavedMode(savedMode: SavedModeEntity)
}

@Dao
interface DNDDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedMode(dndSetting: DNDSettingEntity)

    @Query("SELECT * FROM dnd_settings WHERE mode_type_id = :modeTypeId")
    suspend fun getDNDSetting(modeTypeId: String): DNDSettingEntity?

    @Update
    suspend fun updateDNDSetting(dndSetting: DNDSettingEntity)

    @Delete
    suspend fun deleteDNDSetting(dndSetting: DNDSettingEntity)
}

@Dao
interface ModeTypeDao {

    @Query("SELECT * FROM mode_types")
    fun getAllModeTypes(): Flow<List<ModeTypeEntity>>

    @Query("SELECT * FROM mode_types WHERE mode_type_id = :modeTypeId")
    suspend fun getModeType(modeTypeId: String): ModeTypeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModeType(modeType: ModeTypeEntity)

    @Update
    suspend fun updateModeType(modeType: ModeTypeEntity)

    @Delete
    suspend fun deleteModeType(modeType: ModeTypeEntity)
}


