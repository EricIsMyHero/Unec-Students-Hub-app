package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.repository.CatalogRepository
import com.example.data.repository.NewsRepository
import com.example.data.repository.StudyRepository
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.TestsViewModel
import com.example.ui.viewmodel.TimerViewModel

class UnecApp : Application() {

    lateinit var catalogRepository: CatalogRepository
        private set
    lateinit var newsRepository: NewsRepository
        private set
    lateinit var studyRepository: StudyRepository
        private set

    val viewModelFactory: ViewModelProvider.Factory by lazy {
        viewModelFactory {
            initializer {
                MainViewModel(
                    catalogRepo = catalogRepository,
                    newsRepo = newsRepository,
                    studyRepo = studyRepository
                )
            }
            initializer {
                TestsViewModel(
                    application = this@UnecApp,
                    catalogRepo = catalogRepository,
                    newsRepo = newsRepository,
                    studyRepo = studyRepository
                )
            }
            initializer {
                TimerViewModel(
                    application = this@UnecApp,
                    studyRepo = studyRepository
                )
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        val prefs = PreferencesManager(this)
        catalogRepository = CatalogRepository(this)
        newsRepository = NewsRepository()
        studyRepository = StudyRepository(db, prefs)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TIMER_CHANNEL_ID,
                getString(R.string.timer_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.timer_channel_desc)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val TIMER_CHANNEL_ID = "timer_alerts"
    }
}
