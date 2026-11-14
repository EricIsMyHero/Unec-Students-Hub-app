package com.example.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MaterialsCatalogJson(
    @SerialName("BASE") val base: String,
    @SerialName("EXTRAS_BASE") val extrasBase: String,
    @SerialName("PDF_TYPES") val pdfTypes: Map<String, PdfTypeMeta> = emptyMap(),
    val data: Map<String, CourseDataJson> = emptyMap(),
    val extrasData: Map<String, List<RawExtraPdf>> = emptyMap(),
    @SerialName("MATERIAL_IDENTITIES") val materialIdentities: Map<String, List<LegacyIdentity>> = emptyMap(),
)

@Serializable
data class PdfTypeMeta(
    val label: String,
    val color: String,
    val bg: String = "",
    val border: String = ""
)

@Serializable
data class CourseDataJson(
    val label: String = "",
    val subjects: Map<String, RawSubjectJson> = emptyMap()
)

@Serializable
data class RawSubjectJson(
    val type: String = "",
    val code: String? = null,
    val semester: Int? = null,
    val tudifak: Boolean = false,
    val majors: List<String> = emptyList(),
    val pdfs: List<RawPdfJson> = emptyList()
)

@Serializable
data class RawPdfJson(
    val name: String,
    val file: String,
    val pdfType: String = "",
    val addedAt: String? = null,
    val code: String? = null
)

@Serializable
data class RawExtraPdf(
    val name: String,
    val file: String,
    val desc: String = "",
    val pdfType: String = "optional",
    val addedAt: String? = null
)

@Serializable
data class LegacyIdentity(
    val course: String = "",
    val subject: String = "",
    val type: String = "",
    val semester: Int? = null,
    val code: String? = null
)

data class MaterialAlias(
    val course: String,
    val subject: String,
    val code: String,
    val format: String,
    val semester: Int?
)

data class MaterialItem(
    val id: String,
    val kind: String, // "pdf" or "extra"
    val course: String,
    val subject: String?,
    val name: String,
    val file: String,
    val addedAt: String?,
    val url: String,
    val favKey: String,
    val format: String,
    val semester: Int?,
    val pdfType: String,
    val code: String,
    val desc: String = "",
    val majors: List<String> = emptyList(),
    val tudifak: Boolean = false,
    val aliases: List<MaterialAlias> = emptyList(),
    val searchNormalized: String = ""
)

data class SubjectSummary(
    val name: String,
    val type: String,
    val code: String,
    val legacyCourse: String,
    val majors: List<String>,
    val tudifak: Boolean,
    val entries: List<MaterialItem>,
    val semesters: List<Int>
)

@Serializable
data class CurriculumBundleJson(
    @SerialName("CURRICULUM_DATA") val curriculumData: Map<String, CurriculumMajorJson> = emptyMap(),
    @SerialName("CURRICULUM_CODES") val curriculumCodes: Map<String, CurriculumCodesJson> = emptyMap()
)

@Serializable
data class CurriculumMajorJson(
    val name: String,
    val icon: String = "",
    val semester1: List<CurriculumSubjectJson> = emptyList(),
    val semester2: List<CurriculumSubjectJson> = emptyList(),
    val semester3: List<CurriculumSubjectJson> = emptyList(),
    val semester4: List<CurriculumSubjectJson> = emptyList(),
    val semester5: List<CurriculumSubjectJson> = emptyList(),
    val semester6: List<CurriculumSubjectJson> = emptyList(),
    val semester7: List<CurriculumSubjectJson> = emptyList(),
    val semester8: List<CurriculumSubjectJson> = emptyList()
) {
    fun semesterList(sem: Int): List<CurriculumSubjectJson> = when (sem) {
        1 -> semester1
        2 -> semester2
        3 -> semester3
        4 -> semester4
        5 -> semester5
        6 -> semester6
        7 -> semester7
        8 -> semester8
        else -> emptyList()
    }
}

@Serializable
data class CurriculumSubjectJson(
    val name: String,
    val credit: Int = 0,
    val hours: Int = 0,
    val absenceLimit: Int = 0,
    val weekly: Int = 0,
    val code: String? = null,
    val codes: List<String> = emptyList(),
    val elective: Boolean = false
)

@Serializable
data class CurriculumCodesJson(
    val name: String = "",
    val specialtyCode: String = "",
    val general: List<CodeEntryJson> = emptyList(),
    val major: List<CodeEntryJson> = emptyList(),
    val practice: List<CodeEntryJson> = emptyList(),
    val generalElectives: List<ElectiveBlockJson> = emptyList(),
    val majorElectives: List<ElectiveBlockJson> = emptyList()
)

@Serializable
data class CodeEntryJson(
    val semester: Int = 0,
    val name: String = "",
    val code: String = ""
)

@Serializable
data class ElectiveBlockJson(
    val semester: Int = 0,
    val subjects: List<CodeEntryJson> = emptyList()
)

@Serializable
data class LocalizedText(
    val az: String = "",
    val en: String = ""
) {
    fun get(lang: String): String = if (lang == "en" && en.isNotBlank()) en else az.ifBlank { en }
}

@Serializable
data class CompassTableJson(
    val headers: List<LocalizedText> = emptyList(),
    val rows: List<List<LocalizedText>> = emptyList()
)

@Serializable
data class CompassSectionJson(
    val heading: LocalizedText = LocalizedText(),
    val paragraphs: List<LocalizedText> = emptyList(),
    val bullets: List<LocalizedText> = emptyList(),
    val table: CompassTableJson? = null
)

@Serializable
data class CompassSourceJson(
    val title: LocalizedText = LocalizedText(),
    val url: String = ""
)

@Serializable
data class CompassArticleJson(
    val id: String,
    val category: String,
    val audience: List<String> = emptyList(),
    val icon: String = "",
    val readMinutes: Int = 3,
    val updatedAt: String = "",
    val featured: Boolean = false,
    val title: LocalizedText = LocalizedText(),
    val summary: LocalizedText = LocalizedText(),
    val sections: List<CompassSectionJson> = emptyList(),
    val steps: List<LocalizedText> = emptyList(),
    val sources: List<CompassSourceJson> = emptyList()
)

data class TestSourceFile(
    val course: String,
    val subject: String,
    val file: String,
    val name: String,
    val url: String,
    val addedAt: String
)

data class QuestionOrigin(
    val source: TestSourceFile,
    val page: Int,
    val number: Int
)

data class TestQuestion(
    val question: String,
    val options: List<String>,
    val answerIndex: Int,
    val number: Int,
    val page: Int,
    val origins: List<QuestionOrigin> = emptyList()
)

data class ParseStats(
    val detected: Int = 0,
    val rejected: Int = 0,
    val unmarked: Int = 0,
    val ambiguous: Int = 0,
    val malformed: Int = 0
)

data class TelegramPost(
    val id: Long,
    val text: String,
    val url: String,
    val date: String = ""
)

data class RankTier(
    val name: String,
    val minXp: Int
)

data class RankProgress(
    val current: RankTier,
    val next: RankTier?,
    val progressFraction: Float,
    val xpToNext: Int
)

data class BadgeDef(
    val id: String,
    val nameAz: String,
    val nameEn: String,
    val descAz: String,
    val descEn: String
)
