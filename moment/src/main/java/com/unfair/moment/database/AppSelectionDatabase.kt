package com.unfair.moment.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [AppSelectionEntity::class, SavedModeEntity::class, DNDSettingEntity::class, ModeTypeEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppSelectionDatabase : RoomDatabase() {

    abstract fun appSelectionDao(): AppSelectionDao
    abstract fun savedModeDao(): SavedModeDao
    abstract fun dndDao(): DNDDao
    abstract fun modeTypeDao(): ModeTypeDao

    companion object {
        @Volatile
        private var INSTANCE: AppSelectionDatabase? = null

        fun getDatabase(context: Context): AppSelectionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppSelectionDatabase::class.java,
                    "app_selection_database",
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
