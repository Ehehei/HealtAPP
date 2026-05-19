package com.example.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.BloodPressureDao
import com.example.data.local.dao.BodyPhotoDao
import com.example.data.local.dao.MedicationDao
import com.example.data.local.dao.MedicationIntakeDao
import com.example.data.local.dao.ReminderDao
import com.example.data.local.dao.ScreeningRecordDao
import com.example.data.local.dao.StateOfHealthDao
import com.example.data.local.dao.StepDao
import com.example.data.local.dao.UserProfileDao
import com.example.data.local.dao.WeightDao
import com.example.data.local.entity.BloodPressureEntity
import com.example.data.local.entity.BodyPhotoEntity
import com.example.data.local.entity.MedicationEntity
import com.example.data.local.entity.MedicationIntakeEntity
import com.example.data.local.entity.ReminderEntity
import com.example.data.local.entity.ScreeningRecordEntity
import com.example.data.local.entity.StateOfHealthEntity
import com.example.data.local.entity.StepEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.local.entity.WeightEntity

@Database(
    entities = [
        BloodPressureEntity::class,
        BodyPhotoEntity::class,
        StateOfHealthEntity::class,
        StepEntity::class,
        UserProfileEntity::class,
        WeightEntity::class,
        MedicationEntity::class,
        ReminderEntity::class,
        ScreeningRecordEntity::class,
        MedicationIntakeEntity::class,
    ],
    version = 5,
    exportSchema = true,
)
abstract class HealthDatabase : RoomDatabase() {
    abstract fun bloodPressureDao(): BloodPressureDao
    abstract fun bodyPhotoDao(): BodyPhotoDao
    abstract fun stateOfHealthDao(): StateOfHealthDao
    abstract fun stepDao(): StepDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun weightDao(): WeightDao
    abstract fun medicationDao(): MedicationDao
    abstract fun reminderDao(): ReminderDao
    abstract fun screeningRecordDao(): ScreeningRecordDao
    abstract fun medicationIntakeDao(): MedicationIntakeDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS medication (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                userId TEXT NOT NULL,
                name TEXT NOT NULL,
                dose TEXT NOT NULL,
                form TEXT NOT NULL,
                instructions TEXT,
                registeredInKz INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_medication_userId ON medication(userId)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS reminder (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                userId TEXT NOT NULL,
                type TEXT NOT NULL,
                title TEXT NOT NULL,
                timeOfDaySec INTEGER NOT NULL,
                daysOfWeekMask INTEGER NOT NULL,
                medicationId INTEGER,
                doseOverride TEXT,
                enabled INTEGER NOT NULL,
                createdAtMillis INTEGER NOT NULL,
                FOREIGN KEY(medicationId) REFERENCES medication(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_reminder_userId ON reminder(userId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_reminder_medicationId ON reminder(medicationId)")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS screening_record (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                userId TEXT NOT NULL,
                screeningCode TEXT NOT NULL,
                dateEpochDay INTEGER NOT NULL,
                notes TEXT
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_screening_record_userId ON screening_record(userId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_screening_record_screeningCode ON screening_record(screeningCode)")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE user_profile ADD COLUMN bloodType TEXT NOT NULL DEFAULT 'UNKNOWN'")
        db.execSQL("ALTER TABLE user_profile ADD COLUMN allergies TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE user_profile ADD COLUMN chronicConditions TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE user_profile ADD COLUMN emergencyContactName TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE user_profile ADD COLUMN emergencyContactPhone TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS medication_intake (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                userId TEXT NOT NULL,
                medicationId INTEGER NOT NULL,
                takenAtMillis INTEGER NOT NULL,
                reminderId INTEGER,
                dose TEXT,
                FOREIGN KEY(medicationId) REFERENCES medication(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_medication_intake_userId ON medication_intake(userId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_medication_intake_medicationId ON medication_intake(medicationId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_medication_intake_takenAtMillis ON medication_intake(takenAtMillis)")
    }
}
