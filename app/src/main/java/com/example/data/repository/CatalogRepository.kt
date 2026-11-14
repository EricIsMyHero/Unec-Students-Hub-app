package com.example.data.repository

import android.content.Context
import com.example.data.model.CompassArticleJson
import com.example.data.model.CurriculumBundleJson
import com.example.data.model.CurriculumMajorJson
import com.example.data.model.MaterialAlias
import com.example.data.model.MaterialItem
import com.example.data.model.MaterialsCatalogJson
import com.example.data.model.PdfTypeMeta
import com.example.data.model.SubjectSummary
import com.example.data.model.TestSourceFile
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.text.Normalizer

class CatalogRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    val materialsCatalog: MaterialsCatalogJson by lazy {
        val text = context.assets.open("materials.json").bufferedReader().use { it.readText() }
        json.decodeFromString<MaterialsCatalogJson>(text)
    }

    val curriculumBundle: CurriculumBundleJson by lazy {
        val text = context.assets.open("curriculum.json").bufferedReader().use { it.readText() }
        json.decodeFromString<CurriculumBundleJson>(text)
    }

    val compassArticles: List<CompassArticleJson> by lazy {
        val text = context.assets.open("compass.json").bufferedReader().use { it.readText() }
        json.decodeFromString<List<CompassArticleJson>>(text)
    }

    val pdfTypes: Map<String, PdfTypeMeta> get() = materialsCatalog.pdfTypes

    val allMaterials: List<MaterialItem> by lazy { buildIndex() }

    val subjectCatalog: Map<String, SubjectSummary> by lazy { buildSubjectCatalog() }

    val curriculumMajors: Map<String, CurriculumMajorJson> get() = curriculumBundle.curriculumData

    val testBankSubjects: Map<String, List<TestSourceFile>> by lazy { buildTestBankCatalog() }

    private val majorIndexCache = mutableMapOf<String, MajorIndex>()

    private data class MajorIndex(
        val codes: Map<Int, Set<String>>,
        val names: Map<Int, Set<String>>
    )

    companion object {
        const val TUDIFAK_KEY = "tudifak"

        private val TUDIFAK_SUBJECTS = setOf(
            "Liner cebir ve matematiksel analiz",
            "Azerbaycanın tarihi",
            "Bilgi işlem teknolojileri",
            "Yönetim ve organizasyon",
            "Olasılık teorisi ve matematiksel istatistik"
        )

        private val SUBJECT_ALIASES = mapOf(
            "Azərbaycan tarixi" to "Azərbaycanın tarixi"
        )

        val MAJOR_I18N_KEYS = mapOf(
            "economics" to "İqtisadiyyat",
            "finance" to "Maliyyə",
            "accounting" to "Mühasibat",
            "management" to "Menecment",
            "marketing" to "Marketinq",
            "design" to "Dizayn",
            "foodEngineering" to "Qida mühəndisliyi",
            "internationalRelations" to "Beynəlxalq münasibətlər",
            "internationalTradeLogistics" to "Beynəlxalq ticarət və logistika",
            "ecology" to "Ekologiya",
            "statistics" to "Statistika",
            "publicAdministration" to "Dövlət və bələdiyyə idarəetməsi",
            "businessManagement" to "Biznesin idarə edilməsi",
            "tourism" to "Turizm işinin təşkili",
            "socialWork" to "Sosial iş"
        )

        val MAJOR_I18N_EN = mapOf(
            "economics" to "Economics",
            "finance" to "Finance",
            "accounting" to "Accounting",
            "management" to "Management",
            "marketing" to "Marketing",
            "design" to "Design",
            "foodEngineering" to "Food Engineering",
            "internationalRelations" to "International Relations",
            "internationalTradeLogistics" to "International Trade & Logistics",
            "ecology" to "Ecology",
            "statistics" to "Statistics",
            "publicAdministration" to "Public & Municipal Administration",
            "businessManagement" to "Business Administration",
            "tourism" to "Tourism Business Organization",
            "socialWork" to "Social Work"
        )

        fun normAz(raw: String?): String =
            Normalizer.normalize((raw ?: "").lowercase(), Normalizer.Form.NFD)
                .replace(Regex("[\u0300-\u036f]"), "")
                .replace('ə', 'e')
                .replace('ı', 'i')
                .replace(Regex("\\s+"), " ")
                .trim()

        fun normSubjectName(raw: String?): String {
            var s = (raw ?: "").trim()
            val sel = Regex("^seçmə fənn\\s*(?:-\\s*\\d+)?\\s*\\((.+)\\)\\s*$", RegexOption.IGNORE_CASE).matchEntire(s)
            if (sel != null) s = sel.groupValues[1]
            s = Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD)
                .replace(Regex("[\u0300-\u036f]"), "")
                .replace('ı', 'i')
                .replace('ə', 'e')
                .replace("kompyuter", "komputer")
                .replace(Regex("\\s*-\\s*"), "-")
                .replace(Regex("\\s+"), " ")
                .trim()
            s = Regex("^xarici dilde isguzar ve akademik kommunikasiya\\s*-?\\s*(a1|a2|b1\\+|b1|[1-4])$")
                .replace(s) { m ->
                    val lvl = when (m.groupValues[1]) {
                        "a1" -> "1"
                        "a2" -> "2"
                        "b1" -> "3"
                        "b1+" -> "4"
                        else -> m.groupValues[1]
                    }
                    "xarici dil $lvl"
                }
            return s.replace(Regex("\\s+"), "")
        }

        private val normalizedAliases: Map<String, String> by lazy {
            SUBJECT_ALIASES.entries.associate { (k, v) -> normSubjectName(k) to normSubjectName(v) }
        }

        fun canonSubjectName(raw: String?): String {
            val n = normSubjectName(raw)
            return normalizedAliases[n] ?: n
        }

        private val normalizedTudifak: Set<String> by lazy {
            TUDIFAK_SUBJECTS.map { normSubjectName(it) }.toSet()
        }
    }

    fun majorLabel(key: String, lang: String): String {
        if (key == TUDIFAK_KEY) return "Tudifak"
        return if (lang == "en") {
            MAJOR_I18N_EN[key] ?: curriculumMajors[key]?.name ?: key
        } else {
            MAJOR_I18N_KEYS[key] ?: curriculumMajors[key]?.name ?: key
        }
    }

    private fun buildIndex(): List<MaterialItem> {
        val cat = materialsCatalog
        val list = mutableListOf<MaterialItem>()
        val seen = LinkedHashMap<String, MaterialItem>()

        cat.data.forEach { (course, cd) ->
            cd.subjects.forEach { (subject, subj) ->
                val subjCode = subj.code?.trim().orEmpty()
                subj.pdfs.forEach { p ->
                    val k = "pdf/${p.file}"
                    val identities = cat.materialIdentities[k].orEmpty()
                    val code = p.code?.trim()?.takeIf { it.isNotEmpty() } ?: subjCode
                    val alias = MaterialAlias(course, subject, code, subj.type, subj.semester)
                    val oldAliases = identities.map { old ->
                        MaterialAlias(
                            course = old.course,
                            subject = old.subject,
                            code = old.code ?: code,
                            format = old.type.ifEmpty { subj.type },
                            semester = old.semester ?: subj.semester
                        )
                    }

                    val existing = seen[k]
                    if (existing != null) {
                        if (oldAliases.isEmpty()) {
                            seen[k] = existing.copy(aliases = existing.aliases + alias)
                        }
                    } else {
                        val primary = oldAliases.firstOrNull()
                        val effCourse = primary?.course ?: course
                        val effSubject = primary?.subject ?: subject
                        val effFormat = primary?.format ?: subj.type
                        val effSemester = primary?.semester ?: subj.semester
                        val effCode = primary?.code ?: code
                        val typeLabel = cat.pdfTypes[p.pdfType]?.label.orEmpty()
                        val aliases = oldAliases.ifEmpty { listOf(alias) }
                        val searchParts = listOf(p.name, effSubject, p.file, effCode, typeLabel) +
                            aliases.flatMap { listOf(it.subject, it.code) }

                        val item = MaterialItem(
                            id = "$effCourse|$effSubject|${p.file}",
                            kind = "pdf",
                            course = effCourse,
                            subject = effSubject,
                            name = p.name,
                            file = p.file,
                            addedAt = p.addedAt,
                            url = "${cat.base}pdf/${p.file}",
                            favKey = k,
                            format = effFormat,
                            semester = effSemester,
                            pdfType = p.pdfType,
                            code = effCode,
                            majors = subj.majors,
                            tudifak = subj.tudifak,
                            aliases = aliases,
                            searchNormalized = normAz(searchParts.joinToString(" "))
                        )
                        seen[k] = item
                        list.add(item)
                    }
                }
            }
        }

        cat.extrasData.forEach { (course, extras) ->
            extras.forEach { p ->
                val k = "pdf-extra/${p.file}"
                if (!seen.containsKey(k)) {
                    val rows = cat.materialIdentities[k].orEmpty()
                    val effCourse = rows.firstOrNull()?.course?.takeIf { it.isNotBlank() } ?: course
                    val typeLabel = cat.pdfTypes[p.pdfType]?.label.orEmpty()
                    val item = MaterialItem(
                        id = "$effCourse||${p.file}",
                        kind = "extra",
                        course = effCourse,
                        subject = null,
                        name = p.name,
                        file = p.file,
                        addedAt = p.addedAt,
                        url = "${cat.extrasBase}${p.file}",
                        favKey = k,
                        format = "",
                        semester = null,
                        pdfType = p.pdfType,
                        code = "",
                        desc = p.desc,
                        searchNormalized = normAz(listOf(p.name, p.file, p.desc, typeLabel).joinToString(" "))
                    )
                    seen[k] = item
                    list.add(item)
                }
            }
        }

        return seen.values.sortedByDescending { parseDateMillis(it.addedAt) }
    }

    private fun buildSubjectCatalog(): Map<String, SubjectSummary> {
        val cat = materialsCatalog
        val bySubject = LinkedHashMap<String, MutableList<MaterialItem>>()
        val meta = LinkedHashMap<String, Triple<String, String, Pair<List<String>, Boolean>>>()
        val legacyCourses = LinkedHashMap<String, String>()

        val flatIdentities = cat.materialIdentities.values.flatten()

        cat.data.forEach { (course, cd) ->
            cd.subjects.forEach { (name, subj) ->
                if (!legacyCourses.containsKey(name)) {
                    val foundLegacy = flatIdentities.firstOrNull { it.subject == name }?.course ?: course
                    legacyCourses[name] = foundLegacy
                    val code = subj.code?.trim()?.takeIf { it.isNotEmpty() }
                        ?: subj.pdfs.firstOrNull { !it.code.isNullOrBlank() }?.code?.trim().orEmpty()
                    meta[name] = Triple(subj.type, code, subj.majors to subj.tudifak)
                }
            }
        }

        allMaterials.filter { it.kind == "pdf" && !it.subject.isNullOrBlank() }.forEach { item ->
            val s = item.subject!!
            bySubject.getOrPut(s) { mutableListOf() }.add(item)
        }

        return bySubject.entries.associate { (name, entries) ->
            val (type, code, pair) = meta[name] ?: Triple("", "", emptyList<String>() to false)
            val sems = subjectSemesters(name, code, entries, "all")
            name to SubjectSummary(
                name = name,
                type = type,
                code = code,
                legacyCourse = legacyCourses[name] ?: "1-ci kurs",
                majors = pair.first,
                tudifak = pair.second,
                entries = entries.sortedByDescending { parseDateMillis(it.addedAt) },
                semesters = sems
            )
        }
    }

    private fun buildTestBankCatalog(): Map<String, List<TestSourceFile>> {
        val map = LinkedHashMap<String, LinkedHashMap<String, TestSourceFile>>()
        materialsCatalog.data.forEach { (course, cd) ->
            cd.subjects.forEach { (subject, value) ->
                if (value.type != "test") return@forEach
                value.pdfs.forEach { pdf ->
                    if (!pdf.file.endsWith(".pdf", ignoreCase = true) || pdf.file.contains('/') || pdf.file.contains('\\')) {
                        return@forEach
                    }
                    val encoded = URLEncoder.encode(pdf.file, "UTF-8").replace("+", "%20")
                    val url = "${materialsCatalog.base}pdf/$encoded"
                    val bucket = map.getOrPut(subject) { LinkedHashMap() }
                    bucket.putIfAbsent(
                        url,
                        TestSourceFile(
                            course = course,
                            subject = subject,
                            file = pdf.file,
                            name = pdf.name.ifBlank { pdf.file },
                            url = url,
                            addedAt = pdf.addedAt.orEmpty()
                        )
                    )
                }
            }
        }
        return map.mapValues { (_, files) ->
            files.values.sortedByDescending { parseDateMillis(it.addedAt) }
        }
    }

    private fun getMajorIndex(majorKey: String): MajorIndex {
        majorIndexCache[majorKey]?.let { return it }
        val codes = (1..4).associateWith { mutableSetOf<String>() }
        val names = (1..4).associateWith { mutableSetOf<String>() }

        fun courseOf(sem: Int) = ((sem + 1) / 2).coerceIn(1, 4)

        curriculumBundle.curriculumData[majorKey]?.let { major ->
            for (sem in 1..8) {
                major.semesterList(sem).forEach { e ->
                    names[courseOf(sem)]?.add(canonSubjectName(e.name))
                    e.code?.trim()?.takeIf { it.isNotEmpty() }?.let { codes[courseOf(sem)]?.add(it) }
                    e.codes.forEach { c -> c.trim().takeIf { it.isNotEmpty() }?.let { codes[courseOf(sem)]?.add(it) } }
                }
            }
        }

        curriculumBundle.curriculumCodes[majorKey]?.let { src ->
            (src.general + src.major + src.practice).forEach { x ->
                names[courseOf(x.semester)]?.add(canonSubjectName(x.name))
                x.code.trim().takeIf { it.isNotEmpty() }?.let { codes[courseOf(x.semester)]?.add(it) }
            }
            (src.generalElectives + src.majorElectives).forEach { g ->
                g.subjects.forEach { x ->
                    names[courseOf(g.semester)]?.add(canonSubjectName(x.name))
                    x.code.trim().takeIf { it.isNotEmpty() }?.let { codes[courseOf(g.semester)]?.add(it) }
                }
            }
        }

        return MajorIndex(codes, names).also { majorIndexCache[majorKey] = it }
    }

    fun isTudifakSubject(name: String, explicitTudifak: Boolean): Boolean =
        explicitTudifak || normalizedTudifak.contains(normSubjectName(name))

    fun subjectMatchesMajor(
        subjectName: String,
        code: String,
        explicitMajors: List<String>,
        explicitTudifak: Boolean,
        majorKey: String,
        courseNo: Int? = null
    ): Boolean {
        val tudifak = isTudifakSubject(subjectName, explicitTudifak)
        if (majorKey == TUDIFAK_KEY) return tudifak
        if (majorKey.isEmpty() || majorKey == "all") return true
        if (tudifak) return false
        if (majorKey in explicitMajors) return true
        val idx = getMajorIndex(majorKey)
        val courses = if (courseNo != null) listOf(courseNo) else listOf(1, 2, 3, 4)
        val canon = canonSubjectName(subjectName)
        return courses.any { c ->
            (code.isNotEmpty() && idx.codes[c]?.contains(code) == true) ||
                idx.names[c]?.contains(canon) == true
        }
    }

    fun subjectSemesters(
        subjectName: String,
        code: String,
        entries: List<MaterialItem>,
        majorKey: String
    ): List<Int> {
        val keys = if (majorKey == "all") curriculumMajors.keys else listOf(majorKey)
        val result = sortedSetOf<Int>()
        val normalized = canonSubjectName(subjectName)
        val codes = (listOf(code) + entries.map { it.code }).filter { it.isNotBlank() }.toSet()

        for (key in keys) {
            val rows = mutableListOf<Triple<String, List<String>, Int>>()
            curriculumBundle.curriculumData[key]?.let { plan ->
                for (sem in 1..8) {
                    plan.semesterList(sem).forEach { r ->
                        rows.add(Triple(r.name, listOfNotNull(r.code) + r.codes, sem))
                    }
                }
            }
            curriculumBundle.curriculumCodes[key]?.let { idx ->
                (idx.general + idx.major + idx.practice).forEach { r ->
                    rows.add(Triple(r.name, listOf(r.code), r.semester))
                }
                (idx.generalElectives + idx.majorElectives).forEach { blk ->
                    blk.subjects.forEach { r ->
                        rows.add(Triple(r.name, listOf(r.code), blk.semester))
                    }
                }
            }
            val exact = rows.filter { canonSubjectName(it.first) == normalized }
            val matches = exact.ifEmpty { rows.filter { r -> r.second.any { it in codes } } }
            matches.forEach { if (it.third in 1..8) result.add(it.third) }
        }

        if (result.isEmpty() && (majorKey == "all" || majorKey == TUDIFAK_KEY)) {
            for (item in entries) {
                materialsCatalog.materialIdentities[item.favKey].orEmpty().forEach { old ->
                    val year = old.course.takeWhile { it.isDigit() }.toIntOrNull()
                    val sem = old.semester
                    if (year != null && sem != null) {
                        result.add((year - 1) * 2 + sem)
                    }
                }
            }
        }
        return result.toList()
    }

    fun findMaterialsForCurriculumSubject(subjectName: String, codes: List<String>): List<MaterialItem> {
        val canon = canonSubjectName(subjectName)
        val codeSet = codes.filter { it.isNotBlank() }.toSet()
        return allMaterials.filter { m ->
            m.kind == "pdf" && (
                (m.code.isNotBlank() && m.code in codeSet) ||
                    (m.subject != null && canonSubjectName(m.subject) == canon)
                )
        }
    }

    private fun parseDateMillis(iso: String?): Long {
        if (iso.isNullOrBlank()) return 0L
        return try {
            java.time.OffsetDateTime.parse(iso).toInstant().toEpochMilli()
        } catch (_: Exception) {
            0L
        }
    }
}
