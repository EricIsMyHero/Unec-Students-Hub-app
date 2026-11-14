package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ExamCountdownEntity
import com.example.data.local.GpaSnapshotEntity
import com.example.data.local.GpaSubjectEntity
import com.example.data.local.MaterialRequestEntity
import com.example.data.local.QuizResultEntity
import com.example.data.local.RequestReplyEntity
import com.example.data.local.SubjectNoteEntity
import com.example.data.local.UserPrefsState
import com.example.data.model.TelegramPost
import com.example.data.repository.CatalogRepository
import com.example.data.repository.NewsRepository
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NewsUiState(
    val posts: List<TelegramPost> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class MainViewModel(
    val catalogRepo: CatalogRepository,
    val newsRepo: NewsRepository,
    val studyRepo: StudyRepository
) : ViewModel() {

    val prefsState: StateFlow<UserPrefsState> = studyRepo.prefsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPrefsState())

    val gpaSubjects: StateFlow<List<GpaSubjectEntity>> = studyRepo.gpaSubjectsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gpaHistory: StateFlow<List<GpaSnapshotEntity>> = studyRepo.gpaHistoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exams: StateFlow<List<ExamCountdownEntity>> = studyRepo.examCountdownsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentQuizResults: StateFlow<List<QuizResultEntity>> = studyRepo.recentQuizResultsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val materialRequests: StateFlow<List<MaterialRequestEntity>> = studyRepo.materialRequestsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ratingsMap: StateFlow<Map<String, Int>> = studyRepo.materialRatingsFlow
        .map { list -> list.associate { it.favKey to it.stars } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _newsState = MutableStateFlow(NewsUiState(isLoading = true))
    val newsState: StateFlow<NewsUiState> = _newsState.asStateFlow()

    private val _showUpdatesSheet = MutableStateFlow(false)
    val showUpdatesSheet: StateFlow<Boolean> = _showUpdatesSheet.asStateFlow()

    init {
        viewModelScope.launch {
            studyRepo.ensureInitialGpaRow()
            studyRepo.recordDailyCheckIn()
        }
        refreshTelegramNews()
    }

    fun refreshTelegramNews() {
        viewModelScope.launch {
            _newsState.value = _newsState.value.copy(isLoading = true, error = null)
            val result = newsRepo.fetchLatestTelegramPosts()
            result.onSuccess { posts ->
                _newsState.value = NewsUiState(posts = posts, isLoading = false, error = null)
            }.onFailure { err ->
                _newsState.value = _newsState.value.copy(isLoading = false, error = err.message ?: "Error")
            }
        }
    }

    fun openUpdatesSheet() {
        _showUpdatesSheet.value = true
        val maxId = _newsState.value.posts.maxOfOrNull { it.id } ?: 0L
        if (maxId > 0L) {
            viewModelScope.launch { studyRepo.markSeenTgId(maxId) }
        }
    }

    fun closeUpdatesSheet() {
        _showUpdatesSheet.value = false
    }

    fun toggleQuickLightDark() {
        viewModelScope.launch {
            val current = prefsState.value.themeKey
            val next = if (current == "arctic" || current == "candy" || current == "slate") "unec" else "arctic"
            studyRepo.setTheme(next)
        }
    }

    fun toggleLang() {
        viewModelScope.launch {
            val next = if (prefsState.value.lang == "en") "az" else "en"
            studyRepo.setLang(next)
        }
    }

    fun setLang(lang: String) {
        viewModelScope.launch { studyRepo.setLang(lang) }
    }

    fun setTheme(key: String) {
        viewModelScope.launch { studyRepo.setTheme(key) }
    }

    fun toggleFavorite(favKey: String) {
        viewModelScope.launch { studyRepo.toggleFavorite(favKey) }
    }

    fun toggleSavedCompass(articleId: String) {
        viewModelScope.launch { studyRepo.toggleSavedCompass(articleId) }
    }

    fun setSelectedMajor(major: String) {
        viewModelScope.launch { studyRepo.setSelectedMajor(major) }
    }

    fun setSelectedMaterialsYear(year: Int) {
        viewModelScope.launch { studyRepo.setSelectedMaterialsYear(year) }
    }

    fun setSelectedCurriculumMajor(major: String) {
        viewModelScope.launch { studyRepo.setSelectedCurriculumMajor(major) }
    }

    fun updateProfile(name: String, faculty: String, major: String, year: Int) {
        viewModelScope.launch { studyRepo.updateProfile(name, faculty, major, year) }
    }

    fun recordPdfOpened() {
        viewModelScope.launch { studyRepo.onPdfOpened() }
    }

    // GPA actions
    fun addGpaSubject() {
        viewModelScope.launch {
            val nextOrder = (gpaSubjects.value.maxOfOrNull { it.sortOrder } ?: 0) + 1
            studyRepo.addGpaSubject(nextOrder)
        }
    }

    fun updateGpaSubject(subject: GpaSubjectEntity) {
        viewModelScope.launch { studyRepo.updateGpaSubject(subject) }
    }

    fun deleteGpaSubject(id: Long) {
        viewModelScope.launch { studyRepo.deleteGpaSubject(id) }
    }

    fun saveGpaSnapshot(gpa: Double, totalCredits: Int, earnedCredits: Int, count: Int) {
        viewModelScope.launch { studyRepo.saveGpaSnapshot(gpa, totalCredits, earnedCredits, count) }
    }

    // Exam actions
    fun addExam(subjectName: String, examDateIso: String) {
        if (subjectName.isBlank() || examDateIso.isBlank()) return
        viewModelScope.launch { studyRepo.addExam(subjectName, examDateIso) }
    }

    fun deleteExam(id: Long) {
        viewModelScope.launch { studyRepo.deleteExam(id) }
    }

    // Curriculum actions
    fun observeCurriculumOverrides(majorKey: String): Flow<Map<String, Boolean>> =
        studyRepo.observeCurriculumOverrides(majorKey).map { list ->
            list.associate { it.subjectKey to it.completed }
        }

    fun setCurriculumOverride(majorKey: String, subjectKey: String, completed: Boolean) {
        viewModelScope.launch { studyRepo.setCurriculumOverride(majorKey, subjectKey, completed) }
    }

    // Requests & Community actions
    fun addRequest(text: String, course: String, subject: String) {
        if (text.isBlank()) return
        val author = prefsState.value.profileName
        viewModelScope.launch { studyRepo.addRequest(text, course, subject, author) }
    }

    fun toggleUpvote(req: MaterialRequestEntity) {
        viewModelScope.launch { studyRepo.toggleUpvote(req) }
    }

    fun toggleFulfilled(req: MaterialRequestEntity) {
        viewModelScope.launch { studyRepo.toggleFulfilled(req) }
    }

    fun deleteRequest(id: Long) {
        viewModelScope.launch { studyRepo.deleteRequest(id) }
    }

    fun observeRequestReplies(requestId: Long): Flow<List<RequestReplyEntity>> =
        studyRepo.observeRequestReplies(requestId)

    fun addRequestReply(requestId: Long, text: String) {
        if (text.isBlank()) return
        val author = prefsState.value.profileName
        viewModelScope.launch { studyRepo.addRequestReply(requestId, author, text) }
    }

    fun deleteRequestReply(id: Long) {
        viewModelScope.launch { studyRepo.deleteRequestReply(id) }
    }

    fun observeSubjectNotes(subjectName: String): Flow<List<SubjectNoteEntity>> =
        studyRepo.observeSubjectNotes(subjectName)

    fun addSubjectNote(subjectName: String, tagKey: String?, text: String) {
        if (text.isBlank()) return
        val author = prefsState.value.profileName
        viewModelScope.launch { studyRepo.addSubjectNote(subjectName, author, tagKey, text) }
    }

    fun deleteSubjectNote(id: Long) {
        viewModelScope.launch { studyRepo.deleteSubjectNote(id) }
    }

    fun setMaterialRating(favKey: String, stars: Int) {
        viewModelScope.launch { studyRepo.setMaterialRating(favKey, stars) }
    }
}
