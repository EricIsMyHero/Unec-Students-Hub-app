package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data class MaterialsRoute(
    val initialFavOnly: Boolean = false,
    val initialSubjectSearch: String = ""
)

@Serializable
data class SubjectDetailRoute(val subjectName: String)

@Serializable
data class MaterialDetailRoute(val favKey: String)

@Serializable
data class TestsRoute(val initialSubject: String = "")

@Serializable
data object GpaRoute

@Serializable
data object CurriculumRoute

@Serializable
data object CompassRoute

@Serializable
data class CompassArticleRoute(val articleId: String)

@Serializable
data object RequestsRoute

@Serializable
data class RequestThreadRoute(val requestId: Long)

@Serializable
data object TimerRoute

@Serializable
data object DashboardRoute

@Serializable
data object SupportRoute

@Serializable
data object MoreRoute
