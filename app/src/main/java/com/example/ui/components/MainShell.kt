package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.data.repository.NewsRepository
import com.example.ui.navigation.CompassArticleRoute
import com.example.ui.navigation.CompassRoute
import com.example.ui.navigation.CurriculumRoute
import com.example.ui.navigation.DashboardRoute
import com.example.ui.navigation.GpaRoute
import com.example.ui.navigation.HomeRoute
import com.example.ui.navigation.MaterialDetailRoute
import com.example.ui.navigation.MaterialsRoute
import com.example.ui.navigation.MoreRoute
import com.example.ui.navigation.RequestThreadRoute
import com.example.ui.navigation.RequestsRoute
import com.example.ui.navigation.SubjectDetailRoute
import com.example.ui.navigation.SupportRoute
import com.example.ui.navigation.TestsRoute
import com.example.ui.navigation.TimerRoute
import com.example.ui.screens.CompassArticleScreen
import com.example.ui.screens.CompassScreen
import com.example.ui.screens.CurriculumScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GpaScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MaterialDetailScreen
import com.example.ui.screens.MaterialsScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.RequestThreadScreen
import com.example.ui.screens.RequestsScreen
import com.example.ui.screens.SubjectDetailScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.screens.TestsScreen
import com.example.ui.screens.TimerScreen
import com.example.ui.strings.LocalStrings
import com.example.ui.strings.s
import com.example.ui.theme.LocalUnecPalette
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.TestsViewModel
import com.example.ui.viewmodel.TimerViewModel

private data class PrimaryNavItem(
    val tag: String,
    val labelKey: String,
    val icon: ImageVector,
    val route: Any
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShell(
    factory: ViewModelProvider.Factory,
    mainViewModel: MainViewModel
) {
    val pal = LocalUnecPalette.current
    val strings = LocalStrings.current
    val context = LocalContext.current
    val navController = rememberNavController()

    val prefs by mainViewModel.prefsState.collectAsState()
    val newsState by mainViewModel.newsState.collectAsState()
    val showUpdates by mainViewModel.showUpdatesSheet.collectAsState()
    val gpaSubjects by mainViewModel.gpaSubjects.collectAsState()
    val gpaHistory by mainViewModel.gpaHistory.collectAsState()
    val exams by mainViewModel.exams.collectAsState()
    val recentResults by mainViewModel.recentQuizResults.collectAsState()
    val requests by mainViewModel.materialRequests.collectAsState()
    val ratingsMap by mainViewModel.ratingsMap.collectAsState()

    val testsViewModel: TestsViewModel = viewModel(factory = factory)
    val timerViewModel: TimerViewModel = viewModel(factory = factory)

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = backStackEntry?.destination

    val isPrimaryTab = currentDest?.let { dest ->
        dest.hasRoute<HomeRoute>() ||
            dest.hasRoute<MaterialsRoute>() ||
            dest.hasRoute<SupportRoute>() ||
            dest.hasRoute<MoreRoute>()
    } ?: true

    val topTitle = when {
        currentDest == null || currentDest.hasRoute<HomeRoute>() -> s("appName")
        currentDest.hasRoute<MaterialsRoute>() -> s("coursesTitle")
        currentDest.hasRoute<SubjectDetailRoute>() -> s("subjectInfo")
        currentDest.hasRoute<MaterialDetailRoute>() -> s("mdShortInfo")
        currentDest.hasRoute<TestsRoute>() -> s("navTests")
        currentDest.hasRoute<GpaRoute>() -> s("navGpa")
        currentDest.hasRoute<CurriculumRoute>() -> s("navPlan")
        currentDest.hasRoute<CompassRoute>() || currentDest.hasRoute<CompassArticleRoute>() -> s("navCompass")
        currentDest.hasRoute<RequestsRoute>() || currentDest.hasRoute<RequestThreadRoute>() -> s("navRequests")
        currentDest.hasRoute<TimerRoute>() -> s("navTimer")
        currentDest.hasRoute<DashboardRoute>() -> s("navDashboard")
        currentDest.hasRoute<SupportRoute>() -> s("navSupport")
        currentDest.hasRoute<MoreRoute>() -> s("moreTitle")
        else -> s("appName")
    }

    val navItems = listOf(
        PrimaryNavItem("nav_home", "navHome", Icons.Rounded.Home, HomeRoute),
        PrimaryNavItem("nav_courses", "navCourses", Icons.Rounded.MenuBook, MaterialsRoute()),
        PrimaryNavItem("nav_support", "navSupport", Icons.Rounded.Favorite, SupportRoute),
        PrimaryNavItem("nav_more", "navMore", Icons.Rounded.MoreHoriz, MoreRoute)
    )

    fun navigateTopTab(route: Any) {
        navController.navigate(route) {
            popUpTo(HomeRoute) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val hasUnread = newsState.posts.any { it.id > prefs.seenTgMaxId }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            containerColor = pal.bg,
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                UnecTopBar(
                    title = topTitle,
                    showBack = !isPrimaryTab,
                    onBack = { navController.popBackStack() },
                    hasUnreadUpdates = hasUnread,
                    onOpenUpdates = { mainViewModel.openUpdatesSheet() },
                    onToggleTheme = { mainViewModel.toggleQuickLightDark() },
                    onToggleLang = { mainViewModel.toggleLang() },
                    onOpenDashboard = {
                        navController.navigate(DashboardRoute) { launchSingleTop = true }
                    }
                )
            },
            bottomBar = {
                if (!useRail && isPrimaryTab) {
                    NavigationBar(containerColor = pal.bgAlt) {
                        navItems.forEach { item ->
                            val selected = when (item.route) {
                                is HomeRoute -> currentDest?.hasRoute<HomeRoute>() == true
                                is MaterialsRoute -> currentDest?.hasRoute<MaterialsRoute>() == true
                                is SupportRoute -> currentDest?.hasRoute<SupportRoute>() == true
                                is MoreRoute -> currentDest?.hasRoute<MoreRoute>() == true
                                else -> false
                            }
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopTab(item.route) },
                                icon = { Icon(item.icon, contentDescription = s(item.labelKey)) },
                                label = { Text(s(item.labelKey), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                modifier = Modifier.testTag(item.tag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail && isPrimaryTab) {
                    NavigationRail(containerColor = pal.bgAlt) {
                        navItems.forEach { item ->
                            val selected = when (item.route) {
                                is HomeRoute -> currentDest?.hasRoute<HomeRoute>() == true
                                is MaterialsRoute -> currentDest?.hasRoute<MaterialsRoute>() == true
                                is SupportRoute -> currentDest?.hasRoute<SupportRoute>() == true
                                is MoreRoute -> currentDest?.hasRoute<MoreRoute>() == true
                                else -> false
                            }
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopTab(item.route) },
                                icon = { Icon(item.icon, contentDescription = s(item.labelKey)) },
                                label = { Text(s(item.labelKey)) },
                                modifier = Modifier.testTag(item.tag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = HomeRoute
                    ) {
                        composable<HomeRoute> {
                            HomeScreen(
                                totalMaterials = mainViewModel.catalogRepo.allMaterials.size,
                                totalSubjects = mainViewModel.catalogRepo.subjectCatalog.size,
                                totalMajors = mainViewModel.catalogRepo.curriculumMajors.size,
                                newsState = newsState,
                                onRefreshNews = { mainViewModel.refreshTelegramNews() },
                                onOpenCompass = { navController.navigate(CompassRoute) },
                                onOpenTests = { navController.navigate(TestsRoute()) },
                                onOpenGpa = { navController.navigate(GpaRoute) },
                                onOpenCurriculum = { navController.navigate(CurriculumRoute) },
                                onOpenRequests = { navController.navigate(RequestsRoute) },
                                onOpenTimer = { navController.navigate(TimerRoute) },
                                onOpenDashboard = { navController.navigate(DashboardRoute) },
                                onOpenCourses = { navigateTopTab(MaterialsRoute()) }
                            )
                        }

                        composable<MaterialsRoute> { backEntry ->
                            val args = backEntry.toRoute<MaterialsRoute>()
                            MaterialsScreen(
                                catalogRepo = mainViewModel.catalogRepo,
                                favoriteKeys = prefs.favoriteKeys,
                                selectedMajor = prefs.selectedMajor,
                                selectedYear = prefs.selectedMaterialsYear,
                                initialFavOnly = args.initialFavOnly,
                                initialSubjectSearch = args.initialSubjectSearch,
                                onToggleFavorite = { mainViewModel.toggleFavorite(it) },
                                onSelectMajor = { mainViewModel.setSelectedMajor(it) },
                                onSelectYear = { mainViewModel.setSelectedMaterialsYear(it) },
                                onOpenSubject = { subj -> navController.navigate(SubjectDetailRoute(subj)) },
                                onOpenMaterial = { key -> navController.navigate(MaterialDetailRoute(key)) },
                                onPdfOpened = { mainViewModel.recordPdfOpened() }
                            )
                        }

                        composable<SubjectDetailRoute> { backEntry ->
                            val args = backEntry.toRoute<SubjectDetailRoute>()
                            SubjectDetailScreen(
                                subjectName = args.subjectName,
                                catalogRepo = mainViewModel.catalogRepo,
                                favoriteKeys = prefs.favoriteKeys,
                                notesFlow = mainViewModel.observeSubjectNotes(args.subjectName),
                                onToggleFavorite = { mainViewModel.toggleFavorite(it) },
                                onOpenMaterial = { key -> navController.navigate(MaterialDetailRoute(key)) },
                                onPracticeTest = { subj -> navController.navigate(TestsRoute(initialSubject = subj)) },
                                onAddNote = { tag, txt -> mainViewModel.addSubjectNote(args.subjectName, tag, txt) },
                                onDeleteNote = { id -> mainViewModel.deleteSubjectNote(id) },
                                onPdfOpened = { mainViewModel.recordPdfOpened() }
                            )
                        }

                        composable<MaterialDetailRoute> { backEntry ->
                            val args = backEntry.toRoute<MaterialDetailRoute>()
                            MaterialDetailScreen(
                                favKey = args.favKey,
                                catalogRepo = mainViewModel.catalogRepo,
                                favoriteKeys = prefs.favoriteKeys,
                                currentRating = ratingsMap[args.favKey] ?: 0,
                                onToggleFavorite = { mainViewModel.toggleFavorite(it) },
                                onRate = { key, stars -> mainViewModel.setMaterialRating(key, stars) },
                                onOpenMaterial = { key -> navController.navigate(MaterialDetailRoute(key)) },
                                onOpenSubject = { subj -> navController.navigate(SubjectDetailRoute(subj)) },
                                onPdfOpened = { mainViewModel.recordPdfOpened() }
                            )
                        }

                        composable<TestsRoute> { backEntry ->
                            val args = backEntry.toRoute<TestsRoute>()
                            TestsScreen(
                                viewModel = testsViewModel,
                                catalogRepo = mainViewModel.catalogRepo,
                                initialSubject = args.initialSubject
                            )
                        }

                        composable<GpaRoute> {
                            GpaScreen(
                                subjects = gpaSubjects,
                                history = gpaHistory,
                                onAddSubject = { mainViewModel.addGpaSubject() },
                                onUpdateSubject = { mainViewModel.updateGpaSubject(it) },
                                onDeleteSubject = { mainViewModel.deleteGpaSubject(it) },
                                onSaveSnapshot = { g, t, e, c -> mainViewModel.saveGpaSnapshot(g, t, e, c) }
                            )
                        }

                        composable<CurriculumRoute> {
                            CurriculumScreen(
                                catalogRepo = mainViewModel.catalogRepo,
                                selectedMajor = prefs.selectedCurriculumMajor,
                                profileYear = prefs.profileYear,
                                observeOverrides = { majorKey -> mainViewModel.observeCurriculumOverrides(majorKey) },
                                onSelectMajor = { mainViewModel.setSelectedCurriculumMajor(it) },
                                onToggleOverride = { mKey, sKey, done -> mainViewModel.setCurriculumOverride(mKey, sKey, done) },
                                onOpenSubject = { subj -> navController.navigate(SubjectDetailRoute(subj)) },
                                onSearchMaterials = { q -> navController.navigate(MaterialsRoute(initialSubjectSearch = q)) }
                            )
                        }

                        composable<CompassRoute> {
                            CompassScreen(
                                articles = mainViewModel.catalogRepo.compassArticles,
                                savedIds = prefs.savedCompassIds,
                                onToggleSave = { mainViewModel.toggleSavedCompass(it) },
                                onOpenArticle = { id -> navController.navigate(CompassArticleRoute(id)) }
                            )
                        }

                        composable<CompassArticleRoute> { backEntry ->
                            val args = backEntry.toRoute<CompassArticleRoute>()
                            CompassArticleScreen(
                                articleId = args.articleId,
                                articles = mainViewModel.catalogRepo.compassArticles,
                                savedIds = prefs.savedCompassIds,
                                onToggleSave = { mainViewModel.toggleSavedCompass(it) },
                                onOpenArticle = { id -> navController.navigate(CompassArticleRoute(id)) }
                            )
                        }

                        composable<RequestsRoute> {
                            RequestsScreen(
                                requests = requests,
                                onAddRequest = { text, course, subj -> mainViewModel.addRequest(text, course, subj) },
                                onToggleUpvote = { mainViewModel.toggleUpvote(it) },
                                onToggleFulfilled = { mainViewModel.toggleFulfilled(it) },
                                onDeleteRequest = { mainViewModel.deleteRequest(it) },
                                onOpenThread = { id -> navController.navigate(RequestThreadRoute(id)) }
                            )
                        }

                        composable<RequestThreadRoute> { backEntry ->
                            val args = backEntry.toRoute<RequestThreadRoute>()
                            val req = requests.firstOrNull { it.id == args.requestId }
                            RequestThreadScreen(
                                request = req,
                                repliesFlow = mainViewModel.observeRequestReplies(args.requestId),
                                onAddReply = { txt -> mainViewModel.addRequestReply(args.requestId, txt) },
                                onDeleteReply = { id -> mainViewModel.deleteRequestReply(id) }
                            )
                        }

                        composable<TimerRoute> {
                            TimerScreen(
                                viewModel = timerViewModel,
                                prefs = prefs,
                                exams = exams,
                                onAddExam = { subj, iso -> mainViewModel.addExam(subj, iso) },
                                onDeleteExam = { mainViewModel.deleteExam(it) }
                            )
                        }

                        composable<DashboardRoute> {
                            DashboardScreen(
                                prefs = prefs,
                                recentResults = recentResults,
                                onSaveProfile = { n, f, m, y -> mainViewModel.updateProfile(n, f, m, y) }
                            )
                        }

                        composable<SupportRoute> {
                            SupportScreen()
                        }

                        composable<MoreRoute> {
                            MoreScreen(
                                prefs = prefs,
                                onSetLang = { mainViewModel.setLang(it) },
                                onSetTheme = { mainViewModel.setTheme(it) },
                                onOpenFavorites = { navController.navigate(MaterialsRoute(initialFavOnly = true)) },
                                onOpenCompass = { navController.navigate(CompassRoute) },
                                onOpenRequests = { navController.navigate(RequestsRoute) },
                                onOpenTimer = { navController.navigate(TimerRoute) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showUpdates) {
        ModalBottomSheet(
            onDismissRequest = { mainViewModel.closeUpdatesSheet() },
            containerColor = pal.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(s("notifTitle"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (newsState.posts.isEmpty()) {
                    Text(s("notifEmpty"), style = MaterialTheme.typography.bodySmall, color = pal.textMuted)
                } else {
                    newsState.posts.forEach { post ->
                        Surface(
                            onClick = { IntentHelpers.openUrl(context, post.url) },
                            shape = RoundedCornerShape(12.dp),
                            color = pal.surfaceInner,
                            border = BorderStroke(1.dp, pal.border),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (post.date.isNotBlank()) {
                                    Text(
                                        text = IntentHelpers.formatDateShort(post.date, strings.lang),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = pal.accent2
                                    )
                                }
                                Text(post.text, style = MaterialTheme.typography.bodySmall, maxLines = 4, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
                Button(
                    onClick = { IntentHelpers.openUrl(context, NewsRepository.TELEGRAM_CHANNEL_URL) },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(s("newsTelegram"), fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
