package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GpaDao {
    @Query("SELECT * FROM gpa_subjects ORDER BY sortOrder ASC, id ASC")
    fun observeSubjects(): Flow<List<GpaSubjectEntity>>

    @Query("SELECT COUNT(*) FROM gpa_subjects")
    suspend fun subjectCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: GpaSubjectEntity): Long

    @Update
    suspend fun updateSubject(subject: GpaSubjectEntity)

    @Query("DELETE FROM gpa_subjects WHERE id = :id")
    suspend fun deleteSubject(id: Long)

    @Query("SELECT * FROM gpa_history ORDER BY timestamp DESC LIMIT 20")
    fun observeHistory(): Flow<List<GpaSnapshotEntity>>

    @Insert
    suspend fun insertSnapshot(snapshot: GpaSnapshotEntity)
}

@Dao
interface ExamDao {
    @Query("SELECT * FROM exam_countdowns ORDER BY examDateIso ASC")
    fun observeExams(): Flow<List<ExamCountdownEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamCountdownEntity): Long

    @Query("DELETE FROM exam_countdowns WHERE id = :id")
    suspend fun deleteExam(id: Long)
}

@Dao
interface QuizResultDao {
    @Query("SELECT * FROM quiz_results ORDER BY timestamp DESC LIMIT 20")
    fun observeRecentResults(): Flow<List<QuizResultEntity>>

    @Insert
    suspend fun insertResult(result: QuizResultEntity)
}

@Dao
interface CurriculumOverrideDao {
    @Query("SELECT * FROM curriculum_overrides WHERE majorKey = :majorKey")
    fun observeForMajor(majorKey: String): Flow<List<CurriculumOverrideEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CurriculumOverrideEntity)
}

@Dao
interface RequestDao {
    @Query("SELECT * FROM material_requests ORDER BY createdAt DESC")
    fun observeRequests(): Flow<List<MaterialRequestEntity>>

    @Insert
    suspend fun insertRequest(request: MaterialRequestEntity): Long

    @Update
    suspend fun updateRequest(request: MaterialRequestEntity)

    @Query("DELETE FROM material_requests WHERE id = :id")
    suspend fun deleteRequest(id: Long)

    @Query("SELECT * FROM request_replies WHERE requestId = :requestId ORDER BY createdAt ASC")
    fun observeReplies(requestId: Long): Flow<List<RequestReplyEntity>>

    @Insert
    suspend fun insertReply(reply: RequestReplyEntity)

    @Query("DELETE FROM request_replies WHERE id = :id")
    suspend fun deleteReply(id: Long)
}

@Dao
interface SubjectCommunityDao {
    @Query("SELECT * FROM subject_notes WHERE subjectName = :subjectName ORDER BY createdAt ASC")
    fun observeNotes(subjectName: String): Flow<List<SubjectNoteEntity>>

    @Insert
    suspend fun insertNote(note: SubjectNoteEntity)

    @Query("DELETE FROM subject_notes WHERE id = :id")
    suspend fun deleteNote(id: Long)

    @Query("SELECT * FROM material_ratings")
    fun observeAllRatings(): Flow<List<MaterialRatingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRating(rating: MaterialRatingEntity)
}
