package com.kiienkoromaniuk.stepscounter.di.module

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.*
import com.google.android.gms.fitness.data.DataType
import com.kiienkoromaniuk.stepscounter.model.repository.StepsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Singleton
    @Provides
    fun provideHistoryClient(@ApplicationContext appContext: Context): HistoryClient =
        Fitness.getHistoryClient(appContext, GoogleSignIn.getAccountForExtension(appContext,
            FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .build()))

    @Singleton
    @Provides
    fun provideRecordingClient(@ApplicationContext appContext: Context): RecordingClient =
        Fitness.getRecordingClient(appContext, GoogleSignIn.getAccountForExtension(appContext,
            FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .build()))

    @Provides
    fun provideStepsRepository(recordingClient: RecordingClient,historyClient: HistoryClient) = StepsRepository(recordingClient,historyClient)
}