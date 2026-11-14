package com.example.domain

import com.example.data.model.ParseStats
import com.example.data.model.QuestionOrigin
import com.example.data.model.TestQuestion
import com.example.data.model.TestSourceFile
import java.text.Normalizer

/**
 * Kotlin port of UNECTestBank (scripts/test-bank.js):
 * parses numbered questions, bullet/checkmark options, explicit answer keys,
 * rejects malformed/unmarked/ambiguous/image-placeholder questions, and merges duplicates.
 */
object TestBankParser {

    private const val IMAGE_MARKER = "[UNEC_PDF_IMAGE]"
    private val CHECK_REGEX = Regex("""^[✓✔√☑]""")
    private val TRAILING_CHECK = Regex("""[✓✔☑]\s*$""")
    private val BULLET_REGEX = Regex("""^[•∙·●◦‣▪◾■◆✓✔√☑\uf0b7\uf0fc]\s*""")
    private val HEADER_REGEX = Regex(
        """(?:Yekun\s+imtahan\s+testinin\s+sualları|^\s*(?:Səhifə|Page)\s+\d+(?:\s*/\s*\d+)?\s*$)""",
        RegexOption.IGNORE_CASE
    )
    private val NUMBER_REGEX = Regex("""^(\d{1,4})[.)](?:\s+(.+))?$""")
    private val EXPLICIT_REGEX = Regex(
        """^(?:(?:Düzgün\s+)?cavab|Correct\s+answer|Answer)\s*[:：]\s*([A-H])\s*[.)]?$""",
        RegexOption.IGNORE_CASE
    )
    private val OPTION_LABEL_REGEX = Regex("""^([A-Ha-h])[).:]\s*""")
    private val LETTER_OR_DIGIT_REGEX = Regex("""[\p{L}\p{N}]""")

    fun clean(s: String?): String =
        Normalizer.normalize(s ?: "", Normalizer.Form.NFC)
            .replace("\u00ad", "")
            .replace(Regex("\\s+"), " ")
            .trim()

    private data class ParsedOption(
        var text: String,
        var correct: Boolean,
        var label: String?
    )

    private data class PendingQuestion(
        val number: Int,
        val page: Int,
        val textLines: MutableList<String> = mutableListOf(),
        val options: MutableList<ParsedOption> = mutableListOf(),
        var explicit: String? = null
    )

    private fun parseOption(line: String): ParsedOption? {
        var correct = CHECK_REGEX.containsMatchIn(line) || line.startsWith("\uf0fc")
        var text = line
        val bullet = BULLET_REGEX.find(text)
        if (bullet != null) {
            text = text.substring(bullet.value.length)
        }
        val labelMatch = OPTION_LABEL_REGEX.find(text)
        val label = labelMatch?.groupValues?.get(1)?.uppercase()
        if (labelMatch != null) {
            text = text.substring(labelMatch.value.length)
        }
        if (bullet == null && labelMatch == null) return null
        if (TRAILING_CHECK.containsMatchIn(text)) {
            correct = true
            text = text.replace(TRAILING_CHECK, "")
        }
        return ParsedOption(clean(text), correct, label)
    }

    data class ParseOutput(
        val questions: List<TestQuestion>,
        val stats: ParseStats
    )

    fun parse(rawText: String): ParseOutput {
        val questions = mutableListOf<TestQuestion>()
        var detected = 0
        var rejected = 0
        var unmarked = 0
        var ambiguous = 0
        var malformed = 0
        var current: PendingQuestion? = null
        var page = 1

        fun flush() {
            val q = current ?: return
            detected++
            val questionText = clean(q.textLines.joinToString(" "))
            val optionTexts = q.options.map { clean(it.text) }
            val marked = q.options.mapIndexedNotNull { idx, opt -> if (opt.correct) idx else null }.toMutableList()

            q.explicit?.let { exp ->
                val idx = q.options.indexOfFirst { it.label == exp }
                if (idx >= 0 && idx !in marked) marked.add(idx)
                if (idx < 0) marked.add(-1)
            }

            val labels = q.options.mapNotNull { it.label }
            val invalidLabels = labels.isNotEmpty() && (
                labels.size != optionTexts.size ||
                    labels.indices.any { i -> labels[i] != "ABCDEFGH"[i].toString() }
                )

            val hasImageMarker = (q.textLines + optionTexts).any { it.contains(IMAGE_MARKER) }
            val isMalformed = questionText.length < 3 ||
                !LETTER_OR_DIGIT_REGEX.containsMatchIn(questionText) ||
                hasImageMarker ||
                optionTexts.size < 2 ||
                optionTexts.size > 10 ||
                optionTexts.any { it.isEmpty() } ||
                optionTexts.toSet().size != optionTexts.size ||
                invalidLabels

            when {
                isMalformed -> { rejected++; malformed++ }
                marked.isEmpty() -> { rejected++; unmarked++ }
                marked.size != 1 || marked[0] < 0 -> { rejected++; ambiguous++ }
                else -> questions.add(
                    TestQuestion(
                        question = questionText,
                        options = optionTexts,
                        answerIndex = marked[0],
                        number = q.number,
                        page = q.page
                    )
                )
            }
            current = null
        }

        val normalized = rawText
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .replace("\u000c", "\n\u000c\n")

        for (rawLine in normalized.split('\n')) {
            if (rawLine == "\u000c") {
                page++
                continue
            }
            val line = clean(rawLine)
            if (line.isEmpty() || HEADER_REGEX.containsMatchIn(line)) continue

            val numberMatch = NUMBER_REGEX.matchEntire(line)
            if (numberMatch != null && (current == null || (current!!.options.isNotEmpty() && current!!.options.last().text.isNotEmpty()))) {
                flush()
                val num = numberMatch.groupValues[1].toIntOrNull() ?: 0
                val initialText = numberMatch.groupValues[2]
                current = PendingQuestion(num, page).apply {
                    if (initialText.isNotBlank()) textLines.add(initialText)
                }
                continue
            }

            val cur = current ?: continue
            val expMatch = EXPLICIT_REGEX.matchEntire(line)
            if (expMatch != null) {
                val letter = expMatch.groupValues[1].uppercase()
                if (cur.explicit != null && cur.explicit != letter) {
                    cur.options.add(ParsedOption("", false, null))
                }
                cur.explicit = letter
                continue
            }

            val opt = parseOption(line)
            if (opt != null) {
                val previous = cur.options.lastOrNull()
                if (previous != null && previous.text.isEmpty() && opt.text.isNotEmpty()) {
                    previous.text = opt.text
                    previous.correct = previous.correct || opt.correct
                    previous.label = opt.label
                } else {
                    cur.options.add(opt)
                }
            } else if (cur.options.isNotEmpty()) {
                cur.options.last().text += " $line"
            } else {
                cur.textLines.add(line)
            }
        }
        flush()

        return ParseOutput(
            questions = questions,
            stats = ParseStats(detected, rejected, unmarked, ambiguous, malformed)
        )
    }

    data class MergeOutput(
        val questions: List<TestQuestion>,
        val duplicates: Int,
        val conflicts: Int
    )

    fun merge(files: List<Pair<TestSourceFile, List<TestQuestion>>>): MergeOutput {
        val unique = LinkedHashMap<String, TestQuestion>()
        val conflicts = mutableSetOf<String>()
        var duplicates = 0

        for ((source, list) in files) {
            for (q in list) {
                val sortedOpts = q.options.map(::clean).sorted().joinToString("||")
                val key = "${clean(q.question)}###$sortedOpts"
                val answer = clean(q.options[q.answerIndex])
                val origin = QuestionOrigin(source, q.page, q.number)

                val existing = unique[key]
                if (existing != null) {
                    val prevAnswer = clean(existing.options[existing.answerIndex])
                    if (prevAnswer != answer) {
                        conflicts.add(key)
                    } else {
                        duplicates++
                        unique[key] = existing.copy(origins = existing.origins + origin)
                    }
                } else {
                    unique[key] = q.copy(origins = listOf(origin))
                }
            }
        }

        val finalQuestions = unique.filterKeys { it !in conflicts }.values.toList()
        return MergeOutput(finalQuestions, duplicates, conflicts.size)
    }
}
