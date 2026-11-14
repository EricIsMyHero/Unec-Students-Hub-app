package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ParseStats
import com.example.data.model.TestQuestion
import com.example.data.model.TestSourceFile
import com.example.data.repository.CatalogRepository
import com.example.data.repository.NewsRepository
import com.example.data.repository.StudyRepository
import com.example.domain.PdfTextExtractor
import com.example.domain.TestBankParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class QuizPhase { SETUP, ACTIVE, RESULTS }

data class TestsUiState(
    val phase: QuizPhase = QuizPhase.SETUP,
    val selectedSubject: String = "",
    val selectedFileLabel: String = "",
    val bankQuestions: List<TestQuestion> = emptyList(),
    val parseStats: ParseStats? = null,
    val isLoading: Boolean = false,
    val errorKey: String? = null,
    val selectedCount: Int = 20,
    val quizQuestions: List<TestQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val answers: Map<Int, Int> = emptyMap(),
    val remainingSeconds: Int = 0,
    val totalDurationSeconds: Int = 0,
    val reviewFilter: String = "all" // "all", "wrong", "skipped"
)

class TestsViewModel(
    application: Application,
    private val catalogRepo: CatalogRepository,
    private val newsRepo: NewsRepository,
    private val studyRepo: StudyRepository
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(TestsUiState())
    val state: StateFlow<TestsUiState> = _state.asStateFlow()

    private var timerJob: Job? = null

    init {
        val firstSubject = catalogRepo.testBankSubjects.keys.firstOrNull().orEmpty()
        if (firstSubject.isNotEmpty()) {
            _state.value = _state.value.copy(selectedSubject = firstSubject)
        }
    }

    fun selectSubject(subject: String) {
        if (_state.value.selectedSubject == subject && _state.value.bankQuestions.isNotEmpty()) return
        _state.value = _state.value.copy(
            selectedSubject = subject,
            selectedFileLabel = "",
            bankQuestions = emptyList(),
            parseStats = null,
            errorKey = null
        )
    }

    fun setQuestionCount(count: Int) {
        _state.value = _state.value.copy(selectedCount = count)
    }

    fun loadSubjectBank() {
        val subj = _state.value.selectedSubject
        val sources = catalogRepo.testBankSubjects[subj].orEmpty()
        if (sources.isEmpty()) {
            _state.value = _state.value.copy(errorKey = "testNoQuestions")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorKey = null, bankQuestions = emptyList())
            val parsedFiles = mutableListOf<Pair<TestSourceFile, List<TestQuestion>>>()
            var totalDetected = 0
            var totalRejected = 0
            var totalUnmarked = 0
            var totalAmbiguous = 0
            var totalMalformed = 0
            var anyDownloadSuccess = false

            withContext(Dispatchers.IO) {
                for (src in sources.take(3)) {
                    val bytesRes = newsRepo.downloadBytes(src.url)
                    val bytes = bytesRes.getOrNull() ?: continue
                    anyDownloadSuccess = true
                    val text = PdfTextExtractor.extractText(bytes)
                    if (text.isBlank()) continue
                    val parsed = TestBankParser.parse(text)
                    totalDetected += parsed.stats.detected
                    totalRejected += parsed.stats.rejected
                    totalUnmarked += parsed.stats.unmarked
                    totalAmbiguous += parsed.stats.ambiguous
                    totalMalformed += parsed.stats.malformed
                    if (parsed.questions.isNotEmpty()) {
                        parsedFiles.add(src to parsed.questions)
                    }
                }
            }

            if (!anyDownloadSuccess) {
                _state.value = _state.value.copy(isLoading = false, errorKey = "testLoadError")
                return@launch
            }

            val merged = TestBankParser.merge(parsedFiles)
            val stats = ParseStats(totalDetected, totalRejected, totalUnmarked, totalAmbiguous, totalMalformed)
            if (merged.questions.isEmpty()) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    parseStats = stats,
                    errorKey = "testNoQuestions"
                )
            } else {
                val maxDefault = minOf(20, merged.questions.size)
                _state.value = _state.value.copy(
                    isLoading = false,
                    selectedFileLabel = sources.first().name,
                    bankQuestions = merged.questions,
                    parseStats = stats,
                    selectedCount = maxDefault,
                    errorKey = null
                )
            }
        }
    }

    fun loadLocalPdf(uri: Uri, displayName: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorKey = null, bankQuestions = emptyList())
            val result = withContext(Dispatchers.IO) {
                try {
                    val bytes = getApplication<Application>().contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: return@withContext null
                    val text = PdfTextExtractor.extractText(bytes)
                    TestBankParser.parse(text)
                } catch (_: Exception) {
                    null
                }
            }

            if (result == null) {
                _state.value = _state.value.copy(isLoading = false, errorKey = "testLoadError")
                return@launch
            }

            if (result.questions.isEmpty()) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    parseStats = result.stats,
                    errorKey = "testNoQuestions"
                )
            } else {
                val cleanName = displayName.ifBlank { "PDF" }
                _state.value = _state.value.copy(
                    isLoading = false,
                    selectedSubject = cleanName,
                    selectedFileLabel = cleanName,
                    bankQuestions = result.questions,
                    parseStats = result.stats,
                    selectedCount = minOf(20, result.questions.size),
                    errorKey = null
                )
            }
        }
    }

    fun startQuiz() {
        val bank = _state.value.bankQuestions
        if (bank.isEmpty()) return
        val count = _state.value.selectedCount.coerceIn(1, bank.size)
        val picked = bank.shuffled().take(count).map { q ->
            // Shuffle options so user learns by meaning, not position
            val indexed = q.options.mapIndexed { idx, txt -> idx to txt }.shuffled()
            val newAns = indexed.indexOfFirst { it.first == q.answerIndex }
            q.copy(options = indexed.map { it.second }, answerIndex = newAns)
        }
        val totalSec = picked.size * 60
        _state.value = _state.value.copy(
            phase = QuizPhase.ACTIVE,
            quizQuestions = picked,
            currentIndex = 0,
            answers = emptyMap(),
            remainingSeconds = totalSec,
            totalDurationSeconds = totalSec,
            reviewFilter = "all"
        )
        startCountdown()
    }

    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.phase == QuizPhase.ACTIVE && _state.value.remainingSeconds > 0) {
                delay(1000L)
                val next = _state.value.remainingSeconds - 1
                _state.value = _state.value.copy(remainingSeconds = next)
                if (next <= 0) {
                    finishQuiz()
                    break
                }
            }
        }
    }

    fun selectAnswer(optionIndex: Int) {
        val st = _state.value
        if (st.phase != QuizPhase.ACTIVE) return
        val updated = st.answers.toMutableMap()
        updated[st.currentIndex] = optionIndex
        _state.value = st.copy(answers = updated)
    }

    fun goToQuestion(index: Int) {
        val st = _state.value
        if (index in st.quizQuestions.indices) {
            _state.value = st.copy(currentIndex = index)
        }
    }

    fun finishQuiz() {
        timerJob?.cancel()
        val st = _state.value
        if (st.phase != QuizPhase.ACTIVE) return

        var correct = 0
        var wrong = 0
        var skipped = 0
        st.quizQuestions.forEachIndexed { idx, q ->
            val picked = st.answers[idx]
            when {
                picked == null -> skipped++
                picked == q.answerIndex -> correct++
                else -> wrong++
            }
        }
        val elapsed = (st.totalDurationSeconds - st.remainingSeconds).coerceAtLeast(1)

        _state.value = st.copy(phase = QuizPhase.RESULTS)

        viewModelScope.launch {
            studyRepo.saveQuizResult(
                subjectName = st.selectedSubject.ifBlank { "Test" },
                correct = correct,
                wrong = wrong,
                skipped = skipped,
                total = st.quizQuestions.size,
                durationSec = elapsed
            )
        }
    }

    fun setReviewFilter(filter: String) {
        _state.value = _state.value.copy(reviewFilter = filter)
    }

    fun backToSetup() {
        timerJob?.cancel()
        _state.value = _state.value.copy(phase = QuizPhase.SETUP)
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
