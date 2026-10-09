package com.taxiparis.app.worker

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.Constraints
import androidx.work.NetworkType
import com.taxiparis.app.data.local.TaxiPrefs
import com.taxiparis.app.domain.usecase.SyncDataUseCase
import com.taxiparis.app.widget.CdgWidget
import com.taxiparis.app.widget.OryWidget
import com.taxiparis.app.widget.NewMetroWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class UpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncData: SyncDataUseCase,
    private val prefs: TaxiPrefs
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val state = prefs.taxiState.first()
            
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            
            val isInsideRange = if (state.workingStartHour <= state.workingEndHour) {
                currentHour in state.workingStartHour until state.workingEndHour
            } else {
                currentHour >= state.workingStartHour || currentHour < state.workingEndHour
            }

            val forceMetro = inputData.getBoolean("forceMetro", false)
            if (!isInsideRange && !forceMetro && !state.isForceEnabled) {
                Log.d("UpdateWorker", "Hors plage horaire. Sommeil.")
                scheduleNext(applicationContext)
                return@withContext Result.success()
            }

            syncData(forceMetro = forceMetro)

            updateAllWidgets()
            
            scheduleNext(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Log.e("UpdateWorker", "Fatal worker error", e)
            prefs.setRefreshing(false)
            prefs.setMetroRefreshing(false)
            scheduleNext(applicationContext)
            Result.success()
        }
    }

    private suspend fun updateAllWidgets() {
        CdgWidget().updateAll(applicationContext)
        OryWidget().updateAll(applicationContext)
        NewMetroWidget().updateAll(applicationContext)
    }

    companion object {
        private const val WORK_NAME = "TaxiUpdateWork"
        
        private val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        suspend fun scheduleNext(context: Context) {
            val prefs = TaxiPrefs(context)
            val state = prefs.taxiState.first()
            val interval = state.refreshInterval.toLong().coerceAtLeast(15)

            val request = OneTimeWorkRequestBuilder<UpdateWorker>()
                .setInitialDelay(interval, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }

        fun startImmediately(context: Context, forceMetro: Boolean = false) {
            val data = androidx.work.workDataOf("forceMetro" to forceMetro)
            val request = OneTimeWorkRequestBuilder<UpdateWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }
    }
}
