package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.CurriculumOverrideEntity
import com.example.data.local.ExamCountdownEntity
import com.example.data.local.GpaSnapshotEntity
import com.example.data.local.GpaSubjectEntity
import com.example.data.local.MaterialRatingEntity
import com.example.data.local.MaterialRequestEntity
import com.example.data.local.PreferencesManager
import com.example.data.local.RequestReplyEntity
import com.example.data.local.SubjectNoteEntity
import com.example.data.local.UserPrefsState
import com.example.data.model.BadgeDef
import com.example.data.model.RankProgress
import com.example.data.model.RankTier
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class StudyRepository(
    private val db: AppDatabase,
    private val prefs: PreferencesManager
) {
    val prefsFlow: Flow<UserPrefsState> = prefs.state
    val gpaSubjectsFlow = db.gpaDao().observeSubjects()
    val gpaHistoryFlow = db.gpaDao().observeHistory()
    val examCountdownsFlow = db.examDao().observeExams()
    val recentQuizResultsFlow = db.quizResultDao().observeRecentResults()
    val materialRequestsFlow = db.requestDao().observeRequests()
    val materialRatingsFlow = db.subjectCommunityDao().observeAllRatings()

    companion object {
        val RANKS = listOf(
            RankTier("Freshman", 0),
            RankTier("Scholar", 100),
            RankTier("Analyst", 300),
            RankTier("Expert", 700),
            RankTier("Master", 1500),
            RankTier("Professor", 3000)
        )

        val BADGES = listOf(
            BadgeDef("first_login", "İlk Addım", "First Step", "Tətbiqə ilk giriş", "Joined UNEC Hub"),
            BadgeDef("first_comment", "İlk Şərh", "First Comment", "İlk fənn şərhini yazdın", "Wrote your first note"),
            BadgeDef("first_rating", "İlk Reytinq", "First Rating", "İlk materialı qiymətləndirdin", "Rated a material"),
            BadgeDef("first_pdf", "Oxumağa Başladı", "Started Reading", "İlk PDF materialını açdın", "Opened your first PDF"),
            BadgeDef("streak_3", "3 Günlük Streak", "3-Day Streak", "Ardıcıl 3 gün aktiv oldun", "Active 3 days in a row"),
            BadgeDef("streak_7", "7 Günlük Streak", "7-Day Streak", "Ardıcıl 7 gün aktiv oldun", "Active 7 days in a row"),
            BadgeDef("streak_30", "30 Günlük Streak", "30-Day Streak", "Ardıcıl 30 gün aktiv oldun", "Active 30 days in a row"),
            BadgeDef("focus_master", "Fokus Ustası", "Focus Master", "10 Pomodoro fokus sessiyası tamamladın", "Completed 10 Pomodoro focus sessions"),
            BadgeDef("test_fan", "Test Həvəskarı", "Test Enthusiast", "10 test tamamladın", "Completed 10 tests"),
            BadgeDef("test_expert", "Test Eksperti", "Test Expert", "50 test tamamladın", "Completed 50 tests"),
            BadgeDef("pdf_hunter", "Material Ovçusu", "Material Hunter", "25 material açdın", "Opened 25 materials"),
            BadgeDef("helpful", "Köməksevər", "Helpful", "10 şərh və ya cavab yazdın", "Posted 10 notes or replies"),
            BadgeDef("active_student", "Aktiv Tələbə", "Active Student", "500 XP topladın", "Earned 500 XP"),
            BadgeDef("unec_legend", "UNEC Əfsanəsi", "UNEC Legend", "2000 XP topladın", "Earned 2000 XP")
        )

        fun getRankProgress(xp: Int): RankProgress {
            var current = RANKS.first()
            for (r in RANKS) {
                if (xp >= r.minXp) current = r else break
            }
            val idx = RANKS.indexOf(current)
            val next = RANKS.getOrNull(idx + 1)
            val frac = if (next == null) 1f else {
                ((xp - current.minXp).toFloat() / (next.minXp - current.minXp).toFloat()).coerceIn(0f, 1f)
            }
            return RankProgress(
                current = current,
                next = next,
                progressFraction = frac,
                xpToNext = next?.let { (it.minXp - xp).coerceAtLeast(0) } ?: 0
            )
        }
    }

    suspend fun ensureInitialGpaRow() {
        if (db.gpaDao().subjectCount() == 0) {
            db.gpaDao().insertSubject(GpaSubjectEntity(name = "", credit = 3, score = 0.0, sortOrder = 0))
        }
    }

    suspend fun recordDailyCheckIn() {
        val today = LocalDate.now().toString()
        prefs.editRaw { p ->
            val lastLogin = p[PreferencesManager.RawKeys.LAST_LOGIN] ?: ""
            val badges = (p[PreferencesManager.RawKeys.BADGES] ?: emptySet()).toMutableSet()
            badges.add("first_login")
            if (lastLogin != today) {
                p[PreferencesManager.RawKeys.LAST_LOGIN] = today
                p[PreferencesManager.RawKeys.XP] = (p[PreferencesManager.RawKeys.XP] ?: 0) + 5
            }
            evaluateBadges(p, badges)
            p[PreferencesManager.RawKeys.BADGES] = badges
        }
    }

    private fun updateStreakLocked(p: androidx.datastore.preferences.core.MutablePreferences, today: String) {
        val yesterday = LocalDate.now().minusDays(1).toString()
        val last = p[PreferencesManager.RawKeys.LAST_ACTIVE] ?: ""
        var streak = p[PreferencesManager.RawKeys.STREAK] ?: 0
        when (last) {
            today -> Unit
            yesterday -> streak += 1
            else -> streak = 1
        }
        p[PreferencesManager.RawKeys.STREAK] = streak
        p[PreferencesManager.RawKeys.LAST_ACTIVE] = today
        if (last != today && streak > 0 && streak % 7 == 0) {
            p[PreferencesManager.RawKeys.XP] = (p[PreferencesManager.RawKeys.XP] ?: 0) + 30
        }
    }

    private fun evaluateBadges(
        p: androidx.datastore.preferences.core.MutablePreferences,
        badges: MutableSet<String>
    ) {
        val xp = p[PreferencesManager.RawKeys.XP] ?: 0
        val streak = p[PreferencesManager.RawKeys.STREAK] ?: 0
        val tests = p[PreferencesManager.RawKeys.SOLVED_TESTS] ?: 0
        val pdfs = p[PreferencesManager.RawKeys.PDFS_OPENED] ?: 0
        val notes = p[PreferencesManager.RawKeys.NOTES_COUNT] ?: 0
        val ratings = p[PreferencesManager.RawKeys.RATINGS_COUNT] ?: 0
        val pomoTotal = p[PreferencesManager.RawKeys.POMO_TOTAL] ?: 0

        if (pdfs >= 1) badges.add("first_pdf")
        if (pdfs >= 25) badges.add("pdf_hunter")
        if (notes >= 1) badges.add("first_comment")
        if (notes >= 10) badges.add("helpful")
        if (ratings >= 1) badges.add("first_rating")
        if (streak >= 3) badges.add("streak_3")
        if (streak >= 7) badges.add("streak_7")
        if (streak >= 30) badges.add("streak_30")
        if (pomoTotal >= 10) badges.add("focus_master")
        if (tests >= 10) badges.add("test_fan")
        if (tests >= 50) badges.add("test_expert")
        if (xp >= 500) badges.add("active_student")
        if (xp >= 2000) badges.add("unec_legend")
    }

    suspend fun setLang(lang: String) = prefs.setLang(lang)
    suspend fun setTheme(key: String) = prefs.setTheme(key)
    suspend fun toggleFavorite(favKey: String): Boolean = prefs.toggleFavorite(favKey)
    suspend fun toggleSavedCompass(articleId: String): Boolean = prefs.toggleSavedCompass(articleId)
    suspend fun setSelectedMajor(major: String) = prefs.setSelectedMajor(major)
    suspend fun setSelectedMaterialsYear(year: Int) = prefs.setSelectedMaterialsYear(year)
    suspend fun setSelectedCurriculumMajor(major: String) = prefs.setSelectedCurriculumMajor(major)
    suspend fun updateProfile(name: String, faculty: String, major: String, year: Int) =
        prefs.updateProfile(name, faculty, major, year)
    suspend fun markSeenTgId(maxId: Long) = prefs.markSeenTgId(maxId)

    suspend fun onPdfOpened() {
        val today = LocalDate.now().toString()
        prefs.editRaw { p ->
            p[PreferencesManager.RawKeys.PDFS_OPENED] = (p[PreferencesManager.RawKeys.PDFS_OPENED] ?: 0) + 1
            p[PreferencesManager.RawKeys.XP] = (p[PreferencesManager.RawKeys.XP] ?: 0) + 2
            updateStreakLocked(p, today)
            val badges = (p[PreferencesManager.RawKeys.BADGES] ?: emptySet()).toMutableSet()
            evaluateBadges(p, badges)
            p[PreferencesManager.RawKeys.BADGES] = badges
        }
    }

    suspend fun recordPomodoroFocusCompleted(minutes: Int = 25) {
        val today = LocalDate.now().toString()
        prefs.editRaw { p ->
            val prevDate = p[PreferencesManager.RawKeys.POMO_DATE] ?: ""
            val prevSess = if (prevDate == today) (p[PreferencesManager.RawKeys.POMO_TODAY_SESSIONS] ?: 0) else 0
            val prevMins = if (prevDate == today) (p[PreferencesManager.RawKeys.POMO_TODAY_MINS] ?: 0) else 0
            p[PreferencesManager.RawKeys.POMO_DATE] = today
            p[PreferencesManager.RawKeys.POMO_TODAY_SESSIONS] = prevSess + 1
            p[PreferencesManager.RawKeys.POMO_TODAY_MINS] = prevMins + minutes
            p[PreferencesManager.RawKeys.POMO_TOTAL] = (p[PreferencesManager.RawKeys.POMO_TOTAL] ?: 0) + 1
            p[PreferencesManager.RawKeys.XP] = (p[PreferencesManager.RawKeys.XP] ?: 0) + ((minutes / 5) * 2).coerceAtLeast(2)
            updateStreakLocked(p, today)
            val badges = (p[PreferencesManager.RawKeys.BADGES] ?: emptySet()).toMutableSet()
            evaluateBadges(p, badges)
            p[PreferencesManager.RawKeys.BADGES] = badges
        }
    }

    suspend fun saveQuizResult(
        subjectName: String,
        correct: Int,
        wrong: Int,
        skipped: Int,
        total: Int,
        durationSec: Int
    ) {
        val pct = if (total == 0) 0 else Math.round((correct.toDouble() / total) * 100).toInt()
        db.quizResultDao().insertResult(
            com.example.data.local.QuizResultEntity(
                subjectName = subjectName,
                score = correct,
                total = total,
                percentage = pct,
                correctCount = correct,
                wrongCount = wrong,
                skippedCount = skipped,
                durationSec = durationSec
            )
        )
        val today = LocalDate.now().toString()
        val xpGain = (correct * 2).coerceAtLeast(5)
        prefs.editRaw { p ->
            p[PreferencesManager.RawKeys.SOLVED_TESTS] = (p[PreferencesManager.RawKeys.SOLVED_TESTS] ?: 0) + 1
            p[PreferencesManager.RawKeys.XP] = (p[PreferencesManager.RawKeys.XP] ?: 0) + xpGain
            updateStreakLocked(p, today)
            val badges = (p[PreferencesManager.RawKeys.BADGES] ?: emptySet()).toMutableSet()
            evaluateBadges(p, badges)
            p[PreferencesManager.RawKeys.BADGES] = badges
        }
    }

    suspend fun addGpaSubject(order: Int) {
        db.gpaDao().insertSubject(GpaSubjectEntity(name = "", credit = 3, score = 0.0, sortOrder = order))
    }

    suspend fun updateGpaSubject(subject: GpaSubjectEntity) {
        db.gpaDao().updateSubject(subject)
    }

    suspend fun deleteGpaSubject(id: Long) {
        db.gpaDao().deleteSubject(id)
    }

    suspend fun saveGpaSnapshot(gpa: Double, totalCredits: Int, earnedCredits: Int, count: Int) {
        db.gpaDao().insertSnapshot(
            GpaSnapshotEntity(gpa = gpa, totalCredits = totalCredits, earnedCredits = earnedCredits, subjectCount = count)
        )
        val today = LocalDate.now().toString()
        prefs.editRaw { p ->
            updateStreakLocked(p, today)
            val badges = (p[PreferencesManager.RawKeys.BADGES] ?: emptySet()).toMutableSet()
            evaluateBadges(p, badges)
            p[PreferencesManager.RawKeys.BADGES] = badges
        }
    }

    suspend fun addExam(subjectName: String, examDateIso: String) {
        db.examDao().insertExam(ExamCountdownEntity(subjectName = subjectName.trim(), examDateIso = examDateIso))
    }

    suspend fun deleteExam(id: Long) = db.examDao().deleteExam(id)

    fun observeCurriculumOverrides(majorKey: String) = db.curriculumOverrideDao().observeForMajor(majorKey)

    suspend fun setCurriculumOverride(majorKey: String, subjectKey: String, completed: Boolean) {
        db.curriculumOverrideDao().upsert(CurriculumOverrideEntity(majorKey, subjectKey, completed))
    }

    suspend fun addRequest(text: String, course: String, subject: String, authorName: String) {
        db.requestDao().insertRequest(
            MaterialRequestEntity(
                text = text.trim(),
                course = course.trim(),
                subject = subject.trim(),
                authorName = authorName.ifBlank { "UNEC Student" }
            )
        )
    }

    suspend fun toggleUpvote(req: MaterialRequestEntity) {
        val nextUpvoted = !req.isUpvotedByMe
        val nextCount = (req.upvotes + if (nextUpvoted) 1 else -1).coerceAtLeast(0)
        db.requestDao().updateRequest(req.copy(upvotes = nextCount, isUpvotedByMe = nextUpvoted))
    }

    suspend fun toggleFulfilled(req: MaterialRequestEntity) {
        val nextStatus = if (req.status == "fulfilled") "open" else "fulfilled"
        db.requestDao().updateRequest(req.copy(status = nextStatus))
    }

    suspend fun deleteRequest(id: Long) = db.requestDao().deleteRequest(id)

    fun observeRequestReplies(requestId: Long) = db.requestDao().observeReplies(requestId)

    suspend fun addRequestReply(requestId: Long, authorName: String, text: String) {
        db.requestDao().insertReply(
            RequestReplyEntity(requestId = requestId, authorName = authorName.ifBlank { "UNEC Student" }, text = text.trim())
        )
        prefs.editRaw { p ->
            p[PreferencesManager.RawKeys.NOTES_COUNT] = (p[PreferencesManager.RawKeys.NOTES_COUNT] ?: 0) + 1
            val badges = (p[PreferencesManager.RawKeys.BADGES] ?: emptySet()).toMutableSet()
            evaluateBadges(p, badges)
            p[PreferencesManager.RawKeys.BADGES] = badges
        }
    }

    suspend fun deleteRequestReply(id: Long) = db.requestDao().deleteReply(id)

    fun observeSubjectNotes(subjectName: String) = db.subjectCommunityDao().observeNotes(subjectName)

    suspend fun addSubjectNote(subjectName: String, authorName: String, tagKey: String?, text: String) {
        db.subjectCommunityDao().insertNote(
            SubjectNoteEntity(
                subjectName = subjectName,
                authorName = authorName.ifBlank { "UNEC Student" },
                tagKey = tagKey,
                text = text.trim()
            )
        )
        prefs.editRaw { p ->
            p[PreferencesManager.RawKeys.NOTES_COUNT] = (p[PreferencesManager.RawKeys.NOTES_COUNT] ?: 0) + 1
            p[PreferencesManager.RawKeys.XP] = (p[PreferencesManager.RawKeys.XP] ?: 0) + 3
            val badges = (p[PreferencesManager.RawKeys.BADGES] ?: emptySet()).toMutableSet()
            evaluateBadges(p, badges)
            p[PreferencesManager.RawKeys.BADGES] = badges
        }
    }

    suspend fun deleteSubjectNote(id: Long) = db.subjectCommunityDao().deleteNote(id)

    suspend fun setMaterialRating(favKey: String, stars: Int) {
        db.subjectCommunityDao().upsertRating(MaterialRatingEntity(favKey, stars.coerceIn(1, 5)))
        prefs.editRaw { p ->
            p[PreferencesManager.RawKeys.RATINGS_COUNT] = (p[PreferencesManager.RawKeys.RATINGS_COUNT] ?: 0) + 1
            p[PreferencesManager.RawKeys.XP] = (p[PreferencesManager.RawKeys.XP] ?: 0) + 2
            val badges = (p[PreferencesManager.RawKeys.BADGES] ?: emptySet()).toMutableSet()
            evaluateBadges(p, badges)
            p[PreferencesManager.RawKeys.BADGES] = badges
        }
    }
}
