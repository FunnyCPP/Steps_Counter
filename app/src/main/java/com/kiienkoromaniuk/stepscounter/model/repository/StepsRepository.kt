package com.kiienkoromaniuk.stepscounter.model.repository

import android.util.Log
import com.google.android.gms.fitness.SensorsClient
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataSourcesRequest
import javax.inject.Inject

class StepsRepository @Inject constructor(
    private val sensorsClient: SensorsClient
) {
    private val TAG= "StepsRepository"
    fun getSteps() {
        sensorsClient
            .findDataSources(
                DataSourcesRequest.Builder()
                    .setDataTypes(DataType.TYPE_STEP_COUNT_DELTA)
                    .setDataSourceTypes(DataSource.TYPE_RAW)
                    .build()
            )
            .addOnSuccessListener { dataSources ->
                dataSources.forEach {
                    Log.i(TAG, "Data source found: ${it.streamIdentifier}")
                    Log.i(TAG, "Data Source type: ${it.dataType.name}")

                    if (it.dataType == DataType.TYPE_STEP_COUNT_DELTA) {
                        Log.i(TAG, "Data source for STEP_COUNT_DELTA found!")

                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Find data sources request failed", e)
            }
    }
}