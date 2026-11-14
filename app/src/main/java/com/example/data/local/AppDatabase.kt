package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        GpaSubjectEntity::class,
        GpaSnapshotEntity::class,
        ExamCountdownEntity::class,
        QuizResultEntity::class,
        CurriculumOverrideEntity::class,
        MaterialRequestEntity::class,
        RequestReplyEntity::class,
        SubjectNoteEntity::class,
        MaterialRatingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gpaDao(): GpaDao
    abstract fun examDao(): ExamDao
    abstract fun quizResultDao(): QuizResultDao
    abstract fun curriculumOverrideDao(): CurriculumOverrideDao
    abstract fun requestDao(): RequestDao
    abstract fun subjectCommunityDao(): SubjectCommunityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "unec_hub.db"
                ).build().also { INSTANCE = it }
            }
    }
}
