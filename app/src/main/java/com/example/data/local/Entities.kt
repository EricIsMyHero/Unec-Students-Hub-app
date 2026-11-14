package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gpa_subjects")
data class GpaSubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val credit: Int,
    val score: Double,
    val sortOrder: Int = 0
)

@Entity(tableName = "gpa_history")
data class GpaSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gpa: Double,
    val totalCredits: Int,
    val earnedCredits: Int,
    val subjectCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "exam_countdowns")
data class ExamCountdownEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectName: String,
    val examDateIso: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectName: String,
    val score: Int,
    val total: Int,
    val percentage: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val skippedCount: Int,
    val durationSec: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "curriculum_overrides",
    primaryKeys = ["majorKey", "subjectKey"]
)
data class CurriculumOverrideEntity(
    val majorKey: String,
    val subjectKey: String,
    val completed: Boolean
)

@Entity(tableName = "material_requests")
data class MaterialRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val course: String = "",
    val subject: String = "",
    val authorName: String = "UNEC Student",
    val status: String = "open", // "open" or "fulfilled"
    val upvotes: Int = 1,
    val isUpvotedByMe: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "request_replies")
data class RequestReplyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val requestId: Long,
    val authorName: String,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "subject_notes")
data class SubjectNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectName: String,
    val authorName: String,
    val tagKey: String? = null,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "material_ratings")
data class MaterialRatingEntity(
    @PrimaryKey val favKey: String,
    val stars: Int,
    val updatedAt: Long = System.currentTimeMillis()
)
