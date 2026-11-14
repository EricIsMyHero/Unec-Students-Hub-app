package com.example.data

import com.example.data.model.CompassArticleJson
import com.example.data.model.CourseDataJson
import com.example.data.model.CurriculumBundleJson
import com.example.data.model.CurriculumMajorJson
import com.example.data.model.MaterialsCatalogJson
import com.example.data.model.MobileCatalogJson
import com.example.data.model.RawPdfJson
import com.example.data.model.RawSubjectJson
import kotlinx.serialization.json.Json
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MobileCatalogTest {
    private val base = "https://pub-2179773f096a4b76a772ec1317daec99.r2.dev/"
    private val valid = MobileCatalogJson(
        version = 1,
        materials = MaterialsCatalogJson(
            base = base, extrasBase = "${base}pdf-extra/",
            data = mapOf("all" to CourseDataJson(subjects = mapOf("subject" to RawSubjectJson(
                pdfs = listOf(RawPdfJson(name = "Material", file = "material.pdf"))
            ))))
        ),
        curriculum = CurriculumBundleJson(curriculumData = mapOf("major" to CurriculumMajorJson(name = "Major"))),
        compass = listOf(CompassArticleJson(id = "article", category = "academic"))
    )

    @Test fun acceptsExpectedCatalog() = assertTrue(valid.isUsable())
    @Test fun rejectsUnknownVersionOrEmptyContent() {
        assertFalse(valid.copy(version = 2).isUsable())
        assertFalse(valid.copy(compass = emptyList()).isUsable())
        assertFalse(valid.copy(curriculum = CurriculumBundleJson()).isUsable())
    }
    @Test fun rejectsUnexpectedDownloadOrigins() {
        assertFalse(valid.copy(materials = valid.materials.copy(base = "https://example.com/")).isUsable())
        assertFalse(valid.copy(materials = valid.materials.copy(extrasBase = "https://example.com/")).isUsable())
    }
    @Test fun decodesRealCatalogData() {
        val assets = File("src/main/assets")
        val sample = """{"version":1,"materials":${File(assets, "materials.json").readText()},"curriculum":${File(assets, "curriculum.json").readText()},"compass":${File(assets, "compass.json").readText()}}"""
        val json = Json { ignoreUnknownKeys = true }
        assertTrue(json.decodeFromString<MobileCatalogJson>(sample).isUsable())
    }
}
