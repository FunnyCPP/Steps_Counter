package com.kiienkoromaniuk.stepscounter.di.module

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.SensorsClient
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
    fun provideSensorsClient(@ApplicationContext appContext: Context): SensorsClient =
        Fitness.getSensorsClient(appContext, GoogleSignIn.getAccountForExtension(appContext,
            FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .build()))

    @Provides
    fun provideStepsRepository(sensorsClient: SensorsClient) = StepsRepository(sensorsClient)
}