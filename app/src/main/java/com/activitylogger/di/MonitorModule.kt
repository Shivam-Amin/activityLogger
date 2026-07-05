package com.activitylogger.di

import android.content.Context
import com.activitylogger.monitor.AppLabelResolver
import com.activitylogger.monitor.ContactResolver
import com.activitylogger.monitor.LauncherDetector
import com.activitylogger.monitor.SessionTracker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MonitorModule {

    @Provides
    @Singleton
    fun provideSessionTracker(): SessionTracker {
        return SessionTracker()
    }

    @Provides
    @Singleton
    fun provideLauncherDetector(@ApplicationContext context: Context): LauncherDetector {
        return LauncherDetector(context)
    }

    @Provides
    @Singleton
    fun provideAppLabelResolver(@ApplicationContext context: Context): AppLabelResolver {
        return AppLabelResolver(context)
    }

    @Provides
    @Singleton
    fun provideContactResolver(@ApplicationContext context: Context): ContactResolver {
        return ContactResolver(context)
    }
}
