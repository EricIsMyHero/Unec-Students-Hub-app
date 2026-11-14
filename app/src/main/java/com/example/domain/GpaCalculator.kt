package com.example.domain

import androidx.compose.ui.graphics.Color
import com.example.data.local.GpaSubjectEntity

data class GpaSummary(
    val gpa: Double,
    val totalCredits: Int,
    val earnedCredits: Int,
    val weightedNumerator: Double,
    val gradeKey: String,
    val messageKey: String,
    val color: Color,
    val ringStart: Color,
    val ringEnd: Color
)

sealed class PredictionResult {
    data object Invalid : PredictionResult()
    data object Impossible : PredictionResult()
    data object AlreadyReached : PredictionResult()
    data class LowPass(val target: Double, val credits: Int) : PredictionResult()
    data class Required(val target: Double, val credits: Int, val requiredScore: Double) : PredictionResult()
}

object GpaCalculator {
    fun isPassed(score: Double): Boolean = score >= 51.0

    fun earnedCredit(credit: Int, score: Double): Int =
        if (isPassed(score)) credit else 0

    fun calculate(subjects: List<GpaSubjectEntity>): GpaSummary {
        var numerator = 0.0
        var denominator = 0
        var earned = 0
        for (s in subjects) {
            if (s.credit <= 0 || s.score < 0.0 || s.score > 100.0) continue
            val e = earnedCredit(s.credit, s.score)
            numerator += s.score * e
            denominator += s.credit
            earned += e
        }
        val gpa = if (denominator == 0) 0.0 else numerator / denominator
        val (gradeKey, msgKey, color, ringStart, ringEnd) = classify(gpa)
        return GpaSummary(
            gpa = gpa,
            totalCredits = denominator,
            earnedCredits = earned,
            weightedNumerator = numerator,
            gradeKey = gradeKey,
            messageKey = msgKey,
            color = color,
            ringStart = ringStart,
            ringEnd = ringEnd
        )
    }

    private data class Classification(
        val gradeKey: String,
        val msgKey: String,
        val color: Color,
        val ringStart: Color,
        val ringEnd: Color
    )

    private fun classify(gpa: Double): Classification = when {
        gpa <= 0.0 -> Classification(
            "", "gpaMessage4", Color(0xFF64748B), Color(0xFF334155), Color(0xFF475569)
        )
        gpa >= 91.0 -> Classification(
            "gpaGradeA", "gpaMessage0", Color(0xFF22D3EE), Color(0xFF0891B2), Color(0xFF22D3EE)
        )
        gpa >= 81.0 -> Classification(
            "gpaGradeB", "gpaMessage0", Color(0xFF22C55E), Color(0xFF15803D), Color(0xFF22C55E)
        )
        gpa >= 71.0 -> Classification(
            "gpaGradeC", "gpaMessage1", Color(0xFF86EFAC), Color(0xFF16A34A), Color(0xFF86EFAC)
        )
        gpa >= 61.0 -> Classification(
            "gpaGradeD", "gpaMessage2", Color(0xFFFACC15), Color(0xFFCA8A04), Color(0xFFFACC15)
        )
        gpa >= 51.0 -> Classification(
            "gpaGradeE", "gpaMessage3", Color(0xFFFB923C), Color(0xFFEA580C), Color(0xFFFB923C)
        )
        else -> Classification(
            "gpaGradeF", "gpaMessage4", Color(0xFFF87171), Color(0xFFDC2626), Color(0xFFF87171)
        )
    }

    fun scoreColor(score: Double): Color = when {
        score >= 91.0 -> Color(0xFF22D3EE)
        score >= 81.0 -> Color(0xFF22C55E)
        score >= 71.0 -> Color(0xFF86EFAC)
        score >= 61.0 -> Color(0xFFFACC15)
        score >= 51.0 -> Color(0xFFFB923C)
        score > 0.0 -> Color(0xFFF87171)
        else -> Color(0xFF8CA3C4)
    }

    fun predict(summary: GpaSummary, target: Double?, remainingCredits: Int?): PredictionResult {
        if (target == null || remainingCredits == null || target < 0.0 || target > 100.0 || remainingCredits <= 0) {
            return PredictionResult.Invalid
        }
        val needed = (target * (summary.totalCredits + remainingCredits) - summary.weightedNumerator) / remainingCredits
        return when {
            needed > 100.0 -> PredictionResult.Impossible
            needed < 0.0 -> PredictionResult.AlreadyReached
            needed < 51.0 -> PredictionResult.LowPass(target, remainingCredits)
            else -> PredictionResult.Required(target, remainingCredits, needed)
        }
    }
}
