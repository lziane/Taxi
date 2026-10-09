package com.taxiparis.app.di

import android.content.Context
import com.taxiparis.app.common.LocationTracker
import com.taxiparis.app.data.local.TaxiPrefs
import com.taxiparis.app.notification.NotificationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTaxiPrefs(@ApplicationContext context: Context): TaxiPrefs = TaxiPrefs(context)

    @Provides
    @Singleton
    fun provideLocationTracker(@ApplicationContext context: Context): LocationTracker = LocationTracker(context)

    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager = NotificationManager(context)
}
